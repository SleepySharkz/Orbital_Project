import { Navigate, Route, Routes } from "react-router-dom";
import "./App.css";
import { ProtectedRoute } from "./features/auth/components/ProtectedRoute";
import { PublicOnlyRoute } from "./features/auth/components/PublicOnlyRoute";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { SignupPage } from "./features/auth/pages/SignupPage";
import { DashboardPage } from "./features/auth/pages/Dashboard";
import { LandingPage } from "./features/landing/pages/LandingPage";

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
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App
