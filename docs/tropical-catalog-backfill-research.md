# Tropical catalog backfill OrchardDex notes

This note records the catalog backfill pass for species that were already in OrchardDex but did not yet have a dedicated research note.

The goal of this pass was not to research every town or every cultivar. It was to:
- confirm the existing baseline windows are still defensible
- tighten pollination defaults where the extension literature was clear
- add uncertainty notes where the biology is real but the app should stay conservative
- leave each species with a usable baseline fertility packet, not just timing

## Modeling decisions updated in this pass

- `loquat` now uses `Self-fertile, cross-pollination helps`
- `guava` now uses `Self-fertile, cross-pollination helps`
- uncertainty notes were added for:
  - `mango`
  - `banana`
  - `coconut`
  - `longan`
  - `ambarella`
  - `avocado`
  - `mamoncillo`
  - `atemoya`
  - `abiu`
  - `caimito`
  - `cashew`
  - `jaboticaba`
  - `loquat`
  - `guava`
  - `sapodilla`
  - `soursop`

## Species decisions

### Avocado

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Cross-pollination recommended`
- treat the fertility packet as:
  - self-compatibility: `Partial`
  - pollination mode: `Insect`
  - compatibility nuance: type A/B bloom overlap improves crops, but a lone tree can still fruit
- reason:
  - UF/IFAS says Florida avocado flowers can self- and cross-pollinate
  - some cultivars fruit well alone, while others benefit from or need nearby A/B overlap
  - OrchardDex should not treat avocado as strictly self-fertile or strictly cross-dependent

### Mango

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Self-pollinating` at the app-bucket level, while still acknowledging insect activity matters for good set
- uncertainty note added
- reason:
  - UF/IFAS still supports mango as a basically annual bloom/fill crop in Florida
  - cultivar differences matter more in maturity season than in the app's coarse bloom-phase model
  - mango flowers include both male and perfect flowers, so a lone tree can fruit, but weather and pollinator activity still change real set
  - OrchardDex should keep the species in the self-fertile bucket without pretending the baseline window is equally strong every year

### Longan

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Self-fertile, cross-pollination helps`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Insect`
- uncertainty note added
- reason:
  - UF/IFAS emphasizes cool, dry fall/winter conditions as the big flowering trigger
  - excessive rain during bloom reduces pollination and fruit set
  - reliability is a climate issue first, not a reason to switch the species into repeat/manual mode
  - the safest baseline is still "single tree can crop, but cross-pollination and good bloom weather can improve outcomes"

### Loquat

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- change pollination default to `Self-fertile, cross-pollination helps`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Insect`
  - compatibility nuance: a lone tree can crop, but yield and fruit quality often improve with cross-pollination
- reason:
  - UF/IFAS says loquat is self-compatible
  - the same page says cross-pollination improves fruit set, size, and production
  - winter bloom timing is real, but countdown confidence should still be tempered when frost risk is high

### Guava

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW` as the catalog baseline
- change pollination default to `Self-fertile, cross-pollination helps`
- treat the fertility packet as:
  - self-compatibility: `Self-fertile`
  - pollination mode: `Insect`
  - compatibility nuance: self-set occurs, but cross-pollination is still worth surfacing for better yields
- reason:
  - UF/IFAS says self-pollination is possible
  - the same page says insect cross-pollination gives higher yields
  - Florida guava can be pruned into off-season flowering, but the baseline engine should still start from a spring/summer pattern and let history/manual overrides outrank it

### Caimito

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Cross-pollination recommended`
- treat the fertility packet as:
  - self-compatibility: `Partial`
  - pollination mode: `Insect`
  - compatibility nuance: some cultivars and seedlings set alone, while others clearly benefit from or require a pollinizer
- reason:
  - UF/IFAS says some seedlings and cultivars can set fruit without cross-pollination
  - the same page also says some seedlings require cross-pollination
  - that mixed behavior fits the middle bucket better than a hard self-fertile claim

### Mamoncillo

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Needs cross-pollination`
- treat the fertility packet as:
  - self-compatibility: `Self-sterile`
  - pollination mode: `Hand helpful` as the closest current app bucket
  - sex-expression nuance: the practical grower issue is usually functionally male vs female trees, not ordinary orchard-style self-set
- reason:
  - UF/IFAS describes the species as usually dioecious
  - UF/IFAS directly says two trees, one functionally male and one functionally female, are generally needed for good fruit production
  - this is strong enough to keep OrchardDex in the strict cross-pollination bucket

### Atemoya

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Cross-pollination recommended`
- treat the fertility packet as:
  - self-compatibility: `Partial`
  - pollination mode: `Insect` in the current app bucket, while acknowledging hand pollination is often the operational reality
  - hand-pollination nuance: very relevant
- uncertainty note added
- reason:
  - UF/IFAS documents protandry, poor natural set, and strong hand-pollination response
  - that absolutely justifies caution, but the current OrchardDex pollination buckets still do not have a clean "hand pollination often needed" species default
  - keep the current bucket for now and rely on the structured pollination profile text in detail views

### Soursop

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Cross-pollination recommended`
- treat the fertility packet as:
  - self-compatibility: `Partial`
  - pollination mode: `Insect`
  - hand-pollination nuance: relevant, but not strong enough in this pass to promote to a harder species-wide rule
