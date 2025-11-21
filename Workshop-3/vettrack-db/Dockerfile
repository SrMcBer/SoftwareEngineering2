# vettrack-db/Dockerfile
FROM postgres:16-alpine

# Only set non-sensitive defaults
ENV POSTGRES_DB=vettrack_db \
    POSTGRES_USER=vettrack_user \
    VETTRACK_ENV=dev

# Copy SQL and init scripts
COPY sql/ /docker-entrypoint-initdb.d/sql/
COPY init/00-init-vettrack.sh /docker-entrypoint-initdb.d/00-init-vettrack.sh

# Make sure script is executable
RUN chmod +x /docker-entrypoint-initdb.d/00-init-vettrack.sh