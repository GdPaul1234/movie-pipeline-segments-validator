from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from ..domain.segment_container import Segment, SegmentContainer
from ..lib.video_player.simple_video_only_player import SimpleVideoOnlyPlayerConsumer
from ..lib.video_player.video_player import IVideoPlayer
from ..services.import_segments_from_file import import_segments
from ..settings import Settings


@dataclass
class SegmentValidatorContext:
    filepath: Path
    config: Settings
    imported_segments: dict[str, str] = field(init=False)
    media_player: IVideoPlayer = field(init=False, repr=False)
    segment_container: SegmentContainer = field(default_factory=SegmentContainer)
    selected_segments: list[Segment] = field(default_factory=list)

    def __post_init__(self):
        self.media_player = SimpleVideoOnlyPlayerConsumer(self.filepath)
        self.imported_segments = import_segments(self.filepath)

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
