# ============================================================================
# routes.py - API Routes
# ============================================================================
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status, Header, Request
from sqlalchemy import text
from sqlalchemy.orm import Session
import logging
from .exceptions import InvalidCredentialsError
from .database import get_db
from .dependencies import get_auth_service, extract_bearer_token, get_current_user
from .services import AuthService
from .schemas import (
    UserRegisterRequest,
    UserRegisterResponse,
    LoginRequest,
    LoginResponse,
    UserInfo,
    MessageResponse,
    ChangePasswordRequest,
)
from .exceptions import (
    InvalidCredentialsError,
    UserAlreadyExistsError,
    PasswordHashingError,
    AccountDeactivatedError,
    InvalidSessionError,
)
from .models import AppUser

logger = logging.getLogger(__name__)

router = APIRouter()

@router.get("/health")
def health_check(db: Session = Depends(get_db)):
    """Health check endpoint"""
    db.execute(text("SELECT 1"))
    return {"status": "ok"}

@router.post("/register", response_model=UserRegisterResponse, status_code=status.HTTP_200_OK)
def register(
    payload: UserRegisterRequest,
    auth_service: AuthService = Depends(get_auth_service),
):
    """Register a new user"""
    try:
        user = auth_service.register_user(
            name=payload.name,
            email=payload.email,
            password=payload.password,
        )
        
        return UserRegisterResponse(
            id=user.id,
            name=user.name,
            email=user.email,
            message="User registered successfully",
        )
    
    except UserAlreadyExistsError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        )
    except PasswordHashingError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )
    except Exception as e:
        logger.error(f"Unexpected error during registration: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An unexpected error occurred",
        )

@router.post("/login", response_model=LoginResponse)
def login(
    payload: LoginRequest,
    request: Request,
    auth_service: AuthService = Depends(get_auth_service),
    client_type: Optional[str] = Header(default=None, alias="X-Client-Type"),
):
    """Authenticate user and create session"""
    client_ip = request.client.host if request.client else None
    client_type = client_type.lower() if client_type else "unknown"
    
    try:
        user, session_token = auth_service.authenticate(
            email=payload.email,
            password=payload.password,
            client_type=client_type,
            client_ip=client_ip,
        )
        
        return LoginResponse(
            session_token=session_token,
            user=UserInfo(
                name=user.name,
                email=user.email,
                role=user.role,
            ),
        )
    
    except InvalidCredentialsError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(e),
        )
    except AccountDeactivatedError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=str(e),
        )
    except Exception as e:
        logger.error(f"Unexpected error during login: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An unexpected error occurred",
        )

@router.post("/logout", response_model=MessageResponse)
def logout(
    raw_token: str = Depends(extract_bearer_token),
    auth_service: AuthService = Depends(get_auth_service),
):
    """Logout and revoke session"""
    try:
        auth_service.logout(raw_token)
        
        return MessageResponse(message="Logged out successfully")
    
    except InvalidSessionError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(e),
        )
    except Exception as e:
        logger.error(f"Unexpected error during logout: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An unexpected error occurred",
        )

@router.get("/me", response_model=UserInfo)
def get_me(current_user = Depends(get_current_user)):
    """Get current user information"""
    return UserInfo(
        name=current_user.name,
        email=current_user.email,
        role=current_user.role,
    )

@router.post("/password/change", response_model=MessageResponse)
def change_password(
    payload: ChangePasswordRequest,
    current_user = Depends(get_current_user),
    auth_service: AuthService = Depends(get_auth_service),
):
    """Change user password"""
    try:
        auth_service.change_password(
            user=current_user,
            current_password=payload.current_password,
            new_password=payload.new_password,
        )
        
        return MessageResponse(
            message="Password updated successfully. Please log in again with your new password"
        )
    
    except InvalidCredentialsError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(e),
        )
    except Exception as e:
        logger.error(f"Unexpected error changing password: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An unexpected error occurred",
        )