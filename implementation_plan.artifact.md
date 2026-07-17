# UI Automation and Comparison Script

Create a Python script to automate the comparison between Clinic Ledger and ChatGPT on an Android device. The script will handle side-by-side execution, UI exploration, data collection, and periodic screen captures.

## Proposed Changes

### [NEW] [ui_automation.py](file:///C:/Users/gagan/Downloads/ClinicLedger-main/ui_automation.py)

A standalone Python script that uses `adb` to:
1.  **Launch Apps**: Open `com.clinicledger` and `com.openai.chatgpt`.
2.  **Split Screen**: Attempt to put the apps in split-screen mode (side-by-side).
3.  **UI Exploration**: Crawl through the navigable components of both apps (focusing on ChatGPT as requested).
4.  **Data Collection**: Extract text, resource IDs, and component types from the UI hierarchy.
5.  **Periodic Screenshots**: Capture the screen every second and save to a dedicated folder.
6.  **Comparison Report**: Save the collected data in a structured format (e.g., JSON) for further AI analysis.

## Verification Plan

### Manual Verification
- The user will be asked to unlock the device.
- The user will run the script and verify that:
    - Apps are launched and positioned correctly.
    - Screenshots are being saved.
    - UI data is being collected.
