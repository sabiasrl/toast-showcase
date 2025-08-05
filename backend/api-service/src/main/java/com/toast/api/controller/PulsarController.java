package com.toast.api.controller;

import com.toast.api.service.PulsarService;
import com.toast.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/pulsar")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tag(name = "Pulsar Operations", description = "Direct Pulsar client operations")
@Slf4j
public class PulsarController {

    private final PulsarService pulsarService;

    @PostMapping("/send")
    @Operation(summary = "Send message to Pulsar", description = "Send a message to the user-events topic")
    public ResponseEntity<ApiResponse<String>> sendMessage(@RequestBody String message) {
        try {
            CompletableFuture<MessageId> future = pulsarService.sendMessage(message);
            MessageId messageId = future.get();
            return ResponseEntity.ok(ApiResponse.success("Message sent successfully", messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending message to Pulsar", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    @PostMapping("/send-sync")
    @Operation(summary = "Send message synchronously", description = "Send a message synchronously to the user-events topic")
    public ResponseEntity<ApiResponse<String>> sendMessageSync(@RequestBody String message) {
        try {
            MessageId messageId = pulsarService.sendMessageSync(message);
            return ResponseEntity.ok(ApiResponse.success("Message sent successfully", messageId.toString()));
        } catch (PulsarClientException e) {
            log.error("Error sending message to Pulsar", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    @PostMapping("/send-with-key")
    @Operation(summary = "Send message with key", description = "Send a message with a specific key to the user-events topic")
    public ResponseEntity<ApiResponse<String>> sendMessageWithKey(
            @RequestParam String key,
            @RequestBody String message) {
        try {
            CompletableFuture<MessageId> future = pulsarService.sendMessageWithKey(key, message);
            MessageId messageId = future.get();
            return ResponseEntity.ok(ApiResponse.success("Message sent successfully", messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending message with key to Pulsar", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/producer")
    @Operation(summary = "Get producer stats", description = "Get statistics for the Pulsar producer")
    public ResponseEntity<ApiResponse<Object>> getProducerStats() {
        try {
            Object stats = pulsarService.getProducerStats();
            return ResponseEntity.ok(ApiResponse.success("Producer stats retrieved", stats));
        } catch (PulsarClientException e) {
            log.error("Error getting producer stats", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get producer stats: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/consumer")
    @Operation(summary = "Get consumer stats", description = "Get statistics for the Pulsar consumer")
    public ResponseEntity<ApiResponse<Object>> getConsumerStats() {
        try {
            Object stats = pulsarService.getConsumerStats();
            return ResponseEntity.ok(ApiResponse.success("Consumer stats retrieved", stats));
        } catch (PulsarClientException e) {
            log.error("Error getting consumer stats", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get consumer stats: " + e.getMessage()));
        }
    }

    @PostMapping("/send-user-event")
    @Operation(summary = "Send user event", description = "Send a user event message to Pulsar")
    public ResponseEntity<ApiResponse<String>> sendUserEvent(@RequestBody UserEvent userEvent) {
        try {
            String message = String.format("User event: %s - User: %s - Action: %s", 
                    userEvent.getEventType(), userEvent.getUserId(), userEvent.getAction());
            
            CompletableFuture<MessageId> future = pulsarService.sendMessageWithKey(
                    userEvent.getUserId(), message);
            MessageId messageId = future.get();
            
            return ResponseEntity.ok(ApiResponse.success("User event sent successfully", messageId.toString()));
        } catch (Exception e) {
            log.error("Error sending user event to Pulsar", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send user event: " + e.getMessage()));
        }
    }

    // Inner class for user events
    public static class UserEvent {
        private String eventType;
        private String userId;
        private String action;
        private String timestamp;

        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
} 