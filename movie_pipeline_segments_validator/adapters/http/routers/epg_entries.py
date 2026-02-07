import re
from typing import Annotated

from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field, TypeAdapter

from ..dependencies import get_settings
from ....lib.title_extractor import title_extractor
from ....lib.title_extractor.strategy import MediaGenre, extract_title_serie_episode_from_metadata, get_genre_from_supplied_value
from ....services.edl_scaffolder import get_title_strategy_context
from ....settings import Settings

router = APIRouter(
    prefix='/epg_entries',
    tags=['epg_entries']
)


class EpgEntryExtractTitleBody(BaseModel):
    title: Annotated[str, Field(description='Program title', examples=['Serie Name'])]
    subtitle: Annotated[str, Field(serialization_alias='sub_title', default='', description='Program subtitle or summary', examples=['Serie Name : Episode Name. Série policière. 2022. Saison 1. 16/26.'])]
    description: Annotated[str, Field(description='Program description', default='', examples=[''])]


epg_entry_extract_title_body_adapter = TypeAdapter(EpgEntryExtractTitleBody)


class ExtractTitleOut(BaseModel):
    title: Annotated[str, Field(description='Program title', examples=['Serie Name'])]
    season: Annotated[int | None, Field(description='Program season number', default=None, examples=[1])]
    episode: Annotated[int | None, Field(description='Program episode number', default=None, examples=[16])]
    episode_title: Annotated[str | None, Field(description='Program episode title', default=None, examples=['Episode Name'])]
    formatted_title: Annotated[str, Field(examples=['Serie Name S01E16.mp4'])]
    genre: Annotated[MediaGenre, Field(examples=['serie'])]


@router.post('/extract_title')
def extract_title(
    body: EpgEntryExtractTitleBody,
    config: Annotated[Settings, Depends(get_settings)]
) -> ExtractTitleOut:
    body_as_dict = epg_entry_extract_title_body_adapter.dump_python(body, by_alias=True)
    title_strategy_context = get_title_strategy_context(config)

    title_extractor_output = title_extractor.extract_title(metadata=body_as_dict)
    title_extractor_output.title = title_strategy_context.title_cleaner.clean_title(title_extractor_output.title)

    formatted_title = extract_title_serie_episode_from_metadata(
        title_strategy_context.normalized_title_series_extracted_metadata,
        title_extractor_output
    )

    if (m := re.match(r'S(?P<season>\d+)E(?P<episode>\d+)', formatted_title)) is not None:
        title_extractor_output.season = int(m.group('season'))
        title_extractor_output.episode = int(m.group('episode'))

    return ExtractTitleOut(
        title=title_extractor_output.title,
        season=title_extractor_output.season,
        episode=title_extractor_output.episode,
        episode_title=title_extractor_output.episode_title,
        formatted_title=title_extractor_output.formatted_title,
        genre=get_genre_from_supplied_value(body_as_dict)
    )
