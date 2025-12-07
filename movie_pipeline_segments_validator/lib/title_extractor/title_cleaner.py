from pathlib import Path
import re


class TitleCleaner:
    def __init__(self, blacklist_path: Path) -> None:
        blacklist = blacklist_path.read_text(encoding='utf-8')
        self._blacklist_pattern = re.compile('|'.join(blacklist.splitlines()))

    def clean_title(self, title: str, stripe_apostrophe: bool) -> str:
        cleaned_title = self._blacklist_pattern.sub('', title).strip()

        if stripe_apostrophe:
            cleaned_title = cleaned_title.replace("__'", '__').replace(" '", ' ').strip(" '")

        return cleaned_title
