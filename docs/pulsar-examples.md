# Pulsar Direct Client Examples

This document provides comprehensive examples of using Apache Pulsar directly without Camel integration.

## Overview

The Toast project includes direct Pulsar client implementations that demonstrate:

- **Basic Pulsar Operations**: Simple message sending and receiving
- **Advanced Features**: Batching, schemas, compression, delayed messages
- **Different Subscription Types**: Exclusive, Shared, Failover
- **Message Properties**: Custom metadata and headers
- **Error Handling**: Proper exception handling and retries

## Services

### 1. PulsarService (Basic Operations)

Located in `backend/api-service/src/main/java/com/toast/api/service/PulsarService.java`

**Features:**
- Simple message sending (async and sync)
- Message sending with keys
- Background consumer
- Statistics retrieval

**Example Usage:**

```java
// Send a simple message
CompletableFuture<MessageId> future = pulsarService.sendMessage("Hello Pulsar!");
MessageId messageId = future.get();

// Send message synchronously
MessageId messageId = pulsarService.sendMessageSync("Hello Pulsar!");

// Send message with key
CompletableFuture<MessageId> future = pulsarService.sendMessageWithKey("user123", "User login event");
```

### 2. AdvancedPulsarService (Advanced Features)

Located in `backend/api-service/src/main/java/com/toast/api/service/AdvancedPulsarService.java`

**Features:**
- Batch message sending
- Message properties
- Delayed message delivery
- Schema validation
- Message compression
- Custom message handlers
- Message reading from specific positions

**Example Usage:**

```java
// Send batch messages
String[] messages = {"Message 1", "Message 2", "Message 3"};
CompletableFuture<Void> future = advancedPulsarService.sendBatchMessages(messages);

// Send message with properties
Map<String, String> properties = new HashMap<>();
properties.put("eventType", "user.login");
properties.put("userId", "12345");
CompletableFuture<MessageId> future = advancedPulsarService.sendMessageWithProperties("User logged in", properties);

// Send delayed message
CompletableFuture<MessageId> future = advancedPulsarService.sendMessageWithDelay("Delayed message", 60); // 60 seconds delay
```

## REST API Endpoints

### Basic Pulsar Operations

#### Send Message
```bash
# Send simple message
curl -X POST http://localhost:8080/api/v1/pulsar/send \
  -H "Content-Type: text/plain" \
  -d "Hello Pulsar!"

# Send message synchronously
curl -X POST http://localhost:8080/api/v1/pulsar/send-sync \
  -H "Content-Type: text/plain" \
  -d "Hello Pulsar!"

# Send message with key
curl -X POST "http://localhost:8080/api/v1/pulsar/send-with-key?key=user123" \
  -H "Content-Type: text/plain" \
  -d "User login event"
```

#### Send User Event
```bash
curl -X POST http://localhost:8080/api/v1/pulsar/send-user-event \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "user.login",
    "userId": "12345",
    "action": "login",
    "timestamp": "2024-01-01T12:00:00Z"
  }'
```

#### Get Statistics
```bash
# Get producer stats
curl http://localhost:8080/api/v1/pulsar/stats/producer

# Get consumer stats
curl http://localhost:8080/api/v1/pulsar/stats/consumer
```

### Advanced Pulsar Operations

#### Batch Messages
```bash
curl -X POST http://localhost:8080/api/v1/pulsar/advanced/batch \
  -H "Content-Type: application/json" \
  -d '["Message 1", "Message 2", "Message 3"]'
```

#### Message with Properties
```bash
curl -X POST "http://localhost:8080/api/v1/pulsar/advanced/with-properties?eventType=user.login&userId=12345&source=web" \
  -H "Content-Type: text/plain" \
  -d "User login event"
```

#### Delayed Message
```bash
curl -X POST "http://localhost:8080/api/v1/pulsar/advanced/with-delay?delaySeconds=60" \
  -H "Content-Type: text/plain" \
  -d "This message will be delivered in 60 seconds"
```

#### Message with Schema
```bash
curl -X POST http://localhost:8080/api/v1/pulsar/advanced/with-schema \
  -H "Content-Type: text/plain" \
  -d "Message with schema validation"
```

