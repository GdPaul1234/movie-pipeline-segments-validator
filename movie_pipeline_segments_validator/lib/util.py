import subprocess
import time
from datetime import timedelta
from pathlib import Path


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


def total_movie_duration(movie_file_path: Path | str) -> float:
    # cf https://trac.ffmpeg.org/wiki/FFprobeTips#Duration
    cmds = [
        ('ffprobe', '-v', 'quiet', '-select_streams', 'v:0', '-show_entries', 'stream=duration', '-of', 'default=noprint_wrappers=1:nokey=1', str(movie_file_path)),
        ('ffprobe', '-v', 'quiet', '-show_entries', 'format=duration', '-of', 'default=noprint_wrappers=1:nokey=1', str(movie_file_path))
    ]

    return float(next((duration for cmd in cmds if (duration := subprocess.check_output(cmd).splitlines()[0]) != b'N/A'), -1))
