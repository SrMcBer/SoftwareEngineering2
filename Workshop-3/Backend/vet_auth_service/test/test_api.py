# ============================================================================
# test_api.py - Integration Tests
# ============================================================================
import pytest
from fastapi import status

class TestHealthEndpoint:
    """Test cases for health check endpoint"""
    
    def test_health_check(self, client):
        """Test health check endpoint"""
        response = client.get("/health")
        
        assert response.status_code == status.HTTP_200_OK
        assert response.json() == {"status": "ok"}


class TestRegisterEndpoint:
    """Test cases for registration endpoint"""
    
    def test_register_success(self, client):
        """Test successful registration"""
        
        
        payload = {
            "name": "New User",
            "email": "newuser@example.com",
            "password": "SecurePass123!",
        }
        
        response = client.post("/register", json=payload)
        
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert data["name"] == payload["name"]
        assert data["email"] == payload["email"]
        assert "id" in data
        assert data["message"] == "User registered successfully"
    
    def test_register_duplicate_email(self, client, created_user):
        """Test registration with duplicate email"""
        payload = {
            "name": "Another User",
            "email": created_user.email,
            "password": "password123",
        }
        
        response = client.post("/register", json=payload)
        
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert "already exists" in response.json()["detail"].lower()
    
    def test_register_invalid_email(self, client):
        """Test registration with invalid email format"""
        payload = {
            "name": "Test User",
            "email": "not-an-email",
            "password": "password123",
        }
        
        response = client.post("/register", json=payload)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_CONTENT
 

class TestLoginEndpoint:
    """Test cases for login endpoint"""
    
    def test_login_success(self, client, created_user, sample_user_data):
        """Test successful login"""
        payload = {
            "email": sample_user_data["email"],
            "password": sample_user_data["password"],
        }
        
        response = client.post("/login", json=payload)
        
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "session_token" in data
        assert data["user"]["email"] == sample_user_data["email"]
        assert data["user"]["name"] == sample_user_data["name"]
    
    def test_login_wrong_password(self, client, sample_user_data, created_user):
        """Test login with wrong password"""
        payload = {
            "email": sample_user_data["email"],
            "password": "wrong_password",
        }
        
        response = client.post("/login", json=payload)
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED
        assert "Invalid email or password" in response.json()["detail"]
    
    def test_login_nonexistent_user(self, client):
        """Test login with non-existent email"""
        payload = {
            "email": "nonexistent@example.com",
            "password": "password123",
        }
        
        response = client.post("/login", json=payload)
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED
    
    def test_login_inactive_user(self, client, inactive_user):
        """Test login with inactive account"""
        payload = {
            "email": inactive_user.email,
            "password": "password123",
        }
        
        response = client.post("/login", json=payload)
        
        assert response.status_code == status.HTTP_403_FORBIDDEN
        assert "deactivated" in response.json()["detail"].lower()


class TestLogoutEndpoint:
    """Test cases for logout endpoint"""
    
    def test_logout_success(self, client, valid_session):
        """Test successful logout"""
        headers = {"Authorization": f"Bearer {valid_session['raw_token']}"}
        response = client.post("/logout", headers=headers)
        
        assert response.status_code == status.HTTP_200_OK
        assert "Logged out successfully" in response.json()["message"]
    
    def test_logout_invalid_token(self, client):
        """Test logout with invalid token"""
        headers = {"Authorization": "Bearer invalid_token"}
        response = client.post("/logout", headers=headers)
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED
    
    def test_logout_missing_header(self, client):
        """Test logout without authorization header"""
        response = client.post("/logout")
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED


class TestMeEndpoint:
    """Test cases for /me endpoint"""
    
    def test_get_me_success(self, client, valid_session):
        """Test getting current user info"""
        headers = {"Authorization": f"Bearer {valid_session['raw_token']}"}
        response = client.get("/me", headers=headers)
        
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert data["email"] == valid_session["user"].email
        assert data["name"] == valid_session["user"].name
        assert data["role"] == valid_session["user"].role
    
    def test_get_me_unauthorized(self, client):
        """Test /me without authentication"""
        response = client.get("/me")
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED


class TestChangePasswordEndpoint:
    """Test cases for password change endpoint"""
    
    def test_change_password_success(self, client, valid_session, sample_user_data):
        """Test successful password change"""
        headers = {"Authorization": f"Bearer {valid_session['raw_token']}"}
        payload = {
            "current_password": sample_user_data["password"],
            "new_password": "NewPassword123!",
        }
        
        response = client.post("/password/change", json=payload, headers=headers)
        
        assert response.status_code == status.HTTP_200_OK
        assert "updated successfully" in response.json()["message"].lower()
    
    def test_change_password_wrong_current(self, client, valid_session):
        """Test password change with wrong current password"""
        headers = {"Authorization": f"Bearer {valid_session['raw_token']}"}
        payload = {
            "current_password": "wrong_password",
            "new_password": "NewPassword123!",
        }
        
        response = client.post("/password/change", json=payload, headers=headers)
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED
    
    def test_change_password_unauthorized(self, client):
        """Test password change without authentication"""
        payload = {
            "current_password": "current",
            "new_password": "new",
        }
        
        response = client.post("/password/change", json=payload)
        
        assert response.status_code == status.HTTP_401_UNAUTHORIZED

