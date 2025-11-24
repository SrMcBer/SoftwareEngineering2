# ============================================================================
# services.py - Business Logic Layer (Service Pattern)
# ============================================================================
import secrets
import hashlib
from datetime import datetime, timedelta, timezone
from typing import Tuple, Optional
import logging
from .exceptions import InvalidCredentialsError, UserAlreadyExistsError, PasswordHashingError, AccountDeactivatedError, InvalidSessionError
from .config import Config
from .repositories import UserRepository, SessionRepository
from .models import AppUser

logger = logging.getLogger(__name__)

class TokenService:
    """Service for token generation and validation"""
    
    @staticmethod
    def generate_session_token() -> Tuple[str, str]:
        """
        Generate a session token and its hash.
        Returns: (raw_token, token_hash)
        """
        config = Config()
        raw_token = secrets.token_urlsafe(config.TOKEN_LENGTH)
        token_hash = hashlib.sha256(raw_token.encode("utf-8")).hexdigest()
        return raw_token, token_hash
    
    @staticmethod
    def hash_token(raw_token: str) -> str:
        """Hash a raw token"""
        return hashlib.sha256(raw_token.encode("utf-8")).hexdigest()
    
    @staticmethod
    def calculate_expiry() -> datetime:
        """Calculate session expiry time"""
        config = Config()
        return datetime.now(timezone.utc) + timedelta(hours=config.SESSION_EXPIRY_HOURS)


class AuthService:
    """Service for authentication operations"""
    
    def __init__(self, user_repo: UserRepository, session_repo: SessionRepository):
        self.user_repo = user_repo
        self.session_repo = session_repo
        self.token_service = TokenService()
    
    def register_user(self, name: str, email: str, password: str) -> 'AppUser':
        """Register a new user"""
        logger.info(f"Registration attempt for email: {email}")
        
        # Check if user exists
        existing = self.user_repo.find_by_email(email)
        if existing:
            logger.warning(f"Registration failed: Email {email} already exists")
            raise UserAlreadyExistsError("A user with this email already exists")
        
        # Hash password
        try:
            from .security import hash_password
            password_hash = hash_password(password)
        except Exception as e:
            logger.error(f"Password hashing failed: {e}")
            raise PasswordHashingError("Failed to process password")
        
        # Create user
        user = self.user_repo.create(name, email, password_hash)
        logger.info(f"User successfully created with ID: {user.id}")
        return user
    
    def authenticate(
        self,
        email: str,
        password: str,
        client_type: Optional[str] = None,
        client_ip: Optional[str] = None,
    ) -> Tuple['AppUser', str]:
        """
        Authenticate user and create session.
        Returns: (user, session_token)
        """
        logger.info(f"Login attempt for email: {email}")
        
        # Find user
        user = self.user_repo.find_by_email(email)
        if not user:
            logger.warning(f"Login failed: User not found for email {email}")
            raise InvalidCredentialsError("Invalid email or password")
        
        # Check if user is active
        if not user.status:
            logger.warning(f"Login failed: Account deactivated for user {user.id}")
            raise AccountDeactivatedError("User account is deactivated")
        
        # Verify password
        try:
            from .security import verify_password
            if not verify_password(password, user.password_hash):
                logger.warning(f"Login failed: Invalid password for user {user.id}")
                raise InvalidCredentialsError("Invalid email or password")
        except InvalidCredentialsError:
            raise
        except Exception as e:
            logger.error(f"Error during password verification: {e}")
            raise
        
        logger.info(f"Password verified successfully for user {user.id}")
        
        # Generate session token
        raw_token, token_hash = self.token_service.generate_session_token()
        expires_at = self.token_service.calculate_expiry()
        
        # Sanitize IP address for INET type
        sanitized_ip = None
        if client_ip:
            # Check if it's a valid IP address
            import ipaddress
            try:
                ipaddress.ip_address(client_ip)
                sanitized_ip = client_ip
            except ValueError:
                # Not a valid IP, set to None
                logger.debug(f"Invalid IP address format: {client_ip}, storing as NULL")
                sanitized_ip = None
        
        # Create session
        self.session_repo.create(
            user_id=user.id,
            token_hash=token_hash,
            expires_at=expires_at,
            user_agent=client_type,
            ip_address=sanitized_ip,
        )
        
        # Update last login
        self.user_repo.update_last_login(user)
        
        logger.info(f"Login successful for user {user.id} ({user.email})")
        return user, raw_token
    
    def validate_session(self, raw_token: str) -> 'AppUser':
        """Validate session token and return user"""
        logger.debug("Validating session token")
        
        token_hash = self.token_service.hash_token(raw_token)
        session = self.session_repo.find_valid_session(token_hash)
        
        if not session:
            logger.info("Authentication rejected: Invalid or expired session token")
            raise InvalidSessionError("Invalid or expired session token")
        
        if not session.user:
            logger.error(f"Data integrity issue: Session {session.id} has no user")
            raise InvalidSessionError("User associated with this session no longer exists")
        
        logger.info(f"Authentication successful: User {session.user.id}")
        return session.user
    
    def logout(self, raw_token: str) -> None:
        """Logout by revoking session"""
        logger.debug("Processing logout")
        
        token_hash = self.token_service.hash_token(raw_token)
        session = self.session_repo.find_valid_session(token_hash)
        
        if not session:
            raise InvalidSessionError("Invalid or expired session token")
        
        self.session_repo.revoke(session)
        logger.info(f"Session revoked for user {session.user_id}")
    
    def change_password(
        self,
        user: 'AppUser',
        current_password: str,
        new_password: str,
    ) -> None:
        """Change user password and revoke all sessions"""
        logger.info(f"Password change request for user {user.id}")
        
        # Verify current password
        from .security import verify_password, hash_password
        if not verify_password(current_password, user.password_hash):
            logger.warning(f"Password change failed: Invalid current password")
            raise InvalidCredentialsError("Current password is incorrect")
        
        # Hash new password
        new_hash = hash_password(new_password)
        
        # Update password
        self.user_repo.update_password(user, new_hash)
        
        # Revoke all sessions
        count = self.session_repo.revoke_all_user_sessions(user.id)
        
        logger.info(f"Password changed for user {user.id}, revoked {count} sessions")

