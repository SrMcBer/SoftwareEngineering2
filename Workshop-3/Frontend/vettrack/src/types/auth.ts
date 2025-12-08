// src/types/auth.ts

export interface UserInfo {
  id: string;   // uuid
  name: string;
  email: string;
  role: string; // "vet" | "admin" eventually, but keep string for now
}

export interface UserRegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface UserRegisterResponse {
  id: string;
  name: string;
  email: string;
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  session_token: string;
  user: UserInfo;
}

export interface ChangePasswordRequest {
  current_password: string;
  new_password: string;
}

export interface MessageResponse {
  message: string;
}

// Error shapes from the OpenAPI (you can expand if you want)
export interface ValidationError {
  loc: (string | number)[];
  msg: string;
  type: string;
}

export interface HTTPValidationError {
  detail?: ValidationError[];
}
