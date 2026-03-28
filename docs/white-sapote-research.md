# White sapote OrchardDex notes

This note records the first lean OrchardDex pass for white sapote.

## Modeling choice

White sapote stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** merge into a generic `Sapote` species row.

## Species boundary used in app

This lane is for **`Casimiroa edulis`**.

Accepted aliases:
- `white sapote`
- `Casimiroa edulis`
- `zapote blanco`
- `casimiroa`

Deliberately excluded from this row:
- mamey sapote
- black sapote
- canistel / eggfruit
- green sapote

## Species baseline used in app

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: mid-November through late April / early May
- pollination default: `Cross-pollination recommended`

Florida-oriented season note:
- primary fruit season is roughly `May–Aug`
- a secondary/extended fruit window can run `Sep–Nov` for some cultivars and conditions

Why this is only a broad default:
- Florida and California timing differ a lot by cultivar and climate
- some cultivars can double-crop or hold much longer seasons than others
- species-level timing is therefore only a lean fallback

## Cultivar seed set added

First-pass packeted cultivars added:
- Blumenthal
- Dade
- Denzler (`Densler`)
- Golden (`Max Golden`)
- Homestead
- Lemon Gold
- McDill
- Pike
- Reinecke Commercial (`Reinekie`, `Reinikie`, `Reinikie Commercial`, `Reineke Commercial`)
- Smathers
- Suebelle (`Hubbell`)
- Vernon
- Yellow

## Pollination mapping

Species default:
- `Cross-pollination recommended`

Cultivar overrides used now:
- `Vernon` → `Self-fertile`
- `Blumenthal` → `Needs cross-pollination`
- `Reinecke Commercial` → `Needs cross-pollination`
- `Yellow` → `Needs cross-pollination`
- `Suebelle` → `Self-fertile, cross-pollination helps`
- `Lemon Gold` → `Self-fertile, cross-pollination helps`

Why this is still conservative:
- white sapote pollination is cultivar-dependent and some sources conflict
- `Dade`, `McDill`, `Golden`, and others stay on the species default because the packet support was not clean enough to hard-lock a stronger mapping

## Known simplifications

- No cultivar-specific season logic was added, even though cultivars like `Suebelle`, `Lemon Gold`, and `Vernon` can behave differently.
- Some white-sapote cultivars may show `Casimiroa tetrameria` / woolly-leaf influence or hybrid ancestry; OrchardDex is not modeling that separately in this pass.
- `Golden`, `Max Golden`, and `Golden Globe` are not auto-collapsed; only `Golden` / `Max Golden` are linked here.
