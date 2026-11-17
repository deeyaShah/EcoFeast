/**
 * EcoFeast Firestore Migration Script: Remove `donorPhone` from `food_listings`
 *
 * Safety & Security Features:
 * 1. Firebase Project ID Verification:
 *    - Validates that the service account project_id is exactly 'ecofeast-a0fee'.
 *    - Immediately halts execution before initializing Firestore if the ID does not match.
 * 2. Credential Security:
 *    - Accepts credentials ONLY via command-line argument path or GOOGLE_APPLICATION_CREDENTIALS.
 *    - No internal/local directory fallback searching (e.g. `serviceAccountKey.json` inside scripts/).
 * 3. Dry-Run Mode Support:
 *    - `--dry-run` argument scans all documents using pagination.
 *    - Counts matching documents containing `donorPhone` without making any Firestore writes.
 * 4. Actual Migration Safety:
 *    - Uses `admin.firestore.FieldValue.delete()` to purge `donorPhone` field ONLY.
 *    - Batched writes limited to 500 operations per batch (Firestore limit).
 *    - Safe to re-run multiple times (idempotent).
 */

const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

const EXPECTED_PROJECT_ID = 'ecofeast-a0fee';

// Parse CLI Arguments
const rawArgs = process.argv.slice(2);
const isDryRun = rawArgs.includes('--dry-run') || rawArgs.includes('-d');
const nonOptionArgs = rawArgs.filter(arg => arg !== '--dry-run' && arg !== '-d');

function getCredentialFilePath() {
  const cliPath = nonOptionArgs[0];
  const envPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;

  let candidatePath = null;
  if (cliPath && fs.existsSync(cliPath)) {
    candidatePath = path.resolve(cliPath);
  } else if (envPath && fs.existsSync(envPath)) {
    candidatePath = path.resolve(envPath);
  }

  if (!candidatePath) {
    console.error('================================================================');
    console.error('ERROR: Service Account Key JSON file not found or path not specified!');
    console.error('================================================================');
    console.error('Usage:');
    console.error('  Dry Run Mode:');
    console.error('    node remove_donor_phone_migration.js --dry-run "<PATH_TO_SERVICE_ACCOUNT_JSON>"');
    console.error('  Live Migration Mode:');
    console.error('    node remove_donor_phone_migration.js "<PATH_TO_SERVICE_ACCOUNT_JSON>"');
    console.error('  Or set environment variable:');
    console.error('    GOOGLE_APPLICATION_CREDENTIALS="<PATH_TO_SERVICE_ACCOUNT_JSON>"');
    console.error('================================================================');
    process.exit(1);
  }

  return candidatePath;
}

function initFirebaseAdmin() {
  const keyPath = getCredentialFilePath();

  let serviceAccount;
  try {
    const rawData = fs.readFileSync(keyPath, 'utf8');
    serviceAccount = JSON.parse(rawData);
  } catch (err) {
    console.error(`ERROR: Failed to read or parse service account JSON file at: ${keyPath}`);
    console.error(err.message);
    process.exit(1);
  }

  // 1. PROJECT ID VERIFICATION SECURITY CHECK
  const actualProjectId = serviceAccount.project_id;
  if (actualProjectId !== EXPECTED_PROJECT_ID) {
    console.error('================================================================');
    console.error('SECURITY ERROR: FIREBASE PROJECT ID MISMATCH!');
    console.error('================================================================');
    console.error(`Expected Project ID : ${EXPECTED_PROJECT_ID}`);
    console.error(`Provided Project ID : ${actualProjectId}`);
    console.error('Migration aborted immediately. No database connections were established.');
    console.error('================================================================');
    process.exit(1);
  }

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });

  console.log(`[Init] Firebase Admin initialized for verified project: ${actualProjectId}`);
}

async function runMigration() {
  initFirebaseAdmin();
  const db = admin.firestore();

  console.log(`\n[Mode] ${isDryRun ? '--- DRY RUN (SIMULATION MODE: NO FIRESTORE WRITES WILL BE PERFORMED) ---' : '--- LIVE MIGRATION MODE (FIRESTORE WRITES ACTIVE) ---'}`);
  console.log('[Migration] Scanning `food_listings` collection for legacy `donorPhone` fields...\n');

  const collectionRef = db.collection('food_listings');
  const PAGE_SIZE = 500;

  let lastDoc = null;
  let totalScanned = 0;
  let totalUpdated = 0;
  let totalBatches = 0;
  let hasMore = true;

  while (hasMore) {
    let query = collectionRef.orderBy(admin.firestore.FieldPath.documentId()).limit(PAGE_SIZE);
    if (lastDoc) {
      query = query.startAfter(lastDoc);
    }

    const snapshot = await query.get();
    if (snapshot.empty) {
      hasMore = false;
      break;
    }

    const batch = db.batch();
    let batchOpCount = 0;

    snapshot.docs.forEach(doc => {
      totalScanned++;
      const data = doc.data();

      // Check if `donorPhone` field exists in document
      if (Object.prototype.hasOwnProperty.call(data, 'donorPhone')) {
        if (!isDryRun) {
          batch.update(doc.ref, {
            donorPhone: admin.firestore.FieldValue.delete()
          });
        }
        batchOpCount++;
        totalUpdated++;
      }
    });

    if (batchOpCount > 0) {
      if (!isDryRun) {
        await batch.commit();
        totalBatches++;
        console.log(`[Batch ${totalBatches}] Successfully deleted \`donorPhone\` from ${batchOpCount} documents.`);
      } else {
        totalBatches++;
        console.log(`[Dry Run Page ${totalBatches}] Found ${batchOpCount} documents containing \`donorPhone\` (no writes executed).`);
      }
    } else {
      console.log(`[Page Scan] Scanned ${snapshot.docs.length} documents in this page; 0 \`donorPhone\` fields found.`);
    }

    lastDoc = snapshot.docs[snapshot.docs.length - 1];
    if (snapshot.docs.length < PAGE_SIZE) {
      hasMore = false;
    }
  }

  console.log('\n==================================================');
  console.log(`MIGRATION SUMMARY REPORT (${isDryRun ? 'DRY-RUN SIMULATION' : 'LIVE MIGRATION'})`);
  console.log('==================================================');
  console.log(`Verified Firebase Project ID : ${EXPECTED_PROJECT_ID}`);
  console.log(`Total food_listings scanned  : ${totalScanned}`);
  console.log(`Documents with \`donorPhone\`  : ${totalUpdated}`);
  console.log(`Batches ${isDryRun ? 'calculated' : 'committed'}           : ${totalBatches}`);
  console.log(`Execution Status             : ${isDryRun ? 'COMPLETED (NO CHANGES MADE TO FIRESTORE)' : 'COMPLETED (CHANGES COMMITTED TO FIRESTORE)'}`);
  console.log('==================================================\n');
}

runMigration().catch(err => {
  console.error('Migration failed with error:', err);
  process.exit(1);
});
