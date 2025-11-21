# VetTrack Database

This directory contains the Dockerfile and SQL scripts for the VetTrack PostgreSQL database.

## Build the Docker Image

To build the Docker image, navigate to the `vettrack-db/` directory and run the following command:

```bash
docker build -t vettrack-db:latest .
```

## Run the Docker Image

To run the built Docker image, use the following command:

```bash
docker run -d \
  --name vettrack-db-dev \
  -e POSTGRES_PASSWORD=vettrack_password \
  -e VETTRACK_ENV=dev \
  -p 5432:5432 \
  vettrack-db:latest
```

This command will:

- Run the container in detached mode (`-d`).
- Name the container `vettrack-db-dev`.
- Set the PostgreSQL password to `vettrack_password`.
- Set the `VETTRACK_ENV` to `dev`, which will load development seed data.
- Map port 5432 of the host to port 5432 of the container.
- Use the `vettrack-db:latest` image.

## Environments

The `VETTRACK_ENV` environment variable controls which seed data is loaded into the database.

- `dev`: Loads the common seed data (`10-common-seed.sql`) and the development seed data (`20-dev-seed.sql`). This is the default.
- `prod`: Loads only the common seed data (`10-common-seed.sql`).
