import { Navigate, Route, Routes, useParams } from "react-router-dom";
import "./App.css";
import { ProtectedRoute } from "./features/auth/components/ProtectedRoute";
import { PublicOnlyRoute } from "./features/auth/components/PublicOnlyRoute";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { SignupPage } from "./features/auth/pages/SignupPage";
import { DashboardPage } from "./features/auth/pages/Dashboard";
import { LandingPage } from "./features/landing/pages/LandingPage";
import { CFCDetailPage } from "./features/cfc/pages/CFCDetailPage";
import { CFCPage } from "./features/cfc/pages/CFCPage";
import { MyCFCsPage } from "./features/cfc/pages/MyCFCsPage";
import { ModuleDetailPage } from "./features/modules/pages/ModuleDetailPage";
import { ModulesPage } from "./features/modules/pages/ModulesPage";
import { TFCListPage } from "./features/tfc/pages/TFCListPage";

function CFCDetailRedirect() {
  const { cfcId } = useParams();

  if (!cfcId) {
    return <Navigate to="/my-cfcs" replace />;
  }

  return <Navigate to={`/my-cfcs/${cfcId}`} replace />;
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route
        path="/signup"
        element={
          <PublicOnlyRoute>
            <SignupPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/login"
        element={
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/modules"
        element={
          <ProtectedRoute>
            <ModulesPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/modules/:moduleId"
        element={
          <ProtectedRoute>
            <ModuleDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/cfcs"
        element={
          <ProtectedRoute>
            <CFCPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-cfcs"
        element={
          <ProtectedRoute>
            <MyCFCsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-cfcs/:cfcId"
        element={
          <ProtectedRoute>
            <CFCDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/cfcs/:cfcId"
        element={
          <ProtectedRoute>
            <CFCDetailRedirect />
          </ProtectedRoute>
        }
      />
      <Route
        path="/topic-sheets"
        element={
          <ProtectedRoute>
            <TFCListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/topic-sheets/:tfcId"
        element={
          <ProtectedRoute>
            <TFCListPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