#### Compressed Message
```bash
curl -X POST http://localhost:8080/api/v1/pulsar/advanced/compressed \
  -H "Content-Type: text/plain" \
  -d "This message will be compressed"
```

#### Example User Event
```bash
curl -X POST http://localhost:8080/api/v1/pulsar/advanced/example-user-event \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "user.profile.update",
    "userId": "12345",
    "action": "update_profile",
    "data": "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}",
    "timestamp": "2024-01-01T12:00:00Z"
  }'
```

## Configuration

### Pulsar Client Configuration

The Pulsar client is configured in `PulsarConfig.java`:

```java
@Configuration
public class PulsarConfig {
    
    @Value("${pulsar.service-url:pulsar://localhost:6650}")
    private String pulsarServiceUrl;
    
    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(pulsarServiceUrl)
                .build();
    }
}
```

### Application Properties

Add to `application.yml`:

```yaml
pulsar:
  service-url: pulsar://localhost:6650
```

## Key Features Demonstrated

### 1. Message Batching
```java
// Enable batching in producer
Producer<byte[]> producer = pulsarClient.newProducer()
    .topic("batch-events")
    .enableBatching(true)
    .batchingMaxPublishDelay(10)
    .batchingMaxMessages(1000)
    .create();
```

### 2. Different Subscription Types
```java
// Exclusive subscription (default)
Consumer<String> consumer = pulsarClient.newConsumer(Schema.STRING)
    .topic("user-events")
    .subscriptionName("exclusive-sub")
    .subscriptionType(SubscriptionType.Exclusive)
    .subscribe();

// Shared subscription
Consumer<byte[]> sharedConsumer = pulsarClient.newConsumer()
    .topic("batch-events")
    .subscriptionName("shared-sub")
    .subscriptionType(SubscriptionType.Shared)
    .subscribe();
```

### 3. Message Properties
```java
// Add properties to messages
TypedMessageBuilder<byte[]> messageBuilder = producer.newMessage()
    .value(message.getBytes());

properties.forEach(messageBuilder::property);
return messageBuilder.sendAsync();
```

### 4. Delayed Message Delivery
```java
// Send message with delay
return producer.newMessage()
    .value(message.getBytes())
    .deliverAfter(delaySeconds, TimeUnit.SECONDS)
    .sendAsync();
```

### 5. Message Reading from Position
```java
// Create reader for specific position
Reader<byte[]> reader = pulsarClient.newReader()
    .topic("batch-events")
    .startMessageId(MessageId.earliest)
    .create();
```

## Error Handling

All Pulsar operations include proper error handling:

```java
public CompletableFuture<MessageId> sendMessage(String message) {
    try {
        log.info("Sending message to Pulsar: {}", message);
        return producer.sendAsync(message);
    } catch (Exception e) {
        log.error("Error sending message to Pulsar", e);
        CompletableFuture<MessageId> future = new CompletableFuture<>();
        future.completeExceptionally(e);
        return future;
    }
}
```

## Monitoring and Statistics

The services provide methods to retrieve statistics:

```java
// Get consumer stats
ConsumerStats stats = consumer.getStats();

// Get producer stats
ProducerStats stats = producer.getStats();
```

## Best Practices

1. **Always close resources**: Use `@PreDestroy` to properly close producers and consumers
2. **Handle exceptions**: Wrap Pulsar operations in try-catch blocks
3. **Use async operations**: Prefer `sendAsync()` for better performance
4. **Configure timeouts**: Set appropriate timeouts for operations
5. **Monitor statistics**: Regularly check producer and consumer stats
6. **Use message keys**: For ordered message processing
7. **Implement retries**: For failed operations
8. **Log operations**: Include proper logging for debugging

## Testing

You can test the Pulsar operations using the provided REST endpoints or by running the services and checking the logs for message processing.

## Troubleshooting

### Common Issues

1. **Connection refused**: Ensure Pulsar is running on the configured service URL
2. **Topic not found**: Topics are created automatically when first accessed
3. **Subscription issues**: Check subscription type and name
4. **Message delivery**: Verify consumer is properly configured and running

### Debugging

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.toast.api.service: DEBUG
    org.apache.pulsar: DEBUG
``` 