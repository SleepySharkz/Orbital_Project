import { useState } from "react";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { type SyntheticEvent } from "react";
import { useAuth } from "../context/AuthContext";
import "../styles/authStyles.css"

export function SignupPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { signup } = useAuth();

  async function handleSubmittedForm(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    // Signup shouldnt clear, if incase user mistypes email
    // setEmail("");
    // setPassword("");
    //TODO: Implement banned username words

    if (!username.trim()) {
      setError("Username is required.");
      return;
    }

    const normalizedEmail = email.trim().toLowerCase();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Server side already validates comprehensively, but frontend gives a quick response for surface level checks
    if (!emailRegex.test(normalizedEmail)) {
      setError("Please enter a valid email.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    try {
      const message = await signup({ username, email: normalizedEmail, password });

      setEmail("");
      setPassword("");
      setUsername("");

      navigate("/login", {
        state: { message },
      });
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setError(caughtError.message);
        return;
      }

      setError("Could not connect to server");
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-page">
        <img className="auth-logo" src="/favicon.svg" alt="MindMesh logo" />
        <h1 className="auth-hero-title">Create Your Account</h1>
        <p className="auth-hero-subtitle">Start building your knowledge base</p>

        <div className="auth-card">
          <form className="auth-form" onSubmit={handleSubmittedForm}>
            <div className="auth-field-group">
              <label className="auth-label" htmlFor="username">
                Username
              </label>
              <div className="auth-field">
                <input
                  className="auth-input"
                  id="username"
                  type="text"
                  placeholder="choose-a-username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                />
              </div>
            </div>

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
              Create Account
            </button>
          </form>

          <p className="auth-switch">
            Already have an account?{" "}
            <Link className="auth-link" to="/login">
              Sign in
            </Link>
          </p>

          {error && <p className="auth-message auth-message-error">{error}</p>}
        </div>

        <Link className="auth-back-link" to="/">
          ← Back to home
        </Link>
      </div>
    </div>
  );
}
