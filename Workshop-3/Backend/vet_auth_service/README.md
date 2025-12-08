# VetTrack Auth Service

This is the authentication service for the VetTrack application. This service is responsible for user registration, login, and session management.

## Installation

This project uses [Poetry](https://python-poetry.org/) for dependency management.

1.  **Install Poetry:**

    Follow the instructions on the [official Poetry website](https://python-poetry.org/docs/#installation) to install it on your system.

2.  **Install Dependencies:**

    Navigate to the project root directory and run:
    ```bash
    poetry install
    ```
    This will create a virtual environment and install all the necessary dependencies.

## Testing

The application includes a comprehensive test suite using `pytest`.

### Test Database Setup

The tests require a separate PostgreSQL database. A setup script is provided to create the test database and user.

**Prerequisites:**
- Make sure you have PostgreSQL installed and running.
- Ensure you can connect to PostgreSQL with a user that has permission to create databases and users (e.g., the default `postgres` user).

**Steps:**

1.  Navigate to the `test` directory:
    ```bash
    cd test
    ```

2.  Run the setup script:
    ```bash
    bash set_up_test_db.sh
    ```
    This will create a new database `test_db` with a user `test_user`.

### Running Tests

To run the tests, use `poetry run`:

```bash
poetry run pytest
```

This will execute the tests within the Poetry-managed virtual environment and generate a code coverage report in the `htmlcov` directory.

## Running the Application

To run the application, use `poetry run`:

```bash
poetry run uvicorn app.main:app --reload
```

This will start the development server with auto-reload enabled inside the Poetry-managed virtual environment. You can access the API documentation at `http://127.0.0.1:8000/docs`.