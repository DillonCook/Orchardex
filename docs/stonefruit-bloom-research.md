# Stonefruit Bloom Research Notes

The peach, nectarine, low-chill plum, and tart-cherry additions in the bloom catalog are based on UF/IFAS and Penn State documentation for backyard and commercial-adapted selections.

## Sources

- UF/IFAS EDIS, *Florida Peach and Nectarine Varieties*: https://edis.ifas.ufl.edu/publication/MG374/
- UF/IFAS EDIS, *Dooryard Fruit Varieties*: https://edis.ifas.ufl.edu/publication/MG248
- Penn State Extension, `Cherries in the Garden and the Kitchen`: https://extension.psu.edu/cherries-in-the-garden-and-the-kitchen
- Penn State Extension, `Orchard Pollination: Pollinizers, Pollinators, and Weather`: https://extension.psu.edu/orchard-pollination-pollinizers-pollinators-and-weather

## Implementation note

The app still uses phase-based bloom timing rather than explicit cultivar-by-zone date tables. For the added stonefruit cultivars, bloom phase is inferred from the official cultivar groupings, chill adaptation, and relative earliness of release notes so the forecast engine can keep scaling without hardcoding one table per zone.

Some retail cultivar labels differ from official release names. Where a retail or misspelled form is common enough to matter for user input, it is handled as an autocomplete alias rather than as a separate official cultivar.

## Sour cherry baseline

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating`
  - compatibility nuance: nearby cultivars can still improve total crop size even when a tree can fruit alone
- reason:
  - Penn State explicitly describes tart or sour cherries as self-fruitful
  - Penn State also notes that self-fruitful tart cherries can still crop more heavily with another cultivar nearby
