# Jamaican cherry / Panama berry OrchardDex notes

This note records the first lean OrchardDex pass for Jamaican cherry.

## Modeling choice

Jamaican cherry is wired onto the same reusable everbearing / repeat-bearing path used for Barbados cherry.

Current implementation uses:
- species catalog entry + aliases
- minimal cultivar/form catalog entries
- graded pollination metadata
- existing `MANUAL_ONLY` / separate dashboard everbearing listing

It does **not** fake a normal monthly bloom forecast.
It does **not** pretend there is a rich cultivar universe when the packet support is thin.

## Species baseline used in app

Canonical species key: `jamaican cherry`

Accepted aliases:
- `jamaican cherry`
- `jamaica cherry`
- `panama berry`
- `strawberry tree`
- `cotton candy berry`
- `singapore cherry`
- `jam fruit tree`
- `calabura`
- `Muntingia calabura`

Current OrchardDex behavior:
- forecast behavior: tracked as `Continuous / repeat-bearing`
- automatic monthly bloom windows: disabled
- pollination default: `Self-fertile`

Why this model fits better:
- flowers and fruits are produced nearly year-round in warm climates
- the strongest literature support points to autonomous self-pollination at very high rates
- the real modeling need is repeat-bearing species tracking, not pollination complexity or a deep cultivar catalog

## Cultivar / form seed set added

This species is intentionally cultivar-light.
The first pass only seeds:
- `Standard red-fruited type`
- `Yellow-fruited form`
  - aliases: `Yellow Jamaica Cherry`, `Yellow Jamaican Cherry`, `Yellow Panama Berry`, `Yellow Strawberry Tree`

That is the full credible implementation-first set from the packet.

## Pollination mapping

The current pass uses a single species-level label:
- species default: `Self-fertile`

There are no cultivar-specific hard overrides.
A single plant can fruit very well on its own, and pollination is not needed for normal fruit set.

## Known simplifications

- Common-name aliases are doing most of the catalog work here; OrchardDex should resist inventing fake cultivar precision.
- The yellow-fruited material is treated as a form-style entry, not a proven clone-clean cultivar family.
- If the catalog expands later, it should be because stronger standardized cultivar evidence appears, not because retail trade names are padded into fake rows.
