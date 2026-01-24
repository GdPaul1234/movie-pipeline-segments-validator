import logging
import re
from dataclasses import dataclass
from pathlib import Path
from types import SimpleNamespace
from typing import cast

from ..lib.title_extractor.title_cleaner import TitleCleaner
from ..lib.title_extractor.title_extractor import extract_title
from ..lib.title_extractor.title_serie_extractor import extract_title_serie_episode_from_metadata
from ..lib.util import remove_diacritics
from ..settings import Settings

logger = logging.getLogger(__name__)


class MovieProcessedFileGenerator:
    def __init__(self, movie_file_path: Path, title_cleaner: TitleCleaner, series_extracted_metadata) -> None:
        self._movie_file_path = movie_file_path
        self._title_cleaner = title_cleaner
        self._series_extracted_metadata = series_extracted_metadata

    def extract_title(self) -> str:
        extracted_title = extract_title(self._movie_file_path).formatted_title
        extracted_title = self._title_cleaner.clean_title(extracted_title)
        return extract_title_serie_episode_from_metadata(self._series_extracted_metadata, extracted_title)


channel_pattern = re.compile(r'^([^_]+)_')


@dataclass
class TitleStrategyContext:
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
    series_extracted_metadata = config.PathsContent.series_extracted_metadata or {}

    blacklist_path = cast(Path, SimpleNamespace(read_text=lambda *args, **kwargs: config.PathsContent.title_re_blacklist or ''))
    title_cleaner = TitleCleaner(blacklist_path)

    return TitleStrategyContext(series_extracted_metadata, title_cleaner)
