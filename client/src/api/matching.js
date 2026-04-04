import { createApiInstance } from "./axios";

const matchingApi_axios = createApiInstance(
  import.meta.env.VITE_MATCHING_API_BASE_URL || "http://localhost:8082"
);

export const matchingApi = {
  match: (body) => matchingApi_axios.post("/match/join", body),
  loosematch: (body) => matchingApi_axios.post("/match/join/loose", body),
  leave: (body) => matchingApi_axios.post("/match/leave", body),
};