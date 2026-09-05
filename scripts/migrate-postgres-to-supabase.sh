#!/usr/bin/env bash
set -Eeuo pipefail

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Falta el comando requerido: %s\n' "$1" >&2
    exit 1
  fi
}

require_variable() {
  if [[ -z "${!1:-}" ]]; then
    printf 'Falta la variable requerida: %s\n' "$1" >&2
    exit 1
  fi
}

require_command pg_dump
require_command pg_restore
require_command psql
require_variable SOURCE_DB_URL
require_variable SUPABASE_MIGRATION_DB_URL

if [[ "$SOURCE_DB_URL" == "$SUPABASE_MIGRATION_DB_URL" ]]; then
  printf 'Las bases de datos de origen y destino no pueden ser la misma.\n' >&2
  exit 1
fi

if [[ "$SUPABASE_MIGRATION_DB_URL" == *":6543/"* ]]; then
  printf 'Usa la conexión directa o el Session pooler de Supabase (puerto 5432), no el Transaction pooler (6543).\n' >&2
  exit 1
fi

migration_dir="${MIGRATION_DIR:-.migration}"
mkdir -p "$migration_dir"
migration_dir="$(cd "$migration_dir" && pwd)"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
dump_file="$migration_dir/estrabismo-$timestamp.dump"
source_counts="$migration_dir/source-counts-$timestamp.csv"
target_counts="$migration_dir/target-counts-$timestamp.csv"

target_table_count="$(
  psql -X "$SUPABASE_MIGRATION_DB_URL" -v ON_ERROR_STOP=1 -Atc \
    "select count(*) from pg_tables where schemaname = 'public';"
)"

if [[ "$target_table_count" != "0" && "${ALLOW_NONEMPTY_TARGET:-false}" != "true" ]]; then
  printf 'El esquema public de Supabase ya contiene %s tabla(s).\n' "$target_table_count" >&2
  printf 'Usa un proyecto vacío o define ALLOW_NONEMPTY_TARGET=true después de revisar el destino.\n' >&2
  exit 1
fi

printf 'Exportando el esquema public y sus datos...\n'
pg_dump \
  --dbname="$SOURCE_DB_URL" \
  --format=custom \
  --schema=public \
  --no-owner \
  --no-privileges \
  --no-subscriptions \
  --verbose \
  --file="$dump_file"

printf 'Restaurando el respaldo en Supabase...\n'
pg_restore \
  --dbname="$SUPABASE_MIGRATION_DB_URL" \
  --no-owner \
  --no-privileges \
  --exit-on-error \
  --verbose \
  "$dump_file"

collect_counts() {
  local database_url="$1"
  local output_file="$2"
  local table_name
  local escaped_table_name
  local row_count

  printf 'table_name,row_count\n' >"$output_file"

  while IFS= read -r table_name; do
    escaped_table_name="${table_name//\"/\"\"}"
    row_count="$(
      psql -X "$database_url" -v ON_ERROR_STOP=1 -Atc \
        "select count(*) from public.\"$escaped_table_name\";"
    )"
    printf '"%s",%s\n' "${table_name//\"/\"\"}" "$row_count" >>"$output_file"
  done < <(
    psql -X "$database_url" -v ON_ERROR_STOP=1 -Atc \
      "select tablename from pg_tables where schemaname = 'public' order by tablename;"
  )
}

printf 'Comparando cantidades de registros por tabla...\n'
collect_counts "$SOURCE_DB_URL" "$source_counts"
collect_counts "$SUPABASE_MIGRATION_DB_URL" "$target_counts"

if ! diff -u "$source_counts" "$target_counts"; then
  printf 'La restauración terminó, pero los conteos no coinciden. No cambies todavía la conexión de producción.\n' >&2
  exit 1
fi

printf 'Migración verificada correctamente.\n'
printf 'Respaldo: %s\n' "$dump_file"
printf 'Conteos: %s y %s\n' "$source_counts" "$target_counts"
