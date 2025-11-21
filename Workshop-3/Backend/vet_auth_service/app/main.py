from fastapi import FastAPI

app = FastAPI(title="VetTrack Auth Service")


@app.get("/health")
def health_check():
    return {"status": "ok"}
