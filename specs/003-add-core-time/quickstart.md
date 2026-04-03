# Quickstart: shared:core:time

**Date**: 2026-04-02
**Module**: `:shared:core:time`
**Package root**: `io.github.helmy2.mudawama.core.time`

---

## 1. Add the dependency

`shared:core:time` is re-exported by `shared:umbrella-core`. If your feature module
already depends on `umbrella-core`, no additional Gradle change is needed.

For direct use in another `shared:core:*` module:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.time)
        }
    }
}
```

---

## 2. Wire Koin (app-level, once)

```kotlin
startKoin {
    modules(
        timeModule(rolloverPolicy = RolloverPolicy.fixed(18)), // Islamic 18:00 rollover
        // other modules …
    )
}
```

Midnight rollover (Standard — the default):

```kotlin
startKoin { modules(timeModule()) }
```

The `RolloverPolicy` value should come from the user's stored preferences *before*
`startKoin` is called. Persistence is the responsibility of the Settings module.

---

## 3. Inject TimeProvider in a feature

```kotlin
class LogWirdUseCase(
    private val repo: HabitRepository,
    private val time: TimeProvider,   // resolved by Koin
) {
    suspend operator fun invoke(habitId: String) {
        val today = time.logicalDate()   // honours the configured rollover policy
        repo.logHabit(habitId, today)
    }
}

// Feature Koin module
val habitsModule = module {
    factory { LogWirdUseCase(get(), get()) }
}
```

---

## 4. Format dates for the database

```kotlin
import io.github.helmy2.mudawama.core.time.toIsoDateString

val dateStr: String = toIsoDateString(LocalDate(2026, 4, 2))
// => "2026-04-02"

val tsStr: String = toIsoDateString(Clock.System.now(), TimeZone.currentSystemDefault())
// => "2026-04-02"  (date in local timezone)
```

Output is always `yyyy-MM-dd`, ISO-8601, lexicographically sortable, safe for Room TEXT columns.

---

## 5. Freeze time in tests

`FakeTimeProvider` ships in `commonMain` and is available from any `commonTest` source set
without extra Gradle wiring.

```kotlin
val fake = FakeTimeProvider(
    fixedInstant = Instant.parse("2026-04-06T21:00:00Z"),
    policy = RolloverPolicy.fixed(18),
)

// hour=21 >= H=18  ->  next calendar day
assertEquals(LocalDate(2026, 4, 7), fake.logicalDate(TimeZone.UTC))

// Time-travel: rewind the clock
fake.fixedInstant = Instant.parse("2026-04-06T17:00:00Z")
assertEquals(LocalDate(2026, 4, 6), fake.logicalDate(TimeZone.UTC))
```

---

## 6. Test the Koin module

```kotlin
class TimeModuleTest {
    @BeforeTest fun setup()    { startKoin { modules(timeModule()) } }
    @AfterTest  fun teardown() { stopKoin() }

    @Test
    fun timeProviderIsSingleton() {
        val koin = GlobalContext.get()
        assert(koin.get<TimeProvider>() === koin.get<TimeProvider>())
    }
}
```

---

## 7. Rollover policy reference

| `offsetHour` | Constant | Behaviour |
|---|---|---|
| `0`  | `RolloverPolicy.Standard`  | Midnight rollover — logical day = calendar day |
| `3`  | `RolloverPolicy.fixed(3)`  | Night-owl: 00:00–02:59 still belongs to yesterday |
| `18` | `RolloverPolicy.fixed(18)` | Islamic-style: 18:00+ starts the next logical day |
| `21` | `RolloverPolicy.fixed(21)` | Late-evening rollover |

**Serialise**: `val stored: Int = policy.offsetHour`
**Restore**: `val policy = RolloverPolicy(stored)`

