from datetime import datetime
from uuid import uuid4

from sqlalchemy import (
    Boolean,
    Column,
    DateTime,
    ForeignKey,
    String,
    Text,
    func,
    text,
)
from sqlalchemy.dialects.postgresql import UUID, INET, CITEXT
from sqlalchemy.orm import relationship
from .database import Base


class AppUser(Base):
    __tablename__ = "app_user"

    id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        server_default=text("gen_random_uuid()"),
    )
    name = Column(Text, nullable=False)
    email = Column(CITEXT, unique=True, nullable=False)
    role = Column(String, nullable=False, server_default=text("'vet'"))
    password_hash = Column(Text, nullable=False)
    status = Column(Boolean, nullable=False, server_default=text("TRUE"))
    last_login_at = Column(DateTime(timezone=True))
    created_at = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
    )
    updated_at = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
        onupdate=func.now(),
    )

    sessions = relationship("UserSession", back_populates="user")


class UserSession(Base):
    __tablename__ = "user_session"

    id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        server_default=text("gen_random_uuid()"),
    )
    user_id = Column(
        UUID(as_uuid=True),
        ForeignKey("app_user.id", ondelete="CASCADE"),
        nullable=False,
    )
    token_hash = Column(Text, nullable=False)
    created_at = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
    )
    expires_at = Column(DateTime(timezone=True), nullable=False)
    revoked_at = Column(DateTime(timezone=True))
    user_agent = Column(Text)
    ip_address = Column(INET)

    user = relationship("AppUser", back_populates="sessions")
