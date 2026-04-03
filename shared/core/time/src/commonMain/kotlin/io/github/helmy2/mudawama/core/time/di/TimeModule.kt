package io.github.helmy2.mudawama.core.time.di

import io.github.helmy2.mudawama.core.time.RolloverPolicy
import io.github.helmy2.mudawama.core.time.SystemTimeProvider
import io.github.helmy2.mudawama.core.time.TimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates the Koin module that binds [TimeProvider] as a singleton.
 *
 * The [rolloverPolicy] is closed over at module-creation time, so all injection
 * sites receive the same policy without needing `parametersOf()` (FR-011, Decision 3).
 *
 * **Usage — Standard midnight rollover** (default):
 * ```kotlin
 * startKoin {
 *     modules(
 *         timeModule(),
 *         // other modules …
 *     )
 * }
 * ```
 *
 * **Usage — Islamic evening rollover at 18:00**:
 * ```kotlin
 * startKoin {
 *     modules(
 *         timeModule(RolloverPolicy.fixed(18)),
 *         // other modules …
 *     )
 * }
 * ```
 *
 * @param rolloverPolicy The policy to use when computing the logical date.
 *   Defaults to [RolloverPolicy.Standard] (midnight) to satisfy US4-Scenario-2.
 */
fun timeModule(rolloverPolicy: RolloverPolicy = RolloverPolicy.Standard): Module = module {
    single<TimeProvider> { SystemTimeProvider(rolloverPolicy) }
}

