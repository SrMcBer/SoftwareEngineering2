from uuid import UUID

from pydantic import BaseModel, EmailStr, constr, field_validator

class UserRegisterRequest(BaseModel):
    name: constr(strip_whitespace=True, min_length=1)
    email: EmailStr
    password: constr(min_length=8, max_length=72)
    
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