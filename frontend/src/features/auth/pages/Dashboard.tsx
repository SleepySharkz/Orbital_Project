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
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="dashboard-eyebrow">MINDMESH</p>
          <h1 className="dashboard-title">Welcome!</h1>
          <p className="dashboard-subtitle">Dashboard loaded. Auth verified.</p>
        </div>

        <button
          className="dashboard-logout"
          type="button"
          onClick={handleLogout}
        >
          Log Out
        </button>
      </header>

      <main className="dashboard-content">
        <section className="dashboard-panel">
          <p className="dashboard-label">Signed in as</p>
          <p className="dashboard-email">{user.email}</p>
        </section>
      </main>
    </div>);
}
