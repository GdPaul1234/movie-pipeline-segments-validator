from typing import Any
import PySimpleGUI as sg

from ..adapters.pysimplegui.segments_list_controller import _render_values, add_segment, delete_segments, edit_segment, merge_segments, focus_timeline_selected_segment, forward_updated_event, validate_segments
from ..domain.widget import WidgetEvent, WidgetKey

render_values = _render_values


right_click_menu = [
    '&Segments',
    [
        '&Merge segments',
        '&Delete segment(s)',
    ]
]


def layout():
    return [
        sg.Table(
            headings=('Start', 'End', 'Dur'),
            values=[('00:00:00.00', '00:00:00.00', '00:00')],
            right_click_menu=right_click_menu,
            enable_events=True,
            enable_click_events=True,
            auto_size_columns=True,
            expand_x=True,
            expand_y=True,
            pad=0,
            key=WidgetKey.SEGMENT_LIST_TABLE_KEY.value
        )
    ]


handlers = {
    WidgetEvent.SEGMENT_ADD_EVENT: add_segment,
    WidgetEvent.SEGMENT_DELETE_EVENT: delete_segments,
    WidgetEvent.SEGMENT_MERGE_EVENT: merge_segments,
    WidgetEvent.SEGMENT_LIST_UPDATED_EVENT: forward_updated_event,
    WidgetEvent.SEGMENT_TIMELINE_SELECTED_EVENT: focus_timeline_selected_segment,
    WidgetEvent.SEGMENT_SET_START_EVENT: edit_segment,
    WidgetEvent.SEGMENT_SET_END_EVENT: edit_segment,
    WidgetEvent.SEGMENTS_VALIDATED_EVENT: validate_segments
}


def handle_segments_list(window: sg.Window, event: str, values: dict[str, Any]):
    handlers[WidgetEvent(event)](window, event, values)

