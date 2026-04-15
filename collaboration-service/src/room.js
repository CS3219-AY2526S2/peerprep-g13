import * as Y from "yjs";
import * as syncProtocol from "y-protocols/sync";
import * as awarenessProtocol from "y-protocols/awareness";
import * as encoding from "lib0/encoding";
import * as decoding from "lib0/decoding";

const MESSAGE_SYNC = 0;
const MESSAGE_AWARENESS = 1;
const MESSAGE_END_SESSION = 2;

export class YjsRoom {
  constructor(roomId) {
    this.roomId = roomId;
    this.doc = new Y.Doc();
    this.awareness = new awarenessProtocol.Awareness(this.doc);
    /** @type {Map<WebSocket, { userId: string }>} */
    this.clients = new Map();

    // When the doc is updated by any client, broadcast the update to all others.
    // `origin` is the ws that triggered the update (passed via transactionOrigin).
    this.doc.on("update", (update, origin) => {
      const originUserId = origin ? this.clients.get(origin)?.userId : "unknown";
      const recipients = [...this.clients.keys()].filter((ws) => ws !== origin && ws.readyState === 1);
      console.log(`[Room ${this.roomId}] doc update from "${originUserId}", broadcasting to ${recipients.length} other client(s)`);
      const encoder = encoding.createEncoder();
      encoding.writeVarUint(encoder, MESSAGE_SYNC);
      syncProtocol.writeUpdate(encoder, update);
      const message = encoding.toUint8Array(encoder);
      this.clients.forEach((_, clientWs) => {
        if (clientWs !== origin && clientWs.readyState === 1 /* OPEN */) {
          clientWs.send(message);
        }
      });
    });

    // When awareness changes, track clientIds per socket and broadcast.
    this.awareness.on("update", ({ added, updated, removed }, origin) => {
      // Map the awareness clientIds back to the originating WebSocket
      if (origin && this.clients.has(origin)) {
        const clientMeta = this.clients.get(origin);
        [...added, ...updated].forEach((id) => clientMeta.awarenessClientIds.add(id));
      }
      const allStates = this.awareness.getStates();
      const userIds = [...allStates.values()].map((s) => s?.user?.userId).filter(Boolean);
      console.log(`[Room ${this.roomId}] awareness update — added:${added.length} updated:${updated.length} removed:${removed.length} | all userIds in room: [${userIds.join(", ")}]`);
      const changedClients = added.concat(updated, removed);
      const encoder = encoding.createEncoder();
      encoding.writeVarUint(encoder, MESSAGE_AWARENESS);
      // encodeAwarenessUpdate returns a Uint8Array — write it as a variable-length byte array
      encoding.writeVarUint8Array(
        encoder,
        awarenessProtocol.encodeAwarenessUpdate(this.awareness, changedClients)
      );
      const message = encoding.toUint8Array(encoder);
      this.clients.forEach((_, clientWs) => {
        if (clientWs.readyState === 1 /* OPEN */) {
          clientWs.send(message);
        }
      });
    });
  }

