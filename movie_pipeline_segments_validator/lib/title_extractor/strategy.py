import re
from dataclasses import dataclass
from operator import itemgetter
from pathlib import Path
from typing import cast

from .title_serie_extractor import extract_serie_field, is_serie_from_supplied_value


class NotSuitableTitleExtractorStrategy(Exception):
    pass


@dataclass
class TitleExtractorOutput:
    title: str
    season: int | None = None
    episode: int | None = None
    episode_title: str | None = None

    @property
    def formatted_title(self):
        forbidden_char_pattern = re.compile(r'[\/:*?<>|"]')
        title = re.sub(forbidden_char_pattern, '_', self.title)

        if self.episode is not None:
            return f'{title} S{self.season:02d}E{self.episode:02d}'

        if self.episode_title is not None:
            episode_title = re.sub(forbidden_char_pattern, '_', self.episode_title)
            return f'{title}__{episode_title}'

        return title


def naive_title(movie_path: Path, metadata, **kwargs) -> TitleExtractorOutput:
    if metadata:
        return TitleExtractorOutput(title=metadata['title'])
    elif matches := kwargs['title_pattern'].search(movie_path.stem):
        return TitleExtractorOutput(title=matches.group(1))
    else:
        raise ValueError('Inappropriate file path provided: not following movie name convention')


def expanded_subtitle_title(movie_path: Path, metadata, **kwargs) -> TitleExtractorOutput:
    if not metadata or '...' not in metadata['title']:
        raise NotSuitableTitleExtractorStrategy(f'Not suitable "expanded_subtitle_title" strategy for "{movie_path.stem}" ({kwargs})')

    title, sub_title = cast(tuple[str, str], itemgetter('title', 'sub_title')(metadata))
    sub_title = sub_title.removeprefix(f'{title} : ')

    extracted_title = cast(re.Match[str], kwargs['title_pattern'].match(sub_title)).group(1)
    extracted_episode_title = cast(re.Match[str], kwargs['episode_pattern'].search(sub_title)).group(1) if is_serie_from_supplied_value(sub_title) else None

    if extracted_episode_title is not None:
        extracted_episode_title = extracted_episode_title.strip("' ")

    return TitleExtractorOutput(title=extracted_title, episode_title=extracted_episode_title)


def subtitle_aware_title(movie_path: Path, metadata, **kwargs) -> TitleExtractorOutput:
    if not metadata or not is_serie_from_supplied_value(metadata):
        raise NotSuitableTitleExtractorStrategy(f'Not suitable "subtitle_aware_title" strategy for "{movie_path.stem} ({kwargs})"')

    episode, season = [
        int(extracted_serie_field) if (extracted_serie_field := extract_serie_field(metadata, episode_extractor_params)) else None
        for episode_extractor_params in [kwargs['episode_extractor_params'], kwargs['season_extractor_params']]
    ]

    title: str = metadata['title']
    for episode_extractor_params in [kwargs['episode_extractor_params'], kwargs['season_extractor_params']]:
        if episode_extractor_params[0] == 'title':
            title = re.sub(episode_extractor_params[1], '', title)
    title = re.sub(r'\(\w*\)', '', title).strip('- ')

    return TitleExtractorOutput(title=title, episode=episode, season=season or 1)
