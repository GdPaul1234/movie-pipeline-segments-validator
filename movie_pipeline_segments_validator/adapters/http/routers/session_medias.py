from typing import Annotated
from fastapi import APIRouter, Depends, Path

from ....adapters.http.dependencies import get_media
from ....adapters.repository.resources import Media


router = APIRouter(
    prefix='/sessions/{session_id}/medias',
    tags=['medias']
)


@router.get('/{media_stem}')
def show_media(
    media_stem: Annotated[str, Path(title='media stem (filename without extension)')],
    media: Annotated[Media, Depends(get_media)]
):
    return media
