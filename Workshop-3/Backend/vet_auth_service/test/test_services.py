# ============================================================================
# test_services.py - Service Unit Tests
# ============================================================================
import pytest
from app.exceptions import (
    UserAlreadyExistsError,
    InvalidCredentialsError,
    AccountDeactivatedError,
    InvalidSessionError,
)

class TestTokenService:
    """Test cases for TokenService"""
    
    def test_generate_session_token(self, token_service):
        """Test token generation"""
        raw_token, token_hash = token_service.generate_session_token()
        
        assert raw_token is not None
        assert token_hash is not None
        assert len(raw_token) > 0
        assert len(token_hash) == 64  # SHA256 hex digest length
    
    def test_generate_unique_tokens(self, token_service):
        """Test that generated tokens are unique"""
        token1, hash1 = token_service.generate_session_token()
        token2, hash2 = token_service.generate_session_token()
        
        assert token1 != token2
        assert hash1 != hash2
    
    def test_hash_token(self, token_service):
        """Test token hashing"""
        raw_token = "test_token_12345"
        hashed = token_service.hash_token(raw_token)
        
        assert len(hashed) == 64
        # Verify same input produces same hash
        assert hashed == token_service.hash_token(raw_token)
    
    def test_calculate_expiry(self, token_service):
        """Test expiry calculation"""
        from datetime import datetime, timezone
        
        before = datetime.now(timezone.utc)
        expiry = token_service.calculate_expiry()
        after = datetime.now(timezone.utc)
        
        assert expiry > before
        assert expiry > after


class TestAuthService:
    """Test cases for AuthService"""
    
    def test_register_user_success(self, auth_service):
        """Test successful user registration"""
        user = auth_service.register_user(
            name="Test User",
            email="test@example.com",
            password="password123",
        )
        
        assert user.id is not None
        assert user.name == "Test User"
        assert user.email == "test@example.com"
        assert user.status is True
    
    def test_register_user_duplicate_email(self, auth_service, created_user):
        """Test registration with duplicate email"""
        with pytest.raises(UserAlreadyExistsError):
            auth_service.register_user(
                name="Another User",
                email=created_user.email,
                password="password123",
            )
    
    def test_authenticate_success(self, auth_service, created_user, sample_user_data):
        """Test successful authentication"""
        user, session_token = auth_service.authenticate(
            email=sample_user_data["email"],
            password=sample_user_data["password"],
            client_type="web",
            client_ip="127.0.0.1",
        )
        
        assert user.id == created_user.id
        assert session_token is not None
        assert len(session_token) > 0
    
    def test_authenticate_wrong_email(self, auth_service):
        """Test authentication with non-existent email"""
        with pytest.raises(InvalidCredentialsError):
            auth_service.authenticate(
                email="nonexistent@example.com",
                password="password123",
            )
    
    def test_authenticate_wrong_password(self, auth_service, sample_user_data, created_user):
        """Test authentication with wrong password"""
        with pytest.raises(InvalidCredentialsError):
            auth_service.authenticate(
                email=sample_user_data["email"],
                password="wrong_password",
            )
    
    def test_authenticate_inactive_user(self, auth_service, inactive_user):
        """Test authentication with inactive account"""
        with pytest.raises(AccountDeactivatedError):
            auth_service.authenticate(
                email=inactive_user.email,
                password="password123",
            )
    
    def test_validate_session_success(self, auth_service, valid_session):
        """Test successful session validation"""
        user = auth_service.validate_session(valid_session["raw_token"])
        
        assert user.id == valid_session["user"].id
        assert user.email == valid_session["user"].email
    
    def test_validate_session_invalid_token(self, auth_service):
        """Test validation with invalid token"""
        with pytest.raises(InvalidSessionError):
            auth_service.validate_session("invalid_token")
    
    def test_logout_success(self, auth_service, valid_session, db_session):
        """Test successful logout"""
        auth_service.logout(valid_session["raw_token"])
        
        db_session.refresh(valid_session["session"])
        assert valid_session["session"].revoked_at is not None
    
    def test_logout_invalid_token(self, auth_service):
        """Test logout with invalid token"""
        with pytest.raises(InvalidSessionError):
            auth_service.logout("invalid_token")
    
    def test_change_password_success(self, auth_service, created_user, sample_user_data):
        """Test successful password change"""
        auth_service.change_password(
            user=created_user,
            current_password=sample_user_data["password"],
            new_password="NewPassword123!",
        )
        
        # Verify old password no longer works
        with pytest.raises(InvalidCredentialsError):
            auth_service.authenticate(
                email=created_user.email,
                password=sample_user_data["password"],
            )
        
        # Verify new password works
        user, token = auth_service.authenticate(
            email=created_user.email,
            password="NewPassword123!",
        )
        assert user.id == created_user.id
    
    def test_change_password_wrong_current(self, auth_service, created_user):
        """Test password change with wrong current password"""
        with pytest.raises(InvalidCredentialsError):
            auth_service.change_password(
                user=created_user,
                current_password="wrong_password",
                new_password="NewPassword123!",
            )