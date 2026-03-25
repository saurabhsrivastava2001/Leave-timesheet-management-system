package com.leavemanagement.adminservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    void testSwaggerConfig() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI api = config.customOpenAPI();
        assertNotNull(api);
        assertNotNull(api.getComponents().getSecuritySchemes().get("bearerAuth"));
    }

    @Test
    void testRabbitMQConfig() {
        RabbitMQConfig config = new RabbitMQConfig();
        TopicExchange exchange = config.exchange();
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());

        Queue timesheetQueue = config.timesheetQueue();
        assertEquals(RabbitMQConfig.TIMESHEET_QUEUE, timesheetQueue.getName());

        Queue leaveQueue = config.leaveQueue();
        assertEquals(RabbitMQConfig.LEAVE_QUEUE, leaveQueue.getName());

        Binding bt = config.bindingTimesheet(timesheetQueue, exchange);
        assertEquals(RabbitMQConfig.TIMESHEET_ROUTING_KEY, bt.getRoutingKey());

        Binding bl = config.bindingLeave(leaveQueue, exchange);
        assertEquals(RabbitMQConfig.LEAVE_ROUTING_KEY, bl.getRoutingKey());

        MessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}
