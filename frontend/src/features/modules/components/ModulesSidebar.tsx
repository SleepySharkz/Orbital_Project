import { NavLink } from "react-router-dom";
import type { User } from "../../auth/api/authApi";

type ModulesSidebarProps = {
  user: User;
  onLogout: () => Promise<void>;
};

export function ModulesSidebar({ user, onLogout }: ModulesSidebarProps) {
  return (
    <aside className="modules-sidebar">
      <div className="modules-sidebar-top">
        <div className="modules-brand-block">
          <p className="modules-brand">MINDMESH</p>
          <p className="modules-username">{user.username}</p>
          <p className="modules-email">{user.email}</p>
        </div>

        <nav className="modules-nav" aria-label="App routes">
          <NavLink
            className={({ isActive }) =>
              isActive ? "modules-nav-link modules-nav-link-active" : "modules-nav-link"
            }
            to="/dashboard"
          >
            Dashboard
          </NavLink>

          <NavLink
            className={({ isActive }) =>
              isActive ? "modules-nav-link modules-nav-link-active" : "modules-nav-link"
            }
            to="/modules"
          >
            Modules
          </NavLink>

          <NavLink
            className={({ isActive }) =>
              isActive ? "modules-nav-link modules-nav-link-active" : "modules-nav-link"
            }
            to="/cfcs"
          >
            Create Flashcard (CFC)
          </NavLink>

          <NavLink
            className={({ isActive }) =>
              isActive ? "modules-nav-link modules-nav-link-active" : "modules-nav-link"
            }
            to="/my-cfcs"
          >
            My CFCs
          </NavLink>

          <NavLink
            className={({ isActive }) =>
              isActive ? "modules-nav-link modules-nav-link-active" : "modules-nav-link"
            }
            to="/topic-sheets"
          >
            Topic Sheets
          </NavLink>
        </nav>
      </div>

      <button
        className="modules-logout"
        type="button"
        onClick={() => void onLogout()}
      >
        Log Out
      </button>
    </aside>
  );
}
