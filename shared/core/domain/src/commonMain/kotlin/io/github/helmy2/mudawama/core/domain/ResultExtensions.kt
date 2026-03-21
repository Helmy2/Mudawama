package io.github.helmy2.mudawama.core.domain


inline fun <D, E : DomainError, R> Result<D, E>.map(transform: (D) -> R): Result<R, E> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> Result.Failure(error)
    }

inline fun <D, E : DomainError, F : DomainError> Result<D, E>.mapError(transform: (E) -> F): Result<D, F> =
    when (this) {
        is Result.Success -> Result.Success(data)
        is Result.Failure -> Result.Failure(transform(error))
    }

inline fun <D, E : DomainError> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E : DomainError> Result<D, E>.onFailure(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Failure) action(error)
    return this
}

fun <D, E : DomainError> Result<D, E>.getOrNull(): D? =
    when (this) {
        is Result.Success -> data
        is Result.Failure -> null
    }

inline fun <D, E : DomainError> Result<D, E>.getOrElse(defaultValue: (E) -> D): D =
    when (this) {
        is Result.Success -> data
        is Result.Failure -> defaultValue(error)
    }

fun <D, E : DomainError> Result<D, E>.asEmptyResult(): EmptyResult<E> = map { }

inline fun <D, E : DomainError, T> Result<D, E>.fold(
    onSuccess: (D) -> T,
    onFailure: (E) -> T,
): T =
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Failure -> onFailure(error)
    }

inline fun <D, E : DomainError, T> Result<D, E>.flatMap(transform: (D) -> Result<T, E>): Result<T, E> =
    when (this) {
        is Result.Success -> transform(data)
        is Result.Failure -> this
    }


