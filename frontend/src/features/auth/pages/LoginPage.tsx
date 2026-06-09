import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import type { SyntheticEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/authStyles.css";

type RouteState = {
  message?: string;
};

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const location = useLocation();
  const routeState = location.state as RouteState | null;
  const navigate = useNavigate();
  const { login, user } = useAuth();

  useEffect(() => {
    if (routeState?.message) {
      setSuccess(routeState.message);
    }
  }, [routeState]);

  useEffect(() => {
    if (user) {
      navigate("/dashboard");
    }
  }, [navigate, user]);

  async function handleSubmittedForm(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccess("");
    setError("");

    const normalizedEmail = email.trim().toLowerCase();

    if (!normalizedEmail) {
      setError("Email is required.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    try {
      await login({ email: normalizedEmail, password });
      navigate("/dashboard");
      setPassword("");
      setEmail("");
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setError(caughtError.message);
        return;
      }

      setError("Could not connect to server.");
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-page">
        <img className="auth-logo" src="/favicon.svg" alt="MindMesh logo" />
        <h1 className="auth-hero-title">Welcome Back</h1>
        <p className="auth-hero-subtitle">Sign in to continue learning</p>

        <div className="auth-card">
          <form className="auth-form" onSubmit={handleSubmittedForm}>
            <div className="auth-field-group">
              <label className="auth-label" htmlFor="email">
                Email
              </label>
              <div className="auth-field">
                <input
                  className="auth-input"
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                />
              </div>
            </div>

            <div className="auth-field-group">
              <label className="auth-label" htmlFor="password">
                Password
              </label>
              <div className="auth-field">
                <input
                  className="auth-input"
                  id="password"
                  type="password"
                  placeholder="........"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                />
              </div>
            </div>

            <button className="auth-button" type="submit">
              Sign In
            </button>
          </form>

          <p className="auth-switch">
            Don't have an account?{" "}
            <Link className="auth-link" to="/signup">
              Sign up
            </Link>
          </p>

          {error && <p className="auth-message auth-message-error">{error}</p>}
          {success && (
            <p className="auth-message auth-message-success">{success}</p>
          )}
        </div>

        <Link className="auth-back-link" to="/">
          ← Back to home
        </Link>
      </div>
    </div>
  );
}
