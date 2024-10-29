package com.gitlab.uja2324dae.proyectodaekt.service

import com.gitlab.uja2324dae.proyectodaekt.model.SharedRide
import com.gitlab.uja2324dae.proyectodaekt.model.User
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.sharedRide.OverlappingSharedRideException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserAlreadyExistsException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserNotAPassengerException
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class CarpoolingServiceTests {
    private lateinit var driver: User
    private lateinit var user1: User
    private lateinit var user2: User

    @Autowired
    private lateinit var carpoolingService: CarpoolingService

    @BeforeEach
    fun init() {
        driver = User(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        user1 = User(
            dni = "12345678B",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "pass1@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        user2 = User(
            dni = "12345678C",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "pass2@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        carpoolingService.newUser(driver)
        carpoolingService.newUser(user1)
        carpoolingService.newUser(user2)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun carpoolingServiceTest() {
        Assertions.assertNotNull(carpoolingService)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun newInvalidUserTest() {
        val user = User(
            dni = "12345A",
            birthdate = LocalDate.now(),
            email = "usermail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34766452312"
        )

        assertThrows<ConstraintViolationException> {
            carpoolingService.newUser(user)
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun newValidUserTest() {
        val user = User(
            dni = "12345678H",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "userTest@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        assertDoesNotThrow {
            carpoolingService.newUser(user)
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun newValidDuplicateUserTest() {
        assertThrows<UserAlreadyExistsException> {
            carpoolingService.newUser(driver)
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testValidSharedRide() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = departureTime.plusHours(2)

        val loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        // Create a valid ride. At this point, the driver is not null.
        val sharedRide = carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)
        Assertions.assertNotNull(sharedRide)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testInvalidSharedRideOverlapping() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = departureTime.plusHours(2)

        val loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        // Create a valid ride first
        carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)

        // Try to create a new ride with overlapping times
        val overlappingDepartureTime = departureTime.plusMinutes(30)
        val overlappingArrivalTime = arrivalTime.plusMinutes(30)

        assertThrows<OverlappingSharedRideException> {
            carpoolingService.newSharedRide(
                loggedInDriver,
                "Origin",
                "Destination",
                overlappingDepartureTime,
                overlappingArrivalTime,
                2,
                10.0
            )
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testValidLogin() {
        // Try to log in with valid credentials
        val loggedInUser = carpoolingService.login("user@mail.com", "123456789a1234")

        Assertions.assertEquals(loggedInUser?.dni, driver.dni)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testInvalidLogin() {
        // Try to log in with invalid credentials
        val loggedInUser = carpoolingService.login("user@mail.com", "notvalid")

        Assertions.assertEquals(null, loggedInUser)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testNonExistentUser() {
        // Try to log in with valid credentials
        val loggedInUser = carpoolingService.login("nonexisting@mail.com", "notvalid")

        Assertions.assertEquals(null, loggedInUser)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testFindSharedRides() {
        var rides: List<SharedRide>? = listOf()

        carpoolingService.newSharedRide(
            driver,
            "originTest",
            "destinationTest",
            LocalDateTime.of(2023, 10, 26, 16, 0, 0),
            LocalDateTime.of(2023, 10, 26, 16, 30, 0),
            5,
            30.0
        )

        carpoolingService.newSharedRide(
            driver,
            "originTest",
            "destinationTest",
            LocalDateTime.of(2023, 10, 27, 15, 0, 0),
            LocalDateTime.of(2023, 10, 27, 18, 30, 0),
            5,
            30.0
        )

        carpoolingService.newSharedRide(
            driver,
            "originTest",
            "destinationTest",
            LocalDateTime.of(2023, 10, 30, 12, 0, 0),
            LocalDateTime.of(2023, 10, 30, 12, 45, 0),
            5,
            30.0
        )

        carpoolingService.newSharedRide(
            driver,
            "originTest",
            "destinationTest",
            LocalDateTime.of(2023, 10, 30, 17, 0, 0),
            LocalDateTime.of(2023, 10, 30, 17, 45, 0),
            5,
            30.0
        )

        // The names of the origin and destination city are bad written on purpose, because the method can normalize all the strings
        // First case: Return all the rides on October 26 from 10:00 onwards (expected 1)
        rides = carpoolingService.findSharedRides(
            "originteSt  ",
            "DEStinationTest",
            LocalDateTime.of(2023, 10, 26, 10, 0, 0)
        )
        Assertions.assertEquals(1, rides?.size)

        // Second case: Return all the rides on October 30 from 11:59:59 onwards (expected 2)
        rides = carpoolingService.findSharedRides(
            "origin   Tést",
            "destinat   IOnTêst",
            LocalDateTime.of(2023, 10, 30, 11, 59, 59)
        )
        Assertions.assertEquals(2, rides?.size)

        // Third case: Return all the rides on October 30 from 14:00 onwards (expected 1)
        rides = carpoolingService.findSharedRides(
            "     o RiginTe st",
            "destínátiÔnTest",
            LocalDateTime.of(2023, 10, 30, 14, 0, 0)
        )
        Assertions.assertEquals(1, rides?.size)

        // Fourth case: Return all the rides on October 30 from 12:00:01 onwards (expected 1)
        rides = carpoolingService.findSharedRides(
            "     o RiginTe st",
            "destínátiÔnTest",
            LocalDateTime.of(2023, 10, 30, 12, 0, 1)
        )
        Assertions.assertEquals(1, rides?.size)

        // Fifth case: Return all the rides on November 12 from 10:00 onwards (expected 0)
        rides = carpoolingService.findSharedRides(
            "originTest",
            "des tinat ionTÉst",
            LocalDateTime.of(2023, 11, 12, 10, 0, 0)
        )
        Assertions.assertEquals(0, rides?.size)

        // Sixth case: Return all the rides on October 30 from 17:00:01 onwards (expected 0)
        rides = carpoolingService.findSharedRides(
            "     o RiginTe st",
            "destínátiÔnTest",
            LocalDateTime.of(2023, 10, 30, 17, 0, 1)
        )
        Assertions.assertEquals(0, rides?.size)

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testAcceptUserRequest() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = departureTime.plusHours(2)

        val loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        // Create a valid ride. At this point, the driver is not null.
        carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)

        val loggedInPassenger1 = carpoolingService.login("pass1@mail.com", "123456789a1234")
        val loggedInPassenger2 = carpoolingService.login("pass2@mail.com", "123456789a1234")

        // Make sure that both users are logged in.
        Assertions.assertNotNull(loggedInPassenger1)
        Assertions.assertNotNull(loggedInPassenger2)

        // We search for the only available shared ride
        val rides: List<SharedRide?> =
            carpoolingService.findSharedRides("Origin", "Destination", departureTime.minusMinutes(5))!!
        Assertions.assertEquals(1, rides.size)

        var ride: SharedRide = rides[0]!!

        // Both drivers request the shared ride
        carpoolingService.requestRide(loggedInPassenger1!!, ride, "Hey, I want to travel with u! :)")
        carpoolingService.requestRide(loggedInPassenger2!!, ride, "Hey, I want to travel with u2! :)")
        Assertions.assertEquals(2, ride.requests.size)

        // The driver accept both requests
        carpoolingService.acceptUserRequest(loggedInDriver, loggedInPassenger1.dni, ride.id!!)
        ride = carpoolingService.getSharedRideById(loggedInDriver, ride.id!!)
        Assertions.assertEquals(2, ride.passengers.size)
        Assertions.assertEquals(ride.seats - ride.passengers.size, ride.getAvailableSeats()) // Expected = 4

        carpoolingService.acceptUserRequest(loggedInDriver, loggedInPassenger2.dni, ride.id!!)
        ride = carpoolingService.getSharedRideById(loggedInDriver, ride.id!!)
        Assertions.assertEquals(3, ride.passengers.size)
        Assertions.assertEquals(ride.seats - ride.passengers.size, ride.getAvailableSeats()) // Expected = 3
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testDenyUserRequest() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = departureTime.plusHours(2)

        val loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        // Create a valid ride. At this point, the driver is not null.
        carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)

        val loggedInPassenger1 = carpoolingService.login("pass1@mail.com", "123456789a1234")
        val loggedInPassenger2 = carpoolingService.login("pass2@mail.com", "123456789a1234")

        // Make sure that both users are logged in.
        Assertions.assertNotNull(loggedInPassenger1)
        Assertions.assertNotNull(loggedInPassenger2)

        // We search for the only available shared ride
        val rides: List<SharedRide?> =
            carpoolingService.findSharedRides("Origin", "Destination", departureTime.minusMinutes(5))!!
        Assertions.assertEquals(1, rides.size)

        var ride: SharedRide = rides[0]!!

        // Both drivers request the shared ride
        carpoolingService.requestRide(loggedInPassenger1!!, ride, "Hey, I want to travel with u! :)")
        carpoolingService.requestRide(loggedInPassenger2!!, ride, "Hey, I want to travel with u2! :)")
        ride = carpoolingService.getSharedRideById(loggedInDriver, ride.id!!)
        Assertions.assertEquals(2, ride.requests.size)

        // The driver accept both requests
        carpoolingService.denyUserRequest(loggedInDriver, loggedInPassenger1.dni, ride.id!!)
        ride = carpoolingService.getSharedRideById(loggedInDriver, ride.id!!)
        Assertions.assertEquals(1, ride.passengers.size)
        Assertions.assertEquals(1, ride.requests.size)
        Assertions.assertEquals(ride.seats - ride.passengers.size, ride.getAvailableSeats()) // Expected = 5

        carpoolingService.denyUserRequest(loggedInDriver, loggedInPassenger2.dni, ride.id!!)
        ride = carpoolingService.getSharedRideById(loggedInDriver, ride.id!!)
        Assertions.assertEquals(1, ride.passengers.size)
        Assertions.assertEquals(0, ride.requests.size)
        Assertions.assertEquals(ride.seats - ride.passengers.size, ride.getAvailableSeats()) // Expected = 5
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testGetPendingSharedRides() {
        val departureTime = LocalDateTime.now().plusHours(2)
        val arrivalTime = departureTime.plusHours(4)

        val loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        // Create a valid ride. At this point, the driver is not null.
        carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)

        val loggedInPassenger1 = carpoolingService.login("pass1@mail.com", "123456789a1234")
        val loggedInPassenger2 = carpoolingService.login("pass2@mail.com", "123456789a1234")

        // Make sure that both users are logged in.
        Assertions.assertNotNull(loggedInPassenger1)
        Assertions.assertNotNull(loggedInPassenger2)

        // The driver must have one pending ride
        val driverRides: List<SharedRide> = carpoolingService.getPendingSharedRidesAsDriver(loggedInDriver)
        Assertions.assertEquals(1, driverRides.size)

        // We search for the only available shared ride
        val rides: List<SharedRide?> =
            carpoolingService.findSharedRides("Origin", "Destination", departureTime.minusMinutes(5))!!
        Assertions.assertEquals(1, rides.size)

        val ride: SharedRide = rides[0]!!

        carpoolingService.requestRide(loggedInPassenger1!!, ride, "Hey, I want to travel with u! :)")
        carpoolingService.requestRide(loggedInPassenger2!!, ride, "Hey, I want to travel with u2! :)")

        // The first user is accepted, the second isn't
        carpoolingService.acceptUserRequest(loggedInDriver, loggedInPassenger1.dni, ride.id!!)
        carpoolingService.denyUserRequest(loggedInDriver, loggedInPassenger2.dni, ride.id!!)

        // The first user has 1 pending ride, but the second user hasn't any pending rides (his request got denied)
        val passenger1Rides: List<SharedRide> = carpoolingService.getPendingSharedRidesAsPassenger(loggedInPassenger1)
        Assertions.assertEquals(1, passenger1Rides.size)

        val passenger2Rides: List<SharedRide> = carpoolingService.getPendingSharedRidesAsPassenger(loggedInPassenger2)
        Assertions.assertEquals(0, passenger2Rides.size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testRateUser() {
        val departureTime = LocalDateTime.now()
        val arrivalTime = departureTime.plusHours(2)

        var loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        // Create a valid ride. At this point, the driver is not null.
        carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)

        var loggedInUser1 = carpoolingService.login("pass1@mail.com", "123456789a1234")!!
        var loggedInUser2 = carpoolingService.login("pass2@mail.com", "123456789a1234")!!

        // Make sure that the user is logged in.
        Assertions.assertNotNull(loggedInUser1)
        Assertions.assertNotNull(loggedInUser2)

        // We search for the only available shared ride
        val rides: List<SharedRide?> =
            carpoolingService.findSharedRides("Origin", "Destination", departureTime.minusMinutes(5))!!
        Assertions.assertEquals(1, rides.size)
        val ride: SharedRide = rides[0]!!

        // The user makes a request
        carpoolingService.requestRide(loggedInUser1, ride, "Hey, i want to ride with u! :)")

        // If the driver hasn't accepted the request, the user doesn't belong to that ride, so he can't rate the driver
        Assertions.assertThrows(UserNotAPassengerException::class.java) {
            carpoolingService.rateUser(loggedInUser1, "12345678A", ride.id!!, 4, "It was a nice ride!!")
        }
        loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")
        Assertions.assertEquals(0, carpoolingService.getUserRatings(loggedInDriver!!).size)

        // Once the driver has accepted the request, the user now can rate the driver
        carpoolingService.acceptUserRequest(loggedInDriver, loggedInUser1.dni, ride.id!!)

        // Refresh the user and the driver to keep them updated
        // loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")
        loggedInUser1 = carpoolingService.login("pass1@mail.com", "123456789a1234")!!
        loggedInUser2 = carpoolingService.login("pass2@mail.com", "123456789a1234")!!
        Assertions.assertDoesNotThrow {
            carpoolingService.rateUser(loggedInUser1, loggedInDriver.dni, ride.id!!, 4, "It was a nice ride!!")
        }

        // Now, both passengers rate each other, so now each one has a rating
        carpoolingService.requestRide(loggedInUser2, ride, "Hey, i want to ride with u! :)")
        carpoolingService.acceptUserRequest(loggedInDriver, loggedInUser2.dni, ride.id!!)
        loggedInUser1 = carpoolingService.login("pass1@mail.com", "123456789a1234")!!
        loggedInUser2 = carpoolingService.login("pass2@mail.com", "123456789a1234")!!
        Assertions.assertDoesNotThrow {
            carpoolingService.rateUser(
                loggedInUser1,
                "12345678C",
                ride.id!!,
                5,
                "The best ride companion i've ever meet!!"
            )
        }
        Assertions.assertDoesNotThrow {
            carpoolingService.rateUser(
                loggedInUser2,
                "12345678A",
                ride.id!!,
                2,
                "This guy never shuts up!!"
            )
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testPendingAcceptanceRideRequests() {
        val loggedInDriver = carpoolingService.login("user@mail.com", "123456789a1234")

        // Make sure that the driver is logged in.
        Assertions.assertNotNull(loggedInDriver)

        val departureTime = LocalDateTime.now().plusHours(4)
        val arrivalTime = LocalDateTime.now().plusHours(8)

        // Create a valid ride. At this point, the driver is not null.
        carpoolingService.newSharedRide(loggedInDriver!!, "Origin", "Destination", departureTime, arrivalTime, 3, 10.0)

        val loggedInPassenger1 = carpoolingService.login("pass1@mail.com", "123456789a1234")
        val loggedInPassenger2 = carpoolingService.login("pass2@mail.com", "123456789a1234")

        // Make sure that both users are logged in.
        Assertions.assertNotNull(loggedInPassenger1)
        Assertions.assertNotNull(loggedInPassenger2)

        // We search for the only available shared ride
        val rides: List<SharedRide?> =
            carpoolingService.findSharedRides("Origin", "Destination", departureTime.minusMinutes(5))!!
        Assertions.assertEquals(1, rides.size)
        val ride: SharedRide = rides[0]!!

        // Both drivers request the shared ride
        carpoolingService.requestRide(loggedInPassenger1!!, ride, "Hey, I want to travel with u! :)")

        // The first user has made a request, but the second user hasn't made any request at the moment
        var pendingRides: List<SharedRide> = carpoolingService.getPendingAcceptanceSharedRides(loggedInPassenger1)
        Assertions.assertEquals(1, pendingRides.size)

        pendingRides = carpoolingService.getPendingAcceptanceSharedRides(loggedInPassenger2!!)
        Assertions.assertEquals(0, pendingRides.size)

        // The second user makes the request
        carpoolingService.requestRide(loggedInPassenger2, ride, "Hey, I want to travel with u2! :)")
        pendingRides = carpoolingService.getPendingAcceptanceSharedRides(loggedInPassenger2)
        Assertions.assertEquals(1, pendingRides.size)
    }
}
