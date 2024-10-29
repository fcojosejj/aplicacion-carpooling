package com.gitlab.uja2324dae.proyectodaekt.model

import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserNotAPassengerException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.rating.UserAlreadyRatedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class UserRatingTest {
    private lateinit var targetUser: User
    private lateinit var ratedBy: User
    private lateinit var sharedRide: SharedRide

    @BeforeEach
    fun init() {
        targetUser = User(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        ratedBy = User(
            dni = "87654321B",
            birthdate = LocalDate.of(1995, 5, 10),
            email = "rater@mail.com",
            firstName = "Rater",
            lastName = "Rating",
            password = "password",
            phone = "+34666456789"
        )

        sharedRide = SharedRide(
            owner = targetUser,
            id = 1,
            originCity = "Origin",
            destinationCity = "Destination",
            seats = 3,
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seatPrice = 10.0
        )

        targetUser.sharedRides.add(sharedRide)
        ratedBy.sharedRides.add(sharedRide)
        sharedRide.passengers.add(targetUser)
        sharedRide.passengers.add(ratedBy)
    }

    @Test
    fun testAddValidRating() {
        val rating = 4
        val message = "Great ride!"

        targetUser.addRating(ratedBy, sharedRide, rating, message)

        val userRating = targetUser.ratings.first()
        assertEquals(ratedBy, userRating.ratedBy)
        assertEquals(sharedRide, userRating.sharedRide)
        assertEquals(rating, userRating.rating)
        assertEquals(message, userRating.message)
    }

    @Test
    fun testAddRatingToNonPassenger() {
        val rating = 4
        val message = "Great ride!"

        val otherRide = SharedRide(
            owner = ratedBy,
            id = 2,
            originCity = "Other Origin",
            destinationCity = "Other Destination",
            seats = 3,
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seatPrice = 10.0
        )
        otherRide.passengers.add(ratedBy)
        ratedBy.sharedRides.add(otherRide)

        // Try to rate a user that is not a passenger in the shared ride
        assertThrows<UserNotAPassengerException> {
            targetUser.addRating(ratedBy, otherRide, rating, message)
        }

        assertNull(targetUser.ratings.firstOrNull())
    }

    @Test
    fun testAddDuplicateRating() {
        val rating = 4
        val message = "Great ride"

        // Add a rating
        targetUser.addRating(ratedBy, sharedRide, rating, message)

        // Try to rate 2 times a user in the same shared ride
        assertThrows<UserAlreadyRatedException> {
            targetUser.addRating(ratedBy, sharedRide, rating, message)
        }

        assertEquals(1, targetUser.ratings.size)
    }

    @Test
    fun testAddInvalidRating() {
        val rating = 6 // Invalid rating (1-5)
        val message = "Great ride"

        // Try to rate with an invalid rating value
        assertThrows<IllegalArgumentException> {
            targetUser.addRating(ratedBy, sharedRide, rating, message)
        }

        assertNull(targetUser.ratings.firstOrNull())
    }
}
