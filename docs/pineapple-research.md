# Pineapple OrchardDex notes

This note records the first lean OrchardDex pass for pineapple.

## Modeling choice

Pineapple stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** model pineapple as everblooming / continuous.

## Species baseline used in app

Canonical species key: `pineapple`

Accepted aliases:
- `pineapple`
- `piña`
- `pina`
- `ananas`
- `Ananas comosus`

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: mid-February through late April
- pollination default: `Self-fertile`

Why this baseline is weak by design:
- pineapple flowering is driven more by plant maturity and induction conditions than by a clean annual bloom season
- natural induction is associated with cool weather, short days, and stress, and flowering may also be forced
- the app baseline should therefore be treated only as a rough natural-flowering default

## Important biology note

Pineapple is monocarpic at the rosette level:
- each rosette flowers once
- each rosette fruits once
- slips, suckers, and ratoons continue the line afterward

This is why pineapple does not fit a normal orchard-tree seasonal model very well even though the app still keeps a simple baseline window.

## Cultivar seed set added

Starter cultivars added to the catalog:
- Smooth Cayenne
- Kew
- Giant Kew
- Red Spanish
- Green Spanish
- Singapore Spanish
- Queen
- Natal Queen
- Ripley Queen
- MacGregor
- Victoria (`Queen Victoria`)
- Mauritius (`Red Ceylon`, `Moris`, `Morris`)
- Sugarloaf (`White Sugarloaf`, `Kona Sugar Loaf`, `Kona Sugarloaf`)
- Pernambuco
- Perolera
- Manzana
- MD-2 (`MD2`, `Del Monte Gold`, `Gold Extra Sweet`, `Extra Sweet`)
- N36 (`N-36`)
- Josapine (`Josephine`, `Josaphine`)
- Gandul
- BRS Imperial (`Imperial`)
- BRS Vitória (`BRS Vitoria`)
- Tainung 17
- Tainung 21
- N67-10
- Soft Touch
- Gold Barrel
- Amritha (`Amrutha`)

## Pollination mapping

The current pass uses a single species-level label:
- species default: `Self-fertile`

Practical note:
- a single pineapple plant can fruit on its own
- pollination is usually not needed for edible fruit production
- cross-pollination between different cultivars can make fruit seedy, which is usually undesirable

There are no cultivar-specific hard pollination overrides in this pass.

## Known simplifications

- Fruit season is documented in research notes but not modeled in the bloom engine.
- This baseline should not be read as a strong annual flowering calendar; it is only a rough Florida natural-induction default.
- Alias handling is intentionally conservative so OrchardDex does not over-merge distinct pineapple varieties in the UI.
- White-fleshed low-acid families like `Sugarloaf`, `Pernambuco`, and `Abacaxi` were intentionally not collapsed together.
