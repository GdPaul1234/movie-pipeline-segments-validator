from typing import Any

import PySimpleGUI as sg

from ..controllers.video_controller import render_video_new_frame, rerender_video
from ..domain.widget import WidgetEvent, WidgetKey


def layout():
    return [
        sg.Frame(
            '',
            [[sg.Image('', size=(480, 270), pad=0, key=WidgetKey.VIDEO_OUT_KEY.value)]],
            expand_x=True, expand_y=True,
            element_justification='c',
             pad=0,
            key=WidgetKey.VIDEO_CONTAINER_KEY.value
        )
    ]


handlers = {
    WidgetEvent.VIDEO_NEW_FRAME_EVENT: render_video_new_frame,
    WidgetEvent.CONFIGURE_EVENT: rerender_video
}



def handle_video(window: sg.Window, event: str, values: dict[str, Any]):
    handlers[WidgetEvent(event)](window, event, values)

