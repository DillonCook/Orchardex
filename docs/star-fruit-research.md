# Star fruit / carambola OrchardDex notes

This note records the first lean OrchardDex pass for star fruit.

## Modeling choice

Star fruit fits the current OrchardDex catalog shape well enough without another engine feature.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add species-specific region logic.
It does **not** add a special repeat-bearing engine yet.

## Species baseline used in app

Canonical species key: `star fruit`

Accepted aliases:
- `star fruit`
- `starfruit`
- `carambola`
- `Averrhoa carambola`

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: mid-April through late May / early June
- pollination default: `Cross-pollination recommended`

Why this baseline:
- UF Florida guidance supports April-May as a major bloom period.
- Star fruit can also bloom again later and fruit over long stretches in warm climates.
- That repeat-bearing behavior is real, but not yet modeled as a dedicated multi-window engine feature.

## Cultivar seed set added

Starter cultivars added to the catalog:
- Arkin
- Golden Star
- Fwang Tung
- Kary (`Kari`)
- Kajang (`Kaiang`)
- Sri Kembangan (`Sri Kambangan`)
- Lara
- B-10 (`B10`)
- B-17 (`B17`, `Belimbing Madu`)
- B-16 (`B16`)
- B-2 (`B2`)
- B-1 (`B1`)
- B-11 (`B11`, `Chan Yong I`)
- Hew-1 (`Hew #1`)
- Demak
- Dah Pon
- Tean Ma (`Team Ma`, `Tean-Ma`)
- Mih Tao (`Mei Tao`, `Mih-Tao`)
- Cheng Chui (`Cheng Tsey`, `Chun Choi`)
- Newcomb
- Star King
- Thayer
- Maha
- Thai Knight
- Wheeler

## Pollination overrides added

Explicit cultivar overrides currently wired:
- `Arkin` → `Self-fertile`
- `Golden Star` → `Self-fertile`
- `Kary` → `Self-fertile`
- `Fwang Tung` → `Cross-pollination recommended`
- `B-10` → `Cross-pollination recommended`
- `B-17` → `Cross-pollination recommended`
- `B-2` → `Cross-pollination recommended`
- `B-11` → `Cross-pollination recommended`
- `Tean Ma` → `Cross-pollination recommended`
- `Mih Tao` → `Partial self-incompatibility`
- `Star King` → `Cross-pollination recommended`
- `Wheeler` → `Cross-pollination recommended`

Other seeded cultivars currently inherit the species default.

## Known simplifications

- Secondary Florida bloom / fruit peaks are not separately modeled yet.
- Alias conflicts like `Maha` vs `MAHA 66` are intentionally not auto-collapsed.
- This is a conservative first pass meant to fit the current app model cleanly.
