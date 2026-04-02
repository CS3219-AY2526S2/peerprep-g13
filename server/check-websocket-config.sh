#!/usr/bin/env bash
# WebSocket Configuration Troubleshooting Script

echo "==================================="
echo "PeerPrep WebSocket Configuration Check"
echo "==================================="
echo ""

# Check 1: Server Port
echo "✓ Checking server configuration..."
if grep -q "server.port=8082" src/main/resources/application.properties; then
    echo "   ✅ Server port: 8082"
else
    echo "   ❌ Server port not set to 8082"
    echo "      Fix: Change 'server.port' to 8082 in application.properties"
fi

# Check 2: RabbitMQ Config
echo ""
echo "✓ Checking RabbitMQ configuration..."
if grep -q "spring.rabbitmq.host=localhost" src/main/resources/application.properties; then
    echo "   ✅ RabbitMQ: localhost:5672"
else
    echo "   ⚠️  RabbitMQ config not found"
fi

# Check 3: Redis Config
echo ""
echo "✓ Checking Redis configuration..."
if grep -q "spring.redis.host=localhost" src/main/resources/application.properties; then
    echo "   ✅ Redis: localhost:6379"
else
    echo "   ⚠️  Redis config not found"
fi

# Check 4: WebSocket Files
echo ""
echo "✓ Checking WebSocket configuration files..."
files=(
    "src/main/java/com/g13cs3219/server/config/WebSocketConfig.java"
    "src/main/java/com/g13cs3219/server/config/HttpHandshakeInterceptor.java"
    "src/main/java/com/g13cs3219/server/config/WebSocketAuthChannelInterceptor.java"
    "src/main/java/com/g13cs3219/server/config/UserPrincipal.java"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $(basename $file)"
    else
        echo "   ❌ $(basename $file) - NOT FOUND"
    fi
done

# Check 5: Message Service
echo ""
echo "✓ Checking Message Service..."
if [ -f "src/main/java/com/g13cs3219/server/services/MessageService.java" ]; then
    echo "   ✅ MessageService.java found"
    if grep -q "SimpMessagingTemplate" src/main/java/com/g13cs3219/server/services/MessageService.java; then
        echo "   ✅ SimpMessagingTemplate configured"
    fi
else
    echo "   ❌ MessageService.java not found"
fi

# Check 6: Services
echo ""
echo "✓ Checking required services..."
services=(
    "src/main/java/com/g13cs3219/server/services/ProducerService.java"
    "src/main/java/com/g13cs3219/server/services/ConsumerService.java"
    "src/main/java/com/g13cs3219/server/repositories/MatchingPool.java"
)

for service in "${services[@]}"; do
    if [ -f "$service" ]; then
        echo "   ✅ $(basename $service)"
    else
        echo "   ❌ $(basename $service) - NOT FOUND"
    fi
done

# Runtime Check
echo ""
echo "==================================="
echo "Runtime Requirements"
echo "==================================="
echo ""

# Check RabbitMQ
echo "Checking external services:"
if nc -z localhost 5672 2>/dev/null; then
    echo "   ✅ RabbitMQ running (port 5672)"
else
    echo "   ❌ RabbitMQ NOT running"
    echo "      Start with: rabbitmq-server"
fi

# Check Redis
if nc -z localhost 6379 2>/dev/null; then
    echo "   ✅ Redis running (port 6379)"
else
    echo "   ❌ Redis NOT running"
    echo "      Start with: redis-server"
fi

# Check Server
if nc -z localhost 8082 2>/dev/null; then
    echo "   ✅ Server running (port 8082)"
else
    echo "   ⚠️  Server NOT running on 8082"
    echo "      Start with: mvn spring-boot:run"
fi

echo ""
echo "==================================="
echo "Summary"
echo "==================================="
echo ""
echo "To use WebSocket:"
echo "1. Ensure all files exist ✓"
echo "2. Start RabbitMQ, Redis, and server"
echo "3. Connect from frontend: ws://localhost:8082/ws?userId=YOUR_ID"
echo ""
echo "See WEBSOCKET_SETUP.md and WEBSOCKET_QUICK_REFERENCE.md for details"
echo ""

