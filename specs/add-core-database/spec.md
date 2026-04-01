# Spec: shared:core:database — Offline-first persistence layer (KMP)

Created: 2026-03-23
Feature: shared:core:database
Short name: add-core-database

Overview
--------
Purpose: Provide a robust, idiomatic Kotlin Multiplatform (KMM) offline-first persistence layer for Mudawama shared/core modules. This module will be the canonical local storage implementation consumed by feature `:data` modules (e.g., habits, quran, prayer, athkar) and exported via the umbrella-core for iOS.

Goals:
- Reliable local storage (CRUD + queries) for domain entities.
- Clear, stable, platform-agnostic public API for Kotlin consumers and simple adapters for iOS consumers.
- Offline-first semantics with change-tracking to support optional future server sync.
- Strong migration, encryption, testing and monitoring story.

Scope
-----
The module MUST provide:
- Persistence primitives and repositories for feature-level data: habits, habit occurrences/logs, prayer entries, quran bookmarks, tasbeeh counters, user settings, and a generic outbox/change-log for syncing.
- A documented Kotlin Multiplatform API (interfaces + recommended concrete implementations) exposing suspend/Flow-based read/write patterns.
- SQL mapping (SQLDelight recommended) with migration hooks and testable schema definitions.
- Platform adapters for database drivers, platform key stores for encryption, and a pluggable Encryptor interface.

The module will be consumed by:
- feature/*:data modules (primary consumers) for authoritative local reads/writes
- shared:umbrella-core for export to iOS
- higher-level sync orchestrators (optional) that coordinate server syncs using the outbox/change-log

The module will NOT:
- Implement network sync transports or server-side logic (only provide change-tracking/outbox primitives and sync metadata).
- Own feature-specific business logic beyond canonical mapping and simple invariants (e.g., it does not decide "what constitutes a habit completion window").

Responsibilities & Boundaries
-----------------------------
Owned by shared:core:database:
- Schema definitions, migrations, and storage guarantees (ACID within the local engine).
- Repository interfaces for data access and concurrency-safe implementations.
- Change-tracking / outbox primitives and utilities to build syncable payloads.
- Encryption hooks and platform integration for at-rest encryption.
- Test helpers (in-memory DB drivers, migration test harness).

Owned by callers (feature:data modules / use-cases):
- Higher-level validation and business rules (e.g., what counts as a completed prayer for UI purposes).
- Orchestration of remote sync flows (what to upload, when to retry) though helpers exist in this module.
- UI-specific transformations and view models.

Key Concepts Extracted
----------------------
Actors: mobile app features (habits, quran, prayer), umbrella-core, mobile platforms (Android/iOS)
Actions: store/read/update/delete, stream changes, create outbox entries, perform migrations
Data: habit definitions, habit logs, prayer entries, quran bookmarks, counters, sync metadata
Constraints: KMM compatibility, minimal heavy runtime dependencies, privacy-first design (local-first storage), iOS-friendly API surface

Public API (high-level)
-----------------------
Design principles:
- Expose interfaces from `shared:core:database:api` (pure Kotlin) and provide `shared:core:database:impl` for SQLDelight-backed implementations.
- Use coroutines + Flow for async and streaming reads.
- Keep platform-specific details behind factory functions and interfaces (DatabaseDriverFactory, Encryptor).

Suggested package structure (Kotlin):
- `com.mudawama.core.database` (api surface)
  - `Database` (top-level entry)
  - `Repositories` (interfaces)
  - `entities` (domain canonical entities)
  - `storerepr` (storage representations / mappers)
  - `sync` (outbox/change log types)
  - `migrations` (migration helpers)

Core interfaces and signatures (consumer-facing):

// Kotlin (shared module)
interface Database : Closeable {
    val habitRepository: HabitRepository
    val prayerRepository: PrayerRepository
    val quranRepository: QuranRepository
    val counterRepository: CounterRepository
    val outboxRepository: OutboxRepository

    suspend fun transaction(block: suspend TransactionScope.() -> Unit)
}

interface HabitRepository {
    suspend fun upsertHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    fun observeHabits(): Flow<List<Habit>>
    suspend fun getHabitById(habitId: String): Habit?
}

interface HabitLogRepository {
    suspend fun addLog(log: HabitLog)
    fun observeLogsForHabit(habitId: String): Flow<List<HabitLog>>
    suspend fun queryLogs(habitId: String, since: Instant?, until: Instant?, limit: Int = 100): List<HabitLog>
}

// Sync/outbox
interface OutboxRepository {
    suspend fun pushChange(change: OutboxChange)
    suspend fun fetchNextBatch(limit: Int): List<OutboxChange>
    suspend fun markBatchUploaded(changeIds: List<String>)
}

// Transaction scope
interface TransactionScope {
    suspend fun <T> runSql(block: suspend () -> T): T
}

Example usage (Android / KMP consumer)

// KMP shared code
suspend fun useHabits(database: Database) {
    database.transaction {
        val h = database.habitRepository.getHabitById("habit-123")
        // ...domain logic...
    }
}

// Observing from a ViewModel (Kotlin)
val job = viewModelScope.launch {
   database.habitRepository.observeHabits()
      .collect { habits -> /* update UI state */ }
}

