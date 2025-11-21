from fastapi import FastAPI, Depends, HTTPException, status, Header, Request
from sqlalchemy.orm import Session
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
import logging
from .db import get_db
from . import models
from .schemas import (
    UserRegisterRequest,
    UserRegisterResponse,
    LoginRequest,
    LoginResponse,
    UserInfo,
)
from .security import hash_password, verify_password
from datetime import datetime, timedelta, timezone
import hashlib
import secrets

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

    logger.info(f"Registration completed successfully for user: {new_user.email}")
    return UserRegisterResponse(
        id=new_user.id,
        name=new_user.name,
        email=new_user.email,
        message="User registered successfully.",
    )

@app.post("/login", response_model=LoginResponse)
def login(
  payload: LoginRequest,
  request: Request,
  db: Session = Depends(get_db),
  client_type: str | None = Header(
    default=None,
    alias="X-Client-Type",
    description="Client type, e.g. 'web' or 'mobile'",
  ),
):
  """
  Authenticate user and create a new session.
  
  Returns a session token and user information on successful login.
  """
  
  # Extract client information
  client_ip = request.client.host if request.client else None
  client_type = client_type.lower() if client_type else "unknown"
  
  logger.info(f"Login attempt for email: {payload.email}")
  logger.debug(f"Client info - Type: {client_type}, IP: {client_ip}")
  
  # 1. Find user by email
  try:
      logger.debug("Querying database for user...")
      user = (
          db.query(models.AppUser)
          .filter(models.AppUser.email == payload.email)
          .first()
      )
  except SQLAlchemyError as e:
      logger.error(f"Database error during user lookup: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while processing your request.",
      )
  
  # 2. Validate user exists
  if not user:
      logger.warning(f"Login failed: User not found for email {payload.email}")
      # Use same message as password failure to prevent user enumeration
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid email or password.",
      )
  
  logger.debug(f"User found: ID={user.id}, Name={user.name}")
  
  # 3. Check if user account is active
  if not user.status:
      logger.warning(f"Login failed: Account deactivated for user {user.id}")
      raise HTTPException(
          status_code=status.HTTP_403_FORBIDDEN,
          detail="User account is deactivated. Please contact an administrator.",
      )
  
  # 4. Verify password
  try:
      logger.debug("Verifying password...")
      password_valid = verify_password(payload.password, user.password_hash)
  except Exception as e:
      logger.error(f"Error during password verification: {type(e).__name__}: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while processing your request.",
      )
  
  if not password_valid:
      logger.warning(f"Login failed: Invalid password for user {user.id}")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid email or password.",
      )
  
  logger.info(f"Password verified successfully for user {user.id}")
  
  # 5. Generate session token
  try:
      logger.debug("Generating session token...")
      raw_token = secrets.token_urlsafe(32)
      token_hash = hashlib.sha256(raw_token.encode("utf-8")).hexdigest()
      expires_at = datetime.now(timezone.utc) + timedelta(hours=8)
      logger.debug(f"Token generated, expires at: {expires_at}")
  except Exception as e:
      logger.error(f"Error generating session token: {type(e).__name__}: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while creating your session.",
      )
  
  # 6. Create session record
  logger.debug("Creating session record...")
  session = models.UserSession(
      user_id=user.id,
      token_hash=token_hash,
      expires_at=expires_at,
      user_agent=client_type,
      ip_address=client_ip,
  )
  
  # 7. Update last login timestamp
  user.last_login_at = datetime.now(timezone.utc)
  logger.debug(f"Updated last_login_at for user {user.id}")
  
  # 8. Persist to database
  try:
      logger.debug("Saving session to database...")
      db.add(session)
      db.commit()
      db.refresh(session)
      db.refresh(user)
      logger.info(f"Session created successfully for user {user.id}, session ID: {session.id}")
  except SQLAlchemyError as e:
      logger.error(f"Database error during session creation: {str(e)}")
      db.rollback()
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An unexpected error occurred while creating the session.",
      )
  except Exception as e:
      logger.error(f"Unexpected error during session creation: {type(e).__name__}: {str(e)}")
      db.rollback()
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An unexpected error occurred while creating the session.",
      )
  
  # 9. Build response
  logger.debug("Building response...")
  user_info = UserInfo(
      name=user.name,
      email=user.email,
      role=user.role,
  )
  
  logger.info(f"Login successful for user {user.id} ({user.email})")
  
  return LoginResponse(
      session_token=raw_token,  # Send raw token to client
      user=user_info,
  )