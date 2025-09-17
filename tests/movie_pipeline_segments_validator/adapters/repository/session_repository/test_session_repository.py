from contextlib import closing
import json
import shutil
import unittest
from pathlib import Path
import re

from pydantic import ValidationError

from movie_pipeline_segments_validator.adapters.repository.resources import Segment as MediaSegment
from movie_pipeline_segments_validator.adapters.repository.session_repository import SessionRepository, build_media
from movie_pipeline_segments_validator.domain.segment_container import Segment, SegmentContainer
from movie_pipeline_segments_validator.services import segment_service

from ....concerns import copy_files, create_output_movies_directories, get_output_movies_directories, lazy_load_config_file


class TestSessionRepository(unittest.TestCase):
    def setUp(self) -> None:
        self.input_dir_path = Path(__file__).parent / 'in'
        self.video_path = self.input_dir_path / 'Channel 1_Movie Name_2022-12-05-2203-20.mp4'
        self.serie_path = self.input_dir_path / 'Channel 2_Serie Name_2022-12-05-2203-20.mp4'

        self.output_dir_path, self.movie_dir_path, self.serie_dir_path, _ = get_output_movies_directories(Path(__file__).parent)

        sample_video_path = Path(__file__).parent.parent.parent.parent / 'ressources' / 'counter-30s.mp4'
        copy_files([
            {'source': sample_video_path, 'destination': self.video_path},
            {'source': sample_video_path, 'destination': self.serie_path}
        ])

        video_metadata_content = json.dumps({
            "title": "Movie Name...",
            "sub_title": "Movie Name... : Movie Name, le titre long. Bla Bla Bla"
        }, indent=2)
        self.video_path.with_suffix('.mp4.metadata.json').write_text(video_metadata_content)

        serie_metadata_content = json.dumps({
            "title": "Serie Name",
            "sub_title": "Serie Name : Episode Name. Série policière. 2022. Saison 1. 16/26.",
            "description": ""
        }, indent=2)
        self.serie_path.with_suffix('.mp4.metadata.json').write_text(serie_metadata_content)

        self.serie_segments_content = json.dumps({
            "result_2024-10-05T11:40:39.732479": "00:25:26.000-00:34:06.000,00:40:10.000-01:01:23.000,01:07:34.000-01:17:59.000",
            "auto": "00:00:00.000-01:05:54.840,00:42:38.980-01:49:59.300,01:05:54.840-01:49:59.300"
        })
        self.serie_path.with_suffix('.mp4.segments.json').write_text(self.serie_segments_content)

        create_output_movies_directories(Path(__file__).parent)

        self.config = lazy_load_config_file(Path(__file__).parent)()
        self.session_repository = SessionRepository(self.config)


    def test_create_session(self):
        # create session
        with closing(self.session_repository) as session_repository:
            session = session_repository.create(self.input_dir_path)
            self.assertTrue(self.config.Paths.db_path.with_suffix('.sqlite3').is_file())

        # init medias list
        self.assertEqual(['Movie Name, le titre long.mp4', 'Serie Name S01E16.mp4'], [media.title for media in session.medias.values()])
        self.assertEqual(['no_segment', 'waiting_segment_review'], [media.state for media in session.medias.values()])


    def test_get_session_exist(self):
        with closing(self.session_repository) as session_repository:
            session = session_repository.create(self.input_dir_path)
            self.assertEqual(session, self.session_repository.get(session.id))


    def test_get_session_not_found(self):
        with closing(self.session_repository) as session_repository:
            session_repository.create(self.input_dir_path)

            with self.assertRaisesRegex(KeyError, 'unknown-session'):
                self.session_repository.get('unknown-session')


    def test_update_media_valid(self):
        with closing(self.session_repository) as session_repository:
            session = session_repository.create(self.input_dir_path)
            serie_context = build_media(session.medias[self.serie_path.stem]).to_segment_validator_context(self.config)

            segment_container = SegmentContainer()
            segment_container.add(Segment(start=1526, end=3246))
            serie_context.segment_container = segment_container

            segment_service.validate_segments(serie_context)

            updated_session = session_repository.update_media(session.id, serie_context)

            updated_serie_media = updated_session.medias[self.serie_path.stem]
            self.assertEqual('segment_reviewed', updated_serie_media.state)
            self.assertEqual([MediaSegment(start=1526, end=3246)], updated_serie_media.segments)


    def test_update_media_invalid(self):
        with closing(self.session_repository) as session_repository:
            session = session_repository.create(self.input_dir_path)
            serie_context = build_media(session.medias[self.serie_path.stem]).to_segment_validator_context(self.config)

            serie_context.title = serie_context.title.replace('.mp4', '.invalid_ext')

            expected_error_message = re.escape("String should match pattern '^[\\w&àéèï'!()\\[\\], #-.:]+\\.mp4$' [type=string_pattern_mismatch, input_value='Serie Name S01E16.invalid_ext', input_type=str]")
            with self.assertRaisesRegex(ValidationError, expected_error_message):
                session_repository.update_media(session.id, serie_context)


    def test_destroy_session_exist(self):
        with closing(self.session_repository) as session_repository:
            session = session_repository.create(self.input_dir_path)

            session_repository.delete(session.id)

            with self.assertRaisesRegex(KeyError, session.id):
                session_repository.get(session.id)


    def test_destroy_session_not_found(self):
        with self.assertRaisesRegex(KeyError, 'unknown-session'):
            with closing(self.session_repository) as session_repository:
                session_repository.get('unknown-session')


    def tearDown(self) -> None:
        shutil.rmtree(self.input_dir_path)
        shutil.rmtree(self.output_dir_path)

    @classmethod
    def tearDownClass(cls) -> None:
        [db_file.unlink() for db_file in Path(__file__).parent.glob('sessions.*')]
