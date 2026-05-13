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

    if (!email.trim()) {
      setError("Email is required.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    try {
      const loggedInUser = await login({ email, password });
      setSuccess(`Logged in as ${loggedInUser.email}.`);
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
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Welcome Smartie!</h1>

        <form className="auth-form" onSubmit={handleSubmittedForm}>
          <div className="auth-field">
            <label className="auth-label" htmlFor="email">
              Email
            </label>
            <input
              className="auth-input"
              id="email"
              type="email"
              placeholder="Your Email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>

          <div className="auth-field">
            <label className="auth-label" htmlFor="password">
              Password
            </label>
            <input
              className="auth-input"
              id="password"
              type="password"
              placeholder="Enter Password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>

          <button className="auth-button" type="submit">
            Log In
          </button>
        </form>

        <p className="auth-switch">
          Don't have an account?{" "}
          <Link className="auth-link" to="/signup">
            Sign up
          </Link>
        </p>

        {error && <p className="auth-message auth-message-error">{error}</p>}
        {success && <p className="auth-message auth-message-success">{success}</p>}
      </div>
    </div>
  );
}
