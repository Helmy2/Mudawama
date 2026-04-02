# Tasks: shared:core:database Implementation

Created: 2026-03-23
Feature: shared:core:database

How to read: Tasks are ordered for dependency; earliest tasks are prerequisites for later work.

1) [X] Task: Project scaffolding
- Description: Create Gradle module `shared:core:database`. Add Room/KSP plugin and baseline Gradle config.
- Acceptance Criteria: Gradle builds, module compiles, Room KSP codegen runs generating expected Kotlin classes.
- Owner: Core infra engineer
- Effort: 1d
- Completed: 2026-04-01 — Used Room 2.7.1 KMP with KSP 2.3.6; module added to settings.gradle.kts

2) Task: Define API interfaces
- Description: Implement Kotlin interfaces for `Database`, `HabitRepository`, `HabitLogRepository`, `PrayerRepository`, `QuranRepository`, `OutboxRepository` in the `api` module.
- Acceptance Criteria: Interfaces reviewed and merged; example consumers can compile against 'api' without 'impl'.
- Owner: Backend/KMP dev
- Effort: 2d

3) [X] Task: Implement Room schema V1 (entities + DAOs)
- Description: Create HabitEntity, HabitLogEntity, QuranBookmarkEntity with Room annotations. Implement HabitDao, HabitLogDao, QuranBookmarkDao. MudawamaDatabase with @ConstructedBy and expect/actual constructor.
- Acceptance Criteria: Generated Kotlin types exist and basic CRUD DAO implementations compile.
- Owner: Database dev
- Effort: 3d
- Completed: 2026-04-01 — All entities/DAOs implemented; Room KSP generates _Impl classes and schema JSON

4) [X] Task: Implement DI modules
- Description: Provide Koin modules for commonMain (coreDatabaseModule), androidMain (androidCoreDatabaseModule), and iosMain (iosCoreDatabaseModule).
- Acceptance Criteria: Koin modules wire MudawamaDatabase and all DAOs; Android uses androidContext(), iOS uses NSHomeDirectory().
- Owner: Database dev
- Effort: 1w
- Completed: 2026-04-01 — All DI modules implemented and compile for both platforms

5) Task: Outbox & Sync primitives
- Description: Implement OutboxRepository, batching, and helper utilities for change serialization.
- Acceptance Criteria: Outbox push/fetch/mark implemented and tested.
- Owner: Sync dev
- Effort: 3d

6) Task: Migration harness & tests
- Description: Implement migration testing utilities and create migration files with automated tests from V1 -> V2.
- Acceptance Criteria: Migration tests pass on CI; harness easy to extend.
- Owner: QA/Eng
- Effort: 1w

7) Task: Encryptor interface & Android sample (KeyStore)
- Description: Add `Encryptor` interface and Android implementation using AndroidKeyStore. Integrate with SQLCipher or column-level encryption.
- Acceptance Criteria: Sample Android encryptor works; DB opens with passphrase and data persists encrypted.
- Owner: Security engineer
- Effort: 1w

8) Task: iOS Keychain sample & Native driver setup
- Description: Implement iOS `Encryptor` sample using Keychain, validate NativeSqliteDriver usage and framework export.
- Acceptance Criteria: iOS example can open DB and read/write data using exported framework.
- Owner: iOS/KMP dev
- Effort: 1w

9) Task: Testing & CI integration
- Description: Wire unit and migration tests into CI; add macOS runner tests for K/N if available.
- Acceptance Criteria: CI runs tests and reports results; flaky tests mitigated.
- Owner: DevOps
- Effort: 3d

10) Task: Performance testing & tuning
- Description: Implement benchmarks for bulk inserts and queries; tune indexes and batch sizes.
- Acceptance Criteria: Performance meets targets or documented trade-offs.
- Owner: Performance engineer
- Effort: 3d

11) Task: Docs & sample usage
- Description: Add README, usage examples, and migration docs for devs and platform teams.
- Acceptance Criteria: README contains code snippets for Android and iOS, instructions for key rotation.
- Owner: Tech writer
- Effort: 2d

12) Task: Release & SemVer tagging
- Description: Prepare initial v1.0.0 release, tag repo, and announce in changelog.
- Acceptance Criteria: Artifacts published (if relevant), CHANGELOG updated.
- Owner: Release manager
- Effort: 1d

Optional/Spike tasks
- Spike: SQLCipher proof-of-concept on iOS and Android build sizes (2-3d)
- Spike: Vector-clocks design review (1w)


