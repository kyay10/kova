package org.komapper.extension.validator

import arrow.core.raise.context.either

typealias ConstraintValidator<T> = IdentityValidator<T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = Validator { input ->
    addLog(constraint.id) {
        either {
            accumulateUnless(failFast) {
                constraint.check(input)
                input to contextOf<ValidationContext>()
            }
        }
    }
}