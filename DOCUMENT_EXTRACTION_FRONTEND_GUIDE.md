# Document extraction (auto-fill) — frontend guide

Upload a photo of a document and get back structured fields to pre-fill a form. The backend sends the image to an LLM, which reads the text: Google Gemini first, then Groq and Mistral as fallbacks if Gemini is down or busy. **Images are not stored.** If you also need to keep the Mulkiya images on the vehicle, upload them separately with `POST /mediafiles/upload` as before.

Supported document types: `MULKIYA` (UAE vehicle registration card). More types will be added later and will use the same endpoint.

## Endpoint

`POST /api/v1/document-extraction/{documentType}` — `multipart/form-data`

| Part    | Required | Notes                                         |
|---------|----------|-----------------------------------------------|
| `front` | yes      | Front of the card                             |
| `back`  | no       | Back of the card (send it — chassis, engine, weights and passengers are printed on the back) |

- Allowed files: JPEG, PNG, WEBP, HEIC, PDF. Max 10MB each.
- A call usually takes about 3–10 seconds. Show a loader, and don't fire it again while one is running.
- The server shrinks large photos to 2000px before sending them, so full-size camera photos are fine. Compressing on the device first (to about 2000px, JPEG) makes the upload faster on slow networks.

```js
const form = new FormData();
form.append('front', frontFile);
if (backFile) form.append('back', backFile);

const res = await fetch(`${API}/document-extraction/MULKIYA`, { method: 'POST', body: form });
const json = await res.json();
if (json.status !== 'success') {
  toast(json.message);           // e.g. "Could not read the document, please fill the form manually"
  return;
}
const f = json.body.fields;
```

Don't set the `Content-Type` header yourself. The browser adds the multipart boundary.

## Response

```json
{
  "status": "success",
  "code": "200",
  "message": "Document read successfully",
  "body": {
    "documentType": "MULKIYA",
    "provider": "gemini",
    "fields": {
      "number": "12345",
      "code": "B",
      "state": "DUBAI",
      "tcNumber": "50123456",
      "ownerName": "JOHN SMITH",
      "nationality": "India",
      "brand": "TOYOTA",
      "model": "LAND CRUISER",
      "year": "2021",
      "vehicleColor": "White",
      "vehicleType": "Station Wagon",
      "vehicleTypeCategory": "PRIVATE",
      "chassisNo": "JTMHV01J804123456",
      "engineNumber": "1VD1234567",
      "origin": "Japan",
      "numOfPassenger": "8",
      "emptyWeight": "2500",
      "grossWeight": "3300",
      "regDate": "15-03-2024",
      "regExpiry": "14-03-2026",
      "insuExpiry": "14-03-2026",
      "insuCompany": "ABC Insurance",
      "insuPolicy": "P/01/2024/12345",
      "policyType": null
    }
  }
}
```

Every field can be `null`, meaning it was missing or unreadable. The model is told never to guess.

## Field → registration form

The field names match `VehicleRegistrationRequest`, so most values go straight into the form. Only fill fields that are not null, and don't overwrite anything the user has already typed.

| Field | Form field | Notes |
|---|---|---|
| `number`, `code` | `number`, `code` | Plate number and plate code |
| `state` | `state` | One of `ABU_DHABI`, `DUBAI`, `SHARJAH`, `AJMAN`, `UMM_AL_QUWAIN`, `RAS_AL_KHAIMAH`, `FUJAIRAH` (the plate source codes) |
| `tcNumber` | `tcNumber` | |
| `brand`, `model`, `year` | `brand`, `model`, `year` | Free text. If brand/model are dropdowns, match case-insensitively; if there is no match, leave it for the user |
| `vehicleColor`, `vehicleType`, `origin`, `numOfPassenger` | same names | |
| `vehicleTypeCategory` | `vehicleTypeCategory` | Already one of the `VehicleTypeCategoryEnum` values |
| `chassisNo`, `engineNumber` | same names | Uppercased, no spaces in chassis |
| `regExpiry`, `insuExpiry` | same names | `dd-MM-yyyy`, the same format the form sends |
| `insuCompany`, `insuPolicy` | same names | |
| `policyType` | `policyType` | `THIRD_PARTY` / `COMPREHENSIVE`, usually `null` (not printed on the card). The user must pick it |
| `ownerName`, `nationality`, `regDate`, `emptyWeight`, `grossWeight` | — | Not on the form today. Show them or ignore them |

```js
const fill = (key, value) => { if (value != null && !form.getValues(key)) form.setValue(key, value); };
Object.entries(f).forEach(([k, v]) => fill(k, v));
```

**Always let the user review the values before submitting.** OCR can misread characters such as 0/O and 1/I, especially in chassis numbers.

## Errors

These come back with HTTP 200 and `status: "failed"`, like the other APIs:

| `code` | `message` | When |
|---|---|---|
| 400 | `Missing required request parameter.: front` | No `front` part |
| 400 | `The 'front' file must be less than 10MB` | File too large |
| 400 | `The 'front' file must be JPEG, PNG, WEBP, HEIC or PDF` | Wrong file type |
| 400 | `Unsupported document type: X. Supported: MULKIYA` | Wrong path value |
| 400 | `This file type can't be read right now, please upload a JPEG or PNG photo` | HEIC/PDF upload while Gemini (the only provider that reads them) has no key |
| 502 | `Document reading is busy right now, please try again in a minute` | Every provider was overloaded, rate limited or timed out |
| 502 | `Could not read the document, please fill the form manually` | Every provider failed, or the image was unreadable |
| 502 | `Document reading is not configured on the server` | No provider API key is set |

For a 502, keep the form usable and let the user type the values manually.

## Server setup (backend/devops)

Providers are tried as a ladder: **Gemini → Groq → Mistral**. The next one is used only when the previous one fails. A provider that fails temporarily (503 overloaded, 429 rate limited, or timeout) is moved to the back of the ladder for 60 seconds, so during an outage users go straight to the next provider instead of waiting on the failing one. A provider with no key is skipped, so any one key is enough to run. HEIC and PDF can only be read by Gemini; Groq and Mistral take JPEG, PNG and WEBP.

Set the keys in the environment. Never put them in the properties files.

```bash
export GEMINI_API_KEY=...
export GROQ_API_KEY=...
export MISTRAL_API_KEY=...           # or add them to the pm2 ecosystem env
pm2 restart vehicle-api --update-env
```

Each answer logs which provider read it (`Document MULKIYA read by groq`) and how long it took (`groq extraction took N ms`). The response also carries `provider`.

Optional settings (environment variables):

| Variable | Default | Notes |
|---|---|---|
| `EXTRACTION_PROVIDERS` | `gemini,groq,mistral` | Ladder order |
| `EXTRACTION_COOLDOWN_SECONDS` | `60` | How long a failing provider is tried last |
| `GEMINI_MODEL` | `gemini-2.5-flash` | |
| `GEMINI_TIMEOUT_SECONDS` | `45` | |
| `GEMINI_THINKING_BUDGET` | `0` | 0 = thinking off, which keeps calls fast |
| `GROQ_MODEL` | `qwen/qwen3.8-27b` | Must be a Groq vision model |
| `GROQ_REASONING_EFFORT` | `none` | Set to empty if the model doesn't support `reasoning_effort` |
| `GROQ_TIMEOUT_SECONDS` | `30` | |
| `MISTRAL_MODEL` | `mistral-small-latest` | Must be a Mistral vision model |
| `MISTRAL_TIMEOUT_SECONDS` | `30` | |
