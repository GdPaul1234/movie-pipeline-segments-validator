from typing import Any

import PySimpleGUI as sg

from ..config.widget import WidgetEvent, WidgetKey
from ..controllers.detector_selector_controller import import_segments_from_selected_detector, populate_detector_selector

handlers = {
    WidgetEvent.SEGMENT_IMPORTED_EVENT: populate_detector_selector,
    WidgetEvent.SELECTED_DETECTOR_UPDATED_EVENT: import_segments_from_selected_detector
}


def layout():
    return [
        sg.Combo(
            [],
            enable_events=True,
            expand_x=True,
            pad=((0, 0), (5, 5)),
            key=WidgetKey.DETECTOR_SELECTOR_KEY.value
        )
    ]


def handle_detector(window: sg.Window, event: str, values: dict[str, Any]):
    handlers[WidgetEvent(event)](window, event, values)
