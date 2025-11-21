# vettrack-db/init/00-init-vettrack.sh
#!/usr/bin/env bash
set -e

echo "==> VetTrack init script starting (VETTRACK_ENV=${VETTRACK_ENV:-dev})"

# 1. Load schema: your full VetTrack.sql
echo "==> Applying VetTrack schema (00-schema.sql)..."
psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -f /docker-entrypoint-initdb.d/sql/00-schema.sql

  # 2. Seed data common to dev and prod (admin + exam templates, etc.)
echo '==> Seeding common data (10-common-seed.sql)...'
psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -f /docker-entrypoint-initdb.d/sql/10-common-seed.sql

  # 3. Env-specific seed
case "${VETTRACK_ENV:-dev}" in
  dev)
    echo '==> Seeding DEV data (20-dev-seed.sql)...'
    psql -v ON_ERROR_STOP=1 \
      --username "$POSTGRES_USER" \
      --dbname "$POSTGRES_DB" \
      -f /docker-entrypoint-initdb.d/sql/20-dev-seed.sql
    ;;
  prod)
    echo '==> Seeding PROD data (20-prod-seed.sql)...'
    # Optional: you can leave this file empty, but Postgres will still happily run it
    psql -v ON_ERROR_STOP=1 \
      --username "$POSTGRES_USER" \
      --dbname "$POSTGRES_DB" \
      -f /docker-entrypoint-initdb.d/sql/20-prod-seed.sql
    ;;
  *)
    echo "!! Unknown VETTRACK_ENV '${VETTRACK_ENV}', skipping env-specific seeding."
    ;;
esac

echo "==> VetTrack init script finished."