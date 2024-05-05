from ..domain.context import SegmentValidatorContext, TimelineContext, TimelineSegment

import PySimpleGUI as sg


def draw_segments_to_sg_graph(context: SegmentValidatorContext, timeline_context: TimelineContext, graph: sg.Graph):
    for segment in timeline_context.segments:
        graph.delete_figure(segment.fid)
    timeline_context.segments.clear()

    for segment in context.segment_container.segments:
        top_left = (segment.start / context.duration, 1.)
        bottom_right = (segment.end / context.duration, 0.)
        fill_color = '#f0f3f7' if segment in context.selected_segments else '#283b5b'

        rect = graph.draw_rectangle(top_left, bottom_right, fill_color=fill_color)
        graph.send_figure_to_back(rect)
        timeline_context.segments.append(TimelineSegment(fid=rect, value=segment))


def draw_current_position_indicator_to_sg_graph(context: SegmentValidatorContext, timeline_context: TimelineContext, graph: sg.Graph):
    if timeline_context.position_handle is None:
        position_handle = graph.draw_line((0., 0.), (0., 1.), color='red')
        timeline_context.position_handle = position_handle
    else:
        resize_timeline_in_sg_graph(context, timeline_context, graph)


def resize_timeline_in_sg_graph(context: SegmentValidatorContext, timeline_context: TimelineContext, graph: sg.Graph):
    graph.CanvasSize = graph.get_size()
    graph.relocate_figure(timeline_context.position_handle, context.position_percent, 0)
    draw_segments_to_sg_graph(context, timeline_context, graph)


def forward_selected_segment_from_sg_graph_to_context(
        context: SegmentValidatorContext,
        timeline_context: TimelineContext,
        graph: sg.Graph,
        selected_segment_position: tuple[float, float]
):
    if len(figures := graph.get_figures_at_location(selected_segment_position)):
        forwarded_selected_timeline_segment = next(
            (segment.value for segment in timeline_context.segments if segment.fid == figures[0]),
            None
        )

        if forwarded_selected_timeline_segment is not None:
            context.selected_segments = [forwarded_selected_timeline_segment]
            return forwarded_selected_timeline_segment

    return None


def update_current_position_indicator_position_in_sg_graph(context: SegmentValidatorContext, timeline_context: TimelineContext, graph: sg.Graph):
    graph.relocate_figure(timeline_context.position_handle, context.position_percent, 0)
