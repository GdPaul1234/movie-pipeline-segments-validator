from typing import Any
import PySimpleGUI as sg

from ..adapters.pysimplegui.segments_timeline_controller import draw_current_position_indicator, draw_segments, resize_timeline, update_current_position_indicator_position, update_selected_segment
from ..domain.widget import WidgetEvent, WidgetKey
from ..domain.context import TimelineContext

GRAPH_SIZE = (480, 30)

right_click_menu = [
    '&Segments',
    [
        '&Add segment',
        '---',
        'Set &start',
        'Set &end'
    ]
]


def layout():
    return [
        sg.Graph(
            # Define the graph area
            canvas_size=GRAPH_SIZE, graph_bottom_left=(0., 0.), graph_top_right=(1., 1.),
            float_values=True,
            enable_events=True,  # mouse click events
            right_click_menu=right_click_menu,
            pad=0,
            background_color='#a6b2be',
            metadata=TimelineContext(),
            key=WidgetKey.SEGMENT_TIMELINE_KEY.value
        )
    ]


handlers = {
    WidgetEvent.VIDEO_LOADED_EVENT: draw_current_position_indicator,
    WidgetEvent.CONFIGURE_EVENT: resize_timeline,
    WidgetEvent.SEGMENTS_UPDATED_EVENT: draw_segments,
    WidgetEvent.SEGMENT_TIMELINE_UPDATED_EVENT: update_selected_segment,
    WidgetEvent.TIMELINE_POSTION_UPDATED_EVENT: update_current_position_indicator_position,
    WidgetEvent.VIDEO_POSITION_UPDATED_EVENT: update_current_position_indicator_position
}


def handle_segments_timeline(window: sg.Window, event: str, values: dict[str, Any]):
    handlers[WidgetEvent(event)](window, event, values)

