from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from movie_pipeline_segments_validator.services.edit_decision_file_dumper import extract_title

from ..domain.segment_container import Segment, SegmentContainer
from ..lib.video_player.video_player import IVideoPlayer
from ..services.import_segments_from_file import import_segments
from ..settings import Settings


@dataclass
class SegmentValidatorContext:
    filepath: Path
    config: Settings
    media_player: IVideoPlayer = field(repr=False)
    title: str = ''
    skip_backup: bool = False
    imported_segments: dict[str, str] = field(init=False)
    segment_container: SegmentContainer = field(default_factory=SegmentContainer)
    selected_segments: list[Segment] = field(default_factory=list)

    def __post_init__(self):
        self.imported_segments = import_segments(self.filepath)

        if self.title == '':
            self.title = extract_title(self.filepath, self.config)

    @property
    def position(self) -> float:
        return self.media_player.position

    @property
    def duration(self) -> float:
        return self.media_player.duration

    @property
    def position_percent(self) -> float:
        return self.position / self.duration


@dataclass
class TimelineSegment:
    value: Segment
    fid: Any = None


@dataclass
class TimelineContext:
    position_handle: Any = None
    segments: list[TimelineSegment] = field(default_factory=list)
