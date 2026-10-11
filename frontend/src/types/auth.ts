export interface RegisterRequest {
  username: string;
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface RegisterResponse {
  id: string;
  username: string;
  name: string;
  email: string;
  message: string;
  walletName: string;
  amount: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  authToken: string;
}

export interface ApiError {
  status?: number;
  message: string;
}
