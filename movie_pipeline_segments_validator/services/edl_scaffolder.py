import logging
import re
from dataclasses import dataclass
from pathlib import Path
from types import SimpleNamespace
from typing import cast

from schema import Schema

from ..lib.title_extractor.title_cleaner import TitleCleaner
from ..lib.title_extractor.title_extractor import ITitleExtractor
from ..lib.title_extractor.title_serie_extractor import extract_title_serie_episode_from_metadata
from ..lib.util import remove_diacritics
from ..settings import Settings

logger = logging.getLogger(__name__)


class MovieProcessedFileGenerator:
    def __init__(self, movie_file_path: Path, title_extractor: ITitleExtractor, series_extracted_metadata) -> None:
        self._movie_file_path = movie_file_path
        self._title_extractor = title_extractor
        self._series_extracted_metadata = series_extracted_metadata

    def extract_title(self) -> str:
        extracted_title = self._title_extractor.extract_title(self._movie_file_path)
        return extract_title_serie_episode_from_metadata(self._series_extracted_metadata, extracted_title)


channel_pattern = re.compile(r'^([^_]+)_')
title_strategies_schema = Schema({
    str: lambda strategy: strategy in ('NaiveTitleExtractor', 'SubtitleTitleExpanderExtractor', 'SerieSubTitleAwareTitleExtractor', 'SerieTitleAwareTitleExtractor')
})


@dataclass
class TitleStrategyContext:
    titles_strategies: dict[str, str]
    normalized_title_series_extracted_metadata: dict
    title_cleaner: TitleCleaner

    def __post_init__(self):
        self.normalized_title_series_extracted_metadata = self.normalize_title_series_extracted_metadata(self.normalized_title_series_extracted_metadata)

    @staticmethod
    def normalize_title_series_extracted_metadata(series_extracted_metadata: dict):
        return {
        serie_name: {
            remove_diacritics(episode_name).lower(): episode_metadata
            for episode_name, episode_metadata in episode_title_mappings.items()
        }
        for serie_name, episode_title_mappings in series_extracted_metadata.items()
    }



def get_title_strategy_context(config: Settings) -> TitleStrategyContext:
    titles_strategies = title_strategies_schema.validate(config.PathsContent.title_strategies or {})
    series_extracted_metadata = config.PathsContent.series_extracted_metadata or {}

    blacklist_path = cast(Path, SimpleNamespace(read_text=lambda *args, **kwargs: config.PathsContent.title_re_blacklist or ''))
    title_cleaner = TitleCleaner(blacklist_path)

    return TitleStrategyContext(titles_strategies, series_extracted_metadata, title_cleaner)
