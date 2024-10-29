package com.gitlab.uja2324dae.proyectodaekt.repository

import com.gitlab.uja2324dae.proyectodaekt.model.SharedRide
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface SharedRideRepository : JpaRepository<SharedRide, Long> {
    @Query("SELECT r FROM SharedRide r WHERE r.owner.dni = ?1 AND ((?2 < r.arrivalTime AND ?3 > r.departureTime) OR (?3 > r.departureTime AND ?2 < r.arrivalTime))")
    fun findOverlappingRides(dni: String, departureTime: LocalDateTime, arrivalTime: LocalDateTime): List<SharedRide>

    fun findByOriginCityAndDestinationCityAndDepartureTimeBetween(
        originCity: String,
        destinationCity: String,
        departureTimeStart: LocalDateTime,
        departureTimeEnd: LocalDateTime
    ): List<SharedRide>

    fun findByRequests_User_DniAndDepartureTimeGreaterThan(
        dni: String,
        departureTime: LocalDateTime
    ): List<SharedRide>

    @Query("SELECT r FROM SharedRide r WHERE r.id = ?1 AND (r.owner.dni = ?2 OR EXISTS (SELECT p FROM r.passengers p WHERE p.dni = ?2))")
    fun findByIdIfUserIsPassengerOrDriver(id: Long, dni: String): SharedRide?
}