# JSON Internationalization Design

## Overview

Add multi-language support to `ferry-info.json` so the app can display server-provided content (FAQs, location names, vehicle types, etc.) in the user's language.

## Decisions

- **File layout**: Single `ferry-info.json` with all languages embedded. Each localizable string becomes `{"en": "...", "es": "...", ...}`.
- **Language selection**: Device locale, with English fallback.
- **Resolution layer**: Shared KMP code resolves locale before returning data to native UI. The public `FerryInfo` model keeps plain `String` fields — no UI changes needed.

## JSON Structure

Non-localizable fields (times, coordinates, URLs, numbers, dates, contact) stay as-is. Localizable string fields become `Map<String, String>`:

```json
{
  "schedule": {
    "regularHours": { ... },
    "serviceNote": {
      "en": "Continuous, not on a fixed schedule—first-come, first-served",
      "es": "Continuo, sin horario fijo: por orden de llegada"
    }
  },
  "locations": {
    "iowa": {
      "name": { "en": "Lansing Marina", "es": "Marina de Lansing" },
      "description": { "en": "North driveway off Front Street", "es": "Entrada norte desde Front Street" },
      "latitude": 43.3695,
      "longitude": -91.2202
    }
  },
  "cameras": [
    {
      "id": "wi-landing",
      "name": { "en": "Wisconsin Ferry Landing", "es": "Desembarcadero de Wisconsin" },
      "streamUrl": "...",
      "snapshotUrl": "..."
    }
  ],
  "vehicleRestrictions": {
    "allowed": [
      { "en": "Cars", "es": "Coches" },
      { "en": "Motorcycles", "es": "Motocicletas" }
    ],
    "prohibited": [
      { "en": "Trailers", "es": "Remolques" }
    ],
    "sizeLimits": {
      "heightFeet": 11,
      "lengthFeet": 25,
      "widthFeetInches": { "en": "8 ft 6 in", "es": "2.6 m" },
      "weightTons": 10
    }
  },
  "faqs": [
    {
      "question": { "en": "How much does the ferry cost?", "es": "..." },
      "answer": { "en": "The ferry is free of charge.", "es": "..." }
    }
  ]
}
```

## Localizable Fields

| Path | Current Type | New Type |
|------|-------------|----------|
| `schedule.serviceNote` | `String` | `Map<String, String>` |
| `locations.*.name` | `String` | `Map<String, String>` |
| `locations.*.description` | `String` | `Map<String, String>` |
| `cameras[].name` | `String` | `Map<String, String>` |
| `vehicleRestrictions.allowed[]` | `String` | `Map<String, String>` |
| `vehicleRestrictions.prohibited[]` | `String` | `Map<String, String>` |
| `vehicleRestrictions.sizeLimits.widthFeetInches` | `String` | `Map<String, String>` |
| `faqs[].question` | `String` | `Map<String, String>` |
| `faqs[].answer` | `String` | `Map<String, String>` |

## KMP Architecture

### Two model layers

1. **Raw models** (`RawFerryInfo`, `RawSchedule`, etc.) — deserialized from JSON. Localizable fields are `Map<String, String>`. Internal to the shared module.
2. **Resolved models** (existing `FerryInfo`, `Schedule`, etc.) — plain `String` fields. Public API, unchanged.

### LocaleResolver

```kotlin
object LocaleResolver {
    fun resolve(translations: Map<String, String>, locale: String): String {
        // 1. Exact match (e.g. "es")
        translations[locale]?.let { return it }
        // 2. Language-only match (e.g. "es" from "es-MX")
        val lang = locale.split("-", "_").first()
        translations[lang]?.let { return it }
        // 3. English fallback
        return translations["en"] ?: translations.values.firstOrNull() ?: ""
    }
}
```

### Repository changes

- `FerryRepository` accepts a `locale: String` parameter on `getFerryInfo()` and `refresh()`
- Internally deserializes to `RawFerryInfo`, caches the raw data
- Resolves to `FerryInfo` using `LocaleResolver` before returning
- Cache stores full multi-language data; resolution is cheap and happens on read

### Platform integration

- **Android**: `FerryViewModel` passes `Locale.getDefault().language` to repository
- **iOS**: `FerryViewModel` passes `Locale.current.language.languageCode?.identifier ?? "en"` to repository

No UI code changes needed on either platform since the resolved `FerryInfo` has the same shape.
