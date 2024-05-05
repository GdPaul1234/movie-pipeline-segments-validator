from pathlib import Path

from .import_segments_from_file import prepend_last_segments_to_segment_file
from ..domain.context import SegmentValidatorContext
from ..services.edit_decision_file_dumper import extract_title


def has_any_edl(media_path: Path) -> bool:
    segment_file_path = media_path.with_suffix(f'{media_path.suffix}.segments.json')
    return len(list(segment_file_edl for segment_file_edl in segment_file_path.parent.glob(f'{media_path.name}*.*yml*') if segment_file_edl.suffix != '.txt')) > 0 \
        or media_path.with_suffix('.yml.done').is_file()


def flush_segments_of_previous_loaded_media(context: SegmentValidatorContext):
    prepend_last_segments_to_segment_file(context.filepath, context.segment_container)


def prefill_name(context: SegmentValidatorContext):
    if has_any_edl(context.filepath):
        raise ValueError(f'Validated segments already exists for {context.filepath}')

    return extract_title(context.filepath, context.config)
