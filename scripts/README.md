# EcoFeast Firestore Migration Guide: Removing `donorPhone`

This guide explains how to safely execute the migration script to remove legacy `donorPhone` fields from all documents in the `food_listings` collection in Cloud Firestore for the **`ecofeast-a0fee`** Firebase project.

---

## 1. Safety & Security Guarantees

1. **Strict Project ID Verification:** The script verifies that the service account belongs to **`ecofeast-a0fee`**. If there is any project mismatch, the script aborts immediately before connecting to Firestore.
2. **Credential Security:** Credentials must be supplied via a explicit CLI path or the `GOOGLE_APPLICATION_CREDENTIALS` environment variable. The script will never look for key files inside the repository directory.
3. **Dry-Run Simulation Mode (`--dry-run`):** Allows you to test and scan all documents safely without modifying any data in Cloud Firestore.
4. **Targeted Field Deletion:** Only the `donorPhone` field is removed (`FieldValue.delete()`). No other fields or documents are altered or deleted.
5. **Batching & Pagination:** Documents are processed in chunks of 500 (Firestore max batch limit) with pagination cursors.

---

## 2. Prerequisites

1. **Node.js** installed on your computer (v16 or newer).
2. **Firebase Service Account Key JSON File**:
   - Go to [Firebase Console](https://console.firebase.google.com/) -> **Project Settings** -> **Service Accounts**.
   - Ensure the selected project is **`ecofeast-a0fee`**.
   - Click **Generate New Private Key**.
   - Save the downloaded `.json` key file in a secure folder **outside** your Git repository (e.g. `C:\Users\YourName\Secrets\ecofeast-service-account.json`).
   - **DO NOT** save or copy this JSON file inside the EcoFeast repository folder.

---

## 3. Installation

Open your terminal or command prompt and navigate to the `scripts` directory:

```bash
cd scripts
npm install
```

---

## 4. Usage Commands

### A. Dry Run Simulation (Safe Preview Mode - No Writes)

Run a dry run first to scan all `food_listings` documents and preview how many documents contain `donorPhone`:

```bash
node remove_donor_phone_migration.js --dry-run "/path/to/ecofeast-service-account.json"
```

**Windows PowerShell Example:**
```powershell
node remove_donor_phone_migration.js --dry-run "C:\Users\YourName\Secrets\ecofeast-service-account.json"
```

**Expected Dry-Run Output:**
```text
[Init] Firebase Admin initialized for verified project: ecofeast-a0fee

[Mode] --- DRY RUN (SIMULATION MODE: NO FIRESTORE WRITES WILL BE PERFORMED) ---
[Migration] Scanning `food_listings` collection for legacy `donorPhone` fields...

[Dry Run Page 1] Found 42 documents containing `donorPhone` (no writes executed).

==================================================
MIGRATION SUMMARY REPORT (DRY-RUN SIMULATION)
==================================================
Verified Firebase Project ID : ecofeast-a0fee
Total food_listings scanned  : 42
Documents with `donorPhone`  : 42
Batches calculated           : 1
Execution Status             : COMPLETED (NO CHANGES MADE TO FIRESTORE)
==================================================
```

---

### B. Live Migration (Actual Database Field Removal)

After reviewing the dry-run results, execute the live migration to delete `donorPhone` fields:

```bash
node remove_donor_phone_migration.js "/path/to/ecofeast-service-account.json"
```

**Windows PowerShell Example:**
```powershell
node remove_donor_phone_migration.js "C:\Users\YourName\Secrets\ecofeast-service-account.json"
```

**Expected Live Migration Output:**
```text
[Init] Firebase Admin initialized for verified project: ecofeast-a0fee

[Mode] --- LIVE MIGRATION MODE (FIRESTORE WRITES ACTIVE) ---
[Migration] Scanning `food_listings` collection for legacy `donorPhone` fields...

[Batch 1] Successfully deleted `donorPhone` from 42 documents.

==================================================
MIGRATION SUMMARY REPORT (LIVE MIGRATION)
==================================================
Verified Firebase Project ID : ecofeast-a0fee
Total food_listings scanned  : 42
Documents with `donorPhone`  : 42
Batches committed            : 1
Execution Status             : COMPLETED (CHANGES COMMITTED TO FIRESTORE)
==================================================
```

---

## 5. Verification Steps

After running the live migration script:

1. **Firebase Console Check:**
   - Open **Firebase Console** -> **Firestore Database** -> `food_listings`.
   - Select any document. Confirm `donorPhone` is absent while `donorId`, `donorName`, `foodName`, `pickupAddress`, etc. remain intact.

2. **Re-Run Idempotency Verification:**
   - Execute the script again (or in `--dry-run` mode).
   - Output will state `Documents with donorPhone: 0` and `Total batches committed: 0`, confirming complete field removal.
