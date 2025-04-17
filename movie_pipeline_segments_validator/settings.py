import json
import shutil
from pathlib import Path
from typing import Any, Optional

import yaml
from pydantic import BaseModel, Field
from pydantic.types import DirectoryPath, FilePath
from pydantic_settings import BaseSettings, SettingsConfigDict


class PathSettings(BaseModel):
    movies_folder: DirectoryPath
    series_folder: DirectoryPath
    title_strategies: Optional[FilePath] = None
    title_re_blacklist: Optional[FilePath] = None
    series_extracted_metadata: Optional[FilePath] = None
    db_path: Path = Path.home() / '.movie_pipeline_segments_validator' / 'sessions'


class PathContent(BaseModel):
    title_strategies: Optional[dict] = None
    title_re_blacklist: Optional[str] = None
    series_extracted_metadata: Optional[dict] = None


class MediaSelectorSettings(BaseModel):
    media_extension: str = Field(pattern=r'^\.\w+$', default='.ts')


class ServerSettings(BaseModel):
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG_MODE: bool = False


class Settings(BaseSettings):
    Paths: PathSettings
    PathsContent: PathContent = PathContent()
    MediaSelector: MediaSelectorSettings = MediaSelectorSettings()
    Server: ServerSettings = ServerSettings()

    ffmpeg_path: FilePath = shutil.which('ffmpeg')  # type: ignore

    model_config = SettingsConfigDict(env_nested_delimiter='__')

    def model_post_init(self, __context: Any) -> None:
        if (title_strategies_path := self.Paths.title_strategies) is not None:
            self.PathsContent.title_strategies = yaml.safe_load(title_strategies_path.read_text('utf-8'))

        if (title_re_blacklist_path := self.Paths.title_re_blacklist) is not None:
            self.PathsContent.title_re_blacklist = title_re_blacklist_path.read_text()

        if (series_extracted_metadata_path := self.Paths.series_extracted_metadata) is not None:
            self.PathsContent.series_extracted_metadata = json.loads(series_extracted_metadata_path.read_text(encoding='utf-8'))
