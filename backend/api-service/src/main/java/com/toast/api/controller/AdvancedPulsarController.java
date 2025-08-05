package com.toast.api.controller;

import com.toast.api.service.AdvancedPulsarService;
import com.toast.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/pulsar/advanced")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tag(name = "Advanced Pulsar Operations", description = "Advanced Pulsar client operations with batching, schemas, and more")
@Slf4j
public class AdvancedPulsarController {

    private final AdvancedPulsarService advancedPulsarService;

    @PostMapping("/batch")
    @Operation(summary = "Send batch messages", description = "Send multiple messages in batch to Pulsar")
    public ResponseEntity<ApiResponse<String>> sendBatchMessages(@RequestBody String[] messages) {
        try {
            CompletableFuture<Void> future = advancedPulsarService.sendBatchMessages(messages);
            future.get();
            return ResponseEntity.ok(ApiResponse.success("Batch messages sent successfully", 
                    "Sent " + messages.length + " messages"));
        } catch (Exception e) {
            log.error("Error sending batch messages", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send batch messages: " + e.getMessage()));
        }
    }

    @PostMapping("/with-properties")
    @Operation(summary = "Send message with properties", description = "Send a message with custom properties")
    public ResponseEntity<ApiResponse<String>> sendMessageWithProperties(
            @RequestBody String message,
            @RequestParam Map<String, String> properties) {
        try {
            CompletableFuture<MessageId> future = advancedPulsarService.sendMessageWithProperties(message, properties);
            MessageId messageId = future.get();
            return ResponseEntity.ok(ApiResponse.success("Message with properties sent successfully", 
                    messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending message with properties", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    @PostMapping("/with-delay")
    @Operation(summary = "Send message with delay", description = "Send a message that will be delivered after a delay")
    public ResponseEntity<ApiResponse<String>> sendMessageWithDelay(
            @RequestBody String message,
            @RequestParam long delaySeconds) {
        try {
            CompletableFuture<MessageId> future = advancedPulsarService.sendMessageWithDelay(message, delaySeconds);
            MessageId messageId = future.get();
            return ResponseEntity.ok(ApiResponse.success("Delayed message sent successfully", 
                    messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending delayed message", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send delayed message: " + e.getMessage()));
        }
    }

    @PostMapping("/with-schema")
    @Operation(summary = "Send message with schema", description = "Send a message with schema validation")
    public ResponseEntity<ApiResponse<String>> sendMessageWithSchema(@RequestBody String message) {
        try {
            CompletableFuture<MessageId> future = advancedPulsarService.sendMessageWithSchema(message);
            MessageId messageId = future.get();
            return ResponseEntity.ok(ApiResponse.success("Message with schema sent successfully", 
                    messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending message with schema", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send message with schema: " + e.getMessage()));
        }
    }

    @PostMapping("/compressed")
    @Operation(summary = "Send compressed message", description = "Send a message with compression")
    public ResponseEntity<ApiResponse<String>> sendCompressedMessage(@RequestBody String message) {
        try {
            CompletableFuture<MessageId> future = advancedPulsarService.sendCompressedMessage(message);
            MessageId messageId = future.get();
            return ResponseEntity.ok(ApiResponse.success("Compressed message sent successfully", 
                    messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending compressed message", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send compressed message: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/{topicName}")
    @Operation(summary = "Get topic stats", description = "Get statistics for a specific topic")
    public ResponseEntity<ApiResponse<Object>> getTopicStats(@PathVariable String topicName) {
        try {
            CompletableFuture<Object> future = advancedPulsarService.getTopicStats(topicName);
            Object stats = future.get();
            return ResponseEntity.ok(ApiResponse.success("Topic stats retrieved", stats));
        } catch (Exception e) {
            log.error("Error getting topic stats", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get topic stats: " + e.getMessage()));
        }
    }

    @PostMapping("/start-consumer")
    @Operation(summary = "Start consumer with handler", description = "Start consuming messages with custom handler")
    public ResponseEntity<ApiResponse<String>> startConsumer() {
        try {
            advancedPulsarService.consumeMessagesWithHandler(message -> {
                log.info("Processing message: {}", new String(message.getData()));
                // Add your custom processing logic here
            });
            return ResponseEntity.ok(ApiResponse.success("Consumer started successfully", 
                    "Consumer is now listening for messages"));
        } catch (Exception e) {
            log.error("Error starting consumer", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to start consumer: " + e.getMessage()));
        }
    }

    @PostMapping("/start-reader")
    @Operation(summary = "Start reader from position", description = "Start reading messages from a specific position")
    public ResponseEntity<ApiResponse<String>> startReader() {
        try {
            advancedPulsarService.readMessagesFromPosition(message -> {
                log.info("Reading message from position: {}", new String(message.getData()));
                // Add your custom processing logic here
            });
            return ResponseEntity.ok(ApiResponse.success("Reader started successfully", 
                    "Reader is now reading messages from position"));
        } catch (Exception e) {
            log.error("Error starting reader", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to start reader: " + e.getMessage()));
        }
    }

    @PostMapping("/example-user-event")
    @Operation(summary = "Send example user event", description = "Send a complete example user event with all features")
    public ResponseEntity<ApiResponse<String>> sendExampleUserEvent(@RequestBody UserEventExample userEvent) {
        try {
            // Create properties for the message
            Map<String, String> properties = new HashMap<>();
            properties.put("eventType", userEvent.getEventType());
            properties.put("userId", userEvent.getUserId());
            properties.put("timestamp", userEvent.getTimestamp());
            properties.put("source", "toast-api");

            // Create the message
            String message = String.format("User event: %s - User: %s - Action: %s - Data: %s", 
                    userEvent.getEventType(), 
                    userEvent.getUserId(), 
                    userEvent.getAction(),
                    userEvent.getData());

            // Send with properties
            CompletableFuture<MessageId> future = advancedPulsarService.sendMessageWithProperties(message, properties);
            MessageId messageId = future.get();

            return ResponseEntity.ok(ApiResponse.success("Example user event sent successfully", 
                    messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending example user event", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send example user event: " + e.getMessage()));
        }
    }

    // Inner class for example user events
    public static class UserEventExample {
        private String eventType;
        private String userId;
        private String action;
        private String data;
        private String timestamp;

        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
} 