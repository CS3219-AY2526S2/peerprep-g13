import React, { createContext, useContext, useEffect, useState } from "react";
import { userApi } from "../api/user";

const AuthContext = createContext(null);

export default function ContextProvider({ children }) {
  const [user, setUser] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  async function returnDashboard() {
    const userId = localStorage.getItem("userId");

    if (!userId) {
      setUser(null);
      setDashboard(null);
      setLoading(false);
      return;
    }

    try {
      const res = await userApi.dashboard(userId);
      setDashboard(res.data);
      setUser(res.data.user || null);
    } catch (error) {
      console.error("Failed to fetch dashboard:", error);
      setUser(null);
      setDashboard(null);
      localStorage.removeItem("accessToken");
      localStorage.removeItem("userId");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    returnDashboard();
  }, []);

  async function login(email, password) {
    const res = await userApi.login({ email, password });
    const { accessToken, userId } = res.data.data;

    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("userId", String(userId));

    setLoading(true);
    await returnDashboard();
  }

  async function register(name, username, email, password) {
    await userApi.register({
      Name: name,
      username,
      email,
      password,
    });

    await login(email, password);
  }

  async function logout() {
    try {
      await userApi.logout();
    } catch (error) {
      console.error("Logout request failed:", error);
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("userId");
      setUser(null);
      setDashboard(null);
    }
  }

  const value = {
    user,
    dashboard,
    loading,
    login,
    register,
    logout,
    returnDashboard,
    isAuthenticated: !!user,
    isAdmin: user?.role === "admin",
    isQuestionManager:
      user?.role === "admin" || user?.role === "question_master",
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}