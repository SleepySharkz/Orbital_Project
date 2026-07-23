import { useNavigate } from "react-router-dom";
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
          <h1 className="dashboard-title">Welcome Back</h1>
          <p className="dashboard-subtitle">
            Use the navigation on the left to move between your dashboard, modules, and saved CFCs.
          </p>
        </section>
      </main>
    </div>);
}
