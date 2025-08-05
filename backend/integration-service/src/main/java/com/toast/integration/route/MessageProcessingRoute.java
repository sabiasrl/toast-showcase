package com.toast.integration.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageProcessingRoute extends RouteBuilder {

    @Value("${camel.pulsar.service-url:pulsar://localhost:6650}")
    private String pulsarServiceUrl;

    @Value("${camel.rabbitmq.host:localhost}")
    private String rabbitmqHost;

    @Value("${camel.rabbitmq.port:5672}")
    private String rabbitmqPort;

    @Value("${camel.rabbitmq.username:guest}")
    private String rabbitmqUsername;

    @Value("${camel.rabbitmq.password:guest}")
    private String rabbitmqPassword;

    @Override
    public void configure() throws Exception {
        
        // Error handler
        errorHandler(deadLetterChannel("direct:errorHandler")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000)
                .backOffMultiplier(2));

        // Error handler route
        from("direct:errorHandler")
                .log("Error processing message: ${body}")
                .to("log:error");

        // Pulsar to RabbitMQ route (for development)
        from("pulsar://" + pulsarServiceUrl + "/persistent/public/default/user-events")
                .routeId("pulsar-to-rabbitmq")
                .log("Received message from Pulsar: ${body}")
                .unmarshal().json(JsonLibrary.Jackson)
                .process(exchange -> {
                    log.info("Processing message: {}", exchange.getIn().getBody());
                })
                .marshal().json(JsonLibrary.Jackson)
                .to("rabbitmq://" + rabbitmqHost + ":" + rabbitmqPort + "/user-events?username=" + 
                    rabbitmqUsername + "&password=" + rabbitmqPassword)
                .log("Message sent to RabbitMQ");

        // REST API to Pulsar route
        from("rest:post:/api/v1/messages")
                .routeId("rest-to-pulsar")
                .log("Received REST message: ${body}")
                .to("pulsar://" + pulsarServiceUrl + "/persistent/public/default/user-events")
                .setBody(simple("Message sent to Pulsar successfully"))
                .log("Message sent to Pulsar");

        // RabbitMQ consumer route (for production)
        from("rabbitmq://" + rabbitmqHost + ":" + rabbitmqPort + "/user-events?username=" + 
             rabbitmqUsername + "&password=" + rabbitmqPassword + "&autoDelete=false&durable=true")
                .routeId("rabbitmq-consumer")
                .log("Received message from RabbitMQ: ${body}")
                .unmarshal().json(JsonLibrary.Jackson)
                .process(exchange -> {
                    log.info("Processing RabbitMQ message: {}", exchange.getIn().getBody());
                })
                .log("Message processed successfully");
    }
} 