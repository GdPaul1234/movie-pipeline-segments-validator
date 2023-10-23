import argparse
import logging
from pathlib import Path
import PySimpleGUI as sg
import os

from settings import Settings
from movie_pipeline_segments_validator.main import main as run_gui


def ask_paths() -> list[Path]:
    paths = [sg.popup_get_file('Select a video file')]
    return [path for path in paths if path is not None]


def main():
    scriptname = os.path.basename(__file__)
    parser = argparse.ArgumentParser(scriptname)
    levels = ('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL')

    parser.add_argument('--log-level', default='INFO', choices=levels)
    parser.add_argument('--config-path', default='config.env', help='Config path')

    options = parser.parse_args()
    logging.basicConfig(level=options.log_level)

    config = Settings(_env_file=options.config_path, _env_file_encoding='utf-8')  # type: ignore

    if len(paths := ask_paths()):
        run_gui(paths, config)


if __name__ == '__main__':
    main()
