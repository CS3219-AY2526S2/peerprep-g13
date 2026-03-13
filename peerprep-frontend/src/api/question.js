import api from "./client";

export function listQuestions(params) {
  return api.get("/questions", { params });
}
export function getQuestion(id) {
  return api.get(`/questions/${id}`);
}
export function createQuestion(body) {
  return api.post("/questions", body);
}
export function updateQuestion(id, body) {
  return api.put(`/questions/${id}`, body);
}
export function deleteQuestion(id) {
  return api.delete(`/questions/${id}`);
}