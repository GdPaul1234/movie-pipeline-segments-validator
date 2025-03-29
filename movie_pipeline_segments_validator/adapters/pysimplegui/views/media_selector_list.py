from typing import Any

import PySimpleGUI as sg

from ..config.widget import WidgetEvent, WidgetKey
from ..controllers.media_selector_list_controller import load_new_media, populate_media_selector, remove_validated_media_from_media_selector, toggle_media_selector_visibility


def layout():
    return [
        sg.Listbox(
            values=[],
            key=WidgetKey.MEDIA_SELECTOR_KEY.value,
            enable_events=True,
            auto_size_text=False,
            horizontal_scroll=True,
            expand_x=True,
            expand_y=True
        )
    ]


handlers = {
    WidgetEvent.APPLICATION_LOADED_EVENT: populate_media_selector,
    WidgetEvent.MEDIA_SELECTOR_UPDATED_EVENT: load_new_media,
    WidgetEvent.TOGGLE_MEDIA_SELECTOR_VISIBILITY_EVENT: toggle_media_selector_visibility,
    WidgetEvent.SEGMENTS_SAVED_EVENT: remove_validated_media_from_media_selector
}


def handle_media_selector_list(window: sg.Window, event: str, values: dict[str, Any]):
    handlers[WidgetEvent(event)](window, event, values)

