from fastapi import FastAPI

from .routers import sessions, session_medias

app = FastAPI()

app.include_router(sessions.router)
app.include_router(session_medias.router)
