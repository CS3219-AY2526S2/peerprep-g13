# Collaboration Service

Real-time collaborative code editor backend for PeerPrep. Uses WebSocket and [Yjs](https://yjs.dev/) CRDT to sync document state across multiple users in the same room.

## How it works

1. A client connects via WebSocket: `ws://localhost:4000/<roomId>?token=<JWT>`
2. The server validates the JWT and adds the client to the named room
3. Yjs sync protocol (binary messages) is exchanged between the server and all clients in the room:
   - **Sync step 1/2** — new clients catch up to the current document state
   - **Updates** — each edit is broadcast to all other clients in the room in real time
   - **Awareness** — cursor positions and user presence are broadcast to all clients
4. When all clients leave a room, it is destroyed and memory is freed

## Project structure

```
collaboration-service/
├── src/
│   ├── index.js      # Entry point — HTTP server + WebSocket setup
│   ├── server.js     # WebSocket connection handler, JWT auth, room routing
│   ├── room.js       # YjsRoom class — one Y.Doc per room, sync + awareness logic
│   └── auth.js       # JWT verification using shared secret
├── test/
│   └── test-collab.html  # Browser test page for manual two-tab sync testing
├── .env.example
├── .nvmrc            # Node.js version (22)
└── package.json
```

## Prerequisites

- **Node.js 22** — the service uses native ESM (`import`/`export`). Node 14/16 will not work.

If you use [nvm](https://github.com/nvm-sh/nvm):

```bash
nvm install 22
nvm use 22   # or: nvm use  (reads .nvmrc automatically)
```

## Setup

```bash
cd collaboration-service

# Install dependencies
npm install

# Copy environment file and fill in values
cp .env.example .env
```

### Environment variables (`.env`)

| Variable     | Description                                      | Default                                   |
|--------------|--------------------------------------------------|-------------------------------------------|
| `PORT`       | Port the WebSocket server listens on             | `4000`                                    |
| `JWT_SECRET` | Shared secret used to verify JWTs from user-service | `muc-tieu-luong-thang-2-tram-trieu-dong` |

The `JWT_SECRET` must match the secret configured in the user-service (Spring Boot `JwtService`).

## Running the server

```bash
# Production
npm start

# Development (auto-restarts on file change)
npm run dev
```

The server exposes:
- `ws://localhost:4000/<roomId>?token=<JWT>` — WebSocket endpoint
- `GET http://localhost:4000/health` — health check, returns `{"status":"ok"}`

## WebSocket URL format

The room ID goes in the **path**, not as a query parameter. The JWT token goes as a query parameter named `token`:

```
ws://localhost:4000/room-abc-123?token=eyJhbGci...
```

This matches the convention used by [y-websocket](https://github.com/yjs/y-websocket)'s `WebsocketProvider`.

## JWT format

The service expects JWTs signed with HS256. The `sub` claim should be the user's email (matches Spring Boot's `JwtService`). Expired or invalid tokens are rejected with close code `4001`.

## Manual testing

A standalone browser test page is provided in `test/test-collab.html`. It connects to the local server and binds a textarea to a shared Yjs document.

1. Start the collaboration server (`npm run dev`)
2. Serve the test page over HTTP (required — `file://` URLs block WebSocket connections):

```bash
npx serve test -p 8888
# or
python3 -m http.server 8888 --directory test
```

3. Open `http://localhost:8888/test-collab.html?roomId=test-room-1&token=<JWT>` in two browser tabs
4. Type in one tab — changes should appear instantly in the other

The HTML file has a baked-in test token for convenience. Replace it with a valid JWT from the user-service for proper auth testing.

## Dependencies

| Package        | Purpose                                               |
|----------------|-------------------------------------------------------|
| `ws`           | Node.js WebSocket server                              |
| `yjs`          | CRDT document model (`Y.Doc`, `Y.Text`)               |
| `y-protocols`  | Binary Yjs sync and awareness protocol implementation |
| `lib0`         | Low-level binary encoding/decoding utilities (used by Yjs) |
| `jsonwebtoken` | JWT verification                                      |
| `dotenv`       | Load `.env` into `process.env`                        |
