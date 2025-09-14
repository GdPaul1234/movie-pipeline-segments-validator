# movie pipeline segments validator

A simple GUI to review the part of the video to be kept and generate the edit decision file
for the movie pipeline process command.

## Installation

After installing poetry, just run the following command in the project root to install the backend:

```sh
poetry install
```

To install the new compose multiplatform MoviePipelineSegmentsValidatorUi:

  1) Open the MoviePipelineSegmentsValidatorUi folder in IntelliJ IDEA

  2) Follow the [Kotlin Multiplatform quickstart instructions](https://www.jetbrains.com/help/kotlin-multiplatform-dev/quickstart.html#set-up-the-environment)
     to set up the environment

  3) Sync the project with Gradle

  4) Run the `:composeApp:run` Gradle task to run the application, `:composeApp:packageDistributionForCurrentOS` to
     generate the installer to install MoviePipelineSegmentsValidatorUi on desktop target

## Usage example

  1) Create and fill in the configuration in `~/.movie_pipeline_segments_validator/config.env` according to
      `movie_pipeline_segments_validator/settings.py` if not done

  2) Run the API server (`python server.py` or `movie_pipeline_segments_validator_server` if you install the movie_pipeline_segments_validator package).
      You can open [http://localhost:8000/docs](http://localhost:8000/docs)if you want to access the built-in API documentation.

  3) Launch MoviePipelineSegmentsValidatorUi. Create a session or choose an existing one to open.

  4) Review media segments!

![Reviewing movie segments with movie pipeline segments validator](screenshot.png)

### Requirements

A video MUST meet the following requirements to be processed by this program.

Given a video named `Channel 1_Serie Name. 'Title..._2022-12-05-2203-20.ts`:

- The medatadata file MUST exist and:

  - MUST have the following name : `Channel 1_Serie Name. 'Title..._2022-12-05-2203-20.ts.metadata.json`

  - At least HAVE the same shape than this example:

    ```json
    {
        "title": "Serie Name",
        "sub_title": "Serie Name : Episode Name. Série policière. 2022. Saison 1. 16/26.",
        "description": ""
    }
    ```

- The result of the ```movie_pipeline detect_segments``` MIGHT exists and
  - MUST have the following name: `Channel 1_Serie Name. 'Title..._2022-12-05-2203-20.ts.segments.json`

  - MUST comply to the following schema:

    ```python
    detected_segments_schema = Schema({ str: Regex(r'(?:(?:\d{2}:\d{2}:\d{2}\.\d{2,3})-(?:\d{2}:\d{2}:\d{2}\.\d{2,3}),)+') })
    ```

  > **NOTE**
  >
  > Each key of the `.segments.json` document is the name given to the detected parts of a video that should be kept.
  >
  > The segments to be checked are populated from this file.
  >
  > Whenever you change the video you are reviewing, the currently reviewed segments that you want to keep are saved to this file.

## Release History

- V0.3.0
  - Remove PySimpleGUI movie_pipeline_segments_validator UI
  - Add kotlin compose multiplatform MoviePipelineSeglentsValidatorUI, see Installation section to use it
  - Improve API performance: EDL file contents and imported segments are loaded on demand, so you must load media details
    (`GET /sessions/{session_id}/medias/{media_stem}`) before validating / editing segments to prefill segments from
    imported segments and load filename and `skip_backup` property from EDL files.

- V0.2.2
  - Normalize episode title for extract_title_serie_episode_from_metadata title
  - Do not load segments content when creating API session. These segments can be retrieved when getting a media.

- V0.2.1
  - Improve API performance

- V0.2.0
  - Introduce new API for headless segment validations. Documentation is available at localhost:8000/docs

- V0.1.4
  - Deprecate PySimpleGUI renderer

- V0.1.3
  - Create movie_pipeline_segment_validator package

- V0.1.2
  - Update the title extractor to the latest version.
    - Given a file containing the series name and the name of each episode with the formatted episode number,
        the title extractor can derive the formatted episode number of the series
        from the extracted episode name of the video.

- V0.1.1
  - Add support to review a directory directly from the GUI

- V0.1.0
  - Extract segment validator from the movie-pipeline repository
