import importlib
from pathlib import Path
import yaml

from movie_pipeline.services.edl_scaffolder import MovieProcessedFileGenerator, channel_pattern, PathScaffolder

from ..domain.segment_container import SegmentContainer
from ..domain import edl_content_schema

from settings import Settings


def ensure_decision_file_template(source_path: Path, config: Settings):
    return PathScaffolder(source_path, config).scaffold()


def extract_title(source_path: Path, config: Settings):
    path_scaffolder = PathScaffolder(source_path, config)

    matches = channel_pattern.search(source_path.stem)

    if not matches:
        return 'Nom du fichier  converti.mp4'

    channel = matches.group(1)
    title_strategy_name = path_scaffolder._titles_strategies.get(channel) or 'NaiveTitleExtractor'
    mod = importlib.import_module('movie_pipeline.lib.title_extractor.title_extractor')
    title_strategy = getattr(mod, title_strategy_name)(path_scaffolder._title_cleaner)

    return MovieProcessedFileGenerator(source_path, title_strategy, path_scaffolder._series_extracted_metadata).extract_title()


def dump_decision_file(title: str, source_path: Path, segment_container: SegmentContainer, skip_backup: bool, config: Settings):
    ensure_decision_file_template(source_path, config)
    decision_file_path = source_path.with_suffix(f'{source_path.suffix}.yml')

    decision_file_content = {
        'filename': title,
        'segments': f'{repr(segment_container)},',
        'skip_backup': skip_backup
    }

    if edl_content_schema.is_valid(decision_file_content):
        decision_file_path.write_text(yaml.safe_dump(decision_file_content), encoding='utf-8')
        decision_file_path.with_suffix('.yml.txt').unlink(missing_ok=True)
        return decision_file_path

    return None
