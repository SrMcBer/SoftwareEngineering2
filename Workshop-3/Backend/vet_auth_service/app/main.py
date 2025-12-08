# ============================================================================
# main.py - Application Entry Point
# ============================================================================
from fastapi import FastAPI, logger
from contextlib import asynccontextmanager
import logging
from .routes import router
from .config import Config
from fastapi.middleware.cors import CORSMiddleware

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

app_logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Lifespan context manager for startup and shutdown events.
    Replaces the deprecated @app.on_event decorators.
    """
    # Startup
    config = Config()
    app_logger.info("Application starting...")
    app_logger.info(f"Database URL configured: {bool(config.DATABASE_URL)}")
    app_logger.info(f"Session expiry: {config.SESSION_EXPIRY_HOURS} hours")
    
    yield
    
    # Shutdown (if you need cleanup logic)
    app_logger.info("Application shutting down...")

# Create FastAPI app with lifespan
app = FastAPI(
    title="VetTrack Auth Service",
    description="Authentication service with clean architecture",
    version="2.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routes
app.include_router(router, tags=["auth"])