# OrchardDex Catalog Authoring Schema

This file defines the minimum useful data OrchardDex should collect for species and cultivars as the catalog scales.

The goal is:
- comprehensive enough to improve forecasts, pollination guidance, and search quality
- narrow enough to avoid collecting ornamental or encyclopedic data that the app does not use

This is the authoring schema, not a promise that every field is already stored in the runtime asset exactly as written today.

## Design principles

- Prefer species-level truth over cultivar-by-cultivar speculation.
- Add cultivar timing or fertility overrides only when evidence is strong.
- Do not research every town or region.
- Do not invent day-precise behavior for repeat, continuous, or weakly modeled crops.
- Record uncertainty explicitly instead of burying it in code.

## Core records

OrchardDex should think in three record types:
- `SpeciesRecord`
- `CultivarRecord`
- `EvidenceRecord`
- `SpeciesChecklist`

## SpeciesRecord

Every species pass should produce one `SpeciesRecord`.

### Required fields

- `key`
  - canonical internal species key
  - example: `dragon fruit`
- `displayName`
  - human-facing species label
  - example: `Dragon Fruit`
- `scientificName`
  - primary scientific name used for research disambiguation
  - example: `Selenicereus spp.`
- `aliases[]`
  - common names, spelling variants, taxonomy synonyms users may enter
- `familyOrCropGroup`
  - example: `Cactaceae`, `Annonaceae`, `Citrus`, `Pome fruit`
- `growthForm`
  - one of: `tree`, `shrub`, `vine`, `cane`, `herbaceous`, `palm`
- `patternType`
  - one of: `SINGLE_ANNUAL`, `MULTI_WAVE`, `CONTINUOUS`, `ALTERNATE_YEAR`, `MANUAL_ONLY`, `SUPPRESSED`
- `modelType`
  - one of: `CHILL_HEAT`, `CLIMATE_WINDOW`, `TROPICAL_REPEAT`, `WARM_SEASON_PHOTOPERIOD`, `MANUAL_ONLY`
- `baselineBloom`
  - minimum useful shape:
    - `referenceZoneCode`
    - `startMonth`
    - `startDay`
    - `durationDays`
  - if the species cannot be honestly represented that way, record why it is `MANUAL_ONLY`, `CONTINUOUS`, or otherwise coarse
- `triggerSignals[]`
  - zero or more of:
    - `chill`
    - `spring_heat`
    - `warm_season`
    - `photoperiod`
    - `rainfall`
    - `drought`
    - `pruning`
    - `stress`
- `locationShiftPolicy`
  - `supportsHemisphereShift`
  - `supportsUsdaShift`
- `pollinationDefault`
  - `pollinationRequirement`
  - `selfCompatibility`
  - `pollinationMode`
  - when fertility biology materially changes the user decision, also capture:
    - `sexExpression`
    - `handPollinationNeed`
    - `compatibilityNotes`
- `preferredLearningSignals[]`
  - recommended choices from OrchardDex event history
  - usually some subset of `BUD`, `BLOOM`, `FRUIT_SET`, `HARVEST`
- `uncertaintyNote`
  - one short honest sentence about where the model is weak
- `researchStatus`
  - one of: `draft`, `baseline_only`, `reviewed`, `strong`
- `researchNoteId`
  - filename or identifier of the supporting research note
- `lastReviewedDate`

### SpeciesChecklist

Each species should also carry a lightweight checklist so future sessions can tell whether the species is merely present, properly backfilled, or actually hardened.

Recommended checklist items:
- `identityCaptured`
  - canonical key, display name, scientific name, and crop group are filled
- `aliasesReviewed`
  - aliases are intentionally curated, not just copied blindly
- `patternChosen`
  - `patternType` is deliberate
- `modelChosen`
  - `modelType` is deliberate
- `baselineTimingRecorded`
  - the species has a baseline timing entry or an explicit manual/coarse rationale
- `pollinationDecisionRecorded`
  - a species default pollination decision exists, even if that decision is `UNKNOWN`
- `uncertaintyLogged`
  - the known weakness is explicitly documented
- `researchNoteLinked`
  - a research note exists and is linked
- `cultivarAuditStarted`
  - at least the current cultivar list has been reviewed for alias-only vs timing/pollination overrides
  - if OrchardDex currently carries no named cultivars for the species, an explicit "no cultivar packet yet" decision still counts as audited
