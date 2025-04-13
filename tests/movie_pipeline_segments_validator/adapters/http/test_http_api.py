import json
import os
import shutil
import unittest
from pathlib import Path
from unittest import mock

from fastapi import status
from fastapi.testclient import TestClient
from pydantic import TypeAdapter

from movie_pipeline_segments_validator.adapters.http.main import app
from movie_pipeline_segments_validator.adapters.repository.resources import Media, Segment, Session
from movie_pipeline_segments_validator.adapters.repository.session_repository import SessionRepository
from movie_pipeline_segments_validator.domain.detected_segments import humanize_segments

from ...concerns import copy_files, create_output_movies_directories, get_output_movies_directories, lazy_load_config_file


class TestHttpApi(unittest.TestCase):
    def setUp(self) -> None:
        self.input_dir_path = Path(__file__).parent / 'in'
        self.video_path = self.input_dir_path / 'Channel 1_Movie Name_2022-12-05-2203-20.mp4'
        self.serie_path = self.input_dir_path / 'Channel 2_Serie Name_2022-12-05-2203-20.mp4'

        self.output_dir_path, self.movie_dir_path, self.serie_dir_path, _ = get_output_movies_directories(Path(__file__).parent)

        sample_video_path = Path(__file__).parent.parent.parent / 'ressources' / 'counter-30s.mp4'
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
        self.enterContext(mock.patch.dict(os.environ, {'CONFIG_PATH': str(Path(__file__).parent / 'test_config.env')}))

        self.client = TestClient(app)
        self.session_repository = SessionRepository(self.config)


    # routers/sessions.py
    
    def test_create_session(self):
        response = self.client.post('/sessions', json={ 'root_path': str(self.input_dir_path) })
        self.assertEqual(status.HTTP_201_CREATED, response.status_code)

        actual_session = TypeAdapter(Session).validate_json(response.text)

        # init medias list
        self.assertEqual(['Movie Name, le titre long.mp4', 'Serie Name S01E16.mp4'], [media.title for media in actual_session.medias.values()])
        self.assertEqual(['no_segment', 'waiting_segment_review'], [media.state for media in actual_session.medias.values()])
        self.assertEqual([
            {}, {k: f'{v},' for k, v in json.loads(self.serie_segments_content).items()}],
            [media.imported_segments for media in actual_session.medias.values()]
        )

        # import saved segments
        video_context = actual_session.medias[self.video_path.stem]
        self.assertEqual({}, video_context.imported_segments)


    def test_show_session_exist(self):
        session = self.session_repository.create(self.input_dir_path)

        response = self.client.get(f'/sessions/{session.id}')
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_session = TypeAdapter(Session).validate_json(response.text)
        self.assertEqual(session, actual_session)

    
    def test_show_session_not_found(self):
        response = self.client.get('/sessions/unknown-session')
        self.assertEqual(status.HTTP_404_NOT_FOUND, response.status_code)
        self.assertEqual("Session b'unknown-session' not found", response.json()['detail'])


    def test_destroy_session(self):
        session = self.session_repository.create(self.input_dir_path)

        response = self.client.delete(f'/sessions/{session.id}')
        self.assertEqual(status.HTTP_200_OK, response.status_code)
        self.assertEqual({}, response.json())

        response = self.client.get(f'/sessions/{session.id}')
        self.assertEqual(status.HTTP_404_NOT_FOUND, response.status_code)


    # routers/session_medias.py

    def test_show_media(self):
        session = self.session_repository.create(self.input_dir_path)

        response = self.client.get(f'/sessions/{session.id}/medias/{self.video_path.stem}')
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual(session.medias[self.video_path.stem], actual_media)    


    def test_validate_media_segments(self):
        session = self.session_repository.create(self.input_dir_path)
        session.medias[self.serie_path.stem].segments = [Segment(start=1526, end=3246)]
        serie_context = session.medias[self.serie_path.stem].to_segment_validator_context(self.config)
        self.session_repository.update_media(session.id, serie_context)

        response = self.client.post(f'/sessions/{session.id}/medias/{self.serie_path.stem}/validate_segments')
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        expected_edl_path = self.serie_path.with_suffix('.mp4.yml')
        self.assertTrue(expected_edl_path.is_file())
        self.assertEqual(str(expected_edl_path), response.json()['edl_path'])


    # routers/session_media_segments.py

    def test_load_imported_segments(self):
        session = self.session_repository.create(self.input_dir_path)
        detector_key = 'result_2024-10-05T11:40:39.732479'

        response = self.client.post(f'/sessions/{session.id}/medias/{self.serie_path.stem}/segments/{detector_key}/import')
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual(
            json.loads(self.serie_segments_content)[detector_key],
            humanize_segments([{'start': segment.start, 'end': segment.end} for segment in actual_media.segments])
        )


    def test_create_segment(self):
        session = self.session_repository.create(self.input_dir_path)

        body = {'position': 15.2}
        response = self.client.post(f'/sessions/{session.id}/medias/{self.video_path.stem}/segments', json=body)
        self.assertEqual(status.HTTP_201_CREATED, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual([Segment(start=15.2, end=16.2)], actual_media.segments)


    def test_edit_segment(self):
        session = self.session_repository.create(self.input_dir_path)
        session.medias[self.video_path.stem].segments = [Segment(start=3, end=8), Segment(start=10, end=17), Segment(start=18, end=25)]
        serie_context = session.medias[self.video_path.stem].to_segment_validator_context(self.config)
        self.session_repository.update_media(session.id, serie_context)

        body = {'new_position': 12, 'edge': 'start'}
        response = self.client.patch(f'/sessions/{session.id}/medias/{self.video_path.stem}/segments/10s-17s', json=body)
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual([Segment(start=3, end=8), Segment(start=12, end=17), Segment(start=18, end=25)], actual_media.segments)

        body = {'new_position': 15, 'edge': 'end'}
        response = self.client.patch(f'/sessions/{session.id}/medias/{self.video_path.stem}/segments/12s-17s', json=body)
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual([Segment(start=3, end=8), Segment(start=12, end=15), Segment(start=18, end=25)], actual_media.segments)


    def test_delete_segments(self):
        session = self.session_repository.create(self.input_dir_path)
        session.medias[self.video_path.stem].segments = [Segment(start=3, end=8), Segment(start=10, end=17), Segment(start=18, end=25)]
        serie_context = session.medias[self.video_path.stem].to_segment_validator_context(self.config)
        self.session_repository.update_media(session.id, serie_context)

        body = {'segments': [{'start': segment[0], 'end': segment[1]} for segment in ((3, 8), (18, 25))]}
        response = self.client.request('DELETE', f'/sessions/{session.id}/medias/{self.video_path.stem}/segments', json=body)
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual([Segment(start=10, end=17)], actual_media.segments)


    def test_merge_segments(self):
        session = self.session_repository.create(self.input_dir_path)
        session.medias[self.video_path.stem].segments = [Segment(start=0, end=5), Segment(start=6, end=8), Segment(start=9, end=10)]
        serie_context = session.medias[self.video_path.stem].to_segment_validator_context(self.config)
        self.session_repository.update_media(session.id, serie_context)

        body = {'segments': [{'start': segment[0], 'end': segment[1]} for segment in ((6, 8), (9, 10))]}
        response = self.client.post(f'/sessions/{session.id}/medias/{self.video_path.stem}/segments/merge', json=body)
        self.assertEqual(status.HTTP_200_OK, response.status_code)

        actual_media = TypeAdapter(Media).validate_json(response.text)
        self.assertEqual([Segment(start=0, end=5), Segment(start=6, end=10)], actual_media.segments)


    def tearDown(self) -> None:
        [db_file.unlink() for db_file in Path(__file__).parent.glob('sessions.*')]
        shutil.rmtree(self.input_dir_path)
        shutil.rmtree(self.output_dir_path)
