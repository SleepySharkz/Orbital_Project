import { useState } from "react";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { type SyntheticEvent } from "react";
import { useAuth } from "../context/AuthContext";
import "../styles/authStyles.css"

export function SignupPage() {
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

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Server side already validates comprehensively, but frontend gives a quick response for surface level checks
    if (!emailRegex.test(email)) {
      setError("Please enter a valid email.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    try {
      const message = await signup({ email, password });

      setEmail("");
      setPassword("");

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
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Get Started Now</h1>

        <form className="auth-form" onSubmit={handleSubmittedForm}>
          <div className="auth-field">
            <label className="auth-label" htmlFor="email">
              Email
            </label>
            <input
              className="auth-input"
              id="email"
              placeholder="Your Email"
              type="email"
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
              placeholder="Create Password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>

          <button className="auth-button" type="submit">
            Sign Up
          </button>
        </form>

        <p className="auth-switch">
          Already have an account?{" "}
          <Link className="auth-link" to="/login">
            Log in
          </Link>
        </p>

        {error && <p className="auth-message auth-message-error">{error}</p>}
      </div>
    </div>

  )
}
