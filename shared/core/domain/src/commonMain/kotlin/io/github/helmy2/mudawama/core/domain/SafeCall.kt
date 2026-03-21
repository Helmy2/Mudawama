package io.github.helmy2.mudawama.core.domain

import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a given [block] block safely, catching any potential exceptions and wrapping
 * the result in a domain-specific result type.
 *
 * This utility is typically used in the domain layer to handle unexpected errors
 * during data operations or business logic execution, ensuring that the application
 * remains stable and provides meaningful error feedback.
 */
suspend inline fun <T, E : DomainError> safeCall(
    crossinline block: suspend () -> T,
    crossinline onError: (Throwable) -> E,
): Result<T, E> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Failure(onError(e))
    }