from typing import Any

import PySimpleGUI as sg

from ..adapters.pysimplegui.media_control_controller import goto_selected_segment, set_relative_position, set_video_information, update_video_position
from ..domain.widget import WidgetEvent, WidgetKey


def btn(text, /, *, key=None, size=(6,1), pad=(1,1)):
    return sg.Button(text, key=(key or text), size=size, pad=pad)


def txt(text, key):
    return sg.Column([[sg.Text(text, key=key, pad=0)]], pad=0)

handlers = {
    WidgetEvent.VIDEO_LOADED_EVENT: set_video_information,
    WidgetEvent.VIDEO_POSITION_UPDATED_EVENT: update_video_position,
    WidgetEvent.TIMELINE_POSTION_UPDATED_EVENT: update_video_position,
    WidgetEvent.GOTO_SELECTED_SEGMENT_EVENT: goto_selected_segment,
    WidgetEvent.SET_RELATIVE_POSITION_EVENT: set_relative_position,
}


def layout():
    return [
        txt('00:00:00', key=WidgetKey.VIDEO_POSITION_LABEL_KEY.value),
        sg.Push(),
        btn('>[-', key='goto_selected_segment::start', size=(3,1)),
        btn('-]<', key='goto_selected_segment::end', size=(3,1)),
        sg.Sizer(5),
        btn('-1s', key='set_relative_position::-1', size=(3,1)),
        btn('+1s', key='set_relative_position::1', size=(3,1)),
        sg.Sizer(5),
        btn('-5s', key='set_relative_position::-5', size=(3,1)),
        btn('+5s', key='set_relative_position::5', size=(3,1)),
        sg.Sizer(5),
        btn('-15s', key='set_relative_position::-15', size=(4,1)),
        btn('+15s', key='set_relative_position::15', size=(4,1)),
        sg.Push(),
        txt('00:00:00', key=WidgetKey.VIDEO_DURATION_LABEL_KEY.value),
    ]


def handle_media_control(window: sg.Window, event: str, values: dict[str, Any]):
    if isinstance(event, str):
        evt = event.split('::')[0]
        handlers[WidgetEvent(evt)](window, event, values)
