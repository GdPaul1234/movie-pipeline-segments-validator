from typing import Annotated, Optional

import ffmpeg
from fastapi import APIRouter, Depends, HTTPException, Path, Response, status
from pydantic import BaseModel, Field, computed_field
from pydantic.types import FilePath, NonNegativeFloat

from ....adapters.http.dependencies import get_media, get_segment_validator_context
from ....adapters.repository.resources import Media, MediaMetadata
from ....domain.context import SegmentValidatorContext
from ....lib.video_player.simple_video_only_player import extract_frame
from ....services import segment_service

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
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    media: Annotated[Media, Depends(get_media)]
) -> MediaOut:
    return MediaOut(media=media)


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
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)]
) -> ValidateSegmentsOut:
    if (eld_path := segment_service.validate_segments(segment_validator_context)) is not None:
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
