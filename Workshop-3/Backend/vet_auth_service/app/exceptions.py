# ============================================================================
# exceptions.py - Custom Exceptions
# ============================================================================
class AuthServiceException(Exception):
    """Base exception for auth service"""
    pass

class UserAlreadyExistsError(AuthServiceException):
    """User with this email already exists"""
    pass

class UserNotFoundError(AuthServiceException):
    """User not found"""
    pass

class InvalidCredentialsError(AuthServiceException):
    """Invalid email or password"""
    pass

class AccountDeactivatedError(AuthServiceException):
    """User account is deactivated"""
    pass

class InvalidSessionError(AuthServiceException):
    """Session is invalid or expired"""
    pass

class PasswordHashingError(AuthServiceException):
    """Error during password hashing"""
    pass