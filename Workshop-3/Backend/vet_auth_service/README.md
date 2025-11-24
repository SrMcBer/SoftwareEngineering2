# VetTrack Auth Service

This is the authentication service for the VetTrack application. This service is responsible for user registration, login, and session management.

## Installation

1.  Create and activate a Python virtual environment:

    ```bash
    python3 -m venv .venv
    source .venv/bin/activate
    ```
    
    (On Windows, use `.venv\Scripts\activate`)

2.  Install the required dependencies:

    For production:
    ```bash
    pip install -r requirements.txt
    ```

    For development and testing, install the development dependencies:
    ```bash
    pip install -r requirements-dev.txt
    ```

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

Once the test database is set up and development dependencies are installed, you can run the tests using `pytest`:

```bash
pytest
```

This will run all tests, display the results, and generate a code coverage report in the `htmlcov` directory.

## Running the Application

To run the application, ensure you have your Python virtual environment activated.

1.  Run the application with `uvicorn`:

    ```bash
    uvicorn app.main:app --reload
    ```

This will start the development server with auto-reload enabled. You can access the API documentation at `http://127.0.0.1:8000/docs`.