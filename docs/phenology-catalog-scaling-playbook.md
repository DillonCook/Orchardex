# Phenology Catalog Scaling Playbook

This file exists to keep future OrchardDex catalog work consistent as the app grows past a small hand-maintained species set.

Use this as the default guide for:
- adding a new species
- adding cultivar rows or aliases
- deciding whether a species needs a new forecast model type
- deciding how much source research is actually needed

## Current catalog structure

The forecast stack is currently hybrid:

1. `TreeEntity` resolves to a species profile.
2. If cultivar input matches a known cultivar profile, the cultivar can adjust bloom phase.
3. The forecast engine applies:
   - custom per-tree bloom override
   - history-learned bloom if enough observations exist
   - species/cultivar catalog baseline
   - location shifts from hemisphere, USDA zone, latitude, elevation, and confidence modifiers

Relevant files:
- `app/src/main/java/com/dillon/orcharddex/data/phenology/BloomForecastEngine.kt`
- `app/src/main/java/com/dillon/orcharddex/data/phenology/PhenologyCatalogAssets.kt`
- `app/src/main/assets/phenology_profiles.json`
- `app/src/main/assets/pollination_profiles.json`
- `docs/catalog-authoring-schema.md`
- `docs/catalog-research-pass-template.md`
- `docs/catalog-species-ledger.csv`
- `docs/catalog-cultivar-ledger.csv`
- `scripts/export_catalog_ledgers.ps1`
- `docs/*-research.md`

Asset ownership rules:
- `phenology_profiles.json` is now the main source of truth for species profiles, cultivar timing rows, aliases, and most pollination defaults.
- `pollination_profiles.json` is reserved for targeted overrides or future split-outs; it is not expected to duplicate every default already embedded in the phenology asset.
- Kotlin should hold model logic and export/fallback helpers, not the long-term catalog authority.
- `catalog-species-ledger.csv` and `catalog-cultivar-ledger.csv` are generated audit surfaces. They help track coverage and research status, but they do not replace the runtime assets.

Catalog data is therefore not a giant city-by-city table. It is a compact baseline plus location and history adjustments.

Current pattern/model vocabulary:
- `BloomPatternType`
  - `SINGLE_ANNUAL`
  - `MULTI_WAVE`
  - `CONTINUOUS`
  - `ALTERNATE_YEAR`
  - `MANUAL_ONLY`
  - `SUPPRESSED`
- `PhenologyModelType`
  - `CHILL_HEAT`
  - `CLIMATE_WINDOW`
  - `TROPICAL_REPEAT`
  - `WARM_SEASON_PHOTOPERIOD`
  - `MANUAL_ONLY`

## Core rule

Do not research bloom dates for every region on earth for every species.

That does not scale.

Instead, scale with:
- species model type
- species baseline
- cultivar adjustment
- location climate fingerprint
- learned per-tree history

The research burden should mostly be on the first three items.

## What should be researched for a new species

Every new species should answer these questions before it goes into the catalog:

1. What forecast model type does it belong to?
2. Is the species seasonal, repeat-bearing, suppressed, or effectively manual-only?
3. What is a credible baseline bloom window or trigger set?
4. What pollination default is defensible?
5. Are there important aliases or taxonomy issues users will type?
6. Is the species likely to be strongly controlled by chill, warm-season heat, photoperiod, rainfall seasonality, or a mixture?

Minimum outputs for a new species:
- canonical species key
- alias list
- pollination default
- pattern type
- model type
- baseline bloom timing or trigger family
- uncertainty note
- note about whether cultivar timing matters a lot or only a little

`baseline_only` should include a real fertility packet, not just timing:
- `pollinationRequirement`
- `selfCompatibility`
- `pollinationMode`
- and, when they materially affect user action, sex expression, hand-pollination usefulness, or a short compatibility note

If those details are unknown, the baseline entry should say `UNKNOWN` and explain the uncertainty instead of skipping the category.

## What should be researched for a new cultivar

Do not treat every cultivar like it needs a full independent bloom model.

For most cultivars, research should try to answer only:
- Is this a real cultivar users will search for?
- Are there important aliases or trade-name misspellings?
- Is it earlier, mid, or later than the species baseline?
- Does it have a meaningful pollination override?
- Is there enough evidence to justify a hard override?

Minimum outputs for a cultivar row:
- cultivar name
- aliases if needed
- bloom phase or relative shift if evidence supports it
- pollination override only if evidence is stronger than the species default

If cultivar evidence is weak:
- add alias support
- inherit species timing
- inherit species pollination default
- document uncertainty in the research note

Do not invent fake precision just to make the catalog look fuller.

