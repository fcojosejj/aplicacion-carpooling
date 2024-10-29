package com.gitlab.uja2324dae.proyectodaekt.repository

import com.gitlab.uja2324dae.proyectodaekt.model.UserRating
import org.springframework.data.jpa.repository.JpaRepository

interface UserRatingRepository : JpaRepository<UserRating, Long>