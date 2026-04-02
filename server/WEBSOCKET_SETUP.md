# WebSocket Configuration Guide

## Overview
The PeerPrep server uses Spring WebSocket with STOMP (Simple Text Oriented Messaging Protocol) to enable real-time bidirectional communication between clients and the server. This is used for live match notifications, messages, and timeout alerts.

---

## Architecture Components

### 1. **WebSocketConfig** (Main Configuration)
**File:** `config/WebSocketConfig.java`

Configures the STOMP message broker and WebSocket endpoints.

**Key Settings:**
- **Endpoint:** `/ws` - WebSocket connection point
- **Origin Patterns:** `*` - Allows connections from any origin (with SockJS)
- **Fallback:** SockJS - Provides fallback for browsers without WebSocket support
- **Message Broker:** Simple in-memory broker
  - **Destinations:**
    - `/topic/*` - Public topics (one-to-many)
    - `/queue/*` - User-specific queues (one-to-one)
  - **Prefix:** `/app` - Routes user messages to controllers

### 2. **HttpHandshakeInterceptor** (Authentication)
**File:** `config/HttpHandshakeInterceptor.java`

Extracts `userId` from WebSocket URL query parameters during the handshake.

**Usage:**
```
ws://localhost:8082/ws?userId=123
```

The `userId` is stored in the WebSocket session attributes for later use.

### 3. **WebSocketAuthChannelInterceptor** (Message Interceptor)
**File:** `config/WebSocketAuthChannelInterceptor.java`

Intercepts STOMP messages before they're processed. Sets the `userId` as the principal for each message.

**Flow:**
1. Client connects with `userId` in handshake
2. Attributes are captured in session
3. On first CONNECT message, `userId` is extracted
4. User principal is set for routing purposes

### 4. **UserPrincipal** (Principal Implementation)
**File:** `config/UserPrincipal.java`

Simple implementation of `java.security.Principal` to represent the WebSocket user.

---

## Message Flow

### Sending Messages to Frontend

**Backend:**
```java
// MessageService.java
messagingTemplate.convertAndSendToUser(
    userId,              // User-specific destination
    "/queue/match",      // Queue path
    messageContent       // Payload
);
```

**Frontend Receives:**
```javascript
stompClient.subscribe('/user/queue/match', function(message) {
    const data = JSON.parse(message.body);
    console.log('Received:', data);
});
```

### Types of Messages

1. **Match Found:**
   - Sent to both users when a match is created
   - Contains: `userId1`, `userId2`, `questionId`, `matchInfo`

2. **Match Timeout:**
   - Sent after 30 seconds of waiting
   - Message: "No match found"

3. **Match Cancelled:**
   - Sent when a user cancels their match request
   - Message: "Match cancelled"

---

## Frontend Connection Guide

### Setup (SockJS + Stomp.js)

```html
<!-- Include libraries -->
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
```

### Connect to WebSocket

```javascript
// Establish WebSocket connection with userId
const userId = 152; // Example user ID
const socket = new SockJS(`http://localhost:8082/ws?userId=${userId}`);
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to match messages
    stompClient.subscribe('/user/queue/match', function(message) {
        handleMatchMessage(message.body);
    });
}, function(error) {
    console.error('Connection error:', error);
});

function handleMatchMessage(data) {
    const message = JSON.parse(data);
    if (message === 'No match found') {
        console.log('Timeout: No match found');
    } else if (message === 'Match cancelled') {
        console.log('Match was cancelled');
    } else {
        console.log('Match found:', message);
    }
}
```

### Disconnect

```javascript
stompClient.disconnect(function() {
    console.log('Disconnected');
});
```

---

## Database/Queue Flow

### RabbitMQ Integration

1. **User joins match:**
   - Frontend: POST `/match/join` with `userId`, `topic`, `difficulty`
   - Backend: `ProducerService.sendJoinRequest()` → RabbitMQ

2. **Matching process:**
   - `ConsumerService` listens on RabbitMQ queue
   - `MatchingPool` (Redis) finds available matches
   - `MessageService` sends WebSocket message if found

3. **User leaves:**
   - Frontend: POST `/match/leave` with `userId`
   - `ProducerService.sendCancelRequest()` → RabbitMQ
   - Message sent to user via WebSocket

---

## Server Configuration

### application.properties

```properties
# Server
server.port=8082

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Redis (for matching pool)
spring.redis.host=localhost
spring.redis.port=6379

# Queue Configuration
rabbitmq.queue.name=peerprep-queue
rabbitmq.exchange.name=peerprep-exchange
rabbitmq.routing.key=peerprep-routing-key
```

---

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/match/join` | User joins a match queue |
| POST | `/match/join/loose` | User joins with relaxed matching criteria |
| POST | `/match/leave` | User cancels their match request |
| WS | `/ws?userId=X` | WebSocket STOMP connection |
| SUB | `/user/queue/match` | Receive match notifications |

---

## Debugging

### Check Connection Status

```javascript
// In browser console
stompClient.connected // true if connected
```

### Monitor RabbitMQ

```bash
# RabbitMQ Management UI
http://localhost:15672
# Login: guest / guest
```

### View Redis Keys

```bash
redis-cli
> KEYS *
```

### Server Logs

```bash
# Enable WebSocket debug logging
logging.level.org.springframework.web.socket=DEBUG
logging.level.org.springframework.messaging=DEBUG
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| WebSocket connection fails | Ensure server is on 8082, check CORS, verify SockJS endpoint |
| Messages not received | Check frontend subscriptions, verify userId in URL, check RabbitMQ |
| CONNECT fails | Verify userId parameter is present in WebSocket URL |
| Timeout not working | Check if Redis and RabbitMQ are running |
| 403 Forbidden on REST endpoints | Verify JWT token in Authorization header |

---

## Summary

The WebSocket configuration chains together multiple components:

```
Browser → HttpHandshakeInterceptor (extracts userId)
       ↓
       → WebSocketAuthChannelInterceptor (sets principal)
       ↓
       → MessageService (sends via SimpMessagingTemplate)
       ↓
       → RabbitMQ ↔ Redis (matching logic)
       ↓
       → Browser (receives on /user/queue/match)
```

This ensures real-time bidirectional communication with proper user isolation and message routing.

