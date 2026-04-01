import { WebSocketServer } from "ws";
import { verifyToken } from "./auth.js";
import { YjsRoom } from "./room.js";

/** @type {Map<string, YjsRoom>} */
const rooms = new Map();

function getOrCreateRoom(roomId) {
  if (!rooms.has(roomId)) {
    rooms.set(roomId, new YjsRoom(roomId));
  }
  return rooms.get(roomId);
}

function deleteRoomIfEmpty(roomId) {
  const room = rooms.get(roomId);
  if (room && room.clients.size === 0) {
    // Grace period: keep room alive for 30s so a page reload doesn't lose content
    setTimeout(() => {
      const r = rooms.get(roomId);
      if (r && r.clients.size === 0) {
        r.destroy();
        rooms.delete(roomId);
        console.log(`[Room ${roomId}] Destroyed after grace period.`);
      }
    }, 30_000);
  }
}

export function setupWebSocketServer(httpServer) {
  const wss = new WebSocketServer({ server: httpServer });

  wss.on("connection", (ws, req) => {
    const url = new URL(req.url, `http://${req.headers.host}`);
    const token = url.searchParams.get("token");
    // y-websocket puts the room name in the path: ws://host/<roomId>?token=...
    const roomId = url.pathname.slice(1); // strip leading "/"

    console.log(`[WS] Incoming connection — roomId="${roomId}" hasToken=${!!token}`);

    // Validate inputs
    if (!token || !roomId) {
      ws.close(4000, "Missing token or roomId");
      console.log(`[WS] Rejected: missing token or roomId`);
      return;
    }

    // Validate JWT
    const payload = verifyToken(token);
    if (!payload) {
      ws.close(4001, "Invalid or expired token");
      console.log(`[WS] Rejected: JWT verification failed (check JWT_SECRET matches user-service)`);
      return;
    }

    const userId = payload.sub; // Spring Boot sets sub = email
    console.log(`[WS] ✓ User "${userId}" authenticated, joining room "${roomId}"`);

    const room = getOrCreateRoom(roomId);

    // Count unique userIds already in the room (same userId = same person, allow reconnects)
    const uniqueUsers = new Set([...room.clients.values()].map((m) => m.userId));
    console.log(`[WS] Room "${roomId}" current unique users: [${[...uniqueUsers].join(", ")}]`);

    if (!uniqueUsers.has(userId) && uniqueUsers.size >= 2) {
      ws.close(4003, "Room is full (max 2 users)");
      console.log(`[WS] Room "${roomId}" is full. Rejected user "${userId}"`);
      return;
    }

    room.addClient(ws, userId);
    const newUniqueUsers = new Set([...room.clients.values()].map((m) => m.userId));
    console.log(`[WS] Room "${roomId}" users after join: [${[...newUniqueUsers].join(", ")}] (${newUniqueUsers.size} unique)`);

    ws.on("message", (message) => {
      try {
        room.handleMessage(ws, message);
      } catch (err) {
        console.error(`[WS] Error handling message in room ${roomId}:`, err);
      }
    });

    ws.on("close", () => {
      room.removeClient(ws);
      deleteRoomIfEmpty(roomId);
    });

    ws.on("error", (err) => {
      console.error(`[WS] Socket error for user ${userId}:`, err);
      room.removeClient(ws);
      deleteRoomIfEmpty(roomId);
    });
  });

  console.log("[WS] WebSocket server attached.");
  return wss;
}
