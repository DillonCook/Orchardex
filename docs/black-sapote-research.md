# Black sapote OrchardDex notes

This note records the first lean OrchardDex pass for black sapote.

## Modeling choice

Black sapote stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** collapse into a generic `Sapote` species row.
It does **not** flatten the species into a simple self-fertile story.

## Species boundary used in app

This lane is for **`Diospyros digyna`**.

Accepted aliases currently wired:
- `black sapote`
- `black persimmon`
- `zapote negro`
- `sapote negro`
- `zapote prieto`
- `Diospyros digyna`
- `Diospyros nigra`
- `Diospyros obtusifolia`
- `Sapota nigra`

Why this matters:
- OrchardDex keeps the row on `Diospyros digyna` for project consistency.
- `Diospyros nigra` is preserved as a taxonomic alias so incoming data still resolves correctly.
- Other sapotes remain separate species rows.

## Species baseline used in app

Current OrchardDex fallback:
- reference zone: `10b`
- primary bloom window: mid-March through late August / early September
- pollination default: `Cross-pollination recommended`

Florida-oriented season note:
- primary fruit season is roughly `Dec–Feb`
- a secondary fruit season can run `Jun–Aug`

Why the bloom window is broad:
- black sapote is better treated as a flush-linked split-season species than as one tidy bloom month
- stronger literature supports both spring-flush and summer-flush flowering cohorts in warm climates

## Cultivar seed set added

First-pass packeted cultivars added:
- Merida (`Reineke`, `Reinecke`)
- Bernicker (`Bernecker`)
- Mossman
- Maher
- Ricks Late (`Rick's Late`)
- Superb
- Cocktail

## Pollination mapping

Species default:
- `Cross-pollination recommended`

Cultivar overrides used now:
- none

Why this stays conservative:
- the species clearly has sex-expression and compatibility nuance
- the current packet supports a species-level warning strongly, but not clean enough named-cultivar certainty for hard overrides
- `Superb` has a useful pollination-related seediness note in the packet, but that stays as notes-level nuance for now rather than becoming a fertility override

## Known simplifications

- Secondary fruit timing is preserved in notes only; it is not modeled as a separate fruit-season engine rule.
- Seedling trees may prove male-only, but OrchardDex is not modeling sex expression as a first-class field in this pass.
- `Cocktail` is a real cultivar name in the packet, but it also collides with the generic nursery term `cocktail tree`, so that name should stay caution-heavy.
- Older black-sapote taxonomy and misapplied names remain messy; OrchardDex only normalizes the packet-backed `D. digyna` / `D. nigra` family here.
