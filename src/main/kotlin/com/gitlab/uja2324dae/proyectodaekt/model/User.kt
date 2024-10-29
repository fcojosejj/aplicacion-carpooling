package com.gitlab.uja2324dae.proyectodaekt.model

import com.gitlab.uja2324dae.proyectodaekt.util.UserModelRegex
import com.gitlab.uja2324dae.proyectodaekt.util.annotations.IsOlderThan
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserNotAPassengerException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.rating.UserAlreadyRatedException
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class User(
    @field:Pattern(
        regexp = UserModelRegex.DNI,
        message = "The DNI you entered is not valid!"
    )
    @Id
    val dni: String,

    @field:NotBlank @field:Size(
        min = 2,
        max = 16
    )
    var firstName: String,

    @field:NotBlank @field:Size(
        min = 2,
        max = 64
    )
    var lastName: String,

    @field:Email
    var email: String,

    @field:Pattern(
        regexp = UserModelRegex.PHONE_NUMBER,
        message = "Please enter a valid Spanish phone number!"
    )
    var phone: String,

    @field:IsOlderThan(
        adultAge = 18,
        message = "You need to be +18 years old to use this application!"
    )
    var birthdate: LocalDate,

    var password: String,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_dni")
    val ratings: MutableList<UserRating> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "passengers", cascade = [CascadeType.ALL])
    val sharedRides: MutableList<SharedRide> = mutableListOf()
) {
    fun login(password: String): Boolean {
        return password == this.password
    }

    fun pendingSharedRidesAsPassenger(): List<SharedRide> {
        return sharedRides.filter { it.departureTime.isAfter(LocalDateTime.now()) }
    }

    fun pendingSharedRidesAsDriver(): List<SharedRide> {
        return sharedRides.filter { it.departureTime.isAfter(LocalDateTime.now()) && it.owner.dni == this.dni }
    }

    fun addRating(
        ratedBy: User,
        sharedRide: SharedRide,
        rating: Int,
        message: String
    ) {
        if (sharedRides.find { it.id == sharedRide.id } == null) throw UserNotAPassengerException()
        if (ratings.find { it.ratedBy.dni == ratedBy.dni && it.sharedRide.id == sharedRide.id } != null) throw UserAlreadyRatedException()
        if (ratedBy.sharedRides.find { it.id == sharedRide.id } == null) throw UserNotAPassengerException()
        if (rating < 1 || rating > 5) throw IllegalArgumentException("The rating must be between 1 and 5 stars!")
        ratings.add(
            UserRating(
                ratedBy = ratedBy,
                sharedRide = sharedRide,
                rating = rating,
                message = message
            )
        )
    }
}