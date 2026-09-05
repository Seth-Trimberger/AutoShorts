# AutoShorts

Automated Reddit story scraper and short-form video generator.

## External tools

Video rendering invokes these commands at runtime. They must be on `PATH` or configured via environment variables.

| Tool | Purpose | Install (macOS Homebrew) |
|------|---------|--------------------------|
| `edge-tts` | Text-to-speech | `pip install edge-tts` |
| `whisper` | Caption generation | `pip install openai-whisper` |
| `ffmpeg` | Video assembly | `brew install ffmpeg-full` (needs libass for subtitles) |

### Environment variable overrides

```bash
export FFMPEG_PATH=/opt/homebrew/opt/ffmpeg-full/bin/ffmpeg
export EDGE_TTS_COMMAND=edge-tts
export WHISPER_COMMAND=whisper
```

Verify local FFmpeg has the required subtitle filter:

```bash
ffmpeg -filters | grep subtitles
```

If that prints nothing, install `ffmpeg-full` or set `FFMPEG_PATH` to an
FFmpeg build compiled with libass.

On Linux/Docker, set these to the paths inside your container or venv.

## Running

**Interactive CLI (local dev):**

```bash
mvn spring-boot:run
```

**Headless / systemd** — disable the stdin CLI:

```bash
autoshorts.cli.enabled=false mvn spring-boot:run
```

Scheduled scraping and video rendering continue when the CLI is disabled.

`docker compose up --build` runs both PostgreSQL and the application. The app
container includes Java, FFmpeg with subtitle support, edge-tts, and Whisper.

## CLI commands

- Paste a Reddit URL to queue it
- `process` — scrape the next queued URL
- `render` — render the next pending story
- `render <id>` — render a specific story
- `render <id> voice <name>` — render with a voice override
- `voices` — list the configured voice pool
- `exit` — quit
