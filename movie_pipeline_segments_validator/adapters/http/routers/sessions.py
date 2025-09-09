from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status
from pydantic import BaseModel, Field, ValidationError
from pydantic.types import DirectoryPath

from ....adapters.http.dependencies import get_session, get_session_repository, get_settings
from ....adapters.repository.resources import Session
from ....adapters.repository.session_repository import SessionRepository, build_media
from ....services.media_selector_service import list_medias
from ....settings import Settings

router = APIRouter(
    prefix='/sessions',
    tags=['sessions']
)


class SessionCreateBody(BaseModel):
    root_path: Annotated[DirectoryPath, Field(description='root path for medias', examples=[r'V:\PVR'])]


@router.post('/', status_code=status.HTTP_201_CREATED)
def create_session(
    body: SessionCreateBody,
    session_repository: Annotated[SessionRepository, Depends(get_session_repository)]
) -> Session:
    try:
        return session_repository.create(body.root_path)
    except ValidationError as e:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=e.errors())
    

@router.get('/{session_id}')
def show_session(
    session_id: Annotated[str, Path(title='session id')],
    session: Annotated[Session, Depends(get_session)],
    session_repository: Annotated[SessionRepository, Depends(get_session_repository)],
    config: Annotated[Settings, Depends(get_settings)],
    refresh: Annotated[bool, Query(description='Refresh session medias state')]
) -> Session:
    if not refresh:
        return session

    medias = {
        media.path.stem: build_media(media, config)
        for media in list_medias(session.root_path, config)
    }
    return session_repository.set(session.model_copy(update={"medias": medias}))


@router.delete('/{session_id}')
def destroy_session(
    session_id: Annotated[str, Path(title='session id')],
    session_repository: Annotated[SessionRepository, Depends(get_session_repository)]
):
    try:
        session_repository.delete(session_id)
        return {}
    except KeyError as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f'Session {e.args[0]} not found')
