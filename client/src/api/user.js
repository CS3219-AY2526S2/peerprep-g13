import api from "./axios";

export const userApi = {
  register: (body) => api.post("/user/auth/register", body),
  login: (body) => api.post("/user/auth/login", body),
  logout: () => api.post("/user/auth/logout"),
  updateRole: (targetId, body) => api.patch(`/user/${targetId}/role`, body),
  getAuth: () => api.get("/user/auth"),
};