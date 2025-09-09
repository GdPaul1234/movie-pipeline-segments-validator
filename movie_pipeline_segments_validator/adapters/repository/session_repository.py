import dbm
import logging
import uuid
from datetime import datetime, timezone
from itertools import islice
from typing import Any, Optional

import yaml
from pydantic import TypeAdapter
from pydantic.types import DirectoryPath

from ...adapters.repository.resources import Media, Segment, Session
from ...domain.context import SegmentValidatorContext, import_media_segments
from ...domain.media_path import MediaPath
from ...domain.movie_segments import MovieSegments
from ...services.edit_decision_file_dumper import extract_title
from ...services.media_selector_service import list_medias
from ...settings import Settings

logger = logging.getLogger(__name__)


def import_detector_segments(imported_segments):
    raw_segments = items[0][1] if len(items := list(imported_segments.items())) > 0 else ''
    return  [
        Segment(start=segment[0], end=segment[1]) 
        for segment in MovieSegments(raw_segments).segments
    ]


def build_media(source: MediaPath | Media, config: Optional[Settings] = None, force_load_segments = False):
    """Build media from MediaPath or Media

    Args:
        source (MediaPath | Media): 
            When source is instance of MediaPath, fill title, state, and skip_backup from path,
            When source is instance of Media, import missing informations like imported_segments
        config (Optional[Settings]): config to extract title from source if instance of MediaPath

    Returns:
        Media: Media filled with title, state, skip_backup and imported segments
    """
    if isinstance(source, Media):
        imported_segments = source.imported_segments or import_media_segments(source.filepath)
        imported_detector_segments = source.segments or import_detector_segments(imported_segments)
        filepath, state, title, skip_backup = source.filepath, source.state, source.title, source.skip_backup

    else:
        if config is None:
            raise ValueError('Missing config when source is instance of MediaPath')

        imported_segments = import_media_segments(source.path) if force_load_segments else {}
        imported_detector_segments = import_detector_segments(imported_segments)

        filepath, state = source.path, source.state

        edl_files = list(islice(source.path.parent.glob(f'{source.path.name}*.*yml*'), 1))
        eld_file_content: dict[str, Any] = yaml.safe_load(edl_files[0].read_text()) if len(edl_files) == 1 else {}
        title: str = eld_file_content.get('filename', extract_title(source.path, config))
        skip_backup: bool = eld_file_content.get('skip_backup', False)

    return Media(
        filepath=filepath,
        state=state,
        title=f"{title.removesuffix('.mp4')}.mp4",
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
            created_at=datetime.now(timezone.utc),
            updated_at=datetime.now(timezone.utc),
            root_path=root_path,
            medias={
                media.path.stem: build_media(media, self._config)
                for media in list_medias(root_path, self._config)
            }
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
            session.updated_at = datetime.now(timezone.utc)
            db[session.id] = self._session_type_adapter.dump_json(session)

        return session

    def update_media(self, session_id: str, session_validator_context: SegmentValidatorContext) -> Session:
        new_media = Media.from_segment_validator_context(session_validator_context)

        session = self.get(session_id)
        session.updated_at = datetime.now(timezone.utc)
        session.medias[new_media.filepath.stem] = new_media

        return self.set(session)

    def delete(self, id: str) -> None:
        with dbm.open(self._config.Paths.db_path, 'c') as db:
            del db[id]
