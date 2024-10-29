package com.gitlab.uja2324dae.proyectodaekt.util

class UserModelRegex {
    companion object {
        const val DNI = "\\d{8}[A-HJ-NP-TV-Z]"
        const val PHONE_NUMBER = "(\\+34|0034|34)?[6789]\\d{8}\$"
    }
}
