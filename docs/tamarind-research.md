# Tamarind OrchardDex notes

This note records the first lean OrchardDex pass for tamarind.

## Modeling choice

Tamarind stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** add special landrace or type-group taxonomy behavior.

## Species baseline used in app

Canonical species key: `tamarind`

Accepted aliases:
- `tamarind`
- `tamarindo`
- `Tamarindus indica`
- `imli`
- `ambli`
- `chinch`
- `sampalok`
- `makham`
- `makham waan`

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: mid-May through late August
- pollination default: `Needs cross-pollination`

Why this baseline:
- Florida sources consistently place flowering in late spring through summer.
- Fruit maturity timing varies across Florida sources and ripe pods can hang on the tree for a while, so the app only models a single primary bloom window here.
- Pollination evidence is much stronger than for jackfruit or lychee: tamarind trends much closer to a true cross-pollination-dependent species.

## Cultivar seed set added

Starter cultivars added to the catalog:
- Manila Sweet
- Makham Waan
- PKM-1 (`PKM 1`, `Periyakulam 1`)
- Urigam
- Prathisthan (`Pratishthan`)
- Goma Prateek
- Ajanta
- T-263 (`Tamarind 263`, `T 263`)
- Hasanur
- Tumkur
- DTS-1 (`DTS 1`)
- Yogeshwari
- Sichomphu (`Si Chomphu`, `Sri Chompoo`, `Si Chompoo`)
- Khandee (`Khantee`, `Kantee`)
- Prakai Thong (`Prakaithong`)
- Fak Dap
- Wan Lon
- Sithong (`Si Thong`)
- Sithong Bao (`Si Thong Bao`)
- Nam Phueng (`Nam Pheung`, `Namphueng`)
- Inthaphalam (`Intapalum`, `Intapalam`)
- Mun Jong (`Muen Chong`, `Munjong`)
- Saeng Athit (`Saengathit`)
- Aglibut Sweet
- PSAU Sour 2

## Pollination mapping

The current pass uses a single strong species-level label:
- species default: `Needs cross-pollination`

There are no cultivar-specific hard overrides yet.
The strongest accessible breeding-system evidence is species-level and supports the same practical conclusion across the catalog so far: tamarind usually needs pollen from a different tamarind genotype for reliable fruiting.

## Known simplifications

- Fruit season is documented in research notes but not modeled in the bloom engine.
- `Makham Waan` may behave both as a named cultivar and as a broader sweet-tamarind naming family in trade usage.
- Regional landraces, local selections, and breeding / pre-release lines were intentionally left out of this first pass.
- This is a conservative implementation-first catalog entry, not a full tamarind taxonomy pass.
