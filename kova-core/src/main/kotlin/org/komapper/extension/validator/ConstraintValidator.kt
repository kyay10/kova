package org.komapper.extension.validator

import arrow.core.nel
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.withError

typealias ConstraintValidator<T> = IdentityValidator<T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = Validator { input ->
    addLog(constraint.id) {
        either {
            withError({ it.details }) { constraint.check(input).bind() }
            input to contextOf<ValidationContext>()
        }
    }
}

context(c: ValidationContext)
private val Message.details
    get() = when (this) {
        is Message.Text, is Message.Resource -> SimpleFailureDetail(contextOf<ValidationContext>(), this).nel()
        is Message.ValidationFailure -> details
    }