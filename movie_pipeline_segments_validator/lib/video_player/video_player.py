from abc import ABC
from pathlib import Path


class IVideoPlayer(ABC):
    def __init__(self, source: Path) -> None:
        self._source = source
        self._current_position = 0.
        self._duration = 0.

    @property
    def position(self) -> float:
        return self._current_position

    @property
    def duration(self) -> float:
        return self._duration

    def set_position(self, position: float, **kwargs) -> None:
        self._current_position = position

    def set_relative_position(self, delta: float, **kwargs) -> None:
        self.set_position(self._current_position + delta, **kwargs)
