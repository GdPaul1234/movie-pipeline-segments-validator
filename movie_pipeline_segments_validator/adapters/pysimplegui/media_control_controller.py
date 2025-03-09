from typing import Any, cast

import PySimpleGUI as sg

from ...domain.context import SegmentValidatorContext
from ...domain.widget import WidgetEvent, WidgetKey
from ...lib.util import seconds_to_position


def set_relative_position(window: sg.Window, event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)
    player = metadata.media_player

    command, delta = event.split('::')
    window.perform_long_operation(lambda: getattr(player, command)(float(delta), window), WidgetEvent.TASK_DONE_EVENT.value)


def goto_selected_segment(window: sg.Window, event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)
    table = cast(sg.Table, window[WidgetKey.SEGMENT_LIST_TABLE_KEY.value])
    player, segment_container = metadata.media_player, metadata.segment_container

    selected_segments = [segment_container.segments[row] for row in table.SelectedRows]

    if len(selected_segments) != 1:
        return

    _, position = event.split('::')
    window.perform_long_operation(lambda: player.set_position(getattr(selected_segments[0], position), window), WidgetEvent.TASK_DONE_EVENT.value)


def set_video_information(window: sg.Window, _event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)
    video_duration_label = window[WidgetKey.VIDEO_DURATION_LABEL_KEY.value]

    truncated_duration = seconds_to_position(metadata.duration).split('.')[0]
    video_duration_label.update(value=truncated_duration)


def update_video_position(window: sg.Window, _event: str, _values: dict[str, Any]):
    metadata = cast(SegmentValidatorContext, window.metadata)
    video_position_label = window[WidgetKey.VIDEO_POSITION_LABEL_KEY.value]

    truncated_position = seconds_to_position(metadata.position).split('.')[0]
    video_position_label.update(value=truncated_position)
