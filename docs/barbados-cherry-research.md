# Barbados cherry / acerola OrchardDex notes

This note records the first lean OrchardDex pass for Barbados cherry.

## Modeling choice

Barbados cherry is wired onto the existing reusable everbearing / repeat-bearing path instead of getting a one-off engine rule.

Current implementation uses:
- species catalog entry + aliases
- cultivar catalog entries + aliases
- graded pollination metadata
- existing `MANUAL_ONLY` / separate dashboard everbearing listing

It does **not** create a one-off acerola feature.
It does **not** force acerola into a fake single annual bloom window.

## Species baseline used in app

Canonical species key: `barbados cherry`

Accepted aliases:
- `barbados cherry`
- `acerola`
- `west indian cherry`
- `acerola cherry`
- `Malpighia emarginata`
- `Malpighia glabra`
- `Malpighia punicifolia`

Current OrchardDex behavior:
- forecast behavior: tracked as `Continuous / repeat-bearing`
- automatic monthly bloom windows: disabled
- pollination default: `Self-fertile, cross-pollination helps`

Why this model fits better:
- acerola flowers and fruits in repeated flushes rather than a clean once-per-year orchard cycle
- rainfall and irrigation flushes strongly influence bloom behavior
- sources describe multiple crops per year, and under irrigation the plant can behave nearly continuously

## Important taxonomy note

OrchardDex should treat `Malpighia emarginata` as the canonical species.
Older literature using `M. glabra` or `M. punicifolia` is handled here only as historical naming drift, not as separate species rows.

## Cultivar seed set added

Starter cultivars added to the catalog:
- Florida Sweet
- B-17 (`B17`)
- J.H. Beaumont (`Beaumont`)
- F. Haley (`Haley`)
- Hawaiian Queen
- Maunawili
- Tropical Ruby
- C.F. Rehnborg (`Rehnborg`)
- Manoa Sweet
- Red Jumbo
- Flor Branca
- Junko
- BRS Sertaneja (`Sertaneja`)
- Costa Rica
- Okinawa
- Nikki
- Coopama Nº 1 (`Coopama No. 1`)
- BRS Cabocla (`Cabocla`)
- BRS 235 Apodi (`Apodi`)
- BRS 236 Cereja (`Cereja`)
- BRS 237 Roxinha (`Roxinha`)
- BRS 238 Frutacor (`Frutacor`)
- BRS 366 Jaburu (`Jaburu`)
- Rubra
- Olivier
- Waldy-CATI 30 (`Waldy CATI 30`)
- UEL 3 - Dominga (`UEL3 Dominga`)
- UEL 4 - Ligia (`UEL4 Ligia`)
- UEL 5 - Natalia (`UEL5 Natalia`)

## Pollination mapping

The current pass uses:
- species default: `Self-fertile, cross-pollination helps`
- cultivar override: `Olivier` → `Cross-pollination recommended`

Why:
- acerola can self-pollinate in at least some conditions
- cross-pollination and pollinator presence often improve fruit set substantially
- `Olivier` had the strongest packet-level signal for stronger cross-dependence, so it gets the only cultivar-specific override in this lean pass

## Known simplifications

- The app is not yet storing detailed cultivar pollination notes like the interplanting recommendations for `Cabocla` / `Rubra`.
- If OrchardDex later needs more nuanced repeat-bearing labels, they should generalize beyond acerola instead of creating a species-specific rule.
- Jamaican cherry remains a separate species and should not be merged with Barbados cherry.
