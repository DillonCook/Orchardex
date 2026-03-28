# Jackfruit OrchardDex notes

This note records the first lean OrchardDex pass for jackfruit.

## Modeling choice

Jackfruit stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add another engine feature.
It does **not** model off-season bearing as a separate season system yet.

## Species baseline used in app

Canonical species key: `jackfruit`

Accepted aliases:
- `jackfruit`
- `jack fruit`
- `jack`
- `Artocarpus heterophyllus`
- `kathal`
- `panas`
- `nangka`
- `nagka`

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: late January through March
- pollination default: `Self-fertile, cross-pollination helps`

Why this baseline:
- Florida fruiting guidance points to a main summer season, with flowering months inferred from development interval rather than directly stated in the strongest sources.
- Jackfruit can fruit on a single tree, but cross-pollination can improve seed set and fruit quality.
- Off-season or twice-annual bearing is real for some cultivars, but it is currently kept in notes rather than modeled in the engine.

## Cultivar seed set added

Starter cultivars added to the catalog:
- Black Gold
- Dang Rasimi
- Golden Nugget (`Gold Nugget`)
- Honey Gold
- J-30 (`J30`)
- J-31 (`J31`)
- NS1 (`NS-1`)
- Tabouey
- Cochin
- Chompa Gob (`Chompa Grob`, `Champa Gob`)
- Kun Wi Chan (`Thai Globe`)
- Lemon Gold
- Golden Pillow (`Mong Tong`)
- Fairchild First
- Sweet Fairchild
- Mia 1
- Leung Bang
- Bosworth No. 3
- Galaxy
- Alba
- Hew
- N.A.N.S.I. (`Nansi`)
- Reliance
- Tree Farm
- Yullatin
- Ziemen
- Velipala
- Singapore (`Ceylon Jack`, `Singapore Jack`)
- Panruti Selection
- Thanjavur Jack
- Burliar 1 (`Burliar-1`)
- PLR 1 (`PLR.1`, `Palur 1`, `Palur.1 Jack`)
- PPI 1 (`PPI.1`)
- PLR (J) 2
- Gulabi
- Rudrakshi
- Hazari
- Champa (`Champaka`, `Champa Jack`)
- Siddu (`Siddujack`)
- Shankara
- Muttom Varikka (`Muttam Varikka`, `Muttomvarikka`)
- Sindhoor (`Sindoor`, `Sindhoora Varikka`)

## Pollination mapping

The current pass uses a generalized graded label:
- species default: `Self-fertile, cross-pollination helps`

There are no cultivar-specific hard overrides yet.
The packet support was strongest for the same practical conclusion across named cultivars already sampled: self-set can occur, but cross-pollination improves results.

## Known simplifications

- Fruit season is documented in research notes but not modeled in the bloom engine.
- `J-31`, `PLR 1`, and `PPI 1` have meaningful off-season / second-season behavior that is not separately modeled yet.
- `Cheena` was intentionally left out of this pass because it is a jackfruit × champedak hybrid, not a clean pure-jackfruit cultivar entry.
- `Mia 1` / `Mai 1` remains unresolved, so only `Mia 1` is currently seeded.
