package org.komapper.extension.validator

import arrow.core.Either
import arrow.core.left
import arrow.core.nel

typealias ConstraintValidator<T> = IdentityValidator<T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = Validator { input ->
    addLog(constraint.id) {
        when (val result = constraint.check(input)) {
            is ConstraintResult.Satisfied -> Either.Right(input to contextOf<ValidationContext>())
            is ConstraintResult.Violated -> when (result.message) {
                is Message.Text, is Message.Resource ->
                    SimpleFailureDetail(contextOf<ValidationContext>(), result.message).nel()

                is Message.ValidationFailure -> result.message.details
            }.left()
        }
    }
}