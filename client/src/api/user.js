import api from "./axios";

export const userApi = {
  register: (body) => api.post("/user/auth/register", body),
  login: (body) => api.post("/user/auth/login", body),
  logout: () => api.post("/user/auth/logout"),
  updateRole: (targetId, body) => api.put(`/user/${targetId}/role`, body),
  dashboard: (userId) => api.get(`/user/dashboard/${userId}`),
  updateDashboard: (userId, body) => api.patch(`/user/dashboard/${userId}`, body),
  changePassword: (body) => api.patch(`/user/password`, body),
};