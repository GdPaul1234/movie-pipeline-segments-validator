import json
import re
from functools import lru_cache
from itertools import combinations_with_replacement
from pathlib import Path

from .strategy import NotSuitableTitleExtractorStrategy, TitleExtractorOutput, expanded_subtitle_title, naive_title, subtitle_aware_title


@lru_cache
def load_metadata(movie_path: Path, cache_busting_key: int):
    movie_metadata_path = movie_path.with_suffix(f'{movie_path.suffix}.metadata.json')

    if movie_metadata_path.exists():
        return json.loads(movie_metadata_path.read_text(encoding='utf-8'))


def extract_title(movie_path: Path | None = None, metadata: dict | None = None, cache_busting_key = 0) -> TitleExtractorOutput:
    if metadata is None:
        metadata = load_metadata(movie_path, cache_busting_key)

    def subtitle_title_expander_extractor():
        return expanded_subtitle_title(
            movie_path,
            metadata,
            title_pattern=re.compile(r"([^.]+)\."),
            episode_pattern=re.compile(r"\. (.+) Série \(\w+\)\.")
        )

    def serie_subtitle_aware_title_extractor():
        episode_pattern = re.compile(r'(\d+)[/-]\d+')
        season_pattern = re.compile(r'Saison (\d+)')

        field_combinations = combinations_with_replacement(('sub_title', 'title'), 2)
        for episode_field, season_field in field_combinations:
            title_extractor_output = subtitle_aware_title(
                movie_path,
                metadata,
                episode_extractor_params=(episode_field, episode_pattern),
                season_extractor_params=(season_field, season_pattern)
            )

            if any((title_extractor_output.episode, title_extractor_output.episode_title)):
                return title_extractor_output

        media_id = movie_path.stem if movie_path is not None else 'this entry'
        raise NotSuitableTitleExtractorStrategy(f'Not suitable "serie_subtitle_aware_title_extractor" strategy found for "{media_id}"')

    def naive_title_extractor():
        return naive_title(
            movie_path,
            metadata,
            title_pattern=re.compile(r"_([\w&àéèï'!., ()\[\]#-]+)_")
        )

    for strategy in [subtitle_title_expander_extractor, serie_subtitle_aware_title_extractor, naive_title_extractor]:
        try:
            return strategy()
        except NotSuitableTitleExtractorStrategy:
            pass

    raise NotSuitableTitleExtractorStrategy
