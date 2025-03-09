from typing import Any, cast

import PySimpleGUI as sg

from movie_pipeline_segments_validator.services import detector_service

from ...domain.context import SegmentValidatorContext
from ...domain.widget import WidgetEvent, WidgetKey
from ...views.segments_list import render_values


def populate_detector_selector(window: sg.Window, _event: str, _values: dict[str, Any]):
    selector = cast(sg.Combo, window[WidgetKey.DETECTOR_SELECTOR_KEY.value])
    metadata = cast(SegmentValidatorContext, window.metadata)

    # Update available detectors
    detectors = list(metadata.imported_segments.keys())
    selector.update(values=detectors)

    # Import the first detector result if available
    if len(detectors) >= 1:
        selector.update(value=detectors[0])
        window.write_event_value(WidgetEvent.SELECTED_DETECTOR_UPDATED_EVENT.value, detectors[0])


def import_segments_from_selected_detector(window: sg.Window, _event: str, values: dict[str, Any]):
    detector_key = values[WidgetKey.DETECTOR_SELECTOR_KEY.value]
    detector_service.import_segments_from_selected_detector(window.metadata, detector_key)
    render_values(window)
