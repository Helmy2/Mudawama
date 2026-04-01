# Plan: Implement shared:core:database

Created: 2026-03-23
Feature: shared:core:database

Milestones & Phases
-------------------
1. Design & Approval (1 week)
   - Review spec with stakeholders and finalize data model and API
   - Approve migration strategy and encryption approach

2. Scaffold & Module Setup (2 days)
   - Create Gradle modules: `shared:core:database:api`, `:impl`, `:di`, `:testutils`
   - Add SQLDelight configuration to Gradle
   - Add Koin module skeleton and driver factory interfaces

3. Core Implementation (2-3 weeks)
   - Implement SQLDelight schema and generated models
   - Implement repositories (Habit, HabitLog, PrayerEntry, QuranBookmark, Counter)
   - Implement outbox repository and utilities
   - Provide DatabaseFactory and Database class

4. Migrations & Migration Tests (1 week)
   - Create initial migrations (V1 schema)
   - Write migration harness and tests (V1 -> V2 samples)

5. Encryption & Platform Adapters (1 week)
   - Implement Encryptor interface samples for Android (KeyStore) and iOS (Keychain)
   - Integrate SQLCipher with SQLDelight drivers or provide column-level encryption

6. Testing & CI (1 week)
   - Add unit tests, integration tests, migration tests
   - Configure CI (macOS runner for K/N tests, Android emulator for JVM) to run tests

7. Docs, Samples & Export (3 days)
   - Add README, usage examples for Android and iOS
   - Export umbrella-core example showing DB usage in iOS

8. Polish & Performance Testing (3 days)
   - Run load tests, optimize queries and indexes
   - Add metrics hooks

Total estimated effort: 7-9 weeks (1.5 - 2 months) depending on team size and parallelism

Epics & Tasks (high-level)
- Epic: Module scaffolding
  - Create gradle modules and configuration
  - Add SQLDelight Gradle plugin
  - Create sample schema and run codegen

- Epic: Repositories & APIs
  - Define interfaces in api module
  - Implement repositories in impl module
  - Add transaction support and testing

- Epic: Migrations & Encryption
  - Create migration files
  - Integrate SQLCipher and key management
  - Add migration tests and key rotation docs

- Epic: Testing & CI
  - Add unit and integration tests
  - Configure CI to run tests on macOS and Android

Risk & Mitigation
-----------------
- SQLCipher integration complexities: prototype early in Spike branch and validate build sizes and platform compatibility.
- Multi-device conflicts: defer to optional sync orchestrator; MVP uses last-write-wins.
- CI macOS runners are limited: use targeted tests on macOS for K/N and run broader tests on Android emulators.

Deliverables per milestone
--------------------------
- Design docs & finalized spec
- Gradle modules and SQLDelight schema
- Repository implementations and public API
- Migration tests and sample Android/iOS encryptor
- CI pipeline entries and performance test results
- README and sample consumer code


