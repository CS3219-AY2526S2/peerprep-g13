# PeerPrep Frontend (Client)

React frontend for PeerPrep G13, built with Vite.

## Tech Stack

- React 19
- React Router v7
- Axios
- Mantine UI
- Vite 7

## Prerequisites

- Node.js 18+
- PeerPrep backend server running on port 8080

## Setup

```bash
npm install
```

Create a `.env` file in this directory:

```
VITE_API_BASE_URL=http://localhost:8080
```

## Running

```bash
npm run dev
```

App runs at `http://localhost:5173`.

## Pages

| Route | Description | Access |
|-------|-------------|--------|
| `/auth` | Login / Register | Public |
| `/dashboard` | User dashboard | Authenticated |
| `/questions` | Browse all questions | Authenticated |
| `/questions/:id` | Question detail | Authenticated |
| `/questions/create` | Create new question | Admin / Question Manager |
| `/questions/edit/:id` | Edit question | Admin / Question Manager |

## Roles

| Role | Permissions |
|------|-------------|
| `user` | View questions |
| `question_master` | View, create, edit, delete questions |
| `admin` | All permissions |

## API

All API calls go through `src/api/`:

- `axios.js` — Axios instance with base URL and Bearer token interceptor
- `question.js` — Question CRUD + match endpoint.
- `user.js` — Auth (register, login, logout) and dashboard.

> `userApi.dashboard()` is currently mocked. Wire it to the real user service when available.
