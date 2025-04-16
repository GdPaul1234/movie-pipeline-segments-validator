import logging
import shutil
from pathlib import Path
from typing import cast

import ffmpeg
import PySimpleGUI as sg

from ...adapters.pysimplegui.config.widget import WidgetEvent
from .video_player import IVideoPlayer

logger = logging.getLogger(__name__)

def extract_frame(stream, position_s, **kwargs):
    out, _ = (
        stream
        .output('pipe:', format='rawvideo', pix_fmt='rgb24', vframes=1, **kwargs)
        .run(cmd=['ffmpeg', '-hide_banner', '-ss', str(position_s)], capture_stdout=True, capture_stderr=True)
    )

    return out


class NoOpVideoPositionForwarder(IVideoPlayer):
    pass


class SgVideoOnlyPlayerConsumer(IVideoPlayer):
    def __init__(self, source: Path) -> None:
        super().__init__(source)

        if not(custom_ffmpeg := shutil.which('ffmpeg')):
            raise ValueError('ffmpeg is not installed')

        from deffcode import Sourcer
        sourcer = Sourcer(str(source), custom_ffmpeg=custom_ffmpeg).probe_stream()
        self._metadata = cast(dict, sourcer.retrieve_metadata())
        self._duration = self._metadata['source_duration_sec']
        self._size = self._metadata['source_video_resolution']

    def set_position(self, position: float, **kwargs) -> None:
        window = cast(sg.Window, kwargs['window'])
        super().set_position(position, **kwargs)

        stream = ffmpeg.input(str(self._source))
        frame = extract_frame(stream, self._current_position)

        if self._current_position < 0 or self._current_position >= (self._duration - 1):
            self._current_position = 0.
            window.write_event_value(WidgetEvent.VIDEO_POSITION_UPDATED_EVENT.value, 0.)
            return

        if frame is not None:
            logger.debug(self._current_position)
            window.write_event_value(WidgetEvent.VIDEO_NEW_FRAME_EVENT.value, (self._size, frame))
            window.write_event_value(WidgetEvent.VIDEO_POSITION_UPDATED_EVENT.value, self._current_position)
