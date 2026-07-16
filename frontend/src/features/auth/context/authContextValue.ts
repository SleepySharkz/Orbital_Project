import { createContext } from "react";
import type { LoginCredentials, SignupCredentials, User } from "../api/authApi";

export type AuthContextValue = {
  user: User | null;
  token: string | null;
  loading: boolean;
  signup: (credentials: SignupCredentials) => Promise<string>;
  login: (credentials: LoginCredentials) => Promise<User>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
