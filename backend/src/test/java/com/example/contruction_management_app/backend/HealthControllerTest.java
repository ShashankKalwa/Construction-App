package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.api.v1.HealthController;
import com.example.contruction_management_app.backend.dto.HealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    void healthEndpoint_returnsUpAnd200() {
        ResponseEntity<HealthResponse> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().status());
        assertEquals("Construction Management System (CMS) Backend", response.getBody().service());
        assertEquals("1.0.0", response.getBody().version());
    }
}
