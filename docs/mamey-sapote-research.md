# Mamey sapote OrchardDex notes

This note records the first lean OrchardDex pass for mamey sapote.

## Modeling choice

Mamey sapote stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** go onto the continuous / everbearing path by species default.
It does **not** collapse into a generic `Sapote` species row.

## Species boundary used in app

This lane is for **`Pouteria sapota`**.

Accepted aliases currently wired:
- `mamey sapote`
- `mamey colorado`
- `zapote colorado`
- `zapote mamey`
- `Pouteria sapota`
- `Calocarpum sapota`
- `Calocarpum mammosum`
- `Lucuma mammosa`

Why this matters:
- OrchardDex keeps mamey sapote separate from mammee apple / yellow mamey, green sapote, canistel, white sapote, and black sapote.
- Plain `mamey`, `sapote`, and `zapote` remain too ambiguous to use as merge keys.
- Historical scientific names are normalized for search/importing, not treated as separate species.

## Species baseline used in app

Current OrchardDex fallback:
- reference zone: `10b`
- primary bloom window: early June through early March
- pollination default: `Self-fertile, cross-pollination helps`

Florida-oriented season note:
- primary fruit season fallback is roughly `Mar–Sep`
- cultivar staggering can extend mature fruit availability into winter in mixed plantings

Why this stays broad:
- Florida literature is strongly cultivar-driven for mamey sapote seasonality
- flowers, immature fruit, and mature fruit can overlap on the same tree
- the app still uses a species fallback for unknown cultivars, but cultivar identity matters more than usual here

## Cultivar seed set added

First-pass Florida-forward cultivar rows added:
- Pantin (`Key West`)
- Magana (`Magaña`)
- Pace
- Tazumal
- Mayapan (`AREC No. 2`)
- Copan (`AREC No. 1`)
- Lara
- Florida
- Piloto
- Chenox
- Abuelo
- Francisco Fernandez (`Francisco Fernancez`)
- Flores
- Viejo
- Lorito
- Cepeda Especial (`Cepeda Special`)
- Akil Especial (`Akil Special`)
- AREC No. 3

Recognized but intentionally not shipped as full rows in this first pass:
- `Cayo Hueso`
- `Cuban No. 1`
- `Progreso`

## Pollination mapping

Species default:
- `Self-fertile, cross-pollination helps`

Cultivar overrides used now:
- none

Why this stays conservative:
- the packet supports a middle-ground species default better than either hard self-fertile or hard pollinizer-required logic
- the current evidence does not justify cultivar-specific fertility overrides
- absence of overrides here means weak evidence, not proof that every cultivar behaves identically

## Known simplifications

- Florida fruit maturity is heavily cultivar-driven, but OrchardDex is not modeling cultivar-specific fruit windows yet in this pass.
- `Tazumal` has a historically reported second crop in some sources; that stays as notes-level nuance for now.
- `Florida` and `Lara` are literal cultivar names and should not be mistaken for metadata fields.
- `Pantin` / `Key West` are linked, but `Cayo Hueso` intentionally remains separate because the packet warned against auto-collapsing it.
