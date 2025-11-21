from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError
import logging
from .db import get_db
from . import models
from .schemas import UserRegisterRequest, UserRegisterResponse
from .security import hash_password

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

app = FastAPI(title="VetTrack Auth Service")
logger = logging.getLogger(__name__)

@app.get("/health")
def health_check(db: Session = Depends(get_db)):
    db.execute(text("SELECT 1"))
    return {"status": "ok"}

@app.post("/register", response_model=UserRegisterResponse, status_code=status.HTTP_200_OK)
def register_user(payload: UserRegisterRequest, db: Session = Depends(get_db)):
    logger.info(f"Registration attempt for email: {payload.email}")
    logger.debug(f"Payload received - name: {payload.name}, email: {payload.email}")
    logger.debug(f"Password length (chars): {len(payload.password)}")
    logger.debug(f"Password length (bytes): {len(payload.password.encode('utf-8'))}")

    existing = (
        db.query(models.AppUser)
        .filter(models.AppUser.email == payload.email)
        .first()
    )
    if existing:
        logger.warning(f"Registration failed: Email {payload.email} already exists")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A user with this email already exists.",
        )
    logger.info("Email is available")


    # 2. Hash the password
    try:
        logger.info("Attempting to hash password...")
        logger.debug(f"Password to hash: {payload.password[:3]}... (truncated for security)")
        password_hashed = hash_password(payload.password)
        logger.info("Password hashed successfully")
        logger.debug(f"Hash length: {len(password_hashed)}")
    except Exception as e:
        logger.error(f"Password hashing failed: {type(e).__name__}: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to process password.",
        )
    

    # 3. Create the user instance
    logger.info("Creating user instance...")
    try:
        new_user = models.AppUser(
            name=payload.name,
            email=payload.email,
            password_hash=password_hashed,
        )
        logger.info("User instance created")
    except Exception as e:
        logger.error(f"Failed to create user instance: {type(e).__name__}: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create user object.",
        )

    # 4. Persist to DB
    try:
        logger.info("Adding user to database...")
        db.add(new_user)
        logger.info("Committing transaction...")
        db.commit()
        logger.info("Refreshing user object...")
        db.refresh(new_user)
        logger.info(f"User successfully created with ID: {new_user.id}")
    except IntegrityError as e:
        logger.error(f"IntegrityError during user creation: {str(e)}")
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Could not create user, email may already be registered.",
        )
    except Exception as e:
        logger.error(f"Unexpected error during DB operation: {type(e).__name__}: {str(e)}")
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An unexpected error occurred while registering the user.",
        )

    # 5. Return success response
    logger.info(f"Registration completed successfully for user: {new_user.email}")
    return UserRegisterResponse(
        id=new_user.id,
        name=new_user.name,
        email=new_user.email,
        message="User registered successfully.",
    )