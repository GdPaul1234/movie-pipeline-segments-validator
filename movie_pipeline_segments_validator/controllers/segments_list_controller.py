from typing import Any, cast

import PySimpleGUI as sg

from ..domain.context import SegmentValidatorContext
from ..domain.widget import WidgetEvent, WidgetKey
from ..domain.segment_container import Segment
from ..services import segment_service


def _get_selected_segments(window: sg.Window):
    metadata = cast(SegmentValidatorContext, window.metadata)
    table = cast(sg.Table, window[WidgetKey.SEGMENT_LIST_TABLE_KEY.value])

    return [metadata.segment_container.segments[row] for row in table.SelectedRows]


def _render_values(window: sg.Window):
    metadata = cast(SegmentValidatorContext, window.metadata)
    segments = metadata.segment_container.segments

    def render(segment: Segment):
        return repr(segment).split(',')

    values = [render(segment) for segment in segments]
    window[WidgetKey.SEGMENT_LIST_TABLE_KEY.value].update(values=values)
    window.write_event_value(WidgetEvent.SEGMENTS_UPDATED_EVENT.value, True)


def forward_updated_event(window: sg.Window, _event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)

    metadata.selected_segments = _get_selected_segments(window)
    window.write_event_value(WidgetEvent.SEGMENTS_UPDATED_EVENT.value, True)


def focus_timeline_selected_segment(window: sg.Window, _event: str, values: dict[str, Any]):
    table = cast(sg.Table, window[WidgetKey.SEGMENT_LIST_TABLE_KEY.value])

    if  (row := next(
            (idx for idx, value in enumerate(table.Values)
             if ','.join(value) == repr(values[WidgetEvent.SEGMENT_TIMELINE_SELECTED_EVENT.value])),
            None
        )) is not None:
            table.update(select_rows=[row])


def add_segment(window: sg.Window, _event: str, _values: dict[str, Any]):
    segment_service.add_segment(window.metadata)
    _render_values(window)


def delete_segments(window: sg.Window, _event: str, _values: dict[str, Any]):
    segment_service.delete_selected_segments(window.metadata)
    _render_values(window)


def merge_segments(window: sg.Window, _event: str, _values: dict[str, Any]):
    try:
        segment_service.merge_selected_segments( window.metadata)
        _render_values(window)
    except ValueError as e:
        sg.popup_error(e)


def edit_segment(window: sg.Window, event: str, _values: dict[str, Any]):
    try:
        edge = 'start' if event == WidgetEvent.SEGMENT_SET_START_EVENT.value else 'end'
        segment_service.edit_segment( window.metadata, edge)
        _render_values(window)
    except ValueError as e:
        sg.popup_error(e)


def validate_segments(window: sg.Window, _event: str, values: dict[str, Any]):
    title = values[WidgetKey.OUTPUT_FILENAME_INPUT_KEY.value]
    skip_backup = values[WidgetKey.SKIP_BACKUP_CHECKBOX_KEY.value]

    if edl_path := segment_service.validate_segments(window.metadata, title, skip_backup):
        sg.popup_auto_close(edl_path, title='Segments saved')
        window.write_event_value(WidgetEvent.SEGMENTS_SAVED_EVENT.value, True)
    else:
        sg.popup_auto_close(title='Segments not saved, an error has occured')
