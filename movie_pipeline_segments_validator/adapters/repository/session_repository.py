import datetime
import dbm
import logging
import uuid
from itertools import islice
from typing import Any

import yaml
from pydantic import TypeAdapter
from pydantic.types import DirectoryPath

from ...adapters.repository.resources import Media, Segment, Session
from ...domain.context import SegmentValidatorContext
from ...domain.media_path import MediaPath
from ...domain.movie_segments import MovieSegments
from ...services.edit_decision_file_dumper import extract_title
from ...services.import_segments_from_file import import_segments
from ...services.media_selector_service import list_medias
from ...settings import Settings

logger = logging.getLogger(__name__)


def build_media(media: MediaPath, config: Settings):
    edl_files = list(islice(media.path.parent.glob(f'{media.path.name}*.*yml*'), 1))
    eld_file_content: dict[str, Any] = yaml.safe_load(edl_files[0].read_text()) if len(edl_files) == 1 else {}

    title = eld_file_content.get('filename', extract_title(media.path, config))
    skip_backup = eld_file_content.get('skip_backup', False)

    imported_segments = { 
        k: f"{v.removesuffix(',')}," 
        for k, v in import_segments(media.path).items()
        if v != ''
    }

    raw_segments = items[0][1] if len(
        items := list(imported_segments.items())) > 0 else ''
    imported_detector_segments = [Segment(start=segment[0], end=segment[1]) for segment in MovieSegments(raw_segments).segments]

    return Media(
        filepath=media.path,
        state=media.state,
        title=f'{title}.mp4',
        skip_backup=skip_backup,
        imported_segments=imported_segments,
        segments=imported_detector_segments
    )


class SessionRepository:
    def __init__(self, config: Settings) -> None:
        self._config = config
        self._session_type_adapter = TypeAdapter(Session)

    def create(self, root_path: DirectoryPath) -> Session:
        new_session = Session(
            id=uuid.uuid4().hex,
            created_at=datetime.datetime.now(),
            updated_at=datetime.datetime.now(),
            root_path=root_path,
            medias=[build_media(media, self._config) for media in list_medias(root_path, self._config)]
        )

        with dbm.open(self._config.Paths.db_path, 'c') as db:
            db[new_session.id] = self._session_type_adapter.dump_json(new_session)

        return new_session

    def get(self, id: str) -> Session:
        with dbm.open(self._config.Paths.db_path, 'c') as db:
            session = self._session_type_adapter.validate_json(db[id])

        return session

    def set(self, session: Session) -> Session:
        with dbm.open(self._config.Paths.db_path, 'c') as db:
            session.updated_at = datetime.datetime.now()
            db[session.id] = self._session_type_adapter.dump_json(session)

        return session

    def update_media(self, session_id: str, session_validator_context: SegmentValidatorContext) -> Session:
        new_media = Media.from_segment_validator_context(session_validator_context)

        session = self.get(session_id)
        session.updated_at = datetime.datetime.now()
        session.medias = [
            new_media if media.filepath == new_media.filepath else media
            for media in session.medias
        ]

        return self.set(session)

    def delete(self, id: str) -> None:
        with dbm.open(self._config.Paths.db_path, 'c') as db:
            del db[id]
