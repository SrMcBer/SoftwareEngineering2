# Use an official Python runtime as a parent image
FROM python:3.13-slim

# Set the working directory in the container
WORKDIR /app

# Install poetry
RUN pip install poetry

# Copy the poetry dependency files to the working directory
COPY pyproject.toml poetry.lock ./

# Configure poetry to not create virtualenvs
RUN poetry config virtualenvs.create false

# Install dependencies
RUN poetry install --no-root

# Copy the application code to the working directory
COPY ./app /app/app

# Expose the port the app runs on
EXPOSE 8000

# Run the application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]