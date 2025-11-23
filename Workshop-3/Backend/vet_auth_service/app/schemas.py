from uuid import UUID
from datetime import datetime
from pydantic import BaseModel, EmailStr, constr, field_validator

class UserRegisterRequest(BaseModel):
    name: constr(strip_whitespace=True, min_length=1) # pyright: ignore[reportInvalidTypeForm]
    email: EmailStr
    password: constr(min_length=8, max_length=72) # pyright: ignore[reportInvalidTypeForm]
    
    @field_validator('password')
    @classmethod  
    def validate_password_bytes(cls, v: str) -> str:
        if len(v.encode('utf-8')) > 72:
            raise ValueError('Password cannot exceed 72 bytes when UTF-8 encoded')
        return v

class UserRegisterResponse(BaseModel):
    id: UUID
    name: str
    email: EmailStr
    message: str

class LoginRequest(BaseModel):
    email: EmailStr
    password: constr(min_length=8, max_length=72) # pyright: ignore[reportInvalidTypeForm]


class UserInfo(BaseModel):
    name: str
    email: EmailStr
    role: str


class LoginResponse(BaseModel):
    session_token: str
    user: UserInfo

class MessageResponse(BaseModel):
    message: str


class ChangePasswordRequest(BaseModel):
    current_password: constr(min_length=8, max_length=72) # pyright: ignore[reportInvalidTypeForm]
    new_password: constr(min_length=8, max_length=72) # pyright: ignore[reportInvalidTypeForm]

class SessionInfo(BaseModel):
    id: UUID
    created_at: datetime
    expires_at: datetime
    revoked_at: datetime | None = None
    user_agent: str | None = None
    ip_address: str | None = None
    is_current: bool | None = None  # True for the session being used in this request


class SessionsListResponse(BaseModel):
    sessions: list[SessionInfo]