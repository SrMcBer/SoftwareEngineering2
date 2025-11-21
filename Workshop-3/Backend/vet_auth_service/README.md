# VetTrack Auth Service

This is the authentication service for the VetTrack application.

## Installation

1.  Create a virtual environment:

    ```bash
    python3 -m venv .venv
    ```

2.  Activate your virtual environment:

    On macOS and Linux:

    ```bash
    source .venv/bin/activate
    ```

    On Windows:

    ```bash
    .venv\Scripts\activate
    ```

3.  Install the dependencies:

    ```bash
    pip install -r requirements.txt
    ```

## Running the Application

To run the application, ensure you have your Python virtual environment activated.

1.  Run the application:

    ```bash
    uvicorn app.main:app --reload
    ```

This will start the development server with auto-reload enabled. You can access the application at `http://127.0.0.1:8000`.