from typing import Annotated, Optional

import ffmpeg
from fastapi import APIRouter, Depends, HTTPException, Path, Response, status
from pydantic import BaseModel, Field, computed_field
from pydantic.types import FilePath, NonNegativeFloat

from ....adapters.http.dependencies import get_media, get_segment_validator_context, get_session_repository, get_settings
from ....adapters.repository.resources import Media, MediaMetadata
from ....adapters.repository.session_repository import SessionRepository, build_media
from ....domain import FILENAME_REGEX
from ....domain.context import SegmentValidatorContext
from ....domain.media_path import MediaPath
from ....lib.video_player.simple_video_only_player import extract_frame
from ....services import segment_service
from ....settings import Settings

router = APIRouter(
    prefix='/sessions/{session_id}/medias',
    tags=['medias']
)


class MediaOut(BaseModel):
    media: Annotated[Media, Field(description='media')]
    
    @computed_field(description='media duration in seconds')
    @property
    def duration(self) -> float:
        return self.media.duration
    
    @computed_field(description='media recording metadata')
    @property
    def recording_metadata(self) -> Optional[MediaMetadata]:
        return self.media.metadata


@router.get('/{media_stem}')
def show_media(
    session_id: Annotated[str, Path(title='session id')],
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)],
    session_repository: Annotated[SessionRepository, Depends(get_session_repository)]
) -> MediaOut:
    # refresh media state by updating it from segment_validator_context
    updated_media = session_repository.update_media(session_id, segment_validator_context).medias[media_stem]

    return MediaOut(media=updated_media)


class ValidateSegmentsBody(BaseModel):
    title: Annotated[
        str,
        Field(
            pattern=FILENAME_REGEX,
            description='output title, must ends with .mp4',
            examples=['Movie Name, le titre long.mp4', 'Serie Name S01E16.mp4']
        )
    ]
    skip_backup: Annotated[bool, Field(description='skip backup step')]


class ValidateSegmentsOut(BaseModel):
    edl_path: Annotated[
        FilePath,
        Field(
            description='Movie Pipeline EDL (Edit Decision List) file location',
            examples=[r'V:\PVR\Channel 1_Movie Name_2022-12-05-2203-20.ts.yml']
        )
    ]


@router.post('/{media_stem}/validate_segments')
def validate_media_segments(
    session_id: Annotated[str, Path(title='session id')],
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    body: ValidateSegmentsBody,
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)],
    session_repository: Annotated[SessionRepository, Depends(get_session_repository)]
) -> ValidateSegmentsOut:
    segment_validator_context.title = body.title
    segment_validator_context.skip_backup = body.skip_backup

    if (eld_path := segment_service.validate_segments(segment_validator_context)) is not None:
        session_repository.update_media(session_id, segment_validator_context) # refresh media state
        return ValidateSegmentsOut(edl_path=eld_path)

    raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail='EDL content is invalid')


@router.get('/{media_stem}/frames/{position_s}s')
def show_video_frame(
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    position_s: Annotated[NonNegativeFloat, Path(title='position in seconds')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)]
):
    try:
        stream = ffmpeg.input(str(segment_validator_context.filepath))
        frame = extract_frame(stream, position_s, vcodec='mjpeg')
        return Response(content=frame, media_type='image/jpeg')

    except ffmpeg.Error as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=e.stderr)
