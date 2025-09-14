import os
from contextlib import asynccontextmanager
from importlib.metadata import metadata, version
from pathlib import Path
from typing import Optional

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware

from ...adapters.http.dependencies import get_config_path, get_settings
from ...adapters.http.routers import session_media_segments
from ...adapters.repository.session_repository import SessionRepository
from ...settings import Settings
from .routers import session_medias, sessions


config: Optional[Settings] = None


def get_config():
    global config

    if config is not None:
        return config

    cwd = os.getcwd()
    config = get_settings(get_config_path())
    os.chdir(Path(__file__).parent.parent.parent if config.Server.DEBUG_MODE else cwd)
    return config


def run_server():
    config = get_config()
    uvicorn.run(
        'movie_pipeline_segments_validator.adapters.http.main:app',
        host=config.Server.HOST,
        reload=config.Server.DEBUG_MODE,
        port=config.Server.PORT
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    config = get_config()
    yield
    SessionRepository(config).close()


app = FastAPI(
    title="movie-pipeline-segments-validator",
    version=version('movie_pipeline_segments_validator'),
    summary='A simple API to validate detected segments and generate edit decision files for movie-pipeline',
    description='\n'.join(metadata('movie_pipeline_segments_validator')['Description'].splitlines()[39:]).strip(),
    lifespan=lifespan
)

app.add_middleware(CORSMiddleware, allow_origins=get_config().Server.ALLOW_ORIGINS, allow_methods=('*',))
app.add_middleware(GZipMiddleware)

app.include_router(sessions.router)
app.include_router(session_medias.router)
app.include_router(session_media_segments.router)
