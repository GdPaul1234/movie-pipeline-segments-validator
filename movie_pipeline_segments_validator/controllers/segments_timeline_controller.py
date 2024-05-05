from typing import Any, cast

import PySimpleGUI as sg

from ..domain.widget import WidgetEvent, WidgetKey
from ..services import timeline_service


def draw_segments(window: sg.Window, _event = None, _values = None):
    graph = cast(sg.Graph, window[WidgetKey.SEGMENT_TIMELINE_KEY.value])
    timeline_service.draw_segments_to_sg_graph(window.metadata, graph.metadata, graph)


def draw_current_position_indicator(window: sg.Window, _event: str, _values: dict[str, Any]):
    graph = cast(sg.Graph, window[WidgetKey.SEGMENT_TIMELINE_KEY.value])
    timeline_service.draw_current_position_indicator_to_sg_graph(window.metadata, graph.metadata, graph)
    window.refresh()


def resize_timeline(window: sg.Window, _event: str, _values: dict[str, Any]):
    graph = cast(sg.Graph, window[WidgetKey.SEGMENT_TIMELINE_KEY.value])
    timeline_service.resize_timeline_in_sg_graph(window.metadata, graph.metadata, graph)
    window.refresh()


def update_selected_segment(window: sg.Window, _event: str, values: dict[str, Any]):
    graph = cast(sg.Graph, window[WidgetKey.SEGMENT_TIMELINE_KEY.value])

    forwarded_selected_timeline_segment = timeline_service.forward_selected_segment_from_sg_graph_to_context(
        context=window.metadata,
        timeline_context=graph.metadata,
        graph=graph,
        selected_segment_position=values[WidgetKey.SEGMENT_TIMELINE_KEY.value]
    )

    if forwarded_selected_timeline_segment is not None:
        window.write_event_value(WidgetEvent.SEGMENT_TIMELINE_SELECTED_EVENT.value, forwarded_selected_timeline_segment)


def update_current_position_indicator_position(window: sg.Window, _event: str, _values: dict[str, Any]):
    graph = cast(sg.Graph, window[WidgetKey.SEGMENT_TIMELINE_KEY.value])
    timeline_service.update_current_position_indicator_position_in_sg_graph(window.metadata, graph.metadata, graph)
