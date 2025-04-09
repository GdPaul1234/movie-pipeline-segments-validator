from schema import Optional, Regex, Schema

FILENAME_REGEX = r"^[\w&àéèï'!()\[\], #-.:]+\.mp4$"
STR_SEGMENT_REGEX = r'(?:(?:\d{2}:\d{2}:\d{2}\.\d{2,3})-(?:\d{2}:\d{2}:\d{2}\.\d{2,3}),)+'

edl_content_schema = Schema({
    # valid filename of the output file with .mp4 suffix
    "filename": Regex(FILENAME_REGEX),
    # format: hh:mm:ss.ss-hh:mm:ss.ss,hh-mm:ss…
    "segments": Regex(STR_SEGMENT_REGEX),
    Optional("skip_backup", default=False): bool
})
