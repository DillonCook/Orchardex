# Sugar cane OrchardDex notes

This note records the first lean OrchardDex pass for cultivated sugar cane.

## Modeling choice

Sugar cane does **not** fit the normal OrchardDex fruit-tree reproductive model.

Current implementation uses:
- a dedicated searchable species row for the cultivated hybrid complex
- cultivar / clone / use-group catalog entries with aliases
- suppressed bloom / fruit / pollination forecasting
- `Unknown` fertility instead of fake self-/cross-pollination guidance

It does **not** treat sugar cane as everbearing.
It does **not** add a fruiting window.
It does **not** add a normal bloom window.

## Species row used in app

Canonical key in app:
- `Saccharum spp.`

User-facing label:
- `Sugarcane (cultivated hybrid complex)`

Accepted aliases:
- `sugar cane`
- `sugarcane`
- `cane`
- `Saccharum spp.`
- `Saccharum officinarum`

Why this is the chosen row:
- modern cultivated sugar cane is better described as a hybrid complex than as one clean species
- the harvested product is the stalk, not a fruit crop
- ordinary grower decisions are about clone identity, planting, ratooning, and harvest timing more than pollination

## OrchardDex behavior used in app

Current OrchardDex rule set:
- suppress automatic bloom forecasting
- suppress fruit-season forecasting
- do not surface pollinator advice
- do not route sugar cane into the repeat-bearing / continuous-bloom dashboard list

That keeps the app from teaching the wrong reproductive story for a clonal crop.

## Cultivar model added

This first pass uses both use-group rows and a small set of named clone rows.

Use-group rows:
- `Chewing cane`
- `Syrup cane`
- `Crystal / commercial cane`

Named rows added:
- `Yellow Gal` (`F31-407`, `F 31-407`)
- `White Transparent`
- `Georgia Red`
- `Home Green`
- `Louisiana Ribbon`
- `Louisiana Purple`
- `Louisiana Striped`
- `Green German`
- `CP 96-1252` (`CP96-1252`)
- `CP 01-1372` (`CP01-1372`)
- `CP 00-1101` (`CP00-1101`)
- `CP 89-2143` (`CP89-2143`)

## Pollination / propagation interpretation

Species default:
- `Unknown`

Current OrchardDex interpretation:
- sugar cane is treated as a **clonal crop** maintained from stalk cuttings / seed cane and ratoons
- no cultivar-level pollination overrides are used in this pass
- no self-fertile / cross-pollination labels are attached, because those are not the right operational model for this crop

## Known simplifications

- Planting and harvest timing are only preserved in notes for now; they are not modeled as bloom/fruit phenology.
- The cultivar universe is far larger than this first seed set; this pass keeps only the packet-backed use groups and named clones.
- `Saccharum officinarum` is preserved only as a legacy taxonomy alias, not as the main species row for modern cultivated cane.
