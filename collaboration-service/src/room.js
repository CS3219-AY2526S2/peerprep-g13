import * as Y from "yjs";
import * as syncProtocol from "y-protocols/sync";
import * as awarenessProtocol from "y-protocols/awareness";
import * as encoding from "lib0/encoding";
import * as decoding from "lib0/decoding";

const MESSAGE_SYNC = 0;
const MESSAGE_AWARENESS = 1;

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
      console.log(`[Room ${this.roomId}] doc updated, broadcasting to ${this.clients.size - 1} other client(s)`);
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

    // When awareness changes, broadcast to all connected clients.
    this.awareness.on("update", ({ added, updated, removed }) => {
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
    this.clients.set(ws, { userId });
    console.log(
      `[Room ${this.roomId}] User ${userId} joined. Total: ${this.clients.size}`
    );

    // Send sync step 1 so the new client can catch up to the current doc state
    const syncEncoder = encoding.createEncoder();
    encoding.writeVarUint(syncEncoder, MESSAGE_SYNC);
    syncProtocol.writeSyncStep1(syncEncoder, this.doc);
    ws.send(encoding.toUint8Array(syncEncoder));

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
      const syncType = syncProtocol.readSyncMessage(decoder, encoder, this.doc, ws);
      console.log(`[Room ${this.roomId}] sync subtype=${syncType} (0=step1, 1=step2, 2=update)`);
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
    }
  }

  /**
   * Removes a client. Returns true if the room is now empty.
   */
  removeClient(ws) {
    const client = this.clients.get(ws);
    if (client) {
      awarenessProtocol.removeAwarenessStates(
        this.awareness,
        [this.doc.clientID],
        "client disconnected"
      );
      this.clients.delete(ws);
      console.log(
        `[Room ${this.roomId}] User ${client.userId} left. Remaining: ${this.clients.size}`
      );
    }
    return this.clients.size === 0;
  }

  destroy() {
    this.doc.destroy();
    this.awareness.destroy();
    console.log(`[Room ${this.roomId}] Destroyed.`);
  }
}