  /**
   * Adds a client and performs initial Yjs sync.
   */
  addClient(ws, userId) {
    this.clients.set(ws, { userId, awarenessClientIds: new Set() });
    const allUsers = [...this.clients.values()].map((m) => m.userId);
    console.log(`[Room ${this.roomId}] ✓ User "${userId}" joined. All connections: [${allUsers.join(", ")}] (${this.clients.size} total)`);

    // Send sync step 1 so the new client can catch up to the current doc state
    const syncEncoder = encoding.createEncoder();
    encoding.writeVarUint(syncEncoder, MESSAGE_SYNC);
    syncProtocol.writeSyncStep1(syncEncoder, this.doc);
    ws.send(encoding.toUint8Array(syncEncoder));

    // Also push the full doc state immediately so the client doesn't have to
    // wait for a SyncStep1→SyncStep2 round-trip before seeing room-state data
    // (e.g. questionId written before this client connected).
    const fullUpdate = Y.encodeStateAsUpdate(this.doc);
    if (fullUpdate.length > 2) {
      const updateEncoder = encoding.createEncoder();
      encoding.writeVarUint(updateEncoder, MESSAGE_SYNC);
      syncProtocol.writeUpdate(updateEncoder, fullUpdate);
      ws.send(encoding.toUint8Array(updateEncoder));
    }

    // Send current awareness states to the new client
    const awarenessStates = this.awareness.getStates();
    if (awarenessStates.size > 0) {
      const awarenessEncoder = encoding.createEncoder();
      encoding.writeVarUint(awarenessEncoder, MESSAGE_AWARENESS);
      encoding.writeVarUint8Array(
        awarenessEncoder,
        awarenessProtocol.encodeAwarenessUpdate(
          this.awareness,
          Array.from(awarenessStates.keys())
        )
      );
      ws.send(encoding.toUint8Array(awarenessEncoder));
    }
  }

  /**
   * Handles an incoming binary message from a client.
   */
  handleMessage(ws, message) {
    const decoder = decoding.createDecoder(new Uint8Array(message));
    const messageType = decoding.readVarUint(decoder);
    console.log(`[Room ${this.roomId}] received messageType=${messageType} (0=sync, 1=awareness)`);

    if (messageType === MESSAGE_SYNC) {
      const encoder = encoding.createEncoder();
      encoding.writeVarUint(encoder, MESSAGE_SYNC);
      // Pass `ws` as transactionOrigin so the doc.on('update') handler
      // knows which client sent this and can skip broadcasting back to them
      const userId = this.clients.get(ws)?.userId ?? "unknown";
      const syncType = syncProtocol.readSyncMessage(decoder, encoder, this.doc, ws);
      console.log(`[Room ${this.roomId}] sync from "${userId}" subtype=${syncType} (0=step1, 1=step2, 2=update)`);
      // Send response (e.g. sync step 2) back to this client if there's data
      if (encoding.length(encoder) > 1) {
        ws.send(encoding.toUint8Array(encoder));
      }
    } else if (messageType === MESSAGE_AWARENESS) {
      awarenessProtocol.applyAwarenessUpdate(
        this.awareness,
        decoding.readVarUint8Array(decoder),
        ws
      );
    } else if (messageType === MESSAGE_END_SESSION) {
      const userId = this.clients.get(ws)?.userId ?? "unknown";
      console.log(`[Room ${this.roomId}] User "${userId}" ended the session — notifying others.`);
      this.broadcastEndSession(ws);
    }
  }

  /**
   * Sends a MESSAGE_END_SESSION signal to all clients except the sender.
   */
  broadcastEndSession(senderWs) {
    const encoder = encoding.createEncoder();
    encoding.writeVarUint(encoder, MESSAGE_END_SESSION);
    const message = encoding.toUint8Array(encoder);
    this.clients.forEach((_, clientWs) => {
      if (clientWs !== senderWs && clientWs.readyState === 1 /* OPEN */) {
        clientWs.send(message);
      }
    });
  }

  /**
   * Removes a client. Returns true if the room is now empty.
   */
  removeClient(ws) {
    const client = this.clients.get(ws);
    if (client) {
      if (client.awarenessClientIds.size > 0) {
        awarenessProtocol.removeAwarenessStates(
          this.awareness,
          Array.from(client.awarenessClientIds),
          "client disconnected"
        );
      }
      this.clients.delete(ws);
      const remaining = [...this.clients.values()].map((m) => m.userId);
      console.log(`[Room ${this.roomId}] User "${client.userId}" left. Remaining: [${remaining.join(", ")}] (${this.clients.size})`);
    }
    return this.clients.size === 0;
  }

  destroy() {
    this.doc.destroy();
    this.awareness.destroy();
    console.log(`[Room ${this.roomId}] Destroyed.`);
  }
}
