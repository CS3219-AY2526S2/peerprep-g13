import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/ContextProvider";

export default function RequiredQuestionManager({ children }) {
  const { user, loading } = useAuth();

  if (loading) return <div style={{ padding: 24 }}>Loading...</div>;
  if (!user) return <Navigate to="/auth" replace />;

  const allowed =
    user.role === "admin" || user.role === "question_master";

  if (!allowed) return <Navigate to="/questions" replace />;

  return children;
}