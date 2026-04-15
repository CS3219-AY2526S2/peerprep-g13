import { WebSocketServer } from "ws";
import { verifyToken } from "./auth.js";
import { YjsRoom } from "./room.js";
import redis from "./redis.js";

/** @type {Map<string, YjsRoom>} */
const rooms = new Map();

function getRoom(roomId) {
  return rooms.get(roomId);
}

function getOrCreateRoom(roomId) {
  let room = getRoom(roomId);
  if (!room) {
    room = new YjsRoom(roomId);
    rooms.set(roomId, room);
  }
  return room;
}

async function deleteRoomIfEmpty(roomId) {
  const room = rooms.get(roomId);
  if (room && room.clients.size === 0) {
    // Grace period: keep room alive for 30s so a page reload doesn't lose content
    setTimeout(async () => {
      const r = rooms.get(roomId);
      if (r && r.clients.size === 0) {
        r.destroy();
        rooms.delete(roomId);
        // Permanently close the room in Redis so reconnects are rejected (Issue 3)
        try {
          await redis.del(`room:${roomId}`);
          console.log(`[Room ${roomId}] Destroyed after grace period and removed from Redis.`);
        } catch (error) {
          console.error(`[Room ${roomId}] Destroyed after grace period, but failed to remove from Redis.`, error);
        }
      }
    }, 30_000);
  }
}

export function setupWebSocketServer(httpServer) {
  const wss = new WebSocketServer({ server: httpServer });

  wss.on("connection", async (ws, req) => {
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
      console.log(`[WS] Rejected: JWT verification failed`);
      return;
    }

    const userEmail = payload.sub; // Spring Boot sets sub = email

    // Validate room exists in Redis (Issue 1 & 3)
    let roomData;
    try {
      const raw = await redis.get(`room:${roomId}`);
      if (!raw) {
        ws.close(4004, "Room not found. All rooms must be created through matching.");
        console.log(`[WS] Rejected: room "${roomId}" not found in Redis`);
        return;
      }
      roomData = JSON.parse(raw);
    } catch (err) {
      console.error(`[WS] Redis error for room "${roomId}":`, err.message);
      ws.close(4004, "Room not found. All rooms must be created through matching.");
      return;
    }

    // Validate participant (Issue 1)
    const isParticipant =
      userEmail === roomData.email1 || userEmail === roomData.email2;
    if (!isParticipant) {
      ws.close(4003, "Unauthorized: you are not a participant of this room");
      console.log(`[WS] Rejected: user "${userEmail}" is not a participant of room "${roomId}"`);
      return;
    }

    console.log(`[WS] ✓ User "${userEmail}" authorized for room "${roomId}"`);

    const room = getOrCreateRoom(roomId);

    // Write questionId into Yjs shared map "room-state" so all clients receive it
    // on initial sync (Issue 1). Set only once — idempotent on reconnect.
    if (roomData.questionId != null && !room.doc.getMap("room-state").has("questionId")) {
      room.doc.getMap("room-state").set("questionId", Number(roomData.questionId));
    }

    room.addClient(ws, userEmail);
    const allUsers = [...room.clients.values()].map((m) => m.userId);
    console.log(`[WS] Room "${roomId}" users after join: [${allUsers.join(", ")}]`);

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
      console.error(`[WS] Socket error for user ${userEmail}:`, err);
      room.removeClient(ws);
      deleteRoomIfEmpty(roomId);
    });
  });

  console.log("[WS] WebSocket server attached.");
  return wss;
}
