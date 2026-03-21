package io.github.helmy2.mudawama.core.domain

/**
 * A sealed interface representing the result of an operation that can either succeed with data
 * or fail with a domain-specific error.
 *
 * This provides a type-safe alternative to throwing exceptions for expected business logic
 * failures, allowing for explicit error handling in the domain layer.
 *
 * @param D The type of the data returned on [Success].
 */
sealed interface Result<out D, out E : DomainError> {
    data class Success<out D>(
        val data: D,
    ) : Result<D, Nothing>

    data class Failure<out E : DomainError>(
        val error: E,
    ) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>



