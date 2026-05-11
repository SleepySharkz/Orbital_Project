import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import type { SyntheticEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

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
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setError(caughtError.message);
        return;
      }

      setError("Could not connect to server.");
    }
  }

  return (
    <div>
      <h1>Log In</h1>

      <form onSubmit={handleSubmittedForm}>
        <div>
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>

        <div>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>

        <button type="submit">Log In</button>
      </form>

      <p>
        Don't have an account? <Link to="/signup">Sign up</Link>
      </p>

      {error && <p>{error}</p>}
      {success && <p>{success}</p>}
    </div>
  );
}
