import React, { createContext, useContext, useEffect, useState } from "react";
import { userApi } from "../api/user";

const AuthContext = createContext(null);

export default function ContextProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  async function returnDashboard() {
    const token = localStorage.getItem("accessToken");

    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }

    try {
      const res = await userApi.getAuth();
      setUser(res.data.message || null);
    } catch (error) {
      console.error("Failed to fetch auth:", error);
      setUser(null);
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

  async function register(username, email, password) {
    await userApi.register({ username, email, password });
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
    }
  }

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    returnDashboard,
    isAuthenticated: !!user,
    isAdmin: user?.role === "ADMIN",
    isQuestionManager: user?.role === "ADMIN" || user?.role === "QUESTION_MASTER",
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
