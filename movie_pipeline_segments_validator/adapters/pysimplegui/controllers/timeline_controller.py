from typing import Any, cast

import PySimpleGUI as sg
from decoratorOperations import debounce

from ....domain.context import SegmentValidatorContext
from ....lib.video_player.video_player import IVideoPlayer
from ..config.widget import WidgetKey


@debounce(0.1)
def _seek_to_position(media_player: IVideoPlayer, new_position: float, window: sg.Window):
    media_player.set_position(new_position, window=window)


def init_timeline_slider(window: sg.Window, _event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)
    window[WidgetKey.TIMELINE_SLIDER_KEY.value].update(value=0, range=(0, metadata.duration))


def seek_to_position(window: sg.Window, _event: str, values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)

    new_position = values[WidgetKey.TIMELINE_SLIDER_KEY.value]
    _seek_to_position(metadata.media_player, new_position, window)


def update_timeline_slider(window: sg.Window, _event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)
    window[WidgetKey.TIMELINE_SLIDER_KEY.value].update(value=metadata.position)
