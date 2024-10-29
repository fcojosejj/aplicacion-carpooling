package com.gitlab.uja2324dae.proyectodaekt.model

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class SharedRideRequestTest {

    @Test
    fun testValidRideRequestModel() {
        val requestModel = SharedRideRequest(
            User(
                "12345678A",
                "John",
                "Doe",
                "johndoe@example.com",
                "+34677889922",
                LocalDate.of(2000, 1, 1),
                "password"
            ),
            "Test Message"
        )

        val validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<SharedRideRequest>> = validator.validate(requestModel)
        Assertions.assertEquals(0, violations.size)
    }

    @Test
    fun testInvalidRideRequestModel() {
        val requestModel = SharedRideRequest(
            User(
                "12345678A",
                "John",
                "Doe",
                "johndoe@example.com",
                "+34677889922",
                LocalDate.of(2000, 1, 1),
                "password"
            ),
            ""
        )

        val validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<SharedRideRequest>> = validator.validate(requestModel)
        Assertions.assertEquals(1, violations.size)
    }
}