## Source quality rules

Source priority should be:

1. Extension publications and university cultivar guides
2. Research papers and cultivar evaluations
3. Official variety collections / breeding program releases
4. Government or reputable horticultural institute references
5. Specialist nursery or grower pages only for provisional alias support or cautious cultivar fill-in

Never let nursery prose silently outrank extension or research literature.

If nursery-grade sources are used:
- say so directly in the research doc
- keep the implementation conservative
- prefer aliases over hard timing/pollination claims

## Research packet template

Use:
- `docs/catalog-authoring-schema.md` as the canonical field list
- `docs/catalog-research-pass-template.md` as the default pass template

Every species pass should leave behind a filled checklist state, not just prose. That checklist is what lets future sessions know whether the species is still `baseline_only` or has genuinely been reviewed.

Every new species packet should capture:

- scope of this pass
- main sources used
- species baseline decision
- bloom pattern decision
- model type decision
- pollination default decision
- cultivar names added
- cultivar overrides added
- aliases added
- uncertainty and what was intentionally left out
- follow-up ideas for later model upgrades

Every research-backed species pass should also land with:
- one asset entry in `phenology_profiles.json`
- any needed pollination entry or explicit justification for using the species default
- at least one forecast or matching test that proves the new pattern/model behaves as intended

Keep one species-family or crop-group research note per pass when possible. Do not scatter reasoning only inside code comments.

## When to add a new model type

Do not keep forcing every species into the same fixed-window model.

Add or use a distinct model type when the species clearly behaves like one of these:
- chill + heat driven deciduous bloom
- subtropical seasonal window
- tropical repeat-bearing species
- warm-season / photoperiod-driven bloom
- effectively manual-only or suppressed bloom forecasting

Examples:
- apples, peaches, pears: chill/heat oriented
- citrus, lychee, avocado, mango: subtropical seasonal models with local shifts
- dragon fruit: warm-season / photoperiod style model, not generic USDA-window logic
- bananas or sugar cane: manual/suppressed bloom forecasting may be more honest than fake date precision

If a species obviously needs a different trigger family, do not solve it by adding dozens of region exceptions.

## Relationship to the future climate layer

Planned direction:
- user can search a location online
- app stores `lat/lon/elevation/timezone/country`
- app fetches a free global climate fingerprint
- app caches it locally
- forecast engine uses that climate fingerprint offline after fetch

This changes the research burden in a good way:
- less need for hand-built region exceptions
- less need to overvalue USDA zone
- more need to classify species by trigger family

In other words:
- research species biology
- do not research every town

The online climate layer should reduce regional guesswork, not replace catalog research. A new species still needs a defensible baseline and trigger family before location data can improve it.

## USDA vs advanced location tuning

USDA zone is still useful, especially for US users with limited inputs.

But for bloom timing, USDA should gradually become a supporting signal rather than the main driver once the app has:
- latitude
- longitude
- elevation
- climate fingerprint
- learned bloom history

For future work, prefer this trust order:

1. custom per-tree override
2. history-learned local observations
3. climate-aware species model
4. cultivar timing adjustment
5. USDA zone fallback

USDA is still valuable for survival/hardiness. It is not a complete bloom model.

## Practical scaling rules

When the catalog grows toward 500+ species:

- prioritize species coverage over excessive cultivar depth
- only add cultivar timing overrides where evidence is good
- add aliases aggressively enough for search usability
- keep uncertain cultivar behavior at species default rather than fabricating precision
- create model-type upgrades for whole groups instead of stacking one-off exceptions
- rely on learned observations to improve exact local timing over time

## Minimum implementation checklist for a new species pass

1. Start from `docs/catalog-authoring-schema.md`.
2. Create or update a research note in `docs/` using `docs/catalog-research-pass-template.md`.
3. Add or update the species asset entry first.
4. Add cultivar rows only when justified.
5. Add pollination defaults and overrides if justified.
6. Add aliases for real user input patterns.
7. Add tests for matching and forecast behavior.
8. Record unresolved uncertainties in the research note instead of hiding them in code.

## Anti-patterns

Avoid these:
- hand-writing bloom dates for every region and cultivar pair
- treating USDA as if it fully predicts flowering
- adding nursery trade names as if they are formal cultivars without caution
- using weak cultivar evidence to override species defaults
- keeping all reasoning only in `BloomForecastEngine.kt`
- solving biology-model problems with endless special-case if statements

## Bottom line

OrchardDex should scale by combining:
- researched species baselines
- conservative cultivar adjustments
- climate-aware location inputs
- learned local observations

Future sessions should preserve that structure instead of reverting to a giant static calendar table.
