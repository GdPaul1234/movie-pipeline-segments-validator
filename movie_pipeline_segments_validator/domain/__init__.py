from schema import Optional, Regex, Schema


edl_content_schema = Schema({
    # valid filename of the output file with .mp4 suffix
    "filename": Regex(r"^[\w&àéèï'!()\[\], #-.]+\.mp4$"),
    # format: hh:mm:ss.ss-hh:mm:ss.ss,hh-mm:ss…
    "segments": Regex(r'(?:(?:\d{2}:\d{2}:\d{2}\.\d{2,3})-(?:\d{2}:\d{2}:\d{2}\.\d{2,3}),)+'),
    Optional("skip_backup", default=False): bool
})
