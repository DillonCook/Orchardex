# Canistel / eggfruit OrchardDex notes

This note records the first lean OrchardDex pass for canistel.

## Modeling choice

Canistel stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** go onto the continuous / everbearing path by default.
It does **not** collapse into a generic `Sapote` species row.

## Species boundary used in app

This lane is for **`Pouteria campechiana`**.

Accepted aliases currently wired:
- `canistel`
- `eggfruit`
- `egg fruit`
- `egg-fruit`
- `yellow sapote`
- `zapote amarillo`
- `Pouteria campechiana`
- `Lucuma campechiana`
- `Lucuma nervosa`
- `Pouteria campechiana var. nervosa`
- `Pouteria campechiana var. palmeri`

Why this matters:
- OrchardDex keeps canistel separate from mamey sapote, white sapote, green sapote, and black sapote.
- Historical taxonomy is normalized at the species level, not turned into fake cultivar rows.
- `Ross` is intentionally not shipped in this first pass because the packet flags possible separate-species uncertainty.

## Species baseline used in app

Current OrchardDex fallback:
- reference zone: `10b`
- primary bloom window: mid-January through late June
- pollination default: `Self-fertile, cross-pollination helps`

Florida-oriented season note:
- primary fruit season is roughly `Nov–Mar`
- a secondary fruit season can run `Aug–Oct`

Why this stays broad:
- stronger Florida sources support broad and often bimodal seasonality
- some cultivars can crop irregularly or over long windows, but the species still fits a normal seasonal model better than an everbearing one

## Cultivar seed set added

First-pass packeted cultivars added:
- Bruce
- Fairchild #1 (`Fairchild 1`)
- Fairchild #2 (`Fairchild 2`)
- Fitzpatrick
- Keisau
- Oro
- Trompo
- TREC 9680 (`TREC9680`)
- TREC 9681 (`TREC9681`)

Rows intentionally held back in this pass:
- `Ross`
- `USDA 1`
- `Saludo`

## Pollination mapping

Species default:
- `Self-fertile, cross-pollination helps`

Cultivar overrides used now:
- none

Why this stays conservative:
- the packet supports species-level self-fruitful behavior with insect pollination and possible benefit from another tree
- the current evidence does not support clean cultivar-specific fertility overrides

## Known simplifications

- Secondary fruit timing is preserved in notes only; it is not modeled as a separate season engine.
- `Fairchild` without a number is still ambiguous in real-world sources; OrchardDex only wires explicit `#1` / `#2` rows here.
- `Oro` and `Trompo` are kept separate from `TREC` accessions because the packet did not justify collapsing them.
- `Ross` remains excluded for now because of the possible separate-species conflict flagged in the packet.
