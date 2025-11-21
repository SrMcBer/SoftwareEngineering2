from passlib.context import CryptContext
import logging

logger = logging.getLogger(__name__)

pwd_context = CryptContext(
    schemes=["bcrypt"],
    deprecated="auto",
    bcrypt__default_rounds=12,
    bcrypt__ident="2b"
)

def hash_password(password: str) -> str:
    """Hash a password using bcrypt via passlib."""
    logger.info("hash_password called")
    logger.debug(f"Input password length (chars): {len(password)}")
    logger.debug(f"Input password length (bytes): {len(password.encode('utf-8'))}")

    try:
        # Truncate to 72 characters (which is usually safe for ASCII/UTF-8)
        truncated_password = password[:72]
        logger.debug(f"Truncated password length: {len(truncated_password)}")
        
        logger.debug("Hashing password with passlib...")
        hashed = pwd_context.hash(truncated_password)
        logger.info("Password hashed successfully")
        logger.debug(f"Hash generated, length: {len(hashed)}")
        
        return hashed
    except Exception as e:
        logger.error(f"Error in hash_password: {type(e).__name__}: {str(e)}")
        logger.exception("Full traceback:")
        raise

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify a password against its hash."""
    try:
        return pwd_context.verify(plain_password[:72], hashed_password)
    except Exception as e:
        logger.error(f"Error verifying password: {type(e).__name__}: {str(e)}")
        return False
