from ..domain.context import SegmentValidatorContext
from ..domain.movie_segments import MovieSegments
from ..domain.segment_container import Segment, SegmentContainer
from .segment_service import delete_selected_segments


def import_segments_from_selected_detector(context: SegmentValidatorContext, detector_key: str):
    context.selected_segments = list(context.segment_container.segments)
    delete_selected_segments(context)
    context.segment_container = SegmentContainer()

    imported_detector_segments = MovieSegments(raw_segments=context.imported_segments[detector_key])

    for segment in imported_detector_segments.segments:
        context.segment_container.add(Segment(*segment))
