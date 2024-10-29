package com.gitlab.uja2324dae.proyectodaekt.model

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class UserTests {
    @Test
    fun testValidUserModel() {
        val user = User(
            "12345678A",
            "John",
            "Doe",
            "johndoe@example.com",
            "+34677889922",
            LocalDate.of(2000, 1, 1),
            "password"
        )

        val validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<User>> = validator.validate(user)
        assertEquals(0, violations.size)
    }

    @Test
    fun testInvalidUserModel() {
        val user = User(
            "123B",
            "J",
            "D",
            "notanemail",
            "123",
            LocalDate.of(2010, 1, 1),
            "pw"
        )

        val validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<User>> = validator.validate(user)
        assertEquals(6, violations.size)
    }

    @Test
    fun testLogin() {
        val user = User(
            "12345678A",
            "John",
            "Doe",
            "johndoe@example.com",
            "+34677889922",
            LocalDate.of(2000, 1, 1),
            "password"
        )

        // Test a valid login
        val validLogin = user.login("password")
        assertEquals(true, validLogin)

        // Test an invalid login
        val invalidLogin = user.login("wrongpassword")
        assertEquals(false, invalidLogin)
    }
}
