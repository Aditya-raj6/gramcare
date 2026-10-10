import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import AppLayout from './layout/AppLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import PendingActionsPage from './pages/PendingActionsPage';
import FieldReportsPage from './pages/FieldReportsPage';
import ResidentsPage from './pages/ResidentsPage';
import HealthCentresPage from './pages/HealthCentresPage';
import AppointmentsPage from './pages/AppointmentsPage';
import InventoryPage from './pages/InventoryPage';
import HealthCampsPage from './pages/HealthCampsPage';
import SanitationPage from './pages/SanitationPage';
import WaterPage from './pages/WaterPage';
import ReportsPage from './pages/ReportsPage';
import UsersPage from './pages/UsersPage';

function RequireAuth({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="pending-actions" element={<PendingActionsPage />} />
        <Route path="field-reports" element={<FieldReportsPage />} />
        <Route path="residents" element={<ResidentsPage />} />
        <Route path="health-centres" element={<HealthCentresPage />} />
        <Route path="appointments" element={<AppointmentsPage />} />
        <Route path="inventory" element={<InventoryPage />} />
        <Route path="health-camps" element={<HealthCampsPage />} />
        <Route path="sanitation" element={<SanitationPage />} />
        <Route path="water" element={<WaterPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="users" element={<UsersPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
