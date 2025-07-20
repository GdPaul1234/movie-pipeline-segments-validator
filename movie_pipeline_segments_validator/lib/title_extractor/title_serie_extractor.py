import re

from ...lib.util import remove_diacritics

ExtractorParams = tuple[str, re.Pattern[str]]
serie_hints = ['Série', 'Saison', 'Mini-série']
serie_hints_location = ['description', 'title', 'sub_title']


def extract_serie_field(metadata, extractor_params: ExtractorParams):
    field, pattern = extractor_params

    matches = pattern.search(metadata[field])
    return matches.group(1).rjust(2, '0') if matches else 'xx'


def is_serie_from_supplied_value(supplied_value: str | dict):
    def contains_any_serie_hint(value: str):
        return any(value.count(serie_hint) for serie_hint in serie_hints)

    if isinstance(supplied_value, str):
        return contains_any_serie_hint(supplied_value)
    return any(contains_any_serie_hint(supplied_value[field]) for field in serie_hints_location)


def extract_title_serie_episode_from_metadata(normalized_title_series_extracted_metadata: dict[str, dict[str, dict[str, str]]], extracted_title: str):
    if re.search(r'S\d{2}E\d{2,3}', extracted_title) is not None:
        return extracted_title

    m = re.match(r'(?P<showtitle>^.+)__(?P<title>.+)', extracted_title) \
        or re.match(r"(?P<showtitle>[\w&àéèï'!., ()\[\]#-]+) '(?P<title>.+)'", extracted_title)

    if m is not None:
        show_title, episode_title = m.group('showtitle'), remove_diacritics(m.group('title').lower())
        formatted_episode = normalized_title_series_extracted_metadata.get(show_title, {}).get(episode_title, {}).get('formattedEpisode')
        return ' '.join(value for value in [show_title, formatted_episode] if value is not None)

    return extracted_title
