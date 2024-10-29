package com.gitlab.uja2324dae.proyectodaekt.model.dto

import com.gitlab.uja2324dae.proyectodaekt.model.User
import java.io.Serializable
import java.time.LocalDate

data class UserDto(
    val dni: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val birthdate: LocalDate? = null,
    val password: String? = null
) : Serializable {
    fun toUser(): User {
        return User(
            dni = this.dni!!,
            firstName = this.firstName!!,
            lastName = this.lastName!!,
            email = this.email!!,
            phone = this.phone!!,
            birthdate = this.birthdate!!,
            password = this.password!!
        )
    }

    companion object {
        fun fromUser(user: User): UserDto {
            return UserDto(
                dni = user.dni,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                phone = user.phone,
                birthdate = user.birthdate
            )
        }
    }
}