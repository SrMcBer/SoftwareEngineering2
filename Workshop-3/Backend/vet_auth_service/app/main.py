# ============================================================================
# main.py - Application Entry Point
# ============================================================================
from fastapi import FastAPI
import logging
from .routes import router
from .config import Config

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# Create FastAPI app
app = FastAPI(
    title="VetTrack Auth Service",
    description="Authentication service with clean architecture",
    version="2.0.0",
)

# Include routes
app.include_router(router, tags=["auth"])

# Optional: Add startup event to validate configuration
@app.on_event("startup")
def startup_event():
    """Validate configuration on startup"""
    config = Config()
    logger = logging.getLogger(__name__)
    logger.info("Application starting...")
    logger.info(f"Database URL configured: {bool(config.DATABASE_URL)}")
    logger.info(f"Session expiry: {config.SESSION_EXPIRY_HOURS} hours")