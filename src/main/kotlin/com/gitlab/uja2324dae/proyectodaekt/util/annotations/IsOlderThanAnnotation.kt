package com.gitlab.uja2324dae.proyectodaekt.util.annotations

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IsOlderThanValidator::class])
annotation class IsOlderThan(
    val message: String = "The date is not older than the specified years!",
    val adultAge: Int = 18,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class IsOlderThanValidator : ConstraintValidator<IsOlderThan, LocalDate> {
    private var adultAge: Int = 18

    override fun initialize(constraintAnnotation: IsOlderThan?) {
        if (constraintAnnotation != null) {
            adultAge = constraintAnnotation.adultAge
        }
    }

    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return false
        }

        return value.isBefore(LocalDate.now().minusYears(adultAge.toLong()))
    }

}