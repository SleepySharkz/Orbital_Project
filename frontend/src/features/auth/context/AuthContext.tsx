import {
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
import { AuthContext } from "./authContextValue";

const AUTH_TOKEN_KEY = "authToken";

// SEPARATION OF CONCERNS -> We let AuthProvider provide apis to handle authentication and fetching side for the pages
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem(AUTH_TOKEN_KEY),
  );
  const [loading, setLoading] = useState(Boolean(token));

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
    const storedToken = localStorage.getItem(AUTH_TOKEN_KEY);

    if (!storedToken) {
      return;
    }

    let didCancel = false;

    fetchCurrentUser(storedToken)
      .then((currentUser) => {
        if (didCancel) {
          return;
        }

        setToken(storedToken);
        setUser(currentUser);
      })
      .catch(() => {
        if (didCancel) {
          return;
        }

        localStorage.removeItem(AUTH_TOKEN_KEY);
        setToken(null);
        setUser(null);
      })
      .finally(() => {
        if (!didCancel) {
          setLoading(false);
        }
      });

    return () => {
      didCancel = true;
    };
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
