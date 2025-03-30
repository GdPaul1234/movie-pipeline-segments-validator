from pathlib import Path
from typing import Optional
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


class MediaSelectorSettings(BaseModel):
    media_extension: str = Field(pattern=r'^\.\w+$', default='.ts')


class Settings(BaseSettings):
    Paths: PathSettings
    MediaSelector: MediaSelectorSettings = MediaSelectorSettings()

    model_config = SettingsConfigDict(env_nested_delimiter='__')
