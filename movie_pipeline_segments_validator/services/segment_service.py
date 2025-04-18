from typing import Literal

from ..domain.context import SegmentValidatorContext
from ..domain.segment_container import Segment
from .edit_decision_file_dumper import dump_decision_file


def add_segment(context: SegmentValidatorContext):
    context.segment_container.add(Segment(context.position, context.position + 1))


def delete_selected_segments(context: SegmentValidatorContext):
    for selected_segment in context.selected_segments:
        context.segment_container.remove(selected_segment)


def merge_selected_segments(context: SegmentValidatorContext):
    if len(context.selected_segments) < 2:
        raise ValueError('At least of 2 selected segments are required')
    
    start, end = min(context.selected_segments), max(context.selected_segments)

    all_selected_segments = [
        segment
        for segment in context.segment_container.segments
        if start <= segment <= end
    ]

    context.segment_container.merge(all_selected_segments)


def edit_segment(context: SegmentValidatorContext, edge: Literal['start', 'end']):
    if len(context.selected_segments) != 1:
        raise ValueError('Selected segment must not be blank')

    if edge == 'start':
        edited_segment = Segment(context.position, context.selected_segments[0].end)
    else:
        edited_segment = Segment(context.selected_segments[0].start, context.position)

    context.segment_container.edit(context.selected_segments[0], edited_segment)


def validate_segments(context: SegmentValidatorContext):
    return dump_decision_file(
        title=context.title,
        source_path=context.filepath,
        segment_container=context.segment_container,
        skip_backup=context.skip_backup
    )
