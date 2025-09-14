import logging

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
