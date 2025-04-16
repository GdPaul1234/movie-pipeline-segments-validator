import itertools
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Literal

MediaPathState = Literal[
    'waiting_metadata',
    'no_segment',
    'waiting_segment_review',
    'segment_reviewed',
    'media_processing',
    'media_processed'
]


def cache_media_dir_entries(root_path: Path, media_ext: str):
    if not root_path.is_dir():
        raise ValueError('root_path is not a directory')

    return set(
        itertools.chain(
            root_path.glob(f'*{media_ext}'),
            root_path.glob(f'*{media_ext}.metadata.json'),
            root_path.glob(f'*{media_ext}.segments.json'),
            root_path.glob(f'*{media_ext}.*yml*'),
        )
    )


@dataclass
class MediaPath:
    path: Path
    cached_media_dir_entries: set[Path] = field(repr=False, default_factory=set)

    def __post_init__(self):
        if not len(self.cached_media_dir_entries):
            self.cached_media_dir_entries = cache_media_dir_entries(self.path.parent, self.path.suffix)

    @property
    def state(self) -> MediaPathState:
        media_processing_suffix_regex = re.compile(fr'{re.escape(self.path.name)}\..*yml.*')

        if self.path.with_suffix(f'{self.path.suffix}.metadata.json') not in self.cached_media_dir_entries:
            return 'waiting_metadata'
        elif self.path.with_suffix(f'{self.path.suffix}.segments.json') not in self.cached_media_dir_entries:
            return 'no_segment'
        elif self.path.with_suffix(f'{self.path.suffix}.yml') in self.cached_media_dir_entries:
            return 'segment_reviewed'
        elif self.path.with_suffix(f'{self.path.suffix}.yml.done') in self.cached_media_dir_entries:
            return 'media_processed'
        elif any(segment_file_edl.suffix != '.txt' for segment_file_edl in self.cached_media_dir_entries if media_processing_suffix_regex.match(segment_file_edl.name)):
            return 'media_processing'
        else:
            return 'waiting_segment_review'
