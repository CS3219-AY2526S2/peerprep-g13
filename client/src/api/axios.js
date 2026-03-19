import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:3000",
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url || "";

    if (
      status === 401 &&
      !window.location.pathname.startsWith("/auth") &&
      !requestUrl.startsWith("/user/password")
    ) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("userId");
      window.location.href = "/auth";
    }
    return Promise.reject(error);
  }
);

export default api;