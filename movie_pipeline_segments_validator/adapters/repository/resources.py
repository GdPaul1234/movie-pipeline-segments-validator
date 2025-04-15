import textwrap
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
    start: Annotated[NonNegativeFloat, Field(description='segment start position in seconds', examples=[1526])]
    end: Annotated[NonNegativeFloat, Field(description='segment end position in seconds', examples=[3246])]

    @computed_field(description='segment duration in seconds', examples=[1720])
    @property
    def duration(self) -> float:
        return self.end - self.start


class Media(BaseModel):
    filepath: Annotated[
        FilePath,
        Field(description='media file path', examples=[r'V:\PVR\Channel 1_Movie Name_2022-12-05-2203-20.ts'])
    ]
    state: Annotated[
        MediaPathState,
        Field(
        description=textwrap.dedent('''
            media state:
              * `waiting_metadata` - No metadata file exists
              * `no_segment` - Metadata file exists, but no segments file exists 
              * `waiting_segment_review` - Both metadata and segments files exist but no review
              * `segment_reviewed` - Segments have been reviewed
              * `media_processing` - Processing is in progress
              * `media_processed` -  Processing is complete
        ''')
        )
    ]
    title: Annotated[
        str,
        Field(
            default='',
            pattern=FILENAME_REGEX,
            description='output title, must ends with .mp4',
            examples=['Movie Name, le titre long.mp4', 'Serie Name S01E16.mp4']
        )
    ]
    skip_backup: Annotated[bool, Field(default=False, description='skip backup step')]
    imported_segments: Annotated[
        dict[str, StrSegment],
        Field(
            description='imported segments from `{filepath}.segments.json`',
            examples=[{
                "result_2024-10-05T11:40:39.732479": "00:25:26.000-00:34:06.000,00:40:10.000-01:01:23.000,01:07:34.000-01:17:59.000",
                "auto": "00:00:00.000-01:05:54.840,00:42:38.980-01:49:59.300,01:05:54.840-01:49:59.300"
            }]
        )
    ]
    segments: Annotated[list[Segment], Field(default_factory=list, description='segments for edit decision list output')]

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
    root_path: Annotated[DirectoryPath, Field(description='root path for medias', examples=[r'V:\PVR'])]
    medias: Annotated[dict[str, Media], Field(description='medias to process in root_path indexed by stem (filename without extension)')]
