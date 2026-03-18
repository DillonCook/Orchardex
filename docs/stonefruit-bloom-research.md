# Stonefruit Bloom Research Notes

The peach, nectarine, and low-chill plum additions in the bloom catalog are based on UF/IFAS cultivar documentation for Florida backyard and commercial-adapted selections.

## Sources

- UF/IFAS EDIS, *Florida Peach and Nectarine Varieties*: https://edis.ifas.ufl.edu/publication/MG374/
- UF/IFAS EDIS, *Dooryard Fruit Varieties*: https://edis.ifas.ufl.edu/publication/MG248

## Implementation note

The app still uses phase-based bloom timing rather than explicit cultivar-by-zone date tables. For the added stonefruit cultivars, bloom phase is inferred from the official cultivar groupings, chill adaptation, and relative earliness of release notes so the forecast engine can keep scaling without hardcoding one table per zone.

Some retail cultivar labels differ from official release names. Where a retail or misspelled form is common enough to matter for user input, it is handled as an autocomplete alias rather than as a separate official cultivar.
