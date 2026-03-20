# Green sapote OrchardDex notes

This note records the first lean OrchardDex pass for green sapote.

## Modeling choice

Green sapote stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- a deliberately sparse cultivar / selection layer
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** collapse into a generic `Sapote` species row.
It does **not** overstate cultivar certainty where the literature is thin.

## Species boundary used in app

This lane is for **`Pouteria viridis`**.

Accepted aliases currently wired:
- `green sapote`
- `Pouteria viridis`
- `Calocarpum viride`
- `Achradelpha viridis`
- `injerto`
- `injerto verde`
- `raxtul`
- `zapote injerto`
- `white faisan`
- `red faisan`

Why this matters:
- OrchardDex keeps green sapote separate from mamey sapote, white sapote, black sapote, and canistel.
- Historical scientific synonyms are normalized for search/importing without collapsing species boundaries.
- Risky names like `zapote blanco` are intentionally not wired as merge aliases.

## Species baseline used in app

Current OrchardDex fallback:
- reference zone: `10b`
- primary bloom window: mid-February through late April
- pollination default: `Self-fertile`

Florida-oriented season note:
- primary fruit season is roughly `Dec–Mar`
- peak fruit is around late January

Why this stays conservative:
- stronger Florida support is still fairly thin and traces mainly to Whitman plus later UF/TREC work
- the app uses this as a practical Florida default, not a universal species truth

## Cultivar / selection seed set added

First-pass sparse rows added:
- `UF/TREC selection`
- `Whitman`

Why it stays this small:
- stronger sources support one documented Florida selection much better than a broad cultivar bench
- `Whitman` is retained only as a cautious provisional trade-name row, not as a strongly published cultivar claim

## Pollination mapping

Species default:
- `Self-fertile`

Cultivar overrides used now:
- none

Why this stays simple:
- the packet support points to self-fertility as the least-wrong default
- direct pollination research is still sparse
- there was not enough evidence to justify cultivar-level pollination overrides

## Known simplifications

- Fruit timing and late-January peak are preserved in notes only; OrchardDex is not modeling a separate fruit-season engine.
- `UF/TREC selection` is a documented Florida selection, not a formal cultivar release.
- `Whitman` is a cautious nursery-trade / lineage row and should not be treated as a proven formal cultivar name.
- Graft compatibility with mamey sapote rootstock is historical propagation context only, not a reason to merge species.
