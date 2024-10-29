package com.gitlab.uja2324dae.proyectodaekt.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty

@Entity
class SharedRideRequest(
    @OneToOne
    val user: User,

    @field:NotEmpty
    var message: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)