- `testsPresent`
  - at least one species-relevant test or matching assertion exists
- `status`
  - `draft`
  - `baseline_only`
  - `reviewed`
  - `strong`

The checklist is intentionally operational. It tracks readiness, not perfection.

### Checklist interpretation

- `draft`
  - the species exists, but the entry is still provisional
- `baseline_only`
  - the species has enough operational structure to ship, but the source packet or cultivar audit is still shallow
- `reviewed`
  - the species has a deliberate research note, asset entry, and meaningful validation
- `strong`
  - the species is one of the better-supported entries and is unlikely to need major reclassification soon

## Baseline packet

`baseline_only` is not just "we picked a bloom window."

A species should not be considered baseline-complete unless it has this minimum packet:

- `identity`
  - canonical key
  - display/scientific name
  - crop group
  - aliases
- `forecast core`
  - `patternType`
  - `modelType`
  - baseline timing or an explicit coarse/manual rationale
  - `triggerSignals`
  - location-shift policy
- `fertility core`
  - `pollinationRequirement`
  - `selfCompatibility`
  - `pollinationMode`
  - if the species is operationally shaped by sex expression or hand pollination, capture that too
  - if the right answer is not known, record `UNKNOWN` and explain why in the uncertainty note
- `learning core`
  - preferred learning signals
- `honesty core`
  - one explicit uncertainty note
  - one linked research note
  - at least one species-relevant test or matching assertion

Baseline should therefore include the useful fertility details, but not a giant compatibility encyclopedia.

Examples of fertility details that belong in baseline when they materially affect a grower:
- dioecious / monoecious / hermaphrodite behavior
- protandry / protogyny when it changes hand-pollination usefulness
- "hand pollination often helpful" vs "hand pollination often required"
- a short compatibility note when a simple self-/cross-pollination bucket is too blunt

Examples that do **not** belong in baseline by default:
- full compatible-cultivar pairing matrices
- pollenizer bloom charts for every region
- breeder-level flower morphology notes that do not change user action

### Optional fields

- `catalogLabel`
  - when the app should display a more descriptive name than the raw key
- `alternateYearTendency`
  - boolean or note if applicable
- `manualOnlyReason`
  - why exact bloom forecasting is intentionally suppressed
- `climateNotes`
  - short notes about frost sensitivity, greenhouse distortion, rainfall coupling, or elevation sensitivity
- `pollinationNotes`
  - short note for structured detail views when the bucket alone is too blunt

### Species fields that are intentionally out of scope for now

Do not require these for every species pass:
- flavor description
- fruit size
- disease encyclopedia
- pruning guide
- complete compatible-cultivar matrix
- region-by-region bloom calendars

## CultivarRecord

Only create a `CultivarRecord` when the cultivar is real enough that users will search for it or when it changes behavior meaningfully.

### Required fields

- `speciesKey`
- `cultivar`
- `aliases[]`

### Optional fields, only when justified

- `phaseOffset`
  - or the current bucket form: `EARLY`, `EARLY_MID`, `MID`, `MID_LATE`, `LATE`
- `patternOverride`
  - only if the cultivar truly behaves differently from the species default
- `modelOverride`
  - rare; avoid unless the biology clearly differs
- `pollinationOverride`
  - only when evidence clearly beats the species default
- `sexExpressionOverride`
  - useful for species like papaya if cultivar sex behavior is meaningful
- `catalogOnly`
  - true when the cultivar should exist for autocomplete/search but does not deserve a timing override
- `uncertaintyNote`
- `researchNoteId`
- `lastReviewedDate`

### Cultivar anti-rules

Do not force every cultivar to have:
- its own bloom window
- its own climate model
- its own pollination claim

Most cultivars should inherit:
- species pattern
- species model
- species bloom timing
- species pollination default

## EvidenceRecord

Every meaningful claim should be backed by at least one evidence item in the research note.

### Required fields

- `subjectType`
  - `species` or `cultivar`
- `subjectKey`
- `claimType`
  - `alias`
  - `pattern`
  - `timing`
  - `trigger`
  - `pollination`
  - `cultivar_phase`
  - `cultivar_override`
  - `uncertainty`
- `sourceType`
  - `extension`
  - `paper`
  - `breeding_program`
  - `botanic`
  - `government`
  - `nursery`
- `citation`
- `url`
- `strength`
  - `high`, `medium`, `low`
- `notes`
- `reviewedDate`

