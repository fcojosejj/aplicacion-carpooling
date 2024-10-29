package com.gitlab.uja2324dae.proyectodaekt.model

import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.sharedRide.SharedRideAlreadyRequestedException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.sharedRide.SharedRideNoSeatsLeftException
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class SharedRideTests {

    @Test
    fun testValidSharedRideModel() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = LocalDateTime.now().plusHours(1)

        val sharedRide = SharedRide(
            id = 1,
            originCity = "test1",
            destinationCity = "test2",
            seats = 5,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatPrice = 10.0,
            owner = User(
                dni = "12345678A",
                birthdate = LocalDate.of(1990, 12, 20),
                email = "user@mail.com",
                firstName = "Test user",
                lastName = "Testing",
                password = "123456789a1234",
                phone = "+34666452312"
            )
        )

        val validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<SharedRide>> = validator.validate(sharedRide)
        Assertions.assertEquals(0, violations.size)
    }

    @Test
    fun testInvalidSharedRideModel() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = LocalDateTime.now().plusHours(1)
        val sharedRide = SharedRide(
            id = 1,
            originCity = "t",
            destinationCity = "test2",
            seats = -5,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatPrice = -10.0,
            owner = User(
                dni = "12345678A",
                birthdate = LocalDate.of(1990, 12, 20),
                email = "user@mail.com",
                firstName = "Test user",
                lastName = "Testing",
                password = "123456789a1234",
                phone = "+34666452312"
            )
        )

        val validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<SharedRide>> = validator.validate(sharedRide)
        Assertions.assertEquals(3, violations.size)
    }

    @Test
    fun testAcceptUserRequest() {

        val request1 = SharedRideRequest(
            User(
                "12345678A",
                "John",
                "Doe",
                "johndoe@example.com",
                "+34677889922",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val request2 = SharedRideRequest(
            User(
                "12345678B",
                "James",
                "Doe",
                "jamesdoe@example.com",
                "+34677889955",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val request3 = SharedRideRequest(
            User(
                "12345678C",
                "Jim",
                "Doe",
                "jimdoe@example.com",
                "+34677889977",
                LocalDate.of(2000, 1, 1),
                "password",
            ), "Test Message"
        )

        val request4 = SharedRideRequest(
            User(
                "12345678D",
                "Josh",
                "Doe",
                "joshdoe@example.com",
                "+34677889977",
                LocalDate.of(2000, 1, 1),
                "password",
            ), "Test Message"
        )

        val request5 = SharedRideRequest(
            User(
                "12345678E",
                "Jordan",
                "Doe",
                "jordandoe@example.com",
                "+34677889977",
                LocalDate.of(2000, 1, 1),
                "password",
            ), "Test Message"
        )

        val request6 = SharedRideRequest(
            User(
                "12345678F",
                "Joe",
                "Doe",
                "joedoe@example.com",
                "+34677889977",
                LocalDate.of(2000, 1, 1),
                "password",
            ), "Test Message"
        )

        val departureTime = LocalDateTime.now()
        val arrivalTime = LocalDateTime.now().plusHours(1)
        val sharedRide = SharedRide(
            id = 1,
            originCity = "test1",
            destinationCity = "test2",
            seats = 5,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatPrice = 10.0,
            owner = User(
                dni = "12345678A",
                birthdate = LocalDate.of(1990, 12, 20),
                email = "user@mail.com",
                firstName = "Test user",
                lastName = "Testing",
                password = "123456789a1234",
                phone = "+34666452312"
            ),
            requests = mutableListOf(request1, request2, request3, request4, request5, request6)
        )

        sharedRide.acceptUserRequest(request1.user, request2.user)

        Assertions.assertEquals(5, sharedRide.requests.size)
        Assertions.assertEquals(1, sharedRide.passengers.size)
        Assertions.assertEquals(
            sharedRide.seats - sharedRide.passengers.size,
            sharedRide.getAvailableSeats()
        )

        sharedRide.acceptUserRequest(sharedRide.owner, request1.user)
        sharedRide.acceptUserRequest(sharedRide.owner, request3.user)
        sharedRide.acceptUserRequest(sharedRide.owner, request4.user)
        sharedRide.acceptUserRequest(sharedRide.owner, request5.user)

        // Now the car has the 5 seats occupied, so the method must throw an exception
        Assertions.assertThrows(SharedRideNoSeatsLeftException::class.java) {
            sharedRide.acceptUserRequest(
                sharedRide.owner,
                request6.user
            )
        }
    }

    @Test
    fun testDenyUserRequest() {

        val request1 = SharedRideRequest(
            User(
                "12345678A",
                "John",
                "Doe",
                "johndoe@example.com",
                "+34677889922",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val request2 = SharedRideRequest(
            User(
                "12345678B",
                "James",
                "Doe",
                "jamesdoe@example.com",
                "+34677889955",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val request3 = SharedRideRequest(
            User(
                "12345678C",
                "Jim",
                "Doe",
                "jimdoe@example.com",
                "+34677889977",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val departureTime = LocalDateTime.now()
        val arrivalTime = LocalDateTime.now().plusHours(1)
        val sharedRide = SharedRide(
            id = 1,
            originCity = "test1",
            destinationCity = "test2",
            seats = 5,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatPrice = 10.0,
            owner = User(
                dni = "12345678A",
                birthdate = LocalDate.of(1990, 12, 20),
                email = "user@mail.com",
                firstName = "Test user",
                lastName = "Testing",
                password = "123456789a1234",
                phone = "+34666452312"
            ),
            requests = mutableListOf(request1, request2, request3)
        )

        sharedRide.denyUserRequest(sharedRide.owner, request1.user)

        Assertions.assertEquals(2, sharedRide.requests.size)
        Assertions.assertEquals(0, sharedRide.passengers.size)
        Assertions.assertEquals(sharedRide.seats, sharedRide.getAvailableSeats())
    }

    @Test
    fun testGetAvailableSeats() {

        val request1 = SharedRideRequest(
            User(
                "12345678A",
                "John",
                "Doe",
                "johndoe@example.com",
                "+34677889922",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val request2 = SharedRideRequest(
            User(
                "12345678B",
                "James",
                "Doe",
                "jamesdoe@example.com",
                "+34677889955",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val request3 = SharedRideRequest(
            User(
                "12345678C",
                "Jim",
                "Doe",
                "jimdoe@example.com",
                "+34677889977",
                LocalDate.of(2000, 1, 1),
                "password"
            ), "Test Message"
        )

        val departureTime = LocalDateTime.now()
        val arrivalTime = LocalDateTime.now().plusHours(1)
        val sharedRide = SharedRide(
            id = 1,
            originCity = "test1",
            destinationCity = "test2",
            seats = 5,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatPrice = 10.0,
            owner = User(
                dni = "12345678A",
                birthdate = LocalDate.of(1990, 12, 20),
                email = "user@mail.com",
                firstName = "Test user",
                lastName = "Testing",
                password = "123456789a1234",
                phone = "+34666452312"
            ),
            requests = mutableListOf(request1, request2, request3)
        )

        // Before accepting a new request
        Assertions.assertEquals(
            sharedRide.seats - sharedRide.passengers.size,
            sharedRide.getAvailableSeats()
        )

        sharedRide.acceptUserRequest(sharedRide.owner, request2.user)

        // After accepting a new request
        Assertions.assertEquals(
            sharedRide.seats - sharedRide.passengers.size,
            sharedRide.getAvailableSeats()
        )

    }

    @Test
    fun testRequestRide() {
        val user1 = User(
                "12345678A",
                "John",
                "Doe",
                "johndoe@example.com",
                "+34677889922",
                LocalDate.of(2000, 1, 1),
                "password"
            )

        val user2 = User(
                "12345678B",
                "James",
                "Doe",
                "jamesdoe@example.com",
                "+34677889955",
                LocalDate.of(2000, 1, 1),
                "password")

        val departureTime = LocalDateTime.now()
        val arrivalTime = LocalDateTime.now().plusHours(1)
        val sharedRide = SharedRide(
            id = 1,
            originCity = "test1",
            destinationCity = "test2",
            seats = 5,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatPrice = 10.0,
            owner = User(
                dni = "12345678C",
                birthdate = LocalDate.of(1990, 12, 20),
                email = "user@mail.com",
                firstName = "Test user",
                lastName = "Testing",
                password = "123456789a1234",
                phone = "+34666452312"
            ),
        )

        sharedRide.requestRide(user1, "Hey, test message")
        Assertions.assertEquals(1, sharedRide.requests.size)

        assertThrows<SharedRideAlreadyRequestedException> {
            sharedRide.requestRide(user1, "Hey, test message")
        }
        Assertions.assertEquals(1, sharedRide.requests.size)

        sharedRide.requestRide(user2, "Hey, test message")
        Assertions.assertEquals(2, sharedRide.requests.size)
    }
}