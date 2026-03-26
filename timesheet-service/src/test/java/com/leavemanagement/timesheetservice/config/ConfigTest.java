package com.leavemanagement.timesheetservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
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
        Queue queue = config.timesheetQueue();
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.TIMESHEET_QUEUE, queue.getName());

        MessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}
