package com.example.contruction_management_app.backend

import com.example.contruction_management_app.backend.api.v1.HealthController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class HealthControllerTest {

    private val controller = HealthController()

    @Test
    fun healthEndpoint_returnsUpAnd200() {
        val response = controller.health()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("UP", response.body?.status)
        assertEquals("Construction Management System (CMS) Backend", response.body?.service)
        assertEquals("1.0.0", response.body?.version)
    }
}
