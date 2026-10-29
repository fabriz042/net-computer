"""Load rolled Logback JSONL files into PostgreSQL and remove loaded files."""

from __future__ import annotations

import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

import psycopg
from psycopg.types.json import Jsonb


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
ENV_FILE = REPOSITORY_ROOT / ".env"
ARCHIVE_DIR = REPOSITORY_ROOT / "back-main" / "logs" / "archive"
DATABASE_VARIABLES = (
    "DATABASE_HOST",
    "DATABASE_PORT",
    "DATABASE_NAME",
    "DATABASE_USER",
    "DATABASE_PASSWORD",
)

INSERT_LOG_SQL = """
INSERT INTO application_logs (
    occurred_at,
    level,
    logger_name,
    thread_name,
    message,
    mdc,
    throwable
)
VALUES (%s, %s, %s, %s, %s, %s, %s)
"""

def read_database_settings() -> dict[str, str | int]:
    file_settings: dict[str, str] = {}
    if ENV_FILE.exists():
        for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            name, value = line.split("=", 1)
            name = name.removeprefix("export ").strip()
            value = value.strip()
            if len(value) >= 2 and value[0] == value[-1] and value[0] in ("\"", "'"):
                value = value[1:-1]
            elif "#" in value:
                value = value.partition("#")[0].rstrip()
            if name in DATABASE_VARIABLES:
                file_settings[name] = value

    values = {name: os.getenv(name) or file_settings.get(name) for name in DATABASE_VARIABLES}
    missing = [name for name, value in values.items() if not value]
    if missing:
        raise ValueError("Missing database settings: " + ", ".join(missing))

    return {
        "host": values["DATABASE_HOST"] or "",
        "port": int(values["DATABASE_PORT"] or "5432"),
        "dbname": values["DATABASE_NAME"] or "",
        "user": values["DATABASE_USER"] or "",
        "password": values["DATABASE_PASSWORD"] or "",
        "connect_timeout": 10,
    }


def parse_log_file(path: Path, content: bytes) -> list[tuple[object, ...]]:
    records: list[tuple[object, ...]] = []
    for line_number, raw_line in enumerate(content.splitlines(), start=1):
        if not raw_line.strip():
            continue

        try:
            event = json.loads(raw_line)
            occurred_at = datetime.fromtimestamp(
                event["timestamp"] / 1000,
                tz=timezone.utc,
            )
        except (json.JSONDecodeError, KeyError, TypeError, ValueError) as error:
            raise ValueError(f"Invalid JSON log at {path.name}:{line_number}") from error

        throwable = event.get("throwable")
        records.append(
            (
                occurred_at,
                event.get("level", "UNKNOWN"),
                event.get("loggerName", ""),
                event.get("threadName", ""),
                event.get("formattedMessage", event.get("message")),
                Jsonb(event.get("mdc") or {}),
                Jsonb(throwable) if throwable is not None else None,
            )
        )

    return records


def process_file(connection: psycopg.Connection, path: Path) -> int:
    content = path.read_bytes()
    records = parse_log_file(path, content)

    with connection.transaction():
        with connection.cursor() as cursor:
            cursor.executemany(INSERT_LOG_SQL, records)

    path.unlink(missing_ok=True)
    return len(records)


def main() -> int:
    try:
        database_settings = read_database_settings()
        files = sorted(ARCHIVE_DIR.glob("back-main.*.jsonl"))
        if not files:
            print("No rolled JSONL files to process.")
            return 0

        with psycopg.connect(**database_settings) as connection:
            for path in files:
                record_count = process_file(connection, path)
                print(f"{path.name}: {record_count} records loaded; file removed.")
    except (OSError, ValueError, psycopg.Error) as error:
        sqlstate = getattr(error, "sqlstate", None)
        details = f" (SQLSTATE {sqlstate})" if sqlstate else ""
        print(f"Log ETL failed{details}: {error}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
