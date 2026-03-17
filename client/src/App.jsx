import React from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import ContextProvider from "./context/ContextProvider";
import AuthPage from "./pages/AuthPage/AuthPage";
import DashboardPage from "./pages/DashboardPage/DashboardPage";
import QuestionsPage from "./pages/QuestionsPage/QuestionsPage";
import QuestionDetailPage from "./pages/QuestionDetailPage/QuestionDetailPage";
import CreateQuestionPage from "./pages/CreateQuestionPage/CreateQuestionPage";
import EditQuestionPage from "./pages/EditQuestionPage/EditQuestionPage";
import RequireAuth from "./auth/RequiredAuth";
import RequiredAdmin from "./auth/RequiredAdmin";
import RequiredQuestionManager from "./auth/RequiredQuestionManager";
import AdminUsersPage from "./pages/AdminUsersPage/AdminUsersPage";
import Navbar from "./components/Navbar/Navbar";

function UnauthorizedPage() {
  return <div>Unauthorized</div>;
}

export default function App() {
  return (
    <BrowserRouter>
      <ContextProvider>
        <Navbar />
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/auth" element={<AuthPage initLoginMode={true} />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          <Route
            path="/dashboard"
            element={
              <RequireAuth>
                <DashboardPage />
              </RequireAuth>
            }
          />

          <Route
            path="/questions/create"
            element={
              <RequiredQuestionManager>
                <CreateQuestionPage />
              </RequiredQuestionManager>
            }
          />

          <Route
            path="/questions/edit/:id"
            element={
              <RequiredQuestionManager>
                <EditQuestionPage />
              </RequiredQuestionManager>
            }
          />

          <Route
            path="/questions/:id"
            element={
              <RequireAuth>
                <QuestionDetailPage />
              </RequireAuth>
            }
          />

          <Route
            path="/questions"
            element={
              <RequireAuth>
                <QuestionsPage />
              </RequireAuth>
            }
          />

          <Route
            path="/admin/users"
            element={
              <RequiredAdmin>
                <AdminUsersPage />
              </RequiredAdmin>
            }
          />
        </Routes>
      </ContextProvider>
    </BrowserRouter>
  );
}
