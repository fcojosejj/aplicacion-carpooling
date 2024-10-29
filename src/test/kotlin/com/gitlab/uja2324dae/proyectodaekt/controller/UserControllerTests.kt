package com.gitlab.uja2324dae.proyectodaekt.controller

import com.gitlab.uja2324dae.proyectodaekt.model.dto.UserDto
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {
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
    fun testRegister() {
        val userDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )

        val response = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getLoggedInUserData() {
        // User registers
        val userDto = UserDto(
            dni = "12345678A",
            birthdate = LocalDate.of(1990, 12, 20),
            email = "user@mail.com",
            firstName = "Test user",
            lastName = "Testing",
            password = "123456789a1234",
            phone = "+34666452312"
        )
        val registerResponse = restTemplate.postForEntity("/user/", userDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        // Users performs a valid login
        val dataResponse = restTemplate
            .withBasicAuth(userDto.email, userDto.password)
            .getForEntity("/user/", UserDto::class.java)
        Assertions.assertEquals(userDto.email, dataResponse.body?.email)

        // User performs an invalid login
        val invalidDataResponse = restTemplate
            .withBasicAuth(userDto.email, "invalidPassword")
            .getForEntity("/user/", UserDto::class.java)
        Assertions.assertEquals(401, invalidDataResponse.statusCode.value())
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getNotLoggedInUserData() {
        val response = restTemplate
            .getForEntity("/user/", UserDto::class.java)

        Assertions.assertEquals(401, response.statusCode.value())
    }
}