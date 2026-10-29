"""Rebuild search-term analytics from the generic events table."""

from __future__ import annotations

import os
import sys
from pathlib import Path

import psycopg


RESET_AGGREGATES_SQL = """
UPDATE search_queries
SET search_count = 0,
    searches_with_click = 0,
    products_found = 0
"""

AGGREGATE_SEARCHES_SQL = """
WITH search_events AS (
    SELECT
        e.id,
        e.session_id,
        e.created_at,
        btrim(e.metadata ->> 'searchTerm') AS search_term,
        lower(btrim(e.metadata ->> 'searchTerm')) AS normalized_query,
        CASE
            WHEN jsonb_typeof(e.metadata -> 'productIds') = 'array'
                THEN jsonb_array_length(e.metadata -> 'productIds')
            ELSE 0
        END AS products_found,
        lead(e.created_at) OVER session_searches AS next_search_at,
        lead(e.id) OVER session_searches AS next_search_id
    FROM events AS e
    WHERE e.event_type = 'SEARCH'
      AND jsonb_typeof(e.metadata -> 'searchTerm') = 'string'
      AND nullif(btrim(e.metadata ->> 'searchTerm'), '') IS NOT NULL
      AND char_length(btrim(e.metadata ->> 'searchTerm')) <= 100
    WINDOW session_searches AS (
        PARTITION BY e.session_id
        ORDER BY e.created_at, e.id
    )
), searches_with_click AS (
    SELECT
        s.id,
        s.session_id,
        s.created_at,
        s.search_term,
        s.normalized_query,
        s.products_found,
        s.next_search_at,
        s.next_search_id,
        EXISTS (
            SELECT 1
            FROM events AS c
            WHERE c.session_id = s.session_id
              AND c.event_type = 'CLICK'
              AND c.metadata ->> 'componentId' = 'search-results-product-list'
              AND (c.created_at, c.id) > (s.created_at, s.id)
              AND (
                  s.next_search_at IS NULL
                  OR (c.created_at, c.id) < (s.next_search_at, s.next_search_id)
              )
        ) AS has_click
    FROM search_events AS s
), query_aggregates AS (
    SELECT
        normalized_query,
        (array_agg(search_term ORDER BY created_at DESC, id DESC))[1] AS search_term,
        count(*)::bigint AS search_count,
        count(*) FILTER (WHERE has_click)::bigint AS searches_with_click,
        sum(products_found)::bigint AS products_found,
        min(created_at) AS first_searched_at,
        max(created_at) AS last_searched_at
    FROM searches_with_click
    GROUP BY normalized_query
)
INSERT INTO search_queries (
    normalized_query,
    search_term,
    search_count,
    searches_with_click,
    products_found,
    first_searched_at,
    last_searched_at
)
SELECT
    normalized_query,
    search_term,
    search_count,
    searches_with_click,
    products_found,
    first_searched_at,
    last_searched_at
FROM query_aggregates
ON CONFLICT (normalized_query) DO UPDATE
SET search_term = EXCLUDED.search_term,
    search_count = EXCLUDED.search_count,
    searches_with_click = EXCLUDED.searches_with_click,
    products_found = EXCLUDED.products_found,
    first_searched_at = EXCLUDED.first_searched_at,
    last_searched_at = EXCLUDED.last_searched_at
"""

REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = REPOSITORY_ROOT / ".env"
DATABASE_VARIABLES = {
    "DATABASE_HOST",
    "DATABASE_PORT",
    "DATABASE_NAME",
    "DATABASE_USER",
    "DATABASE_PASSWORD",
}


def read_database_variables_from_env_file() -> dict[str, str]:
    if not ENV_FILE.exists():
        return {}

    database_variables: dict[str, str] = {}
    with ENV_FILE.open(encoding="utf-8") as env_file:
        for line in env_file:
            entry = line.strip()
            if not entry or entry.startswith("#"):
                continue

            if entry.startswith("export "):
                entry = entry[7:].lstrip()

            name, separator, value = entry.partition("=")
            name = name.strip()
            if not separator or name not in DATABASE_VARIABLES:
                continue

            value = value.strip()
            if len(value) >= 2 and value[0] == value[-1] and value[0] in ("\"", "'"):
                value = value[1:-1]
            elif "#" in value:
                value = value.partition("#")[0].rstrip()

            database_variables[name] = value

    return database_variables


def get_connection_parameters() -> dict[str, str | int]:
    env_file_variables = read_database_variables_from_env_file()

    def get_value(name: str) -> str | None:
        return os.getenv(name) or env_file_variables.get(name)

    required_variables = (
        "DATABASE_HOST",
        "DATABASE_PORT",
        "DATABASE_NAME",
        "DATABASE_USER",
        "DATABASE_PASSWORD",
    )
    missing_variables = [name for name in required_variables if not get_value(name)]
    if missing_variables:
        raise ValueError(
            "Set these process environment variables: "
            + ", ".join(missing_variables)
        )

    connection_parameters: dict[str, str | int] = {
        "host": get_value("DATABASE_HOST") or "",
        "port": int(get_value("DATABASE_PORT") or "5432"),
        "dbname": get_value("DATABASE_NAME") or "",
        "user": get_value("DATABASE_USER") or "",
        "password": get_value("DATABASE_PASSWORD") or "",
        "connect_timeout": 10,
    }

    return connection_parameters


def main() -> int:
    try:
        connection_parameters = get_connection_parameters()
    except (ValueError, KeyError) as error:
        print(f"Configuration error: {error}", file=sys.stderr)
        return 2

    try:
        with psycopg.connect(**connection_parameters) as connection:
            with connection.transaction():
                connection.execute(RESET_AGGREGATES_SQL)
                result = connection.execute(AGGREGATE_SEARCHES_SQL)
                processed_terms = result.rowcount
    except psycopg.errors.UndefinedColumn:
        print(
            "The search_queries schema is missing ",
            file=sys.stderr,
        )
        return 1
    except psycopg.Error as error:
        print(
            f"Database processing failed ({error.sqlstate or type(error).__name__}).",
            file=sys.stderr,
        )
        return 1

    print(f"Search analytics rebuilt for {processed_terms} terms from events.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
