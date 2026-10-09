import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AppShell from "./components/AppShell";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import VerifyEmailPage from "./pages/auth/VerifyEmailPage";
import DashboardPage from "./pages/app/DashboardPage";
import ExpensesPage from "./pages/app/ExpensesPage";
import WalletsPage from "./pages/app/WalletsPage";
import CategoriesPage from "./pages/app/CategoriesPage";
import IncomePage from "./pages/app/IncomePage";
import GroupsPage from "./pages/app/GroupsPage";
import SharedPage from "./pages/app/SharedPage";
import BudgetsPage from "./pages/app/BudgetsPage";
import AnalyticsPage from "./pages/app/AnalyticsPage";
import NotificationsPage from "./pages/app/NotificationsPage";
import AdminPage from "./pages/app/AdminPage";
import AccountPage from "./pages/app/AccountPage";
import LandingPage from "./pages/LandingPage";

export default function App() {
  return <BrowserRouter><AuthProvider><Routes>
    <Route path="/" element={<LandingPage />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route path="/verify-email" element={<VerifyEmailPage />} />
    <Route element={<ProtectedRoute />}>
      <Route path="/app" element={<AppShell />}>
        <Route index element={<DashboardPage />} />
        <Route path="expenses" element={<ExpensesPage />} />
        <Route path="wallets" element={<WalletsPage />} />
        <Route path="categories" element={<CategoriesPage />} />
        <Route path="income" element={<IncomePage />} />
        <Route path="groups" element={<GroupsPage />} />
        <Route path="shared" element={<SharedPage />} />
        <Route path="budgets" element={<BudgetsPage />} />
        <Route path="analytics" element={<AnalyticsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="admin" element={<AdminPage />} />
        <Route path="account" element={<AccountPage />} />
      </Route>
    </Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes></AuthProvider></BrowserRouter>;
}