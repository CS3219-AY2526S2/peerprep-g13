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
    room.destroy();
    rooms.delete(roomId);
  }
}

export function setupWebSocketServer(httpServer) {
  const wss = new WebSocketServer({ server: httpServer });

  wss.on("connection", (ws, req) => {
    const url = new URL(req.url, `http://${req.headers.host}`);
    const token = url.searchParams.get("token");
    // y-websocket puts the room name in the path: ws://host/<roomId>?token=...
    const roomId = url.pathname.slice(1); // strip leading "/"

    // Validate inputs
    if (!token || !roomId) {
      ws.close(4000, "Missing token or roomId");
      return;
    }

    // Validate JWT
    const payload = verifyToken(token);
    if (!payload) {
      ws.close(4001, "Invalid or expired token");
      return;
    }

    const userId = payload.sub; // Spring Boot sets sub = email
    console.log(`[WS] User ${userId} connecting to room ${roomId}`);

    const room = getOrCreateRoom(roomId);
    room.addClient(ws, userId);

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
