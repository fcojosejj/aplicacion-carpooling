package com.gitlab.uja2324dae.proyectodaekt.model.dto

import com.gitlab.uja2324dae.proyectodaekt.model.SharedRide
import java.io.Serializable
import java.time.LocalDateTime

data class SharedRideDto(
    val ownerDni: String? = null,
    val ownerFirstName: String? = null,
    val ownerLastName: String? = null,
    val originCity: String? = null,
    val destinationCity: String? = null,
    val seats: Int? = null,
    val departureTime: LocalDateTime? = null,
    val arrivalTime: LocalDateTime? = null,
    val seatPrice: Double? = null,
    val id: Long? = null
) : Serializable {
    companion object {
        fun fromSharedRide(sharedRide: SharedRide): SharedRideDto {
            return SharedRideDto(
                ownerDni = sharedRide.owner.dni,
                ownerFirstName = sharedRide.owner.firstName,
                ownerLastName = sharedRide.owner.lastName,
                originCity = sharedRide.originCity,
                destinationCity = sharedRide.destinationCity,
                seats = sharedRide.seats,
                departureTime = sharedRide.departureTime,
                arrivalTime = sharedRide.arrivalTime,
                seatPrice = sharedRide.seatPrice,
                id = sharedRide.id
            )
        }
    }
}