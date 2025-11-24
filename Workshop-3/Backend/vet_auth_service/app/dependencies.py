# ============================================================================
# dependencies.py - FastAPI Dependencies
# ============================================================================
from fastapi import Depends, Header, HTTPException, status, Request
from sqlalchemy.orm import Session
from typing import TYPE_CHECKING, Optional
import logging
from .database import get_db
from .services import AuthService
from .repositories import UserRepository, SessionRepository
from .exceptions import InvalidSessionError

if TYPE_CHECKING:
    from .models import AppUser


logger = logging.getLogger(__name__)

def get_auth_service(db: Session = Depends(get_db)) -> AuthService:
    """Dependency to get AuthService instance"""
    user_repo = UserRepository(db)
    session_repo = SessionRepository(db)
    return AuthService(user_repo, session_repo)

def extract_bearer_token(
    authorization: Optional[str] = Header(default=None, alias="Authorization")
) -> str:
    """Extract and validate bearer token from Authorization header"""
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing Authorization header",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    parts = authorization.split()
    
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid Authorization header format. Expected 'Bearer <token>'",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    raw_token = parts[1].strip()
    
    if not raw_token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Empty bearer token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    return raw_token

def get_current_user(
    raw_token: str = Depends(extract_bearer_token),
    auth_service: AuthService = Depends(get_auth_service),
):
    """Dependency to get current authenticated user"""
    try:
        return auth_service.validate_session(raw_token)
    except InvalidSessionError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(e),
            headers={"WWW-Authenticate": "Bearer"},
        )
    except Exception as e:
        logger.error(f"Unexpected error during authentication: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred while processing your request",
        )

def get_client_info(request: Request) -> dict:
    """Extract client information from request"""
    return {
        "ip": request.client.host if request.client else None,
        "user_agent": request.headers.get("user-agent", "unknown"),
    }