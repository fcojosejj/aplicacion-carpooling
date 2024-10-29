package com.gitlab.uja2324dae.proyectodaekt.controller

import com.gitlab.uja2324dae.proyectodaekt.model.SharedRide
import com.gitlab.uja2324dae.proyectodaekt.model.User
import com.gitlab.uja2324dae.proyectodaekt.model.dto.SharedRideDto
import com.gitlab.uja2324dae.proyectodaekt.model.dto.SharedRideRequestDto
import com.gitlab.uja2324dae.proyectodaekt.model.dto.UserDto
import com.gitlab.uja2324dae.proyectodaekt.service.CarpoolingService
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.sharedRide.SharedRideNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDateTime

@RestController
@RequestMapping("/sharedRides")
class SharedRideController {

    @Autowired
    private lateinit var carpoolingService: CarpoolingService

    @PostMapping("/")
    fun createSharedRide(@RequestBody rideDto: SharedRideDto, principal: Principal): ResponseEntity<SharedRideDto> {
        return try {
            val newRide = carpoolingService.newSharedRide(
                carpoolingService.getUserByEmail(principal.name)!!,
                rideDto.originCity!!,
                rideDto.destinationCity!!,
                rideDto.departureTime!!,
                rideDto.arrivalTime!!,
                rideDto.seats!!,
                rideDto.seatPrice!!
            )
            ResponseEntity.status(HttpStatus.CREATED).body(SharedRideDto.fromSharedRide(newRide))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @GetMapping("/")
    fun findSharedRides(
        @RequestParam originCity: String,
        @RequestParam destinationCity: String,
        @RequestParam departureTime: String
    ): ResponseEntity<List<SharedRideDto>> {
        return try {
            val rides = carpoolingService.findSharedRides(originCity, destinationCity, LocalDateTime.parse(departureTime))
            if (rides != null){
                ResponseEntity.status(HttpStatus.OK).body(rides.map { SharedRideDto.fromSharedRide(it) })
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @GetMapping("/{id}")
    fun getSharedRideById(@PathVariable id: Long, principal: Principal): ResponseEntity<SharedRideDto> {
        return try {
            val ride = carpoolingService.getSharedRideById(carpoolingService.getUserByEmail(principal.name)!!, id)
            val rideDto = SharedRideDto.fromSharedRide(ride)
            ResponseEntity.status(HttpStatus.OK).body(rideDto)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PostMapping("/{id}/passengers/{dni}")
    fun acceptUserRequest(
        @PathVariable id: Long,
        @PathVariable dni: String,
        principal: Principal
    ): ResponseEntity<SharedRideDto> {
        return try {
            carpoolingService.acceptUserRequest(carpoolingService.getUserByEmail(principal.name)!!, dni, id)
            ResponseEntity.status(HttpStatus.OK).body(SharedRideDto.fromSharedRide(carpoolingService.getSharedRideById(carpoolingService.getUserByEmail(principal.name)!!, id)))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @DeleteMapping("/{id}/request/{dni}")
    fun denyUserRequest(
        @PathVariable id: Long,
        @PathVariable dni: String,
        principal: Principal
    ): ResponseEntity<SharedRideDto> {
        return try {
            val user = carpoolingService.getUserByEmail(principal.name)!!
            carpoolingService.denyUserRequest(user, dni, id)
            ResponseEntity.status(HttpStatus.OK).body(SharedRideDto.fromSharedRide(carpoolingService.getSharedRideById(user, id)))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    /**
     * Get all the shared rides that the logged-in user is the owner of, and they have not started yet.
     */
    @GetMapping("/pending/driver")
    fun getPendingDriverRides(principal: Principal): ResponseEntity<List<SharedRideDto>> {
        return try {
            val user = carpoolingService.getUserByEmail(principal.name)
            val pendingDriverRequests = carpoolingService.getPendingSharedRidesAsDriver(user!!)
            ResponseEntity.ok(pendingDriverRequests.map { SharedRideDto.fromSharedRide(it) })
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    /**
     * Get all the shared rides that the logged-in user is a passenger of, and they have not started yet.
     */
    @GetMapping("/pending/passenger")
    fun getPendingPassengerRides(principal: Principal): ResponseEntity<List<SharedRideDto>> {
        return try {
            val user = carpoolingService.getUserByEmail(principal.name)
            val pendingPassengerRequests = carpoolingService.getPendingSharedRidesAsPassenger(user!!)
            ResponseEntity.ok(pendingPassengerRequests.map { SharedRideDto.fromSharedRide(it) })
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    /**
     * Get all the shared rides that the logged-in user has requested to join, and they have not started yet.
     */
    @GetMapping("/pending/acceptance")
    fun getPendingAcceptanceRequests(principal: Principal): ResponseEntity<List<SharedRideDto>> {
        return try {
            val user = carpoolingService.getUserByEmail(principal.name)
            val pendingAcceptanceRequests = carpoolingService.getPendingAcceptanceSharedRides(user!!)
            ResponseEntity.ok(pendingAcceptanceRequests.map { SharedRideDto.fromSharedRide(it) })
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PostMapping("/{id}/requests")
    fun createRequest(@PathVariable id: Long, @RequestBody sharedRideRequestDto: SharedRideRequestDto, principal: Principal): ResponseEntity<SharedRideDto> {
        return try {
            val user = carpoolingService.getUserByEmail(principal.name)
            val ride = carpoolingService.getSharedRideById(user!!, id)
            carpoolingService.requestRide(user, ride, sharedRideRequestDto.message ?: "")
            ResponseEntity.status(HttpStatus.CREATED).body(SharedRideDto.fromSharedRide(ride))
        } catch (e: SharedRideNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }
}