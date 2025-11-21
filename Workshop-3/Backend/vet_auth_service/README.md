# VetTrack Auth Service

This is the authentication service for the VetTrack application.

## Running the Application

To run the application, ensure you have your Python virtual environment activated and the dependencies installed.

1.  Activate your virtual environment:

    On macOS and Linux:

    ```bash
    source .venv/bin/activate
    ```

    On Windows:

    ```bash
    .venv\Scripts\activate
    ```

2.  Run the application:

    ```bash
    uvicorn app.main:app --reload
    ```

This will start the development server with auto-reload enabled. You can access the application at `http://127.0.0.1:8000`.