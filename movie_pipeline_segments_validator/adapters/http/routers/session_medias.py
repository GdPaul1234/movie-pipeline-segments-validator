from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Path, status
from pydantic import BaseModel, Field
from pydantic.types import FilePath

from movie_pipeline_segments_validator.services import segment_service

from ....adapters.http.dependencies import get_media, get_segment_validator_context
from ....adapters.repository.resources import Media
from ....domain.context import SegmentValidatorContext

router = APIRouter(
    prefix='/sessions/{session_id}/medias',
    tags=['medias']
)


@router.get('/{media_stem}')
def show_media(
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    media: Annotated[Media, Depends(get_media)]
) -> Media:
    return media


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
