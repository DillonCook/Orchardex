# Citrus Bloom Research Notes

This catalog expansion is based on two layers:

1. Species bloom baselines by citrus type.
2. Cultivar phase adjustments using early, mid, and late season variety references.

The app does not store a hand-written table for every USDA zone and cultivar pair. Instead, each cultivar is anchored to a reference citrus bloom window and shifted by USDA half-zone so the forecast can scale as more cultivars are added.

## Bloom baseline sources

- UF/IFAS, *Flowering and Fruit Set of Citrus*: oranges, grapefruit, and mandarins usually have one main spring bloom, most often in March in subtropical regions, while lemons and limes keep a main spring bloom with lighter repeat bloom through the year.
- UC IPM, *Timings for Key Cultural and Management Practices / Citrus*: California citrus bloom falls in the spring crop-development window and shifts with local conditions and cultivar timing.
- USDA Plant Hardiness Zone Map: zone framework used for the app's half-zone offsets.

## Cultivar list sources

- University of Arizona Cooperative Extension, *Low Desert Citrus Varieties*: orange, grapefruit, lemon, lime, mandarin, tangelo, and hybrid variety lists with relative earliness and season windows.
- UF/IFAS, *The Satsuma Mandarin*: satsuma cultivar list including Owari, Brown Select, Early St. Ann, Xie Shan, Miho, Obawase, Okitsu, Armstrong, and Seto.
- UCR Givaudan Citrus Variety Collection: additional cultivar names and early/mid/late maturity references for navels, mandarins, satsumas, lemons, and grapefruit hybrids.

## Implementation note

When a specific cultivar bloom chart was not available, cultivar phase was inferred from extension or variety-collection earliness descriptors relative to the species baseline. That keeps the monthly dashboard forecast useful while leaving room for more exact regional cultivar data later.