- uncertainty note added
- reason:
  - this pass did not find a stronger current UF/IFAS home-landscape pollination source than the broader annona guidance
  - keep the species conservative until a fuller annona pollination pass is done

### Banana

- keep `CONTINUOUS + MANUAL_ONLY`
- keep `Pollination not required`
- treat the fertility packet as:
  - self-compatibility: not the useful question for edible dessert bananas
  - pollination mode: not required for ordinary edible fruit production
  - sex-expression nuance: edible dessert bananas still carry female and male flowers, but market fruit develops parthenocarpically
- uncertainty note added
- reason:
  - the product need is still "active season / watch now," not exact countdowns
  - edible dessert bananas do not benefit from pretending we can forecast one clean bloom window per plant
  - UF/IFAS explicitly says the rapidly growing ovaries in edible cultivars develop parthenocarpically, without pollination
  - that makes `Pollination not required` the useful species default for the app, even though seeded or wild bananas are a different biological story

### Abiu

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown`
  - pollination mode: `Unknown`
  - compatibility nuance: do not imply lone-tree reliability until the source packet is stronger
- uncertainty note added
- reason:
  - Florida timing is clear enough for a baseline window
  - pollination guidance is sparse enough that a fake self-fertile label would be weaker than staying unknown

### Ambarella

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown`
  - pollination mode: `Unknown`
  - compatibility nuance: do not imply a dependable lone-tree outcome until the source packet is stronger
- uncertainty note added
- reason:
  - current evidence was enough for a climate-fit baseline, not enough for a confident pollination rule

### Jaboticaba

- keep `MULTI_WAVE + TROPICAL_REPEAT`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown`
  - pollination mode: `Unknown`
  - compatibility nuance: repeat-bearing behavior is more defensible than a species-wide fertility claim right now
- uncertainty note added
- reason:
  - OrchardDex already has enough user-facing evidence to treat jaboticaba as a repeat/multi-wave plant rather than a single seasonal window
  - the immediate weakness is not timing pattern but species/cultivar naming overlap and thin fertility guidance
  - until a dedicated jaboticaba pass is done, the honest move is a repeat model with conservative fertility metadata

### Cashew

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Needs cross-pollination` for now
- treat the fertility packet as:
  - self-compatibility: `Self-sterile`
  - pollination mode: `Hand helpful` as the closest current app bucket
  - compatibility nuance: this is still a conservative orchard-facing warning, not a claim that every cultivar has identical breeding behavior
- uncertainty note added
- reason:
  - this is still a conservative placeholder
  - revisit in a later Anacardiaceae pass if a better extension packet supports a softer pollination bucket

### Coconut

- keep `CONTINUOUS + MANUAL_ONLY`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown` at the species level
  - pollination mode: `Unknown` at the species level
  - sex-expression nuance: coconut is monoecious, but dwarf, tall, and hybrid palms do not share the same effective breeding behavior
- uncertainty note added
- reason:
  - the app should not fabricate a single annual bloom window for coconut palms
  - tall coconuts are commonly cross-pollinating, while dwarf coconuts are much more self-pollinating, and hybrids sit in between
  - that is exactly the kind of species where a single self-fertile vs cross-pollination label would be more misleading than an explicit `Unknown`
  - this species needs either a dedicated palm-style model later or continued manual/history-led handling

### Sapodilla

- keep `SINGLE_ANNUAL + CLIMATE_WINDOW`
- keep `Unknown` pollination default
- treat the fertility packet as:
  - self-compatibility: `Unknown`
  - pollination mode: `Unknown`
  - compatibility nuance: do not hard-label lone-tree reliability while the source packet is still mixed
- uncertainty note added
- reason:
  - UF/IFAS supports the general Florida adaptation and seasonality
  - pollination guidance is still too inconsistent for a confident species-wide label

## Sources used in this pass

- UF/IFAS, `Avocado Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG213
- UF/IFAS, `Mango Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG216
- UF/IFAS, `Longan Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG049
- UF/IFAS, `Banana Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG040
- UF/IFAS Gardening Solutions, `Coconut Palm`
  - https://gardeningsolutions.ifas.ufl.edu/plants/trees-and-shrubs/palms-and-cycads/coconut-palm/
- PMC, `Fruit Biology of Coconut (Cocos nucifera L.)`
  - https://pmc.ncbi.nlm.nih.gov/articles/PMC9738799/
- UF/IFAS, `Loquat Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG050
- UF/IFAS, `Guava Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG045
- UF/IFAS, `Caimito (Star Apple) Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/HS309
- UF/IFAS, `Mamoncillo (Genip) Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/HS310
- UF/IFAS, `Atemoya Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG332
- UF/IFAS, `Sapodilla Growing in the Florida Home Landscape`
  - https://edis.ifas.ufl.edu/publication/MG057
- UF/IFAS, `Tropical and Subtropical Fruit Crops for the Home Landscape: Alternatives to Citrus`
  - https://edis.ifas.ufl.edu/publication/MG373

## Follow-up species passes still worth doing

- a dedicated annona pollination pass that covers `atemoya`, `soursop`, and any future cherimoya/custard-apple additions
- an Anacardiaceae pass for `mango`, `cashew`, and `ambarella`
- a palms/monocots pass for `banana` and `coconut`
