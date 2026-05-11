import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import {
  fetchCurrentUser,
  loginRequest,
  logoutRequest,
  signupRequest,
  type LoginCredentials,
  type SignupCredentials,
  type User,
} from "../api/authApi";

type AuthContextValue = {
  user: User | null;
  token: string | null;
  loading: boolean;
  signup: (credentials: SignupCredentials) => Promise<string>;
  login: (credentials: LoginCredentials) => Promise<User>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
};

const AUTH_TOKEN_KEY = "authToken";

// Also we have a reusable security context for the pages to cleanly use.
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// SEPARATION OF CONCERNS -> We let AuthProvider provide apis to handle authentication and fetching side for the pages
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem(AUTH_TOKEN_KEY),
  );
  const [loading, setLoading] = useState(true);

  async function refreshUser() {
    const storedToken = localStorage.getItem(AUTH_TOKEN_KEY);

    if (!storedToken) {
      setToken(null);
      setUser(null);
      setLoading(false);
      return;
    }

    setLoading(true);

    try {
      const currentUser = await fetchCurrentUser(storedToken);
      setToken(storedToken);
      setUser(currentUser);
    } catch {
      localStorage.removeItem(AUTH_TOKEN_KEY);
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }

  async function login(credentials: LoginCredentials) {
    const data = await loginRequest(credentials);
    localStorage.setItem(AUTH_TOKEN_KEY, data.token);
    setToken(data.token);
    setUser(data.user);
    return data.user;
  }

  async function signup(credentials: SignupCredentials) {
    return signupRequest(credentials);
  }

  async function logout() {
    const storedToken = localStorage.getItem(AUTH_TOKEN_KEY);

    try {
      await logoutRequest(storedToken);
    } finally {
      localStorage.removeItem(AUTH_TOKEN_KEY);
      setToken(null);
      setUser(null);
    }
  }

  useEffect(() => {
    void refreshUser();
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        signup,
        login,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider.");
  }

  return context;
}
