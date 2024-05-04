from dataclasses import dataclass
from typing import cast

from ..lib.util import position_in_seconds


segment_t = tuple[float, float]


@dataclass
class MovieSegments:
    segments: list[segment_t]

    def __init__(self, raw_segments: str) -> None:
        splitted_raw_segments = raw_segments.removesuffix(',').split(',')

        self.segments = [
            cast(segment_t, tuple(position_in_seconds(x) for x in segment.split('-', 2)))
            for segment in splitted_raw_segments
        ] if splitted_raw_segments != [''] else []

    @property
    def total_seconds(self) -> float:
        return sum([stop - start for start, stop in self.segments])