### Evidence rules

- Extension and research literature should drive species defaults.
- Nursery pages are acceptable for alias support and cautious cultivar fill-in.
- Nursery pages should not silently overrule extension literature on pollination or timing.

## What each species pass must collect

Minimum required outputs:
- one `SpeciesRecord`
- one `SpeciesChecklist`
- one research note
- aliases
- pattern choice
- model choice
- baseline timing or explicit coarse/manual rationale
- pollination default
- uncertainty note
- at least one matching or forecast test

## What each cultivar pass must collect

Minimum required outputs:
- cultivar name
- aliases if needed
- explicit decision:
  - `catalog-only`
  - `phase-adjusted`
  - `pollination override`
- evidence note for anything stronger than alias-only support

## Storage mapping

### Runtime asset now

Today these belong directly in `phenology_profiles.json`:
- species key
- display label
- aliases
- reference zone / start / duration
- pattern type
- model type
- forecast behavior
- most pollination defaults
- cultivar phase rows
- catalog-only cultivar rows
- uncertainty note

### Runtime override asset now

Use `pollination_profiles.json` only for:
- targeted species overrides
- targeted cultivar overrides
- future split-out cases where pollination data needs separate maintenance

### Research note now

Keep these in `docs/*-research.md`:
- scientific naming notes
- source packet
- reasoning behind timing and pollination decisions
- unresolved uncertainty
- notes too nuanced for a simple runtime enum

## Suggested backfill tiers

### Tier 1: baseline-ready

- species record complete
- aliases complete enough for search
- pattern/model chosen
- baseline timing chosen
- pollination default chosen
- uncertainty note written

### Tier 2: strong species pass

- all Tier 1 items
- evidence packet with strong extension/research sources
- cultivar aliases and major timing overrides reviewed
- test coverage added

### Tier 3: mature species group

- all Tier 2 items
- weak cultivars cleaned up
- pollination edge cases documented
- model limitations clearly stated
- ready for large-scale catalog expansion in that crop family

## JSON authoring examples

### SpeciesRecord example

```json
{
  "key": "dragon fruit",
  "displayName": "Dragon Fruit",
  "scientificName": "Selenicereus spp.",
  "aliases": ["dragonfruit", "pitaya"],
  "familyOrCropGroup": "Cactaceae",
  "growthForm": "vine",
  "patternType": "MULTI_WAVE",
  "modelType": "WARM_SEASON_PHOTOPERIOD",
  "baselineBloom": {
    "referenceZoneCode": "10a",
    "startMonth": 6,
    "startDay": 1,
    "durationDays": 70
  },
  "triggerSignals": ["warm_season", "photoperiod"],
  "locationShiftPolicy": {
    "supportsHemisphereShift": false,
    "supportsUsdaShift": false
  },
  "pollinationDefault": {
    "pollinationRequirement": "CROSS_POLLINATION_RECOMMENDED",
    "selfCompatibility": "PARTIAL",
    "pollinationMode": "INSECT"
  },
  "preferredLearningSignals": ["BUD", "BLOOM", "FRUIT_SET", "HARVEST"],
  "uncertaintyNote": "Use warm-season climate plus local observations. Avoid day-precise forecasts without location history.",
  "researchStatus": "reviewed",
  "researchNoteId": "pollination-research.md",
  "lastReviewedDate": "2026-03-29"
}
```

### CultivarRecord example

```json
{
  "speciesKey": "mango",
  "cultivar": "Ataulfo",
  "aliases": ["Honey", "Honey mango", "Champagne", "Champagne mango"],
  "catalogOnly": true,
  "researchNoteId": "tropical-catalog-backfill-research.md",
  "lastReviewedDate": "2026-03-29"
}
```

### EvidenceRecord example

```json
{
  "subjectType": "species",
  "subjectKey": "loquat",
  "claimType": "pollination",
  "sourceType": "extension",
  "citation": "UF/IFAS, Loquat Growing in the Florida Home Landscape",
  "url": "https://edis.ifas.ufl.edu/publication/MG050",
  "strength": "high",
  "notes": "Self-compatible, but cross-pollination improves fruit set and size.",
  "reviewedDate": "2026-03-29"
}
```

## Bottom line

If a field does not improve one of these, it should not be required in the species/cultivar schema:
- forecast quality
- pollination guidance
- search matching
- future learning from user logs
- nursery/hobby planning decisions
