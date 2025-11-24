import pytest
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session
from fastapi.testclient import TestClient
from typing import Generator
import os

# Use PostgreSQL for testing
os.environ["DATABASE_URL"] = "postgresql://test_user:test_pass@localhost:5433/test_db"

from app.database import Base
from app.models import AppUser, UserSession
from app.main import app
from app.database import get_db
from app.repositories import UserRepository, SessionRepository
from app.services import AuthService, TokenService
from app.security import hash_password


# ============================================================================
# Database Fixtures
# ============================================================================

@pytest.fixture(scope="session")
def test_engine():
    """Create a test database engine"""
    # Create test database connection
    engine = create_engine(
        os.environ["DATABASE_URL"],
        pool_pre_ping=True,
    )
    
    # Enable CITEXT extension (required for PostgreSQL)
    with engine.connect() as connection:
        connection.execute(text("CREATE EXTENSION IF NOT EXISTS citext"))
        connection.commit()
    
    # Create all tables
    Base.metadata.create_all(bind=engine)
    
    yield engine
    
    # Drop all tables after tests
    Base.metadata.drop_all(bind=engine)
    
    # Drop extension
    with engine.connect() as connection:
        connection.execute(text("DROP EXTENSION IF EXISTS citext CASCADE"))
        connection.commit()
    
    engine.dispose()


@pytest.fixture(scope="function")
def db_session(test_engine) -> Generator[Session, None, None]:
    """Create a test database session with transaction rollback"""
    connection = test_engine.connect()
    transaction = connection.begin()
    
    TestSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=connection)
    session = TestSessionLocal()
    
    try:
        yield session
    finally:
        session.close()
        if transaction.is_active:
            transaction.rollback()
        connection.close()


@pytest.fixture(scope="function")
def client(db_session):
    """Create a test client with overridden database dependency"""
    def override_get_db():
        try:
            yield db_session
        finally:
            pass
    
    app.dependency_overrides[get_db] = override_get_db
    
    with TestClient(app) as test_client:
        yield test_client
    
    app.dependency_overrides.clear()


# ============================================================================
# Repository Fixtures
# ============================================================================

@pytest.fixture
def user_repository(db_session):
    """Create a UserRepository instance"""
    return UserRepository(db_session)


@pytest.fixture
def session_repository(db_session):
    """Create a SessionRepository instance"""
    return SessionRepository(db_session)


# ============================================================================
# Service Fixtures
# ============================================================================

@pytest.fixture
def token_service():
    """Create a TokenService instance"""
    return TokenService()


@pytest.fixture
def auth_service(user_repository, session_repository):
    """Create an AuthService instance"""
    return AuthService(user_repository, session_repository)


# ============================================================================
# Data Fixtures
# ============================================================================

@pytest.fixture
def sample_user_data():
    """Sample user registration data"""
    return {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "password": "SecurePass123!",
    }


@pytest.fixture
def created_user(db_session, sample_user_data):
    """Create a user in the database"""
    password_hash = hash_password(sample_user_data["password"])
    user = AppUser(
        name=sample_user_data["name"],
        email=sample_user_data["email"],
        password_hash=password_hash,
    )
    db_session.add(user)
    db_session.commit()
    db_session.refresh(user)
    return user


@pytest.fixture
def inactive_user(db_session):
    """Create an inactive user"""
    password_hash = hash_password("password123")
    user = AppUser(
        name="Inactive User",
        email="inactive@example.com",
        password_hash=password_hash,
        status=False,
    )
    db_session.add(user)
    db_session.commit()
    db_session.refresh(user)
    return user


@pytest.fixture
def valid_session(db_session, created_user, token_service):
    """Create a valid session"""
    from datetime import datetime, timedelta, timezone
    
    raw_token, token_hash = token_service.generate_session_token()
    expires_at = datetime.now(timezone.utc) + timedelta(hours=8)
    
    session = UserSession(
        user_id=created_user.id,
        token_hash=token_hash,
        expires_at=expires_at,
    )
    db_session.add(session)
    db_session.commit()
    db_session.refresh(session)
    
    return {"session": session, "raw_token": raw_token, "user": created_user}