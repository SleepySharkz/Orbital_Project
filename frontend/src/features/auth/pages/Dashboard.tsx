import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/dashboardStyles.css";

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
      <aside className="dashboard-sidebar">
        <div className="dashboard-sidebar-top">
          <div className="dashboard-brand-block">
            <p className="dashboard-brand">MINDMESH</p>
            <p className="dashboard-username">{user.username}</p>
            <p className="dashboard-email">{user.email}</p>
          </div>

          <nav className="dashboard-nav" aria-label="Dashboard routes">
            <NavLink
              className={({ isActive }) =>
                isActive ? "dashboard-nav-link dashboard-nav-link-active" : "dashboard-nav-link"
              }
              to="/dashboard"
            >
              Dashboard
            </NavLink>

            <NavLink
              className={({ isActive }) =>
                isActive ? "dashboard-nav-link dashboard-nav-link-active" : "dashboard-nav-link"
              }
              to="/modules"
            >
              Modules
            </NavLink>
          </nav>
        </div>

        <button
          className="dashboard-logout"
          type="button"
          onClick={handleLogout}
        >
          Log Out
        </button>
      </aside>

      <main className="dashboard-main">
        <section className="dashboard-panel">
          <p className="dashboard-eyebrow">Dashboard</p>
          <h1 className="dashboard-title">Welcome Back</h1>
          <p className="dashboard-subtitle">
            Use the navigation on the left to move between your dashboard and modules.
          </p>
        </section>
      </main>
    </div>);
}
