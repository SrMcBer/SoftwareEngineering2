from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text

from .db import get_db
from . import models  # ensures models are imported so SQLAlchemy metadata is set

app = FastAPI(title="VetTrack Auth Service")


@app.get("/health")
def health_check(db: Session = Depends(get_db)):
    # Simple DB check: run a lightweight query
    db.execute(text("SELECT 1"))  # 👈 wrap in text()
    return {"status": "ok"}
