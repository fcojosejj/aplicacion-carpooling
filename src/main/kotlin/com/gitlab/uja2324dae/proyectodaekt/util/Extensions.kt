package com.gitlab.uja2324dae.proyectodaekt.util

import java.text.Normalizer

fun String.normalize(): String {
    return Normalizer.normalize(this.lowercase().replace(" ", ""), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}

