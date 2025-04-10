import os
from importlib.metadata import metadata, version

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.gzip import GZipMiddleware

from ...adapters.http.dependencies import get_settings
from ...adapters.http.routers import session_media_segments
from .routers import session_medias, sessions


def run_server():
    cwd = os.getcwd()
    config = get_settings()
    os.chdir(cwd)

    uvicorn.run(
        'movie_pipeline_segments_validator.adapters.http.main:app',
        host=config.Server.HOST,
        reload=config.Server.DEBUG_MODE,
        port=config.Server.PORT
    )


app = FastAPI(
    title="movie-pipeline-segments-validator",
    version=version('movie_pipeline_segments_validator'),
    summary='A simple API to validate detected segments and generate edit decision files for movie-pipeline',
    description='\n'.join(metadata('movie_pipeline_segments_validator')['Description'].splitlines()[4:]).strip()
)

app.add_middleware(GZipMiddleware)

app.include_router(sessions.router)
app.include_router(session_medias.router)
app.include_router(session_media_segments.router)
