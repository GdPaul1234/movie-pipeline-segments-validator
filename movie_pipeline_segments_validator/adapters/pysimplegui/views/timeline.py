from typing import Any

import PySimpleGUI as sg

from ..config.widget import WidgetEvent, WidgetKey
from ..controllers.timeline_controller import init_timeline_slider, seek_to_position, update_timeline_slider


def layout():
    return [
        sg.Slider(
            (0, 0),
            0,
            orientation='h',
            enable_events=True,
            disable_number_display=True,
            expand_x=True,
            pad=0,
            key=WidgetKey.TIMELINE_SLIDER_KEY.value
        )
    ]


handlers = {
    WidgetEvent.VIDEO_LOADED_EVENT: init_timeline_slider,
    WidgetEvent.TIMELINE_POSTION_UPDATED_EVENT: seek_to_position,
    WidgetEvent.VIDEO_POSITION_UPDATED_EVENT: update_timeline_slider
}


def handle_timeline(window: sg.Window, event: str, values: dict[str, Any]):
    handlers[WidgetEvent(event)](window, event, values)

