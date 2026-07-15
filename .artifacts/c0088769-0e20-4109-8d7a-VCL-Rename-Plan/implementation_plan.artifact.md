# Implementation Plan - Project Cleanup and Rename

This plan covers renaming the project from "Clinic Ledger" to "Clinic Ledger", fixing the CI build, cleaning up documentation, and integrating useful features from the `codex` branch.

## User Review Required

> [!IMPORTANT]
> I am renaming the root project name and package-related strings. This may affect your local IDE configuration (you might need to re-import or sync).
> I am also updating the Gradle version to **8.11** to fix the CI build failure.

## Proposed Changes

### Project Renaming
Renaming "Clinic Ledger" to "Clinic Ledger" and "क्लिनिक लेजर" to "क्लिनिक लेजर" (removing "Village"/"ग्राम" prefix).

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/gagan/Downloads/ClinicLedger-main/settings.gradle.kts)
- Update `rootProject.name` to `"Clinic Ledger (क्लिनिक लेजर)"`.

#### [MODIFY] [build.gradle](file:///C:/Users/gagan/Downloads/ClinicLedger-main/build.gradle)
- Update project description in comments.

#### [MODIFY] [app/build.gradle](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/build.gradle)
- Update project description in comments.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/AndroidManifest.xml)
- Update label/description comments.

#### [MODIFY] [VillageClinicLedgerDatabase.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/villageclinicledger/data/local/VillageClinicLedgerDatabase.kt)
- Update KDoc/comments.

#### [MODIFY] [strings.xml](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/res/values/strings.xml)
- Update `app_name` to "Clinic Ledger".
- Update other strings containing "Clinic Ledger".

#### [MODIFY] [strings.xml (Hindi)](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/res/values-hi/strings.xml)
- Update `app_name` to "क्लिनिक लेजर".
- Update other strings containing "क्लिनिक लेजर".

### CI and Build Fixes
Fixing the "Gradle version 9.5 does not exist" error.

#### [MODIFY] [gradle-wrapper.properties](file:///C:/Users/gagan/Downloads/ClinicLedger-main/gradle/wrapper/gradle-wrapper.properties)
- Downgrade Gradle to `8.11` (or latest stable).

#### [MODIFY] [android-ci.yml](file:///C:/Users/gagan/Downloads/ClinicLedger-main/.github/workflows/android-ci.yml)
- Update `gradle-version` to `8.11`.

### Documentation Cleanup
Cleaning up `README.md`, `flow.jsx`, and `metadata.json`.

#### [MODIFY] [README.md](file:///C:/Users/gagan/Downloads/ClinicLedger-main/README.md)
- Rename project headings.
- Remove "Generated from google-gemini/aistudio-repository-template".
- Remove links to Google AI Studio (except the "I made this on..." part).
- Update the "About" section as requested.

#### [MODIFY] [flow.jsx](file:///C:/Users/gagan/Downloads/ClinicLedger-main/flow.jsx)
- Update project name and phase descriptions.

#### [MODIFY] [metadata.json](file:///C:/Users/gagan/Downloads/ClinicLedger-main/metadata.json)
- Update `name`.

### Codex Branch Integration
The `codex` branch contains `GeminiManager.kt` and some test improvements. I will integrate `GeminiManager.kt` into the main branch if it's found useful for AI features.

#### [NEW] [GeminiManager.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/villageclinicledger/ai/GeminiManager.kt)
- Add the Gemini integration for voice/text processing if it adds value.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project builds with the new Gradle version and renamed strings.
- Run `./gradlew testDebugUnitTest` to verify no regressions.

### Manual Verification
- Inspect `README.md` and `flow.jsx` to ensure all "Village" references and template links are removed.
- Check the app name in the manifest and strings.
