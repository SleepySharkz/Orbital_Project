import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function DashboardPage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user) {
    return null;
  }

  return (
    <div>
      <h1>Dashboard</h1>
      <p>Dashboard loaded! Auth verified.</p>
      <p>Welcome, {user.email}</p>
      <button type="button" onClick={handleLogout}>
        Log Out
      </button>
    </div>
  );
}
