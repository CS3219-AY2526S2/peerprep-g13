import api from "./axios";

export const userApi = {
  register: (body) => api.post("/user/auth/register", body), 
  login: (body) => api.post("/user/auth/login", body),       
  logout: () => api.post("/user/auth/logout"),               
  dashboard: (userId) => api.get(`/user/dashboard/${userId}`)
};