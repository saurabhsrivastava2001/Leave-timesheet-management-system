package com.leavemanagement.identityservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IdentityConfigTest {

    @Test
    void testSwaggerConfig() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI api = config.customOpenAPI();
        assertNotNull(api);
        assertNotNull(api.getComponents().getSecuritySchemes().get("bearerAuth"));
    }
}
