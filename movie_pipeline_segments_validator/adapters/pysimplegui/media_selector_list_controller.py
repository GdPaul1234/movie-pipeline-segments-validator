from pathlib import Path
from typing import Any, cast

import PySimpleGUI as sg

from ...services import media_selector_service

from ...domain.context import SegmentValidatorContext
from ...domain.widget import WidgetEvent, WidgetKey
from ...views.texts import TEXTS


def populate_media_selector(window: sg.Window, _event: str, values: dict[str, Any]):
    selector = cast(sg.Listbox, window[WidgetKey.MEDIA_SELECTOR_KEY.value])

    media_paths, = values[WidgetEvent.APPLICATION_LOADED_EVENT.value]
    selector.update(values=media_paths, set_to_index=0)


def load_new_media(window: sg.Window, _event: str, values: dict[str, Any]):
    flash_notice_label = cast(sg.Text, window[WidgetKey.FLASH_TOP_NOTICE_KEY.value])
    selector = cast(sg.Listbox, window[WidgetKey.MEDIA_SELECTOR_KEY.value])
    metadata = cast(SegmentValidatorContext | None, window.metadata)
    filepath = cast(Path, values[WidgetKey.MEDIA_SELECTOR_KEY.value][0])

    if metadata is not None:
        media_selector_service.flush_segments_of_previous_loaded_media(metadata)

    flash_notice_label.update(value=TEXTS['loading_media'])
    window.refresh()

    if filepath.is_file():
        config = metadata.config if metadata else values['config']

        # init metadata
        window.metadata = SegmentValidatorContext(filepath, config)
        window.write_event_value(WidgetEvent.VIDEO_LOADED_EVENT.value, True)
        window.write_event_value(WidgetEvent.SEGMENT_IMPORTED_EVENT.value, True)

        # prefill name
        try:
            extracted_title = media_selector_service.prefill_name(window.metadata)
            window.write_event_value(WidgetEvent.PREFILL_NAME_EVENT.value, f'{extracted_title}.mp4')
        except ValueError as e:
            sg.popup_auto_close(str(e), title='Aborting segments validation')
            window.write_event_value(WidgetEvent.SEGMENTS_SAVED_EVENT.value, True)

    flash_notice_label.update(value=TEXTS['review_segments_description'])
    window.set_title(f'Segments Reviewer - {str(filepath)}')
    selector.set_value(filepath)
    window.refresh()


def toggle_media_selector_visibility(window: sg.Window, _event: str, _values: dict[str, Any]):
    media_selector_container = cast(sg.Frame, window[WidgetKey.MEDIA_SELECTOR_CONTAINER_KEY.value])
    toggle_media_selector_button = cast(sg.Button, window[WidgetKey.TOGGLE_MEDIA_SELECTOR_VISIBILITY_KEY.value])

    media_selector_container_visibility = not media_selector_container.visible
    toggle_media_selector_button.update(text='<<' if media_selector_container_visibility else '>>')
    media_selector_container.update(visible=media_selector_container_visibility)


def remove_validated_media_from_media_selector(window: sg.Window, _event: str, _values: dict[str, Any]):
    selector = cast(sg.Listbox, window[WidgetKey.MEDIA_SELECTOR_KEY.value])
    metadata = cast(SegmentValidatorContext, window.metadata)

    medias: list[Path] = selector.get_list_values()
    next_media = medias[(medias.index(metadata.filepath) + 1) % len(medias)]

    updated_medias = [media for media in medias if media != metadata.filepath]
    selector.update(values=updated_medias)

    window.write_event_value(WidgetEvent.MEDIA_SELECTOR_UPDATED_EVENT.value, [next_media])

