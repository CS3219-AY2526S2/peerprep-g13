import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/ContextProvider";

export default function RequireAdmin({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div style={{ padding: 24 }}>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "admin") return <Navigate to="/questions" replace />;
  return children;
}