iOS consumer (Swift) — high-level guidance
- Compile `shared:umbrella-core` to a framework exposing `Database` as Kotlin/Native exported APIs.
- Provide a small Swift wrapper where needed to convert callbacks to Combine or async/await (iOS 13+) patterns.

Example Swift (pseudocode):

let db = MudawamaCoreDatabaseFactory().createDatabase(iosEncryptor: MyKeychainEncryptor())
Task {
   let habits = try await db.habitRepository.getHabitById("habit-123")
   // Map to Swift models
}

Data Model Definitions
----------------------
Canonical domain entities (Kotlin data classes)

data class Habit(
  val id: String, // UUID or stable id generated by feature module
  val title: String,
  val type: HabitType,
  val icon: String?,
  val metadata: Map<String, String>,
  val createdAt: Instant,
  val updatedAt: Instant?
)

data class HabitLog(
  val id: String,
  val habitId: String,
  val timestamp: Instant,
  val value: Int?, // for numeric counters
  val note: String?,
  val createdAt: Instant
)

data class PrayerEntry(
  val id: String,
  val date: LocalDate,
  val prayer: PrayerType,
  val completedAt: Instant?,
  val status: PrayerStatus,
  val createdAt: Instant
)

data class QuranBookmark(
  val id: String,
  val surah: Int,
  val ayah: Int,
  val updatedAt: Instant
)

data class Counter(
  val id: String,
  val name: String,
  val currentValue: Int,
  val target: Int?
)

Sync/outbox model

data class OutboxChange(
  val id: String,
  val entityType: String,
  val entityId: String,
  val operation: OutboxOperation, // CREATE, UPDATE, DELETE
  val payload: String, // JSON serialized domain snapshot
  val createdAt: Instant,
  val attempts: Int = 0
)

Storage representation (SQLDelight example schema)

-- File: database.sq

CREATE TABLE habit (
  id TEXT NOT NULL PRIMARY KEY,
  title TEXT NOT NULL,
  type TEXT NOT NULL,
  icon TEXT,
  metadata TEXT DEFAULT '{}', -- JSON string
  created_at INTEGER NOT NULL, -- epoch millis
  updated_at INTEGER
);

CREATE INDEX idx_habit_updated_at ON habit (updated_at);

CREATE TABLE habit_log (
  id TEXT NOT NULL PRIMARY KEY,
  habit_id TEXT NOT NULL REFERENCES habit(id) ON DELETE CASCADE,
  timestamp INTEGER NOT NULL,
  value INTEGER,
  note TEXT,
  created_at INTEGER NOT NULL
);

CREATE INDEX idx_habit_log_habit_id_ts ON habit_log (habit_id, timestamp);

