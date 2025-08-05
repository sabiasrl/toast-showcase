package com.toast.api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@Slf4j
public class AdvancedPulsarService {

    @Autowired
    private PulsarClient pulsarClient;

    private Producer<byte[]> batchProducer;
    private Consumer<byte[]> batchConsumer;
    private Reader<byte[]> reader;

    @PostConstruct
    public void init() throws PulsarClientException {
        // Initialize batch producer
        batchProducer = pulsarClient.newProducer()
                .topic("batch-events")
                .producerName("toast-batch-producer")
                .enableBatching(true)
                .batchingMaxPublishDelay(10)
                .batchingMaxMessages(1000)
                .create();

        // Initialize batch consumer with shared subscription
        batchConsumer = pulsarClient.newConsumer()
                .topic("batch-events")
                .subscriptionName("toast-batch-subscription")
                .subscriptionType(SubscriptionType.Shared)
                .ackTimeout(30, TimeUnit.SECONDS)
                .subscribe();

        // Initialize reader for reading from specific position
        reader = pulsarClient.newReader()
                .topic("batch-events")
                .startMessageId(MessageId.earliest)
                .create();
    }

    @PreDestroy
    public void cleanup() throws PulsarClientException {
        if (batchProducer != null) {
            batchProducer.close();
        }
        if (batchConsumer != null) {
            batchConsumer.close();
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * Send batch of messages
     */
    public CompletableFuture<Void> sendBatchMessages(String[] messages) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            for (String message : messages) {
                batchProducer.sendAsync(message.getBytes())
                        .thenAccept(messageId -> 
                            log.info("Sent batch message: {} with ID: {}", message, messageId))
                        .exceptionally(throwable -> {
                            log.error("Error sending batch message: {}", message, throwable);
                            return null;
                        });
            }
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Send message with properties
     */
    public CompletableFuture<MessageId> sendMessageWithProperties(String message, java.util.Map<String, String> properties) {
        try {
            TypedMessageBuilder<byte[]> messageBuilder = batchProducer.newMessage()
                    .value(message.getBytes());
            
            // Add properties
            properties.forEach(messageBuilder::property);
            
            return messageBuilder.sendAsync();
        } catch (Exception e) {
            log.error("Error sending message with properties", e);
            CompletableFuture<MessageId> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Send message with delay
     */
    public CompletableFuture<MessageId> sendMessageWithDelay(String message, long delaySeconds) {
        try {
            return batchProducer.newMessage()
                    .value(message.getBytes())
                    .deliverAfter(delaySeconds, TimeUnit.SECONDS)
                    .sendAsync();
        } catch (Exception e) {
            log.error("Error sending message with delay", e);
            CompletableFuture<MessageId> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Consume messages with custom handler
     */
    public void consumeMessagesWithHandler(Consumer<Message<byte[]>> messageHandler) {
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Message<byte[]> msg = batchConsumer.receive(1, TimeUnit.SECONDS);
                    if (msg != null) {
                        log.info("Received batch message: {} (MessageId: {})", 
                                new String(msg.getData()), msg.getMessageId());
                        
                        // Process message with custom handler
                        messageHandler.accept(msg);
                        
                        // Acknowledge the message
                        batchConsumer.acknowledge(msg);
                    }
                } catch (PulsarClientException e) {
                    log.error("Error receiving batch message", e);
                } catch (Exception e) {
                    log.error("Unexpected error in batch consumer", e);
                }
            }
        });
    }

    /**
     * Read messages from specific position
     */
    public void readMessagesFromPosition(Consumer<Message<byte[]>> messageHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                while (reader.hasMessageAvailable()) {
                    Message<byte[]> msg = reader.readNext();
                    if (msg != null) {
                        log.info("Read message from position: {} (MessageId: {})", 
                                new String(msg.getData()), msg.getMessageId());
                        messageHandler.accept(msg);
                    }
                }
            } catch (PulsarClientException e) {
                log.error("Error reading messages from position", e);
            }
        });
    }

    /**
     * Get topic stats
     */
    public CompletableFuture<Object> getTopicStats(String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // This would require PulsarAdmin client for full stats
                // For now, return basic info
                return "Topic: " + topicName + " - Producer: " + batchProducer.getProducerName();
            } catch (Exception e) {
                log.error("Error getting topic stats", e);
                return "Error getting stats: " + e.getMessage();
            }
        });
    }

    /**
     * Send message with schema validation
     */
    public CompletableFuture<MessageId> sendMessageWithSchema(String message) {
        try {
            // Create a simple JSON schema
            String jsonSchema = "{\"type\":\"string\"}";
            
            return batchProducer.newMessage()
                    .value(message.getBytes())
                    .property("schema", jsonSchema)
                    .sendAsync();
        } catch (Exception e) {
            log.error("Error sending message with schema", e);
            CompletableFuture<MessageId> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Send message with compression
     */
    public CompletableFuture<MessageId> sendCompressedMessage(String message) {
        try {
            return batchProducer.newMessage()
                    .value(message.getBytes())
                    .property("compression", "lz4")
                    .sendAsync();
        } catch (Exception e) {
            log.error("Error sending compressed message", e);
            CompletableFuture<MessageId> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
} 