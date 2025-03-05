import argparse
import logging
from pathlib import Path
from typing import Literal, cast
import PySimpleGUI as sg
import os

from .services.media_selector_service import list_medias
from .settings import Settings
from .main import main as run_gui


def ask_paths_item_type():
    event, values = sg.Window('Segments Reviewer', [
        [sg.Column([
            [sg.Text('Welcome to Movie Pipeline Segments Validator', font='Any 12')],
            [sg.Text('Choose a file or a directory to begin')],
            [sg.Button('Open File', key='file'),
                sg.Button('Open Directory', key='directory')]
        ], element_justification='center', justification='center')]
    ]).read(close=True)  # type: ignore

    return cast(Literal['file', 'directory'] | None, event)


def ask_paths(config: Settings) -> list[Path]:
    paths = []

    while not (len(paths)):
        item_type = ask_paths_item_type()
        match item_type:
            case sg.WIN_CLOSED:
                break
            case 'file':
                paths = list(map(Path, filter(bool, [sg.popup_get_file('Select a video file')])))
            case 'directory':
                if (folder_path := sg.popup_get_folder('Select a folder')) is not None:
                    paths = [
                        media_path.path
                        for media_path in list_medias(Path(folder_path), config)
                        if media_path.state == 'waiting_segment_review'
                    ]

    return paths


def main():
    scriptname = os.path.basename(__file__)
    parser = argparse.ArgumentParser(scriptname)
    levels = ('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL')

    parser.add_argument('--log-level', default='INFO', choices=levels)
    parser.add_argument('--config-path', default=Path.home() / '.movie_pipeline_segments_validator' / 'config.env', help='Config path', type=Path)

    options = parser.parse_args()
    logging.basicConfig(level=options.log_level)

    if not options.config_path.is_file():
        options.config_path.parent.mkdir(exist_ok=True)
        options.config_path.write_text('')

    os.chdir(options.config_path.parent)
    config = Settings(_env_file=options.config_path, _env_file_encoding='utf-8')  # type: ignore

    if len(paths := ask_paths(config)):
        run_gui(paths, config)


if __name__ == '__main__':
    main()
