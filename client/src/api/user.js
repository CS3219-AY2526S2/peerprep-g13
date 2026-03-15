import api from "./axios";

export const userApi = {
  register: (body) => api.post("/user/auth/register", body), 
  login: (body) => api.post("/user/auth/login", body),       
  logout: () => api.post("/user/auth/logout"),               
  // dashboard: (userId) => api.get(`/user/dashboard/${userId}`)
  dashboard: async (userId) => {
    return Promise.resolve({
      data: {
        user: {
          userId: Number(userId),
          name: "Brian Vu",
          username: "brianvu",
          role: "admin",
          avatarUrl: "",
          preferredLanguage: "python",
          preferredTopic: ["graphs", "dp", "arrays"],
          createdAt: "2026-03-10T10:00:00Z",
          updatedAt: "2026-03-14T18:30:00Z",
          isActive: true,
        },
        history: [
          { questionId: 101, finishedDate: "2026-03-11T14:20:00Z" },
          { questionId: 205, finishedDate: "2026-03-13T20:10:00Z" },
          { questionId: 309, finishedDate: "2026-03-14T09:45:00Z" },
        ],
        metrics: {
          totalSolved: 3,
        },
      },
    });
  },
};