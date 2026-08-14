# 🏥 Clinic Ledger OS (क्लिनिक लेजर)

> **An offline-first, voice-assisted clinic ledger and patient memory database**
> for clinical practitioners in India. v2.0 adds an Ultra-Performance "Body"
> (120FPS) and a Native Agentic Diagnostic Hub.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose) ![License](https://img.shields.io/badge/License-GPL--3.0--or--later-blue?style=for-the-badge) ![CI](https://github.com/gaganjainse/ClinicLedger/actions/workflows/android-ci.yml.yml/badge.svg)

- **License:** GPL-3.0-or-later
- **Owner:** Gagan Jain ([@gaganjainse](https://github.com/gaganjainse))
- **Stack:** Kotlin · Jetpack Compose · Room · Android SDK 37+ · AGP 9.3.0

---

## v2.0 Ultra-Performance

- **120FPS Body** — `@Immutable`/`@Stable` modeling and `LazyColumn` optimizations for high-refresh-rate scrolling.
- **Native Architectural Diagnostic Hub** — live database metrics, NLU intent confidence logs, and a real-time log streamer from the SystemGuardian.
- **Agentic Brain & Personalization** — persistent voice speed + Active Learning, habit-aware pacing, state-preserving navigation.

## Hubs

| Hub | Description |
| :--- | :--- |
| **Main Dashboard** | Central command center with high-performance search and morning briefings. |
| **Ledger Search Hub** | Ultra-fast discovery with fuzzy matching for aliases and villages. |
| **Analytics Dashboard** | Financial insights, debtor aging buckets, village-level metrics. |
| **Diagnostic Hub** | Architectural transparency with live node status and log streams. |

## Quick start

```bash
./gradlew :app:assembleDebug     # build
./gradlew :app:testDebugUnitTest # test (100+ JUnit tests)
```

## Status

CI green. Security: [SECURITY.md](SECURITY.md).

## Documentation index

- **Compiled reading:** [shesh-docs](https://github.com/gaganjainse/shesh-docs)

## License

GPL-3.0-or-later — see [LICENSE](LICENSE).
