package com.gitlab.uja2324dae.proyectodaekt.controller

import com.gitlab.uja2324dae.proyectodaekt.model.dto.SharedRideDto
import com.gitlab.uja2324dae.proyectodaekt.model.dto.SharedRideRequestDto
import com.gitlab.uja2324dae.proyectodaekt.model.dto.UserDto
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SharedRideControllerTests {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jacksonConverter: MappingJackson2HttpMessageConverter

    private lateinit var restTemplate: TestRestTemplate

    @PostConstruct
    fun init() {
        val restTemplateBuilder = RestTemplateBuilder()
            .rootUri("http://localhost:$port")
            .additionalMessageConverters(jacksonConverter)

        restTemplate = TestRestTemplate(restTemplateBuilder)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testCreateSharedRide() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seats = 4,
            seatPrice = 10.0
        )

        // Owner creates a shared ride
        val responseUser = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseUser.statusCode, HttpStatus.OK)

        val response2 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(response2.statusCode, HttpStatus.CREATED)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testFindSharedRides() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now().plusHours(2),
            arrivalTime = LocalDateTime.now().plusHours(3),
            seats = 4,
            seatPrice = 10.0
        )

        // Owner creates a shared ride
        val responseUser = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseUser.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        // User finds the shared ride
        val responseFind = restTemplate
            .getForEntity(
                "/sharedRides/?originCity=${sharedRideDto.originCity}&destinationCity=${sharedRideDto.destinationCity}&departureTime=${
                    sharedRideDto.departureTime!!.minusSeconds(
                        10
                    )
                }", List::class.java
            )

        Assertions.assertEquals(responseFind.statusCode, HttpStatus.OK)
        Assertions.assertEquals(responseFind.body!!.size, 1)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testGetSharedRideById() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now().plusHours(2),
            arrivalTime = LocalDateTime.now().plusHours(3),
            seats = 4,
            seatPrice = 10.0,
        )

        val responseUser = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseUser.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        val responseFind = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .getForEntity("/sharedRides/{id}", SharedRideDto::class.java, responseRide1.body!!.id)
        Assertions.assertEquals(responseFind.statusCode, HttpStatus.OK)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testCreateRequest() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val userDto = UserDto(
            dni = "12345678B",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user1@mail.com",
            firstName = "user3",
            lastName = "user4",
            password = "123456789a1234",
            phone = "+34666452313"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seats = 4,
            seatPrice = 10.0
        )

        val sharedRideRequestDto = SharedRideRequestDto(
            message = "Test message",
        )

        // Owner creates a shared ride
        val responseOwner = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseOwner.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        // User creates an account
        val responseUser1 = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(responseUser1.statusCode, HttpStatus.OK)

        // User requests a seat in the shared ride
        val responseRequest = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .postForEntity(
                "/sharedRides/{id}/requests",
                sharedRideRequestDto,
                SharedRideDto::class.java,
                responseRide1.body!!.id
            )

        Assertions.assertEquals(HttpStatus.CREATED, responseRequest.statusCode)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testAcceptRequest() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val userDto = UserDto(
            dni = "12345678B",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user1@mail.com",
            firstName = "user3",
            lastName = "user4",
            password = "123456789a1234",
            phone = "+34666452313"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seats = 4,
            seatPrice = 10.0
        )

        val sharedRideRequestDto = SharedRideRequestDto(
            message = "Test message",
        )

        // Owner creates a shared ride
        val responseOwner = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseOwner.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        // User creates an account
        val responseUser1 = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(responseUser1.statusCode, HttpStatus.OK)

        // User requests a seat in the shared ride
        val responseRequest = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .postForEntity(
                "/sharedRides/{id}/requests",
                sharedRideRequestDto,
                SharedRideDto::class.java,
                responseRide1.body!!.id
            )

        Assertions.assertEquals(HttpStatus.CREATED, responseRequest.statusCode)

        //Owner accepts the request
        val responseAccept = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity(
                "/sharedRides/{id}/passengers/{dni}",
                null,
                SharedRideDto::class.java,
                responseRide1.body!!.id,
                userDto.dni
            )
        Assertions.assertEquals(HttpStatus.OK, responseAccept.statusCode)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testDenyRequest() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val userDto = UserDto(
            dni = "12345678B",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user1@mail.com",
            firstName = "user3",
            lastName = "user4",
            password = "123456789a1234",
            phone = "+34666452313"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seats = 4,
            seatPrice = 10.0
        )

        val sharedRideRequestDto = SharedRideRequestDto(
            message = "Test message",
        )

        // Owner creates a shared ride
        val responseOwner = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseOwner.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        // User creates an account
        val responseUser1 = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(responseUser1.statusCode, HttpStatus.OK)

        // User requests a seat in the shared ride
        val responseRequest = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .postForEntity(
                "/sharedRides/{id}/requests",
                sharedRideRequestDto,
                SharedRideDto::class.java,
                responseRide1.body!!.id
            )

        Assertions.assertEquals(HttpStatus.CREATED, responseRequest.statusCode)

        //Owner denies the request
        val responseDeny = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .delete(
                "/sharedRides/{id}/request/{dni}",
                responseRide1.body!!.id,
                userDto.dni
            )

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testGetPendingDriverRequests() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now().plusHours(2),
            arrivalTime = LocalDateTime.now().plusHours(3),
            seats = 4,
            seatPrice = 10.0,
        )

        val responseUser = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseUser.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        val responsePendingRides = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .getForEntity("/sharedRides/pending/driver", List::class.java)

        Assertions.assertEquals(HttpStatus.OK, responsePendingRides.statusCode)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testGetPendingPassengerRides() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val userDto = UserDto(
            dni = "12345678B",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user1@mail.com",
            firstName = "user3",
            lastName = "user4",
            password = "123456789a1234",
            phone = "+34666452313"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seats = 4,
            seatPrice = 10.0
        )

        val sharedRideRequestDto = SharedRideRequestDto(
            message = "Test message",
        )

        // Owner creates a shared ride
        val responseOwner = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseOwner.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        // User creates an account
        val responseUser1 = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(responseUser1.statusCode, HttpStatus.OK)

        // User requests a seat in the shared ride
        val responseRequest = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .postForEntity(
                "/sharedRides/{id}/requests",
                sharedRideRequestDto,
                SharedRideDto::class.java,
                responseRide1.body!!.id
            )

        Assertions.assertEquals(HttpStatus.CREATED, responseRequest.statusCode)

        //Owner accepts the request
        val responseAccept = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity(
                "/sharedRides/{id}/passengers/{dni}",
                null,
                SharedRideDto::class.java,
                responseRide1.body!!.id,
                userDto.dni
            )
        Assertions.assertEquals(HttpStatus.OK, responseAccept.statusCode)

        //User gets pending rides
        val responsePendingRides = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .getForEntity("/sharedRides/pending/passenger", List::class.java)

        Assertions.assertEquals(HttpStatus.OK, responsePendingRides.statusCode)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun testGetPendingAcceptancePassengerRides() {
        val ownerDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "user",
            lastName = "user2",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val userDto = UserDto(
            dni = "12345678B",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user1@mail.com",
            firstName = "user3",
            lastName = "user4",
            password = "123456789a1234",
            phone = "+34666452313"
        )

        val sharedRideDto = SharedRideDto(
            originCity = "Test1",
            destinationCity = "Test2",
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            seats = 4,
            seatPrice = 10.0
        )

        val sharedRideRequestDto = SharedRideRequestDto(
            message = "Test message",
        )

        // Owner creates a shared ride
        val responseOwner = restTemplate.postForEntity("/user/", ownerDto, UserDto::class.java)
        Assertions.assertEquals(responseOwner.statusCode, HttpStatus.OK)

        val responseRide1 = restTemplate
            .withBasicAuth(ownerDto.email, ownerDto.password)
            .postForEntity("/sharedRides/", sharedRideDto, SharedRideDto::class.java)
        Assertions.assertEquals(responseRide1.statusCode, HttpStatus.CREATED)

        // User creates an account
        val responseUser1 = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(responseUser1.statusCode, HttpStatus.OK)

        // User requests a seat in the shared ride
        val responseRequest = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .postForEntity(
                "/sharedRides/{id}/requests",
                sharedRideRequestDto,
                SharedRideDto::class.java,
                responseRide1.body!!.id
            )

        Assertions.assertEquals(HttpStatus.CREATED, responseRequest.statusCode)

        //User gets pending rides
        val responsePendingAcceptanceRides = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .getForEntity("/sharedRides/pending/acceptance", List::class.java)

        Assertions.assertEquals(HttpStatus.OK, responsePendingAcceptanceRides.statusCode)
    }
}