# Sugar apple / sweetsop OrchardDex notes

This note records the first lean OrchardDex pass for sugar apple.

## Modeling choice

Sugar apple stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** add a special protogyny-specific pollination mode.

## Species baseline used in app

Canonical species key: `sugar apple`

Accepted aliases:
- `sugar apple`
- `sweetsop`
- `sweet sop`
- `Annona squamosa`
- `sitaphal`

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: mid-March through mid/late June
- pollination default: `Cross-pollination recommended`

Why this baseline:
- UF Florida guidance supports bloom from March through June.
- The real pollination nuance is protogynous flower timing and weak natural pollination, not a clean orchard-style second-cultivar rule.
- The current pollination label is therefore only an approximation of “pollen transfer between different flowers is commonly needed or helpful.”

## Cultivar seed set added

Starter cultivars added to the catalog:
- Lessard Thai (`Thai Lessard`)
- Kampong Mauve
- Purple or Red
- Cuban Seedless (`Seedless Cuban`)
- Brazilian Seedless (`Brazilian seedless`)
- Thai Seedless
- LeahReese
- Na Dai
- Balanagar (`Balangar`)
- Kakarlapahad
- Washington
- Mammoth (`A squamosa var mammoth`)
- Red
- Red-speckled (`Red Speckled`)
- Crimson
- Yellow
- White-stemmed (`White Stemmed`)
- Barbados
- British Guiana
- Red Sitaphal
- Raidurg
- APK-1 (`APK1`)
- Barbados Seedlings
- Washington 07005
- Washington 98797
- NMK-1 Golden (`NMK-1`)
- Beni Mazar
- Abd El Razik (`Abd E1 Razik`)

## Pollination overrides added

Explicit cultivar overrides currently wired:
- `Brazilian Seedless` → `Needs cross-pollination`

Other seeded cultivars currently inherit the species default.

## Known simplifications

- Fruit season is documented in research notes but not modeled in the bloom engine.
- `Purple or Red` is intentionally kept separate from `Red` instead of being auto-collapsed.
- `Thai Seedless`, `Cuban Seedless`, and `Brazilian Seedless` are treated as separate entries because seedless-family identity is still unresolved.
- This is a conservative first pass meant to fit the current app model cleanly.
