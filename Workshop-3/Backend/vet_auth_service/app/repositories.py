# ============================================================================
# repositories.py - Data Access Layer (Repository Pattern)
# ============================================================================
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from typing import Optional, List
from datetime import datetime, timezone
from uuid import UUID
import logging
from .exceptions import UserAlreadyExistsError

logger = logging.getLogger(__name__)

class UserRepository:
    """Repository for User data access"""
    
    def __init__(self, db: Session):
        self.db = db
    
    def find_by_email(self, email: str) -> Optional['AppUser']:
        """Find user by email"""
        try:
            from .models import AppUser
            return self.db.query(AppUser).filter(AppUser.email == email).first()
        except SQLAlchemyError as e:
            logger.error(f"Database error finding user by email: {e}")
            raise
    
    def find_by_id(self, user_id: UUID) -> Optional['AppUser']:
        """Find user by ID"""
        try:
            from .models import AppUser
            return self.db.query(AppUser).filter(AppUser.id == user_id).first()
        except SQLAlchemyError as e:
            logger.error(f"Database error finding user by ID: {e}")
            raise
    
    def create(self, name: str, email: str, password_hash: str) -> 'AppUser':
        """Create a new user"""
        try:
            from .models import AppUser
            user = AppUser(
                name=name,
                email=email,
                password_hash=password_hash,
            )
            self.db.add(user)
            self.db.commit()
            self.db.refresh(user)
            return user
        except IntegrityError:
            self.db.rollback()
            raise UserAlreadyExistsError("User with this email already exists")
        except SQLAlchemyError as e:
            self.db.rollback()
            logger.error(f"Database error creating user: {e}")
            raise
    
    def update_last_login(self, user: 'AppUser') -> None:
        """Update user's last login timestamp"""
        try:
            user.last_login_at = datetime.now(timezone.utc)
            self.db.commit()
        except SQLAlchemyError as e:
            self.db.rollback()
            logger.error(f"Database error updating last login: {e}")
            raise
    
    def update_password(self, user: 'AppUser', new_password_hash: str) -> None:
        """Update user's password"""
        try:
            user.password_hash = new_password_hash
            self.db.commit()
        except SQLAlchemyError as e:
            self.db.rollback()
            logger.error(f"Database error updating password: {e}")
            raise


class SessionRepository:
    """Repository for Session data access"""
    
    def __init__(self, db: Session):
        self.db = db
    
    def create(
        self,
        user_id: UUID,
        token_hash: str,
        expires_at: datetime,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None,
    ) -> 'UserSession':
        """Create a new session"""
        try:
            from .models import UserSession
            session = UserSession(
                user_id=user_id,
                token_hash=token_hash,
                expires_at=expires_at,
                user_agent=user_agent,
                ip_address=ip_address,
            )
            self.db.add(session)
            self.db.commit()
            self.db.refresh(session)
            return session
        except SQLAlchemyError as e:
            self.db.rollback()
            logger.error(f"Database error creating session: {e}")
            raise
    
    def find_valid_session(self, token_hash: str) -> Optional['UserSession']:
        """Find a valid (non-expired, non-revoked) session with active user"""
        try:
            from .models import UserSession, AppUser
            now = datetime.now(timezone.utc)
            return (
                self.db.query(UserSession)
                .join(AppUser)
                .filter(
                    UserSession.token_hash == token_hash,
                    UserSession.expires_at > now,
                    UserSession.revoked_at.is_(None),
                    AppUser.status.is_(True),
                )
                .first()
            )
        except SQLAlchemyError as e:
            logger.error(f"Database error finding valid session: {e}")
            raise
    
    def revoke(self, session: 'UserSession') -> None:
        """Revoke a session"""
        try:
            session.revoked_at = datetime.now(timezone.utc)
            self.db.commit()
        except SQLAlchemyError as e:
            self.db.rollback()
            logger.error(f"Database error revoking session: {e}")
            raise
    
    def revoke_all_user_sessions(self, user_id: UUID) -> int:
        """Revoke all active sessions for a user"""
        try:
            from .models import UserSession
            now = datetime.now(timezone.utc)
            count = (
                self.db.query(UserSession)
                .filter(
                    UserSession.user_id == user_id,
                    UserSession.revoked_at.is_(None),
                    UserSession.expires_at > now,
                )
                .update({"revoked_at": now}, synchronize_session=False)
            )
            self.db.commit()
            return count
        except SQLAlchemyError as e:
            self.db.rollback()
            logger.error(f"Database error revoking all sessions: {e}")
            raise
