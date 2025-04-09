from typing import Annotated, Literal

from fastapi import APIRouter, Depends, HTTPException, Path, status
from pydantic import BaseModel, Field
from pydantic.types import NonNegativeFloat

from ....adapters.http.dependencies import get_segment_validator_context
from ....adapters.repository.resources import Media, Segment
from ....domain.context import SegmentValidatorContext
from ....domain.segment_container import Segment as SegmentContainerSegment
from ....services import detector_service, segment_service

router = APIRouter(
    prefix='/sessions/{session_id}/medias/{media_stem}/segments',
    tags=['segments']
)


class ImportedSegmentBody(BaseModel):
    detector_key: Annotated[
        str,
        Field(
            description='detector key. See `Show Media` / `imported_segments` keys for a list of valid detector keys.',
            examples=['auto', 'match_template', 'crop', 'axcorrelate_silence', 'result_2024-12-04T23:17:11.387688']
        )
    ]


@router.post('/import')
def load_imported_segments(
    body: ImportedSegmentBody,
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)]
) -> Media:
    try:
        detector_service.import_segments_from_selected_detector(segment_validator_context, body.detector_key)
        return Media.from_segment_validator_context(segment_validator_context)
    except KeyError as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Detector key {e.args[0]} not found for {media_stem}")


class SegmentCreateBody(BaseModel):
    position: Annotated[NonNegativeFloat, Field(description='segment position in seconds')]


@router.post('/', description='Create a segment with 1 second duration at given position')
def create_segment(
    body: SegmentCreateBody,
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)]
) -> Media:
    segment_validator_context.media_player.set_position(body.position)
    segment_service.add_segment(segment_validator_context)

    return Media.from_segment_validator_context(segment_validator_context)


class SegmentEditBody(BaseModel):
    new_position: Annotated[NonNegativeFloat, Field(description='segment position in seconds')]
    edge: Annotated[Literal['start', 'end'], Field(description='edge to apply new position')]


@router.patch('/{start}-{end}')
def edit_segment(
    start: Annotated[NonNegativeFloat, Path(description='segment start position in seconds')],
    end: Annotated[NonNegativeFloat, Path(description='segment end position in seconds')],
    body: SegmentEditBody,
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)]
) -> Media:
    try:
        segment_validator_context.selected_segments = [SegmentContainerSegment(start, end)]
        segment_validator_context.media_player.set_position(body.new_position)
        segment_service.edit_segment(segment_validator_context, body.edge)

        return Media.from_segment_validator_context(segment_validator_context)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=e.args[0])


class SegmentsDeleteBody(BaseModel):
    segments: Annotated[list[Segment], Field(description='segments to delete')]


@router.delete('/')
def delete_segments(
    body: SegmentsDeleteBody,
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)] 
)-> Media:
    segment_validator_context.selected_segments = [SegmentContainerSegment(segment.start, segment.end) for segment in body.segments]
    segment_service.delete_selected_segments(segment_validator_context)

    return Media.from_segment_validator_context(segment_validator_context)


class SegmentsMergeBody(BaseModel):
    segments: Annotated[list[Segment], Field(description='segments to merge')]


@router.post('/merge')
def merge_segments(
    body: SegmentsDeleteBody,
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    segment_validator_context: Annotated[SegmentValidatorContext, Depends(get_segment_validator_context)] 
) -> Media:
    segment_validator_context.selected_segments = [SegmentContainerSegment(segment.start, segment.end) for segment in body.segments]
    segment_service.merge_selected_segments(segment_validator_context)

    return Media.from_segment_validator_context(segment_validator_context)
