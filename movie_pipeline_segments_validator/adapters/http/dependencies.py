import os
from functools import cache
from pathlib import Path
from typing import Annotated

from fastapi import Depends, HTTPException, status

from ...adapters.repository.session_repository import SessionRepository
from ...settings import Settings


@cache
def get_settings():
    config_path = Path(os.getenv('CONFIG_PATH', default=Path.home() / '.movie_pipeline_segments_validator' / 'config.env'))
    
    if not config_path.is_file():
        config_path.parent.mkdir(exist_ok=True)
        config_path.write_text('')

    os.chdir(config_path.parent)
    settings = Settings(_env_file=config_path, _env_file_encoding='utf-8')  # type: ignore

    return settings


def get_session_repository(config: Annotated[Settings, Depends(get_settings)]):
    return SessionRepository(config)


def get_session(session_repository: Annotated[SessionRepository, Depends(get_session_repository)], session_id: str):
    try:
        return session_repository.get(session_id)
    except KeyError as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f'Session {e.args[0]} not found')
