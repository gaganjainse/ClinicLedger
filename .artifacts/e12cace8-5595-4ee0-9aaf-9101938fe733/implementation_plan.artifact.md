# Implementation Plan - Patient Photo Identification (Scanning)

This plan outlines the integration of patient profile pictures and a "Scan by Photo" feature to identify patients in the clinic.

## User Review Required

> [!IMPORTANT]
> The "Scan by Photo" feature will use **Google ML Kit Face Detection** combined with a local image similarity check. This requires adding the Camera permission and ML Kit dependencies.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [Patient.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/data/models/Patient.kt)
- Add `photoPath: String? = null` to the `Patient` data class.
- Update the `@Entity` to include the new column.

#### [MODIFY] [PatientDao.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/data/local/PatientDao.kt)
- No direct changes needed, but ensure `getAllPatients()` includes the new field.

---

### [Service Layer]

#### [NEW] [PhotoStorageService.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/service/PhotoStorageService.kt)
- Handle saving/loading images from internal storage.
- Generate unique filenames for patient photos.

#### [NEW] [FaceRecognitionService.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/service/FaceRecognitionService.kt)
- Use ML Kit to detect faces in a bitmap.
- Implement a basic image matching logic (e.g., comparing facial embeddings or histograms) to find the most likely patient match from the database.

---

### [UI Layer]

#### [MODIFY] [PatientComponents.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/compose/components/PatientComponents.kt)
- Update `PatientListItem` to display the profile photo if available, otherwise show the initials.
- Update `AddPatientDialog` to include a "Take Photo" button that launches the camera.

#### [MODIFY] [PatientDetailHeader.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/compose/components/PatientDetailHeader.kt)
- Update the avatar section to display the patient's actual photo.

#### [MODIFY] [LedgerSearchHub.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/compose/LedgerSearchHub.kt)
- Add a camera icon button to the search bar.
- Launch a camera capture flow when clicked.
- On capture, invoke `FaceRecognitionService` to find the matching patient and navigate to their detail screen.

#### [NEW] [CameraCaptureActivity.kt](file:///C:/Users/gagan/Downloads/ClinicLedger-main/app/src/main/java/com/clinicledger/ui/CameraCaptureActivity.kt)
- A simple activity/screen using CameraX to capture a photo and return it to the caller.

## Verification Plan

### Automated Tests
- Unit tests for `PhotoStorageService` to verify file creation and deletion.
- Unit tests for `FaceRecognitionService` with sample images to verify matching logic.

### Manual Verification
1. Register a new patient and take their profile photo.
2. Verify the photo appears in the list and detail screens.
3. Go to the Dashboard, click the "Scan" icon, and point the camera at the patient.
4. Verify the app correctly identifies the patient and opens their record.
