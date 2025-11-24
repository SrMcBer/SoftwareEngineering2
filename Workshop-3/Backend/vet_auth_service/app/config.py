# ============================================================================
# config.py - Configuration Management (Singleton Pattern)
# ============================================================================
import os
from dotenv import load_dotenv
from typing import Optional

class Config:
    """Singleton configuration manager"""
    _instance: Optional['Config'] = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._initialized = False
        return cls._instance
    
    def __init__(self):
        if self._initialized:
            return
            
        load_dotenv()
        self.DATABASE_URL = os.getenv("DATABASE_URL")
        self.SESSION_EXPIRY_HOURS = int(os.getenv("SESSION_EXPIRY_HOURS", "8"))
        self.TOKEN_LENGTH = int(os.getenv("TOKEN_LENGTH", "32"))
        
        if not self.DATABASE_URL:
            raise RuntimeError("DATABASE_URL is not set in environment")
        
        self._initialized = True
