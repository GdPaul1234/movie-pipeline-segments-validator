import unittest
from movie_pipeline_segments_validator.domain.detected_segments import humanize_segments
from movie_pipeline_segments_validator.domain.segment_container import Segment, SegmentContainer


class TestSegmentContainer(unittest.TestCase):

    def setUp(self):
        self.segment_container = SegmentContainer()

    def test_initial_state_empty(self):
        """Test that the initial state of a SegmentContainer is empty."""
        self.assertEqual(self.segment_container.segments, tuple())

    def test_add_segment(self):
        """Test adding a valid segment."""
        segment = Segment(0.0, 10.0)
        self.segment_container.add(segment)
        self.assertIn(segment, self.segment_container.segments)

    def test_non_overlapping_segments(self):
        """Test adding non-overlapping segments."""
        segment1 = Segment(0.0, 5.0)
        self.segment_container.add(segment1)

        segment2 = Segment(6.0, 10.0)  # This one is not overlapping with the first segment
        self.segment_container.add(segment2)

        self.assertEqual(len(self.segment_container.segments), 2)

    def test_overlapping_segments(self):
        """Test adding overlapping segments."""
        segment1 = Segment(0.0, 5.0)
        self.segment_container.add(segment1)

        segment2 = Segment(3.0, 8.0)  # This one overlaps with the first segment
        self.segment_container.add(segment2)

        self.assertEqual(len(self.segment_container.segments), 1)

    def test_remove_segment(self):
        """Test removing a valid segment."""
        segment = Segment(0.0, 10.0)
        self.segment_container.add(segment)
        self.segment_container.remove(segment)
        self.assertNotIn(segment, self.segment_container.segments)

    def test_merge_segments(self):
        """Test merging segments."""
        segment1 = Segment(0.0, 5.0)
        self.segment_container.add(segment1)

        segment2 = Segment(6.0, 8.0)
        self.segment_container.add(segment2)

        segment3 = Segment(9.0, 10.0)
        self.segment_container.add(segment3)
        self.assertEqual(len(self.segment_container.segments), 3)

        self.segment_container.merge([segment2, segment3])
        self.assertEqual(len(self.segment_container.segments), 2)

        merged_segment = Segment(6.0, 10.0)  # This is the expected merged segment
        self.assertIn(merged_segment, self.segment_container.segments)

    def test_representation(self):
        """Test the string representation of the SegmentContainer."""
        segment1 = Segment(0.0, 5.0)
        self.segment_container.add(segment1)

        segment2 = Segment(8.0, 12.0)
        self.segment_container.add(segment2)

        expected_output = humanize_segments([{ 'start': segment1.start, 'end': segment1.end }, { 'start': segment2.start, 'end': segment2.end }])
        self.assertEqual(str(self.segment_container), expected_output)


if __name__ == '__main__':
    unittest.main()
