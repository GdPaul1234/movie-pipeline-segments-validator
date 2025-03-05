from dataclasses import dataclass
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


@dataclass
class MediaPath:
    path: Path

    @property
    def state(self) -> MediaPathState:
        if not self.path.with_suffix(f'{self.path.suffix}.metadata.json').is_file():
            return 'waiting_metadata'
        elif not self.path.with_suffix(f'{self.path.suffix}.segments.json').is_file():
            return 'no_segment'
        elif self.path.with_suffix(f'{self.path.suffix}.yml').is_file():
            return 'segment_reviewed'
        elif self.path.with_suffix(f'{self.path.suffix}.yml.done').is_file():
            return 'media_processed'
        elif any(segment_file_edl.suffix != '.txt' for segment_file_edl in self.path.parent.glob(f'{self.path.name}*.*yml*')):
            return 'media_processing'
        else:
            return 'waiting_segment_review'
