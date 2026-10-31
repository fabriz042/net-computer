"""Load rolled Logback JSONL files into PostgreSQL with PySpark."""

from __future__ import annotations

import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

import psycopg
from psycopg.types.json import Jsonb
from pyspark.sql import SparkSession, functions as F
from pyspark.sql.types import LongType, StringType, StructField, StructType


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

LOG_EVENT_SCHEMA = StructType(
    [
        StructField("timestamp", LongType(), True),
        StructField("level", StringType(), True),
        StructField("loggerName", StringType(), True),
        StructField("threadName", StringType(), True),
        StructField("formattedMessage", StringType(), True),
        StructField("message", StringType(), True),
    ]
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


# Read PostgreSQL settings from the environment or the root .env file
def read_database_settings() -> dict[str, str | int]:
    file_settings: dict[str, str] = {}
    if ENV_FILE.exists():
        for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            name, value = (part.strip() for part in line.split("=", 1))
            if name in DATABASE_VARIABLES:
                file_settings[name] = value

    values = {
        name: os.getenv(name) or file_settings.get(name)
        for name in DATABASE_VARIABLES
    }
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


# Create a local Spark session configured to use UTC timestamps
def create_spark_session() -> SparkSession:
    spark = (
        SparkSession.builder.appName("application-log-etl")
        .master("local[2]")
        .config("spark.sql.session.timeZone", "UTC")
        .getOrCreate()
    )
    spark.sparkContext.setLogLevel("WARN")
    return spark


# Parse and transform one JSONL log file into database-ready records
def transform_log_file(
    spark: SparkSession,
    path: Path,
) -> list[tuple[object, ...]]:
    source = (
        spark.read.text(str(path))
        .where(F.length(F.trim(F.col("value"))) > 0)
        .withColumn(
            "event",
            F.from_json(
                F.col("value"),
                LOG_EVENT_SCHEMA,
                {"mode": "FAILFAST"},
            ),
        )
    )

    invalid_logs = source.where(
        F.col("event").isNull() | F.col("event.timestamp").isNull()
    )
    if invalid_logs.limit(1).count() > 0:
        raise ValueError(f"Invalid JSON log or missing timestamp in {path.name}")

    transformed_logs = source.select(
        F.expr("timestamp_millis(event.timestamp)").alias("occurred_at"),
        F.coalesce(F.col("event.level"), F.lit("UNKNOWN")).alias("level"),
        F.coalesce(F.col("event.loggerName"), F.lit("")).alias("logger_name"),
        F.coalesce(F.col("event.threadName"), F.lit("")).alias("thread_name"),
        F.coalesce(
            F.col("event.formattedMessage"),
            F.col("event.message"),
        ).alias("message"),
        F.get_json_object(F.col("value"), "$.mdc").alias("mdc_json"),
        F.get_json_object(F.col("value"), "$.throwable").alias("throwable_json"),
    )

    records: list[tuple[object, ...]] = []
    for row in transformed_logs.collect():
        occurred_at: datetime = row["occurred_at"]
        if occurred_at.tzinfo is None:
            occurred_at = occurred_at.replace(tzinfo=timezone.utc)
        else:
            occurred_at = occurred_at.astimezone(timezone.utc)

        mdc_value = json.loads(row["mdc_json"]) if row["mdc_json"] else {}
        mdc_value = mdc_value or {}
        throwable_value = (
            json.loads(row["throwable_json"])
            if row["throwable_json"] is not None
            else None
        )

        records.append(
            (
                occurred_at,
                row["level"],
                row["logger_name"],
                row["thread_name"],
                row["message"],
                Jsonb(mdc_value),
                Jsonb(throwable_value) if throwable_value is not None else None,
            )
        )

    return records


# Insert one file's records and remove the file after a successful commit
def process_file(
    spark: SparkSession,
    connection: psycopg.Connection,
    path: Path,
) -> int:
    records = transform_log_file(spark, path)

    if records:
        with connection.transaction():
            with connection.cursor() as cursor:
                cursor.executemany(INSERT_LOG_SQL, records)

    path.unlink(missing_ok=True)
    return len(records)


# Process every archived log file and return the command's exit status
def main() -> int:
    spark: SparkSession | None = None
    try:
        database_settings = read_database_settings()
        files = sorted(ARCHIVE_DIR.glob("back-main.*.jsonl"))
        if not files:
            print("No rolled JSONL files to process.")
            return 0

        spark = create_spark_session()
        with psycopg.connect(**database_settings) as connection:
            for path in files:
                record_count = process_file(spark, connection, path)
                print(f"{path.name}: {record_count} records loaded; file removed.")
    except Exception as error:
        sqlstate = getattr(error, "sqlstate", None)
        details = f" (SQLSTATE {sqlstate})" if sqlstate else ""
        print(f"Log ETL failed{details}: {error}", file=sys.stderr)
        return 1
    finally:
        if spark is not None:
            spark.stop()

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
