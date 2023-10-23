from typing import Optional
from pydantic import BaseModel
from pydantic.types import DirectoryPath, FilePath
from pydantic_settings import BaseSettings, SettingsConfigDict


class PathSettings(BaseModel):
    movies_folder: DirectoryPath
    series_folder: DirectoryPath
    title_strategies: Optional[FilePath] = None
    title_re_blacklist: Optional[FilePath] = None
    series_extracted_metadata: Optional[FilePath] = None


class Settings(BaseSettings):
    Paths: PathSettings

    model_config = SettingsConfigDict(env_nested_delimiter='__')
