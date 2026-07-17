# Implementation Plan - UI Overhaul and Feature Removal

Overhaul the landing dashboard to match a modern, minimal AI assistant style (similar to ChatGPT). This includes removing the legacy "Morning Brief" snapshot, disabling push-to-talk (PTT) for the microphone, and adding interactive suggestion chips.

## User Review Required

> [!IMPORTANT]
> The "Hold the Mic" (PTT) feature will be completely removed in favor of a single-tap activation, as requested.
> The "Morning Brief" is now a suggestion rather than a permanent dashboard section.

## Proposed Changes

### [Dashboard Overhaul]

#### [MODIFY] [LedgerDashboardScreen.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/compose/LedgerDashboardScreen.kt)
- Remove `MorningBriefSection` from the top.
- Replace the center icon and greeting with "Clinic Ledger" text.
- Add three suggestion chips: "Morning Brief", "New Registration", and "View Analytics".
- Update `ChatAssistantBar` to remove `detectTapGestures` PTT logic, making it a simple `IconButton` click.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/MainActivity.kt)
- Simplify mic callbacks to handle only tap events.
- Remove PTT-specific state management.

### [Voice Interaction Enhancements]

#### [MODIFY] [VoiceInputSheetCompose.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/compose/VoiceInputSheetCompose.kt)
- Enhance `ListeningAnim` with a "live" shrinking/expanding circle and an audio wave simulation.
- Use `rmsDb` from the speech recognizer to drive the animation if possible (requires ViewModel update).

#### [MODIFY] [VoiceAssistantViewModel.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/compose/VoiceAssistantViewModel.kt)
- Add a `rmsDb` state to track live audio levels for the animation.

## Verification Plan

### Automated Tests
- Run `app:assembleDebug` to ensure compilation.

### Manual Verification
- Verify the dashboard shows "Clinic Ledger" without an icon.
- Verify the three suggestions are visible and functional.
- Verify the mic button starts the assistant on a single tap.
- Observe the "live" audio wave and expanding circle animations during voice input.
- Verify the navigation bar size remains unchanged.
