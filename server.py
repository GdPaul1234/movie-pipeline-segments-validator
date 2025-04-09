import os
import uvicorn

from movie_pipeline_segments_validator.adapters.http.dependencies import get_settings


def run_server():
    cwd = os.getcwd()
    config = get_settings()
    os.chdir(cwd)

    uvicorn.run(
        'movie_pipeline_segments_validator.adapters.http.main:app',
        host=config.Server.HOST,
        reload=config.Server.DEBUG_MODE,
        port=config.Server.PORT
    )


if __name__ == "__main__":
    run_server()
