from fastapi import FastAPI
from fastapi.middleware.gzip import GZipMiddleware

from movie_pipeline_segments_validator.adapters.http.routers import session_media_segments

from .routers import sessions, session_medias

app = FastAPI()

app.add_middleware(GZipMiddleware)

app.include_router(sessions.router)
app.include_router(session_medias.router)
app.include_router(session_media_segments.router)
