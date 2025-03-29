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
    imported_segments: dict[str, str]
    media_player: IVideoPlayer = field(repr=False)
    segment_container: SegmentContainer = field(default_factory=SegmentContainer)
    selected_segments: list[Segment] = field(default_factory=list)

    @property
    def position(self) -> float:
        return self.media_player.position

    @property
    def duration(self) -> float:
        return self.media_player.duration

    @property
    def position_percent(self) -> float:
        return self.position / self.duration

    @classmethod
    def init_context(cls, filepath: Path, config: Settings):
        media_player = SimpleVideoOnlyPlayerConsumer(filepath)
        imported_segments = import_segments(filepath)

        return cls(
            media_player=media_player,
            filepath=filepath,
            imported_segments=imported_segments,
            config=config
        )


@dataclass
class TimelineSegment:
    value: Segment
    fid: Any = None


@dataclass
class TimelineContext:
    position_handle: Any = None
    segments: list[TimelineSegment] = field(default_factory=list)
