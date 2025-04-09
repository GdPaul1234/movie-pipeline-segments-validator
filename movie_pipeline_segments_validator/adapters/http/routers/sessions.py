from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Path, status
from pydantic import BaseModel, Field, ValidationError
from pydantic.types import DirectoryPath

from ....adapters.http.dependencies import get_session, get_session_repository
from ....adapters.repository.resources import Session
from ....adapters.repository.session_repository import SessionRepository

router = APIRouter(
    prefix='/sessions',
    tags=['sessions']
)


class SessionCreateBody(BaseModel):
    root_path: Annotated[DirectoryPath, Field(description='root path for medias')]


@router.post('/', status_code=status.HTTP_201_CREATED)
def create_session(
    body: SessionCreateBody,
    session_repository: Annotated[SessionRepository, Depends(get_session_repository)]
) -> Session:
    try:
        return session_repository.create(body.root_path)
    except ValidationError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=e.errors())
    

@router.get('/{session_id}')
def show_session(
    session_id: Annotated[str, Path(title='session id')],
    session: Annotated[Session, Depends(get_session)]
) -> Session:
    return session


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
