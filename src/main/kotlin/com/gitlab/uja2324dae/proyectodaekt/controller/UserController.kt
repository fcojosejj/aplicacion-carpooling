package com.gitlab.uja2324dae.proyectodaekt.controller

import com.gitlab.uja2324dae.proyectodaekt.model.dto.UserDto
import com.gitlab.uja2324dae.proyectodaekt.service.CarpoolingService
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserAlreadyExistsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/user")
class UserController {
    @Autowired
    private lateinit var carpoolingService: CarpoolingService

    /**
     * Register a new user
     */
    @PostMapping("/")
    fun register(@RequestBody userDto: UserDto): ResponseEntity<UserDto> {
        return try {
            val registerResult = carpoolingService.newUser(userDto.toUser())
            ResponseEntity.ok(UserDto.fromUser(registerResult))
        } catch (e: UserAlreadyExistsException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Get the logged-in user data
     */
    @GetMapping("/")
    fun getLoggedInUserData(principal: Principal): ResponseEntity<UserDto> {
        val user = carpoolingService.getUserByEmail(principal.name)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(UserDto.fromUser(user))
    }
}