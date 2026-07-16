# Clinic Ledger OS (क्लिनिक लेजर) - v2.0 Ultra-Performance

An offline-first, voice-assisted clinic ledger and patient memory database designed for clinical practitioners in India. **v2.0** introduces an **Ultra-Performance "Body" (120FPS)** and a **Native Agentic Diagnostic Hub**, transforming the app from a simple ledger into a robust, high-fidelity clinical operating system.

---

## 🚀 v2.0 Ultra-Performance Upgrades

### 1. The "120FPS Body"
- **Aggressive Memoization**: Full utilization of `@Immutable` and `@Stable` modeling to hit 8ms frame targets for butter-smooth high-refresh-rate scrolling.
- **Zero-Lag Lists**: Re-engineered all major lists (Analytics, Search, Transactions) with advanced `LazyColumn` optimizations (`key` and `contentType`).
- **Standardized Architecture**: Renamed and reorganized all core hubs to follow professional Android industry standards (e.g., `MainDashboardScreen`, `LedgerSearchHub`).

### 2. Native Architectural Diagnostic Hub
- **Internal Maintenance Tool**: Shifted the `flow.jsx` vision into a functional, native Compose component with high-fidelity `Canvas` visuals.
- **Real-Time Monitoring**: Integrated live database metrics, NLU intent confidence logs, and a real-time log streamer from the `SystemGuardian`.
- **Integrated Testing**: Connected a comprehensive suite of **100+ JUnit 6 tests** to the diagnostic dashboard.

### 3. Agentic Brain & Personalization
- **Persistent Tuning**: Voice speed and Active Learning settings are now saved locally, persisting across app restarts.
- **Habit-Aware Pacing**: The Assistant automatically adjusts its response speed based on the doctor's learned speech patterns and hurried tone.
- **State-Preserving Navigation**: Fixed the language-switch bug; the app now perfectly preserves your current screen and data state during transitions.

---

## 🎨 Visual Identity & Hubs

| Hub | Description |
| :--- | :--- |
| **Main Dashboard** | The central command center with high-performance search and morning briefings. |
| **Ledger Search Hub** | Ultra-fast discovery with fuzzy matching for aliases and villages. |
| **Analytics Dashboard** | Deep financial insights, debtor aging buckets, and village-level metrics. |
| **Diagnostic Hub** | Full architectural transparency with live node status and log streams. |

---

## ⚡ Setup, Building, and Testing

### Prerequisites
- **Android SDK 37+** (Targeting latest Android APIs)
- **AGP 9.3.0**
- **JDK 26**
- **JUnit 6**

### Local Build
```bash
./gradlew :app:assembleDebug
```

### Comprehensive Testing (100+ Tests)
```bash
./gradlew test
```

---

## 📂 Project Package Structure (Refactored)

```
app/src/main/java/com/clinicledger/
│
├── data/
│   ├── local/              # Room Database v5.0 (SQLite Engine)
│   ├── models/             # @Immutable Data Models
│   └── repository/         # Cached Repositories (Village, Patient, Transaction)
│
├── service/                # ClinicalActionToolbox, ContextualBrain, SystemGuardian
│
├── ui/
│   ├── util/               # LocaleManager, FeedbackProvider, HabitMapper
│   └── compose/            # Standardized Hubs & Screens
│       ├── MainDashboardScreen.kt   # Root Navigation Hub
│       ├── LedgerSearchHub.kt       # Search & Listing Engine
│       ├── PatientRegistrationScreen.kt # Ergonomic Forms
│       ├── ArchitecturalDiagnosticHub.kt # Native Maintenance Tool
│       └── ...
```

---

## 🌐 Industrial-Grade Reliability
Clinic Ledger OS is designed to be **ironclad**. With no deletions allowed, every adjustment is an entry in a medical audit trail. The **System Guardian** runs continuous health checks on data integrity and semantic link consistency, ensuring the ledger remains a source of truth for the clinic.
