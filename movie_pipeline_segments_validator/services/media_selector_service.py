from pathlib import Path

from ..domain.context import SegmentValidatorContext
from ..domain.media_path import MediaPath
from ..services.edit_decision_file_dumper import extract_title
from ..settings import Settings
from .import_segments_from_file import prepend_last_segments_to_segment_file


def has_any_edl(media_path: Path, config: Settings) -> bool:
    return any(
        media_path.state in ['segment_reviewed', 'media_processing', 'media_processed']
        for media_path in list_medias(media_path, config)
    )


def flush_segments_of_previous_loaded_media(context: SegmentValidatorContext):
    prepend_last_segments_to_segment_file(context.filepath, context.segment_container)


def prefill_name(context: SegmentValidatorContext):
    if has_any_edl(context.filepath, context.config):
        raise ValueError(f'Validated segments already exists for {context.filepath}')

    return extract_title(context.filepath, context.config)


def list_medias(filepath: Path, config: Settings) -> list[MediaPath]:
    paths = [filepath] if filepath.is_file() \
        else filepath.glob(f'*{config.MediaSelector.media_extension}')

    return sorted(
        (MediaPath(path) for path in paths),
        key=lambda media_path: media_path.path.name
    )
