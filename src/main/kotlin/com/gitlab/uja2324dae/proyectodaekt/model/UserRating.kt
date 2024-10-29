package com.gitlab.uja2324dae.proyectodaekt.model

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size

@Entity
class UserRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    val sharedRide: SharedRide,

    @ManyToOne
    val ratedBy: User,

    @field:DecimalMin(value = "1", message = "The minimum rating is 1 stars", inclusive = true)
    @field:DecimalMax(value = "5", message = "The maximum rating is 5 stars", inclusive = true)
    val rating: Int,

    @field:Size(min = 0, max = 200, message = "The rating message cannot be longer than 200 characters")
    val message: String
)