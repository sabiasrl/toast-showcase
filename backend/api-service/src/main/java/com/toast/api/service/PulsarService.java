package com.toast.api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PulsarService {

    @Autowired
    private PulsarClient pulsarClient;

    private Producer<String> producer;
    private Consumer<String> consumer;

    @PostConstruct
    public void init() throws PulsarClientException {
        // Initialize producer
        producer = pulsarClient.newProducer(Schema.STRING)
                .topic("user-events")
                .producerName("toast-api-producer")
                .create();

        // Initialize consumer
        consumer = pulsarClient.newConsumer(Schema.STRING)
                .topic("user-events")
                .subscriptionName("toast-api-subscription")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();

        // Start consuming messages in background
        startConsumer();
    }

    @PreDestroy
    public void cleanup() throws PulsarClientException {
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
    }

    /**
     * Send a message to Pulsar topic
     */
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

    /**
     * Send a message synchronously
     */
    public MessageId sendMessageSync(String message) throws PulsarClientException {
        log.info("Sending message synchronously to Pulsar: {}", message);
        return producer.send(message);
    }

    /**
     * Send a message with key
     */
    public CompletableFuture<MessageId> sendMessageWithKey(String key, String message) {
        try {
            log.info("Sending message with key {} to Pulsar: {}", key, message);
            return producer.newMessage()
                    .key(key)
                    .value(message)
                    .sendAsync();
        } catch (Exception e) {
            log.error("Error sending message with key to Pulsar", e);
            CompletableFuture<MessageId> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Start consuming messages in background
     */
    private void startConsumer() {
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    // Wait for a message
                    Message<String> msg = consumer.receive(1, TimeUnit.SECONDS);
                    if (msg != null) {
                        log.info("Received message from Pulsar: {} (MessageId: {})", 
                                msg.getValue(), msg.getMessageId());
                        
                        // Acknowledge the message
                        consumer.acknowledge(msg);
                    }
                } catch (PulsarClientException e) {
                    log.error("Error receiving message from Pulsar", e);
                } catch (Exception e) {
                    log.error("Unexpected error in consumer", e);
                }
            }
        });
    }

    /**
     * Get consumer stats
     */
    public ConsumerStats getConsumerStats() throws PulsarClientException {
        return consumer.getStats();
    }

    /**
     * Get producer stats
     */
    public ProducerStats getProducerStats() throws PulsarClientException {
        return producer.getStats();
    }
} 