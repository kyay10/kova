package org.komapper.extension.validator

typealias ConstraintValidator<T> = IdentityValidator<T>

fun <T> ConstraintValidator(constraint: Constraint<T>): ConstraintValidator<T> = Validator { input ->
    addLog(constraint.id) {
        when (val result = constraint.check(input)) {
            is ConstraintResult.Satisfied -> ValidationResult.Success(input, contextOf<ValidationContext>())
            is ConstraintResult.Violated -> {
                ValidationResult.Failure(
                    when (result.message) {
                        is Message.Text, is Message.Resource -> listOf(
                            SimpleFailureDetail(contextOf<ValidationContext>(), result.message)
                        )

                        is Message.ValidationFailure -> result.message.details
                    }
                )
            }
        }
    }
}