import api from "./axios";

// Normalize server response fields to client field names:
//   questionId -> id
//   example    -> examples
function normalizeQuestion(q) {
  return {
    ...q,
    id: q.questionId ?? q.id,
    examples: q.example ?? q.examples ?? [],
  };
}

// Map client form fields back to server field names before sending:
//   examples -> example
function toServerBody(body) {
  const { examples, ...rest } = body;
  if (examples !== undefined) rest.example = examples;
  return rest;
}

export const questionApi = {
  list: async () => {
    const res = await api.get("/questions");
    return { data: { questions: (res.data.questions || []).map(normalizeQuestion) } };
  },

  filter: async (body) => {
    const params = {};
    if (body.topic) params.topic = body.topic;
    if (body.difficulty) params.difficulty = body.difficulty;
    const res = await api.get("/questions", { params });
    return { data: { questions: (res.data.questions || []).map(normalizeQuestion) } };
  },

  getById: async (questionId) => {
    const res = await api.get(`/questions/${questionId}`);
    return { data: { question: normalizeQuestion(res.data.question) } };
  },

  create: async (body) => {
    return api.post("/questions", toServerBody(body));
  },

  update: async (questionId, body) => {
    return api.put(`/questions/${questionId}`, toServerBody(body));
  },

  delete: (questionId) => api.delete(`/questions/${questionId}`),

  match: (body) => api.post("/questions/match", body),
};
