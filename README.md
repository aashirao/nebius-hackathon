# Pocketwise

An Android-first student money tracker for irregular income and flexible savings goals.

**Status: Android debug APK built successfully; not yet a verified submission or production banking app.**
GitHub Actions passed the five mocked backend tests, Java core checks, and Android compilation on 3 October 2026. [Successful build and APK download](https://github.com/aashirao/nebius-hackathon/actions/runs/37103329536): open **Artifacts → pocketwise-debug-apk**, download and unzip it, then install `app-debug.apk` on your Android test phone. GitHub sign-in may be required to download the artifact. This is a debug test build, not a Play Store release.

Public source is now in this repository. Phone/emulator behavior, real notification capture, backend deployment and a live Nebius call remain unverified. Never treat successful compilation as proof of those features.

## Implemented source

- Native Android screens for tracked balance, transactions, corrections, income configuration and savings goals.
- Consent-based NotificationListenerService with an explicit package allowlist; no SMS permission.
- Conservative INR parser, local merchant rules, exact guardian/pocket-money/stipend sender matching, reference deduplication and possible-duplicate checks.
- Optional sources, stipend amount and interval in months; schedules never create income. Expected amounts and intervals are stored preferences, not yet reminders or forecasting.
- Goals created by the user, funded and released on demand. Incoming funds update the balance and are available for existing goals; they do not create duplicate goals or automatically allocate money.
- Integer-paise accounting; allocations cannot exceed the tracked unallocated balance.
- Stateless Python HTTPS-proxy backend for runtime NVIDIA Nemotron classification through Nebius Token Factory. The deployment needs HTTPS termination.
- AI disabled until explicit consent. Only redacted merchant labels go to the backend, not raw notifications or balances. Name-only personal payments remain ambiguous.
- Sample import, category correction, explicit AI retry, CI, MIT license and submission checklist.

## Run the backend

Requires Python 3.12; no third-party Python dependencies.

1. Set `NEBIUS_API_KEY`, `NEBIUS_MODEL`, `NEBIUS_BASE_URL` and `APP_ACCESS_TOKEN` in your host's secret settings. See `backend/.env.example`. This server does not automatically read .env files.
2. Generate a dedicated access token, for example with `python -c "import secrets; print(secrets.token_urlsafe(32))"` on your own machine. Do not commit it.
3. Run `python backend/server.py`, or build/deploy `backend/Dockerfile` behind HTTPS.
4. Check `/health`. `ai_configured: true` only means a key exists, not that inference works.
5. Configure the HTTPS backend URL and dedicated access token in the Android settings. The Nebius API key belongs ONLY in the backend.

Default model and regional endpoint follow Nebius's official Nemotron example:
`nvidia/nemotron-3-super-120b-a12b` at `https://api.tokenfactory.us-central1.nebius.com/v1`.
Verify model availability in your own account; change server environment settings if necessary.
Official reference: https://nebius.com/services/token-factory/nemotron

The shared app access token is suitable only for a controlled single-user hackathon build. Before public rollout, replace it with per-user authentication, revocable device credentials, request quotas and rate limiting. Do not put an unrestricted backend token or Nebius key in a public APK or README.

## Build Android

Use Android Studio with JDK 17, Gradle 8.9, SDK 35 and build tools compatible with Android Gradle Plugin 8.7.3. Open `android/`. No Gradle wrapper binary is included; use installed Gradle 8.9 or generate a wrapper locally.

```
gradle -p android assembleDebug
```

APK output: `android/app/build/outputs/apk/debug/app-debug.apk`.
The GitHub workflow installs those build dependencies and uploads the APK as an Actions artifact. The first successful run is linked above.

On phone:
1. Set opening balance to your actual balance immediately before your first imported transaction.
2. Configure any desired income sources using the exact sender label present in supported notifications. Blank sender fields disable matching. UPI matching only works if the notification actually contains that handle; names can collide.
3. Enable local capture, enter the package IDs of payment apps you wish to allow, save, then grant notification access through Android settings. Do not allow your SMS app: this prototype intentionally avoids collecting message content.
4. Verify the first transactions against your payment app. Notification access observes available notifications, not an authoritative bank transaction feed.
5. Create a goal, such as Phone / ₹30,000. Add or release amounts whenever funds are available.
6. For AI, configure the backend and consent; import a new expense or use Retry AI on an existing expense. Corrections are preserved.

## Safe demo inputs

Import these fictional notifications separately:

```
INR 5000 received from Mom via UPI ref DEMO000001
INR 20 paid to Campus Supermarket via UPI ref DEMO000002
INR 500 paid to Uber via UPI ref DEMO000003
INR 300 paid to Aarav via UPI ref DEMO000004
INR 10000 received from Internship Company via UPI ref DEMO000005
```

Set guardian to `Mom` and stipend sender to `Internship Company`. The person-only Aarav payment requires review: correct it to Friend repayment. If a taxi driver is paid under a personal name, the app cannot infer travel from that name or the amount. Never present this limitation as solved.

With a zero opening balance these inputs produce ₹14,180 tracked balance. Allocating ₹2,000 to Phone leaves ₹12,180 available. Reimporting the same reference should not change totals.

## Tests

```
python -m unittest discover -s backend -v
javac -d /tmp/pocketwise-test android/app/src/main/java/in/pocketwise/app/TransactionParser.java tests/CoreTest.java
java -cp /tmp/pocketwise-test CoreTest
```

Backend tests mock inference; they are not proof of a live Nebius call. Android UI, lifecycle, notification delivery and local persistence still require emulator/device tests.

## Known limits / next acceptance gates

- Conservative parser handles only the demonstrated English INR formats. It can miss notifications or pick up unsuitable text; build and test per-bank/app adapters before real use. Refunds/reversals and pending/failed payments are rejected, not reconciled. No historical statement import yet.
- Reference deduplication is basic; same-reference partial events/multiple accounts are not supported. Without references, identical notifications may be skipped; ambiguous repeats need statement reconciliation.
- Local records use app-private preferences, backup disabled. No encrypted database, biometric lock, export or deletion UI yet; clearing app storage deletes data. Backend access token is stored in app-private preferences; migrate it to Keystore-backed encryption before broad distribution.
- Matching is exact-label based and supports one guardian, one pocket-money source and one stipend source. It is not bank-verified identity.
- Captured notifications are not a reliable bank balance. App allocations do not reserve or transfer real bank funds. External spending can reduce funds below allocations, which the UI flags.
- AI classification sends redacted merchant names only. It cannot infer repayment intent or use trip receipts yet. The confidence threshold is not calibrated accuracy. AI failures preserve local categories and can be retried manually; durable background retries are not implemented.
- A completed incoming transaction updates funds automatically; no real-time savings prompt or automatic allocation policy is implemented.
- Android notification listener and UI layouts must be verified on a device before making working-app claims. No Play Store approval is implied.

See `docs/HACKATHON.md` for submission requirements and `docs/DEMO.md` for a proposed video sequence.