CREATE TABLE prayer_entry (
  id TEXT NOT NULL PRIMARY KEY,
  date TEXT NOT NULL, -- ISO local date
  prayer TEXT NOT NULL,
  completed_at INTEGER, -- epoch millis
  status TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE INDEX idx_prayer_date ON prayer_entry (date);

CREATE TABLE quran_bookmark (
  id TEXT NOT NULL PRIMARY KEY,
  surah INTEGER NOT NULL,
  ayah INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE counter (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  current_value INTEGER NOT NULL DEFAULT 0,
  target INTEGER
);

CREATE TABLE outbox_change (
  id TEXT NOT NULL PRIMARY KEY,
  entity_type TEXT NOT NULL,
  entity_id TEXT NOT NULL,
  operation TEXT NOT NULL,
  payload TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  attempts INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_created_at ON outbox_change (created_at);

Offline-first Behavior
----------------------
Read/write semantics:
- Canonical reads are local (single source of truth is the local DB for offline-first MVP).
- Writes are persisted immediately to the local DB and also create an OutboxChange entry when the caller requests syncable behavior.
- Observers (Flow) provide reactive updates to consumers. Flows must be cold and safe to collect on any dispatcher.

Caching and consistency:
- No separate in-memory cache by default. SQLDelight queries returned via Flows are considered the cache layer and should be observed directly.
- For heavy read patterns (large lists), repositories may provide paged queries or summary projections to optimize memory.

Conflict resolution:
- Local-first write wins by default for MVP (last-local-update wins). Each row carries an updated_at timestamp.
- For future server sync, allow the sync orchestrator to provide conflict resolution policies per entity:
  - Merge strategy using last-updated (timestamp)
  - Field-level merge for JSON payloads when necessary
  - Optionally support vector clocks if multi-device simultaneous edits become a requirement [NEEDS CLARIFICATION: do we need multi-device strong conflict resolution?]

Recommended sync primitives:
- Outbox change-log table (guaranteed ordered writes). Sync clients read batches, upload, and call markBatchUploaded to delete/ack entries.
- Optionally maintain an entity-level change_sequence integer to support incremental sync by sequence.
- Provide utility to compute "changed since" using updated_at timestamps.

Sync Strategy (design)
----------------------
Principles:
- Keep the database module agnostic of network transports.
- Provide an opinionated outbox pattern and helpers that make implementing a sync worker straightforward.

Outbox pattern flow:
1. Local write occurs and also creates/pushes an OutboxChange entry in the same transaction.
2. A background sync worker (feature-level or app-level) fetches a batch (limit configurable), attempts upload, and marks successful items via `markBatchUploaded`.
3. On permanent failures, escalate via an error counter and a TTL, then surface to the app for manual remediation.

Change-tracking options:
- Basic: `updated_at` timestamp + outbox.
- Stronger: add `change_sequence` monotonic integer column and store the last synced sequence per remote user.
- For concurrency heavy requirements, consider vector clocks (advanced, increases complexity).

Retries/backoff:
- Sync worker should perform exponential backoff on transient failures, with a per-batch max attempts threshold configurable in higher-level code.

KMM Implementation Choices
--------------------------
Recommendation: Use SQLDelight as the primary persistence engine.
Rationale:
- SQLDelight is battle-tested for KMM and produces idiomatic Kotlin interfaces across platforms.
- Compiles to native for iOS with small runtime overhead.
- Good support for migrations, typed queries, and Flow bindings via coroutines.
- Avoids bringing in heavy proprietary runtimes (Realm has heavier native footprint and licensing considerations).

Driver choices and platform adapters:
- Android: Use SQLDelight Android driver with optional SQLCipher wrapping for at-rest encryption.
- iOS (K/N): Use SQLDelight Native driver (NativeSqliteDriver). To enable encryption on iOS, use SQLCipher built into the native driver or leverage an encrypted file system provided by platform if available.
- Provide a `DatabaseDriverFactory` interface with platform implementations:

interface DatabaseDriverFactory {
    fun createDriver(schema: SqlDriver.Schema, name: String?, openHelper: Any? = null): SqlDriver
}

Encryption & Security
---------------------
Threat model and decisions:
- Primary threat: device loss or physical access to device storage.
- Secondary threat: attacker with app-level code execution (rooted/jailbroken) — out of scope for full protection, but mitigations are documented.

At-rest encryption:
- Provide a pluggable `Encryptor` interface used to encrypt database files or encrypt sensitive columns before persist.
- Recommend platform-backed key stores:
  - Android: AndroidKeyStore + AES-GCM per-db file key (using KeyStore to protect the key).
  - iOS: Keychain + Keychain-protected symmetric key (AES-GCM).

Pluggable interface:

interface Encryptor {
    suspend fun getDatabasePassphrase(): ByteArray
    fun clear()
}

The module should support two modes:
1. Transparent DB file encryption via SQLCipher (preferred for simplicity): database is fully encrypted at rest using a passphrase from Encryptor.
2. Column-level encryption: for compliance or incremental rollout, sensitive fields (payload JSON) are encrypted before writing.

Key management:
- Keys must never be derived from user passwords unless explicitly chosen by higher-level features (documented).
- Provide a platform sample implementation that stores an AES key in KeyStore / Keychain and returns it to SQLCipher.
- Provide instructions for rotating keys: create a migration to re-encrypt the DB with the new key.

Migrations
----------
Strategy:
- Use SQLDelight's migration mechanism with versioned SQL migration files (V1__init.sql, V2__add_column.sql...).
- Each migration file must be deterministic and tested with the included migration test harness.
- Provide a MigrationTest helper that accepts pairs of (oldSchemaDump, newSchemaApplier) and runs a set of sample data to validate migration.

APIs:
- Expose a `Migration` interface for optional programmatic migrations that can't be expressed in SQL alone (e.g., column re-encryption).

Testing requirements:
- Each migration must include an automated test that:
  - Creates DB at previous version, inserts representative data
  - Runs migration
  - Validates important invariants after migration (row counts, important fields non-null, referential integrity)

Concurrency & Threading Model
----------------------------
- Use Kotlin coroutines everywhere in the public API.
- Repository implementations must confine SQL operations to a single dispatcher (e.g., a `DispatcherProvider.io` dispatcher) and use SQLDelight transaction APIs.
- Provide a `DispatcherProvider` abstraction to allow tests to switch to an immediate dispatcher.
- Ensure that SQLDelight's drivers used are thread-safe per their platform drivers (Native driver is thread-safe when using proper connection pooling). Document any caveats.

Dependency & DI Integration
---------------------------
- Provide a `databaseModule` Koin module in `shared:core:database:di` that wire-ups:
  - Platform-specific driver via `DatabaseDriverFactory`
  - Encryptor implementation (pluggable)
  - `Database` singleton (scoped to app lifecycle)

Sample Koin registration (Kotlin):

val coreDatabaseModule = module {
    single<DatabaseDriverFactory> { PlatformDatabaseDriverFactory(get()) }
    single<Encryptor> { PlatformEncryptor() }
    single<Database> {
        val driver = get<DatabaseDriverFactory>().createDriver(MudawamaDatabase.Schema, "mudawama.db")
        MudawamaDatabase(driver, get(), Dispatchers.IO)
    }
}

Testing Strategy
----------------
Unit tests:
- Use SQLDelight in-memory driver or `SqliteDriver(MEMORY)` to test repository logic.
- Mock Encryptor for tests.

Integration tests:
- Migration tests described earlier.
- End-to-end repository tests running against the native driver on CI (macOS for K/N and Android emulator for JVM driver).

Performance & Load tests:
- Create tests for bulk inserts (e.g., inserting 10k HabitLog rows) to measure write throughput and average query latency.

Test fixtures:
- Helper factories to create sample Habit, PrayerEntry, Bookmark fixtures with deterministic IDs.

Performance Targets & Monitoring
-------------------------------
Targets (MVP/default):
- DB initialization: < 200ms on typical modern devices
- Average simple query latency (single row): < 10ms
- Bulk query (paged list 100 rows): < 50ms
- Bulk inserts (1000 rows): complete within 2 seconds on typical devices

Monitoring hooks:
- Expose instrumentation points for repository operations (timings, counts)
- Provide a `DatabaseMetrics` interface where the app can register a metrics backend (optional). Metrics to expose: query latency histogram, transaction counts, migration durations, outbox backlog size.

Non-functional requirements
---------------------------
- Privacy: Data must remain local by default. Any sync must be opt-in and explicit.
- Size: Minimize native binary size impact (favor SQLDelight over heavier runtimes).
- API stability: Follow semantic versioning and deprecation policy documented below.

API Stability Policy & Versioning
--------------------------------
- Module follows semantic versioning (MAJOR.MINOR.PATCH).
- Public API (interfaces in `api` artifact) are protected: any breaking change bumps MAJOR version.
- Deprecation policy: Mark API with @Deprecated and provide one minor release cycle for clients to migrate before removing in next MAJOR.

Acceptance Criteria
-------------------
The feature is accepted when:
- `shared:core:database` builds on Linux/Mac/Android and compiles into umbrella-core framework for iOS.
- Repositories implement CRUD operations with unit tests covering happy path and common edge cases.
- Migration test suite validates migration from V1 -> latest.
- Outbox pattern functions: pushChange, fetchNextBatch, markBatchUploaded implemented and tested.
- Encryption hooks present and sample platform implementations for Android KeyStore and iOS Keychain included.
- Example consumer usages (habits feature) implemented in `specs` or sample code demonstrating reads, writes and flows.

Assumptions
-----------
- Preferred persistence library is SQLDelight (explicit per repo constraints).
- App uses Koin; DI integration will follow existing patterns in `docs/ARCHITECTURE.md`.
- Sync transport and server API are out-of-scope for this module; higher-level features will implement them.
- Multi-device strong-conflict strategies (vector clocks) are optional and only needed if product requires concurrent cross-device edits.

Open Questions / Clarifications
------------------------------
1. [NEEDS CLARIFICATION: Multi-device conflict resolution]
   - Context: Whether we must support advanced multi-device conflict resolution (vector clocks) or last-write-wins timestamp merging is acceptable for MVP.
   - Impact: Choosing vector clocks increases implementation complexity and storage requirements.

(Only 1 clarification was inserted because a reasonable default exists: last-write-wins.)

Examples for feature modules (Habit) — usage snippets
---------------------------------------------------
Kotlin (in feature:data)

class HabitLocalDataSource(private val database: Database) {
    suspend fun addHabit(h: Habit) {
        database.transaction {
            database.habitRepository.upsertHabit(h)
            database.outboxRepository.pushChange(OutboxChange(...)) // optional
        }
    }

    fun observeHabits(): Flow<List<Habit>> = database.habitRepository.observeHabits()
}

Swift (iOS) — consuming exported framework

// Pseudocode: generated Kotlin/Native API exposed to Swift
let db = MudawamaCoreDatabaseFactory().createDatabase(iosEncryptor: KeychainEncryptor())
Task {
  let habits = try await db.habitRepository.observeHabits().first()
  // Map to Swift structs
}

Files created by this spec
-------------------------
- specs/add-core-database/spec.md (this file)



