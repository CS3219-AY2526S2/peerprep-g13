import React, { createContext, useContext, useEffect, useState } from "react";
import { userApi } from "../api/user";

const userContext = createContext()

const ContextProvider = ({children}) => {
  const [user, setUser] = useState(null); 
  const [loading, setLoading] = useState(true);

  async function returnDashboard() {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      const res = await userApi.dashboard(userId);
      setUser(res.data.user); 
    } catch {
      setUser(null);
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
    await returnDashboard();
  }

  async function register(name, username, email, password) {
    await userApi.register({name, username, email, password})
    await login({email, password})
  }
  async function logout() {
    try {
      await userApi.logout();
    } catch {
      // ignore (still clear locally)
    }
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userId");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, returnDashboard }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(userContext);
}