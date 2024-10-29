package com.gitlab.uja2324dae.proyectodaekt.model

import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.sharedRide.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
class SharedRide(
    @ManyToOne
    val owner: User,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    val passengers: MutableList<User> = mutableListOf(),

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "ride_id")
    val requests: MutableList<SharedRideRequest> = mutableListOf(),

    @field:NotBlank @field:Size(
        min = 2,
        max = 44
    )
    var originCity: String,

    @field:NotBlank @field:Size(
        min = 2,
        max = 44
    )
    var destinationCity: String,

    @field:PositiveOrZero
    var seats: Int,

    var departureTime: LocalDateTime,

    var arrivalTime: LocalDateTime,

    @field:Positive
    var seatPrice: Double,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun acceptUserRequest(user: User, targetUser: User) {
        if (user.dni != owner.dni)
            throw UserNotOwnerException()

        val result = requests.removeIf { it.user.dni == targetUser.dni }
        if (!result)
            throw UserRequestNotFoundException()

        if (getAvailableSeats() > 0) {
            passengers.add(targetUser)
            targetUser.sharedRides.add(this)
        } else if (getAvailableSeats() == 0)
            throw SharedRideNoSeatsLeftException()
    }

    fun denyUserRequest(user: User, targetUser: User) {
        if (user.dni != owner.dni)
            throw UserNotOwnerException()

        val result = requests.removeIf { it.user.dni == targetUser.dni }
        if (!result)
            throw UserRequestNotFoundException()
    }

    fun requestRide(user: User, message: String) {
        if (requests.any { it.user.dni == user.dni })
            throw SharedRideAlreadyRequestedException()

        if (passengers.any { it.dni == user.dni })
            throw UserIsAlreadyAPassenger()

        if (passengers.size == seats)
            throw SharedRideNoSeatsLeftException()

        if (user.dni == owner.dni)
            throw UserIsOwnerException()

        requests.add(SharedRideRequest(user, message))
    }

    fun getAvailableSeats(): Int = seats - passengers.size
}