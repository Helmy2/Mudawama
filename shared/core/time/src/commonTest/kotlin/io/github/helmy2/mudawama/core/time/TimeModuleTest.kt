package io.github.helmy2.mudawama.core.time

import io.github.helmy2.mudawama.core.time.di.timeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TimeModuleTest {

    private lateinit var koinApp: KoinApplication

    @BeforeTest
    fun setup() {
        koinApp = startKoin {
            modules(timeModule())
        }
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun timeProviderIsSingleton() {
        val first = koinApp.koin.get<TimeProvider>()
        val second = koinApp.koin.get<TimeProvider>()
        assertTrue(first === second, "Expected the same TimeProvider singleton instance")
    }

    @Test
    fun defaultModuleResolvesWithoutError() {
        val provider = koinApp.koin.get<TimeProvider>()
        assertNotNull(provider.nowInstant(), "nowInstant() must return a non-null Instant")
        assertNotNull(provider.logicalDate(), "logicalDate() must return a non-null LocalDate")
    }

    @Test
    fun customPolicyModuleLoads() {
        stopKoin()
        koinApp = startKoin {
            modules(timeModule(RolloverPolicy.fixed(18)))
        }
        val provider = koinApp.koin.get<TimeProvider>()
        assertNotNull(provider, "TimeProvider must resolve with a custom policy")
    }
}

