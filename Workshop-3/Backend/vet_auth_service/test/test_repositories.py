# ============================================================================
# test_repositories.py - Repository Unit Tests
# ============================================================================
import pytest
from app.exceptions import UserAlreadyExistsError
from app.security import hash_password
from app.services import TokenService

class TestUserRepository:
    """Test cases for UserRepository"""
    
    def test_find_by_email_existing(self, user_repository, created_user):
        """Test finding an existing user by email"""
        user = user_repository.find_by_email(created_user.email)
        
        assert user is not None
        assert user.id == created_user.id
        assert user.email == created_user.email
        assert user.name == created_user.name
    
    def test_find_by_email_nonexistent(self, user_repository):
        """Test finding a non-existent user"""
        user = user_repository.find_by_email("nonexistent@example.com")
        assert user is None
    
    def test_find_by_id_existing(self, user_repository, created_user):
        """Test finding an existing user by ID"""
        user = user_repository.find_by_id(created_user.id)
        
        assert user is not None
        assert user.id == created_user.id
        assert user.email == created_user.email
    
    def test_find_by_id_nonexistent(self, user_repository):
        """Test finding a non-existent user by ID"""
        from uuid import uuid4
        user = user_repository.find_by_id(uuid4())
        assert user is None
    
    def test_create_user_success(self, user_repository):
        """Test creating a new user"""
        password_hash = hash_password("password123")
        user = user_repository.create(
            name="Test User",
            email="test@example.com",
            password_hash=password_hash,
        )
        
        assert user.id is not None
        assert user.name == "Test User"
        assert user.email == "test@example.com"
        assert user.password_hash == password_hash
        assert user.status is True
        assert user.role == "vet"
    
    def test_create_user_duplicate_email(self, user_repository, created_user):
        """Test creating a user with duplicate email raises exception"""
        password_hash = hash_password("password123")
        
        with pytest.raises(UserAlreadyExistsError):
            user_repository.create(
                name="Another User",
                email=created_user.email,
                password_hash=password_hash,
            )
    
    def test_update_last_login(self, user_repository, created_user, db_session):
        """Test updating last login timestamp"""
        from datetime import datetime, timezone
        
        original_login = created_user.last_login_at
        user_repository.update_last_login(created_user)
        db_session.refresh(created_user)
        
        assert created_user.last_login_at is not None
        assert created_user.last_login_at != original_login
    
    def test_update_password(self, user_repository, created_user, db_session):
        """Test updating user password"""
        new_password_hash = hash_password("NewPassword123!")
        original_hash = created_user.password_hash
        
        user_repository.update_password(created_user, new_password_hash)
        db_session.refresh(created_user)
        
        assert created_user.password_hash == new_password_hash
        assert created_user.password_hash != original_hash


class TestSessionRepository:
    """Test cases for SessionRepository"""
    
    def test_create_session(self, session_repository, created_user, token_service):
        """Test creating a new session"""
        from datetime import datetime, timedelta, timezone
        
        raw_token, token_hash = token_service.generate_session_token()
        expires_at = datetime.now(timezone.utc) + timedelta(hours=8)
        
        session = session_repository.create(
            user_id=created_user.id,
            token_hash=token_hash,
            expires_at=expires_at,
            user_agent="test-client",
            ip_address="127.0.0.1",
        )
        
        assert session.id is not None
        assert session.user_id == created_user.id
        assert session.token_hash == token_hash
        assert session.user_agent == "test-client"
        assert session.ip_address == "127.0.0.1"
        assert session.revoked_at is None
    
    def test_find_valid_session_success(self, session_repository, valid_session):
        """Test finding a valid session"""
        token_hash = TokenService.hash_token(valid_session["raw_token"])
        session = session_repository.find_valid_session(token_hash)
        
        assert session is not None
        assert session.id == valid_session["session"].id
        assert session.user_id == valid_session["user"].id
    
    def test_find_valid_session_nonexistent(self, session_repository):
        """Test finding a non-existent session"""
        fake_hash = "nonexistent_hash"
        session = session_repository.find_valid_session(fake_hash)
        assert session is None
    
    def test_find_valid_session_revoked(self, session_repository, valid_session, db_session):
        """Test that revoked sessions are not returned"""
        from datetime import datetime, timezone
        
        # Revoke the session
        valid_session["session"].revoked_at = datetime.now(timezone.utc)
        db_session.commit()
        
        token_hash = TokenService.hash_token(valid_session["raw_token"])
        session = session_repository.find_valid_session(token_hash)
        assert session is None
    
    def test_revoke_session(self, session_repository, valid_session, db_session):
        """Test revoking a session"""
        session = valid_session["session"]
        assert session.revoked_at is None
        
        session_repository.revoke(session)
        db_session.refresh(session)
        
        assert session.revoked_at is not None
    
    def test_revoke_all_user_sessions(self, session_repository, created_user, token_service, db_session):
        """Test revoking all sessions for a user"""
        from datetime import datetime, timedelta, timezone
        
        # Create multiple sessions
        for _ in range(3):
            raw_token, token_hash = token_service.generate_session_token()
            expires_at = datetime.now(timezone.utc) + timedelta(hours=8)
            session_repository.create(created_user.id, token_hash, expires_at)
        
        # Revoke all
        count = session_repository.revoke_all_user_sessions(created_user.id)
        
        assert count == 3
        
        # Verify all are revoked
        from app.models import UserSession
        sessions = db_session.query(UserSession).filter(
            UserSession.user_id == created_user.id
        ).all()
        
        for session in sessions:
            assert session.revoked_at is not None