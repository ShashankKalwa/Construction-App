package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HealthController(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HealthController() {
        this.jdbcTemplate = null;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        boolean dbAlive = false;
        if (jdbcTemplate != null) {
            try {
                dbAlive = jdbcTemplate.queryForObject("SELECT 1", Integer.class) != null;
            } catch (Exception e) {
                dbAlive = false;
            }
        }

        String status = (dbAlive || jdbcTemplate == null) ? "UP" : "DEGRADED";

        return ResponseEntity.ok(
            new HealthResponse(
                status,
                "Construction Management System (CMS) Backend",
                "1.0.0"
            )
        );
    }
}
