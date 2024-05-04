import time
from datetime import timedelta
from pathlib import Path

import ffmpeg


def position_in_seconds(time: str) -> float:
    hours, mins, secs = time.split(':', maxsplit=3)

    return timedelta(
        hours=int(hours),
        minutes=int(mins),
        seconds=float(secs)
    ).total_seconds()


def seconds_to_position(seconds: float) -> str:
    formatted_time = time.strftime('%H:%M:%S', time.gmtime(seconds))
    formatted_decimal_part = f'{(seconds % 1):.3f}'.removeprefix('0')
    return f"{formatted_time}{formatted_decimal_part}"


def total_movie_duration(movie_file_path: Path|str) -> float:
        probe = ffmpeg.probe(str(movie_file_path))

        video_streams = [stream for stream in probe['streams']
                         if stream.get('codec_type', 'N/A') == 'video']

        return float(video_streams[0]['duration'])
