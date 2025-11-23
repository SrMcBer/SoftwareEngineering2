from fastapi import FastAPI, Depends, HTTPException, status, Header, Request
from sqlalchemy.orm import Session
from pydantic import EmailStr
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
    MessageResponse,
    ChangePasswordRequest,
    SessionInfo,
    SessionsListResponse,
)
from .security import hash_password, verify_password
from datetime import datetime, timedelta, timezone
from uuid import UUID
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

@app.post("/logout", response_model=MessageResponse)
def logout(
    authorization: str | None = Header(default=None, alias="Authorization"),
    db: Session = Depends(get_db),
):
  """
  Logout the current session: revoke the session associated with the bearer token.
  """
  if not authorization:
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Missing Authorization header.",
      )
  
  parts = authorization.split()
  if len(parts) != 2 or parts[0].lower() != "bearer":
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid Authorization header format. Expected 'Bearer <token>'.",
      )

  raw_token = parts[1].strip()
  if not raw_token:
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Empty bearer token.",
      )
  
  token_hash = hashlib.sha256(raw_token.encode("utf-8")).hexdigest()
  now = datetime.now(timezone.utc)

  session = (
      db.query(models.UserSession)
      .filter(
          models.UserSession.token_hash == token_hash,
          models.UserSession.expires_at > now,
          models.UserSession.revoked_at.is_(None),
      )
      .first()
  )
  
  
  if not session:
      # token invalid / already revoked / expired
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid or expired session token.",
      )
  
  
  session.revoked_at = now
  try:
      db.commit()
  except Exception:
      db.rollback()
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An unexpected error occurred while logging out.",
      )

  return MessageResponse(message="Logged out successfully.")

def get_current_user(
    authorization: str | None = Header(default=None, alias="Authorization"),
    db: Session = Depends(get_db),
) -> models.AppUser:
  """
  Dependency that extracts the Bearer token from the Authorization header,
  validates the session, and returns the associated AppUser.
  
  Raises:
      HTTPException: If authentication fails for any reason
  
  Returns:
      AppUser: The authenticated user
  """
  logger.debug("Authentication attempt - validating bearer token")
  
  # 1. Check Authorization header exists
  if not authorization:
      logger.warning("Authentication failed: Missing Authorization header")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Missing Authorization header.",
          headers={"WWW-Authenticate": "Bearer"},
      )
  
  # 2. Parse Authorization header
  logger.debug("Parsing Authorization header...")
  parts = authorization.split()
  
  if len(parts) != 2:
      logger.warning(f"Authentication failed: Invalid header format (expected 2 parts, got {len(parts)})")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid Authorization header format. Expected 'Bearer <token>'.",
          headers={"WWW-Authenticate": "Bearer"},
      )
  
  if parts[0].lower() != "bearer":
      logger.warning(f"Authentication failed: Invalid auth scheme '{parts[0]}' (expected 'Bearer')")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid Authorization header format. Expected 'Bearer <token>'.",
          headers={"WWW-Authenticate": "Bearer"},
      )
  
  raw_token = parts[1].strip()
  
  if not raw_token:
      logger.warning("Authentication failed: Empty bearer token")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Empty bearer token.",
          headers={"WWW-Authenticate": "Bearer"},
      )
  
  logger.debug(f"Token received (length: {len(raw_token)})")
  
  # 3. Hash the token
  try:
      logger.debug("Hashing token for comparison...")
      token_hash = hashlib.sha256(raw_token.encode("utf-8")).hexdigest()
      logger.debug(f"Token hash generated: {token_hash[:16]}...")
  except Exception as e:
      logger.error(f"Error hashing token: {type(e).__name__}: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while processing your request.",
      )
  
  # 4. Query for valid session
  now = datetime.now(timezone.utc)
  logger.debug(f"Querying for valid session (current time: {now})")
  
  try:
      session = (
          db.query(models.UserSession)
          .join(models.AppUser)
          .filter(
              models.UserSession.token_hash == token_hash,
              models.UserSession.expires_at > now,
              models.UserSession.revoked_at.is_(None),
              models.AppUser.status.is_(True),
          )
          .first()
      )
      
      if session:
          logger.debug(f"Session found: ID={session.id}, User ID={session.user_id}")
      else:
          logger.warning("Authentication failed: No valid session found for token")
          
  except SQLAlchemyError as e:
      logger.error(f"Database error during session lookup: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while processing your request.",
      )
  except Exception as e:
      logger.error(f"Unexpected error during session lookup: {type(e).__name__}: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while processing your request.",
      )
  
  # 5. Validate session exists
  if not session:
      # Could be: invalid token, expired, revoked, or user disabled
      logger.info("Authentication rejected: Invalid or expired session token")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Invalid or expired session token.",
          headers={"WWW-Authenticate": "Bearer"},
      )
  
  # 6. Validate user exists (safety check)
  user = session.user
  
  if not user:
      # Extremely unlikely if DB is consistent, but we guard anyway
      logger.error(f"Data integrity issue: Session {session.id} has no associated user")
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="User associated with this session no longer exists.",
          headers={"WWW-Authenticate": "Bearer"},
      )
  
  logger.info(f"Authentication successful: User {user.id} ({user.email})")
  logger.debug(f"User details - Name: {user.name}, Role: {user.role}, Status: {user.status}")
  
  return user

@app.get("/me", response_model=UserInfo, status_code=status.HTTP_200_OK)
def read_current_user(current_user: models.AppUser = Depends(get_current_user)):
  """
  Get the currently authenticated user's information.
  
  Returns basic profile information for the authenticated user.
  """
  logger.info(f"Fetching profile for user {current_user.id}")
  logger.debug(f"Returning user info - Email: {current_user.email}, Role: {current_user.role}")
  
  try:
      user_info = UserInfo(
          name=current_user.name,
          email=current_user.email,
          role=current_user.role,
      )
      logger.debug("User info constructed successfully")
      return user_info
      
  except Exception as e:
      logger.error(f"Error constructing user info: {type(e).__name__}: {str(e)}")
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An error occurred while retrieving user information.",
      )

@app.post("/password/change", response_model=MessageResponse)
def change_password(
    payload: ChangePasswordRequest,
    current_user: models.AppUser = Depends(get_current_user),
    db: Session = Depends(get_db),
):
  """
  Change the password for the currently authenticated user.
  Requires current_password and new_password.
  After changing, all active sessions are revoked.
  """
  # 1. Verify current password
  if not verify_password(payload.current_password, current_user.password_hash):
      raise HTTPException(
          status_code=status.HTTP_401_UNAUTHORIZED,
          detail="Current password is incorrect.",
      )

  # 2. Hash new password
  new_hash = hash_password(payload.new_password)
  current_user.password_hash = new_hash

  # 3. Revoke all active sessions for this user (including current)
  now = datetime.now(timezone.utc)
  (
      db.query(models.UserSession)
      .filter(
          models.UserSession.user_id == current_user.id,
          models.UserSession.revoked_at.is_(None),
          models.UserSession.expires_at > now,
      )
      .update({"revoked_at": now}, synchronize_session=False)
  )

  try:
      db.commit()
  except Exception:
      db.rollback()
      raise HTTPException(
          status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
          detail="An unexpected error occurred while changing the password.",
      )

  return MessageResponse(
      message="Password updated successfully. Please log in again with your new password."
  )

