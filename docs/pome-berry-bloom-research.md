# Pome and Berry Bloom Research Notes

This packet now covers the pome, berry, fig, grape, mulberry, persimmon, and pomegranate species that OrchardDex keeps in the temperate baseline catalog.

The app still uses the same core forecast shape for these crops: a species bloom baseline tied to a reference USDA zone, plus cultivar phase adjustments only where they are actually justified.

## Sources

- Stark Bro's apple bloom and pollination chart: https://www.starkbros.com/page/apple-tree-bloom-and-pollination-chart
- University of Georgia / Southern Region Small Fruit Consortium, *Blueberry Cultivars for Georgia*: https://smallfruits.org/files/2019/06/06bbcvproc_Nov0206.pdf
- UF/IFAS blueberry cultivar topic pages: https://edis.ifas.ufl.edu/topics/blueberry_varieties
- Penn State Extension, `Asian Pears in the Home Orchard - Variety Selection`: https://extension.psu.edu/asian-pears-in-the-home-orchard-variety-selection/
- Penn State Extension, `Pear Production in Home Fruit Plantings`: https://extension.psu.edu/pear-production-in-home-fruit-plantings/
- Penn State Extension, `Pollination Requirements for Various Fruits and Nuts`: https://extension.psu.edu/pollination-requirements-for-various-fruits-and-nuts
- Penn State Extension, `Figs in the Home Garden`: https://extension.psu.edu/figs-in-the-home-garden/
- Penn State Extension, `Strawberry Pollinator Diversity, Significance, and Management`: https://extension.psu.edu/strawberry-pollinator-diversity-significance-and-management
- Utah State University Extension, `Pomegranate, Fruit of the Desert`: https://extension.usu.edu/yardandgarden/research/pomegranate-fruit-of-the-desert
- North Carolina Extension Gardener Plant Toolbox, `Mulberry - Morus rubra`: https://plants.ces.ncsu.edu/plants/morus-rubra/common-name/mulberry/
- Clemson HGIC, `How to Grow Persimmons in South Carolina`: https://hgic.clemson.edu/factsheet/persimmon/

## Species decisions

### Asian pear

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Needs cross-pollination`
- treat the fertility packet as:
  - self-compatibility: `Self-sterile`
  - pollination mode: `Hand helpful` as the closest current app bucket
  - compatibility nuance: Asian pears generally need a second compatible Asian pear cultivar, and common pears may not overlap enough to help
- reason:
  - Penn State says to plant at least two Asian pear varieties for reliable fruit set
  - Penn State also notes Asian and European pear pollen can be compatible but often do not overlap well enough in bloom

### Blackberry

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: most cultivars are self-fruitful, but a few exceptions exist
- reason:
  - Penn State lists most blackberry and dewberry cultivars as self-fruitful
  - the species entry should stay broad rather than encoding every outlier as a species-level warning

### Fig

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: the home-garden baseline is common fig, not the full Smyrna/caprification system
- reason:
  - Penn State's home-garden fig guidance centers on common figs that develop fruit parthenocarpically
  - the app should not imply every fig pollination system is modeled equally

### Grape

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: bunch grapes fit this baseline better than muscadines or wild grapes
- reason:
  - Penn State lists most grapes as self-fruitful
  - the species baseline stays intentionally broad because OrchardDex is not splitting bunch grapes from muscadines in this packet

### Mulberry

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown`
  - pollination mode: `Unknown`
  - sex-expression nuance: many mulberries are dioecious, but not all fruiting cultivars behave the same way
- reason:
  - NC State describes red mulberry as typically dioecious with male and female flowers usually on separate trees
  - the broader `Morus spp.` entry is too mixed to justify a stronger orchard-style species label

### Persimmon

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown`
  - pollination mode: `Unknown`
  - compatibility nuance: American persimmon and Oriental persimmon do not share the same fertility behavior
- reason:
  - Clemson says native persimmons are typically dioecious and often need male and female trees
  - the same source says Oriental persimmons are usually self-fruitful
  - that split is exactly why the generic `Diospyros spp.` baseline should stay conservative

### Pomegranate

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: insects still matter for good pollen transfer and fruit quality
- reason:
  - Utah State says pomegranates are self-pollinating
  - the same source still notes bee and insect activity contributes to pollen transfer

### Raspberry

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: crop size can still improve with cross-pollination, especially across caneberry types
- reason:
  - Penn State lists most raspberries as self-fruitful, but still notes crop-size improvement from cross-pollination in some groups

### Strawberry

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: pollinator visits still strongly affect shape and fullness of the berry
- reason:
  - Penn State's strawberry pollination guidance explicitly treats cultivated strawberries as self-fertile
  - the same guidance emphasizes that more complete pollination still improves berry quality

## Implementation note

Blueberry bloom timing in the app is inferred from official chilling and relative flowering guidance rather than an explicit cultivar-by-zone date table. Cultivars documented as earlier or later flowering than Climax or other reference cultivars were placed into earlier or later bloom phases, then shifted by USDA zone in the forecast engine.

For the added temperate species in this packet, the main goal is not cultivar-perfect timing. It is a defensible species baseline plus an honest fertility bucket and uncertainty note.
