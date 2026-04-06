import api from "./axios";

export const matchingApi = {
  match: (body) => api.post("/match/join", body),
  loosematch: (body) => api.post("/match/join/loose", body),
  leave: (body) => api.post("/match/leave", body),
};