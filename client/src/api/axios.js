import axios from "axios";

function createApiInstance(baseURL) {
  const instance = axios.create({
    baseURL,
    timeout: 10000,
  });

  instance.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  instance.interceptors.response.use(
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

  return instance;
}

const api = createApiInstance(
  import.meta.env.VITE_API_BASE_URL || "http://localhost:3000"
);

export { createApiInstance };
export default api;