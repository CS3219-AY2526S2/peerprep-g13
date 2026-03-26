import "dotenv/config";
import http from "http";
import { setupWebSocketServer } from "./server.js";

const PORT = process.env.PORT || 4000;

const httpServer = http.createServer((req, res) => {
  if (req.url === "/health") {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "ok" }));
    return;
  }
  res.writeHead(404);
  res.end();
});

setupWebSocketServer(httpServer);

httpServer.listen(PORT, () => {
  console.log(`[Server] Collaboration service running on port ${PORT}`);
});
