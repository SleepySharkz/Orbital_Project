import { Link, useNavigate } from "react-router-dom";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import { useAuth } from "../context/useAuth";
import "../../modules/styles/modulesStyles.css";
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
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="dashboard-main">
        <section className="dashboard-panel">
          <h1 className="dashboard-title">What do you need help with?</h1>
          <p className="dashboard-subtitle">
            Choose a workflow below to continue building or reviewing your study material.
          </p>
          <nav className="dashboard-help-actions" aria-label="Help workflows">
            <Link className="dashboard-primary-button" to="/modules">Set up a module</Link>
            <Link className="dashboard-primary-button" to="/cfcs">Create a CFC</Link>
            <Link className="dashboard-primary-button" to="/my-cfcs">Review saved CFCs</Link>
          </nav>
        </section>
      </main>
    </div>);
}
