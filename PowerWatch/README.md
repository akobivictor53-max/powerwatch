# PowerWatch

An Android app for verifying Nigerian prepaid electricity meters, backed by a
Node.js proxy server that talks to the Sogo API. The app never sees, stores,
or ships the Sogo secret key — only the backend does.

```
PowerWatch/
├── android/    Kotlin + Jetpack Compose app (clean architecture, MVVM)
└── backend/    Node.js + Express server that proxies calls to Sogo
```

---

## ⚠️ Before you deploy: read this

Sogo's public bill-payments/developer API (which would cover meter
verification) is **not yet publicly documented** — as of this build, Sogo's
own developer page lists it as "in the works." That means the exact request
shape, auth header, and response fields in
`backend/src/services/sogoClient.js` are a **best-guess placeholder**
following common conventions used by Nigerian bill-payment aggregators, so
the rest of the system (routing, validation, security, the whole Android
app) is fully built and ready to go.

**Before going live, you must:**
1. Get real API docs / an OpenAPI spec from Sogo.
2. Update `SOGO_API_BASE_URL` in `backend/.env`.
3. Update `buildRequest()` and `normalizeResponse()` in
   `backend/src/services/sogoClient.js` to match Sogo's actual request/response
   shape.

Everything else — the Android UI, the backend's routes, validation, rate
limiting, and error handling — talks to the *normalized* shape that function
returns, so fixing that one file is the only integration work left.

The app is also built to **never invent data**. If Sogo doesn't return a
customer name, meter type, or power status, the app simply doesn't show that
field — it does not fill in a guess or a fake default.

---

## How it fits together

1. User picks a Disco and types a meter number in the Android app.
2. The app calls **your backend** (`POST /api/meters/verify`) — never Sogo
   directly.
3. Your backend validates the input, calls Sogo's meter-verification
   endpoint using the secret key from its environment variables, and
   returns a normalized JSON response.
4. The app renders whatever the backend actually returned: verified status,
   customer name (if given), a masked meter number, and meter type.

```
Android App  --https-->  Your Backend  --https+secret key-->  Sogo API
   (no secret)              (holds secret)
```

---

## Backend setup

```bash
cd backend
cp .env.example .env
# Edit .env: fill in SOGO_API_BASE_URL, SOGO_API_SECRET_KEY, and generate
# APP_CLIENT_KEY with: openssl rand -hex 32
npm install
npm run dev      # http://localhost:8080
```

Key files:
- `src/config/env.js` — the only file that reads `process.env`; fails fast
  if secrets are missing.
- `src/services/sogoClient.js` — the Sogo integration adapter (see warning
  above).
- `src/routes/meterRoutes.js` — `GET /api/discos`, `POST /api/meters/verify`.
- `src/routes/communityRoutes.js` — optional user-generated power reports.
- `src/middleware/auth.js` — requires an `x-app-key` header matching
  `APP_CLIENT_KEY`, so random clients can't hammer your Sogo quota.

Deploy this anywhere that supports Node + environment variables (Render,
Railway, Fly.io, a VPS, etc). Set the same env vars there — never commit
`.env`.

## Android setup

```bash
cd android
cp local.properties.example local.properties
# Set sdk.dir to your Android SDK path.
```

Edit `gradle.properties` (or override in `local.properties`):

```properties
POWERWATCH_BACKEND_BASE_URL=https://your-deployed-backend.example.com/
POWERWATCH_APP_CLIENT_KEY=<same value as backend's APP_CLIENT_KEY>
```

Then open the `android/` folder in Android Studio (Koala or newer) and run
the app. No API keys of any kind go in this project — only your backend's
public URL and the low-sensitivity app-client key.

## Architecture (Android)

- **`domain/`** — pure Kotlin: models, repository interfaces, use cases.
  No Android or networking imports.
- **`data/`** — Retrofit DTOs, API interface, repository implementation
  that maps network responses (and failures) into domain models/`AppResult`.
- **`ui/`** — Jetpack Compose screens, `HomeViewModel` (MVVM), reusable
  components. Supports light/dark theme automatically via
  `isSystemInDarkTheme()`.
- **`di/`** — Hilt modules wiring it all together.

## Security notes

- The Sogo secret key exists **only** in the backend's environment
  variables (`backend/.env`, never committed). It is never referenced by
  any Android source file.
- `network_security_config.xml` blocks cleartext (HTTP) traffic from the
  app — HTTPS only.
- The backend validates all input (meter number format, known Disco codes)
  before calling Sogo, and rate-limits the verify endpoint.
- Logs are configured to redact auth headers and secret fields.

## What "verified" means here

The app shows exactly what the backend/Sogo reports — verified status,
customer name if provided, a masked meter number, and meter type. It does
**not** simulate or infer power on/off status. The optional "Community
reports" section is always labeled as user-submitted and is kept visually
and semantically separate from the verified provider data.
