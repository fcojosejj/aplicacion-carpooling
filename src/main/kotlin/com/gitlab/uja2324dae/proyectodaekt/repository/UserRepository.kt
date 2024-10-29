package com.gitlab.uja2324dae.proyectodaekt.repository

import com.gitlab.uja2324dae.proyectodaekt.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): User?
    fun findByDni(dni: String): User?
    fun findByEmailAndPassword(email: String, password: String): User?
}