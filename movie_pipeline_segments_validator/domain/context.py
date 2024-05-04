from pathlib import Path
from typing import Any

from pydantic import BaseModel, ConfigDict

from ..controllers.import_segments_from_file import import_segments
from ..domain.segment_container import Segment, SegmentContainer
from ..lib.video_player.simple_video_only_player import SimpleVideoOnlyPlayerConsumer
from ..lib.video_player.video_player import IVideoPlayer
from ..settings import Settings


class SegmentValidatorContext(BaseModel):
    segment_container: SegmentContainer = SegmentContainer()
    selected_segments: list[Segment] = []
    imported_segments: dict[str, str]
    media_player: IVideoPlayer
    filepath: Path
    config: Settings

    model_config = ConfigDict(arbitrary_types_allowed=True)

    @property
    def position(self) -> float:
        return self.media_player.position

    @property
    def duration(self) -> float:
        return self.media_player.duration

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


class TimelineSegment(BaseModel):
    fid: Any = None
    value: Segment


class TimelineContext(BaseModel):
    position_handle: Any = None
    segments: list[TimelineSegment] = []
