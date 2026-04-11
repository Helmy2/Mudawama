package io.github.helmy2.mudawama.athkar.domain.error

import io.github.helmy2.mudawama.core.domain.DomainError

sealed interface AthkarError : DomainError {
    data object DatabaseError : AthkarError
    data object NotificationSchedulingError : AthkarError
    data object InvalidInput : AthkarError
}
