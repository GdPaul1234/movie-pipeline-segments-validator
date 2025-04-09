from dataclasses import asdict
from typing import Annotated

from pydantic import BaseModel, Field, PastDatetime, computed_field
from pydantic.types import DirectoryPath, FilePath, NonNegativeFloat

from ...domain import FILENAME_REGEX, STR_SEGMENT_REGEX
from ...domain.context import SegmentValidatorContext
from ...domain.media_path import MediaPath, MediaPathState
from ...domain.segment_container import Segment as SegmentContainerSegment
from ...domain.segment_container import SegmentContainer
from ...settings import Settings

StrSegment = Annotated[str, Field(pattern=STR_SEGMENT_REGEX)]


class Segment(BaseModel):
    start: Annotated[NonNegativeFloat, Field(description='segment start position in seconds')]
    end: Annotated[NonNegativeFloat, Field(description='segment end position in seconds')]

    @computed_field
    @property
    def duration(self) -> float:
        return self.end - self.start


class Media(BaseModel):
    filepath: Annotated[FilePath, Field(description='media file path')]
    state: Annotated[MediaPathState, Field(description='media state')]
    title: Annotated[str, Field(default='', pattern=FILENAME_REGEX, description='ouput title, must ends with .mp4')]
    skip_backup: Annotated[bool, Field(default=False, description='skip backup step')]
    imported_segments: Annotated[dict[str, StrSegment], Field(description='imported segments from `{filepath}.segments.json`')]
    segments: Annotated[list[Segment], Field(default_factory=list, description='segments for edit decision list ouput')]

    def to_segment_validator_context(self, config: Settings):
        segment_container = SegmentContainer()

        for segment in self.segments:
            segment_container.add(SegmentContainerSegment(segment.start, segment.end))

        return SegmentValidatorContext(
            filepath=self.filepath,
            config=config,
            title=self.title,
            skip_backup=self.skip_backup,
            segment_container=segment_container
        )

    @classmethod
    def from_segment_validator_context(cls, context: SegmentValidatorContext):
        return cls(
            filepath=context.filepath,
            state=MediaPath(context.filepath).state,
            title=context.title,
            skip_backup=context.skip_backup,
            imported_segments=context.imported_segments,
            segments=[Segment(**asdict(segment)) for segment in context.segment_container.segments]
        )


class Session(BaseModel):
    id: Annotated[str, Field(description='session id')]
    created_at: PastDatetime
    updated_at: PastDatetime
    root_path: Annotated[DirectoryPath, Field(description='root path for medias')]
    medias: Annotated[dict[str, Media], Field(description='medias in root_path to process indexed by stem (filename without extension)')]
