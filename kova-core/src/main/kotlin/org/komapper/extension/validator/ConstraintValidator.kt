package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate

typealias ConstraintValidator<T> = IdentityValidator<T>

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun constrain(id: String, check: context(ValidationContext) () -> Unit) {
    accumulating { addLog(id) { check() } }
}

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = Validator { input ->
    addLog(constraint.id) {
        constraint.check(input)
        input to contextOf<ValidationContext>()
    }
}