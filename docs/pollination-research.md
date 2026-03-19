# Pollination research notes

## Scope
This note backs the first OrchardDex pollination metadata pass:
- dragon fruit cultivar pollination status
- species/cultivar defaults for the existing bloom catalog where sources were reasonably clear
- cautious use of `cross-pollination recommended` when a cultivar can set some fruit but usually performs better with a pollinizer

## Main sources used

### Dragon fruit
1. **Cal Poly Pomona Nursery — Dragon Fruit Variety Resource Page**
   - https://calpolypomonanursery.com/dragon-fruit-variety-resource-page/
   - Strongest single source used for named dragon fruit cultivars and self-fertile vs self-sterile status.
   - Used for: American Beauty, AX, Condor, Connie Mayer, Dark Star, Delight, Frankie's Red, Halley's Comet, La Verne, Lisa, Natural Mystic, Palora, Physical Graffiti, Purple Haze, Rixford, San Ignacio, Shayna, Sin Espinas, Sugar Dragon, Valdivia Roja, Vietnamese Red, Vietnamese White, Zamorano.
2. **UF/IFAS EDIS HS303 — Pitaya Growing in the Florida Home Landscape**
   - https://edis.ifas.ufl.edu/publication/HS303
   - Used for the species-level bloom season baseline in warm climates and the general warning that some cultivars are self-incompatible and benefit from hand/cross-pollination.
3. **Supplemental cultivar spot checks (provisional / nursery-grade, not extension-grade)**
   - Spicy Exotics search results and similar cultivar pages were used only to extend the dragon fruit list beyond the Cal Poly page when there was a reasonably clear pollination claim.
   - These extra cultivars should be treated as easier to revise later than the Cal Poly-backed entries.
   - Used for: Cosmic Charlie, Neon, Voodoo Child, Yellow Thai.

### General fruit pollination defaults
1. **Penn State Extension — Pollination Requirements for Various Fruits and Nuts**
   - https://extension.psu.edu/pollination-requirements-for-various-fruits-and-nuts
   - Key rules used:
     - Apple: cross-pollination needed
     - Apricot: self-fruitful; cross helpful
     - Blueberry: fruit set and crop size improve with cross-pollination
     - Sweet cherry: most self-unfruitful, but newer cultivars such as Lapins, Stella, Sweetheart are self-fruitful
     - Tart cherry: commercial varieties self-fruitful
     - Grape: most self-fruitful
     - Nectarine: most self-fruitful
     - Peach: commercial cultivars self-fruitful except J. H. Hale
     - Pear: a few self-fruitful, but a pollinizer improves the crop
     - Plum: mixed; safest rule is to provide pollinizers
     - Raspberry / strawberry: mostly self-fruitful
     - Citrus apomixis and banana parthenocarpy are called out as exceptions
2. **University of Minnesota Extension — Growing apples in the home garden**
   - https://extension.umn.edu/fruit/growing-apples
   - Used to reinforce the conservative orchard rule that apples generally need pollen from a different variety.
3. **USU Extension — Sweet Cherry Varieties for Box Elder County**
   - https://extension.usu.edu/boxelder/agriculture/fruit-trees/sweet-cherries
   - Used to confirm cultivar-specific sweet cherry exceptions now in the catalog:
     - Stella: self fruitful
     - Lapins: self fertile
     - Sweetheart: self pollinating
     - Black Tartarian primarily used as a pollinator for others
4. **UF/IFAS plum references (search-result snippets + EDIS/IFAS news references)**
   - HS250 / Growing Plums in Florida and related IFAS notes consistently state the Gulf series plums are not self-fruitful and require cross-pollination.
   - Used for Gulfbeauty, Gulfblaze, and Gulfrose.
5. **Penn State blueberry guidance (search-result snippet)**
   - Blueberries are self-fruitful but typically crop better with cross-pollination.
   - Used for a cautious `cross-pollination recommended` default rather than forcing `needs cross-pollination`.

## Modeling choices in app
- `Self-fertile` = commonly sets fruit with its own pollen.
- `Needs cross-pollination` = should not be treated as reliably self-fruitful.
- `Cross-pollination recommended` = can set some fruit alone or has mixed evidence, but usually performs better with a compatible pollinizer.
- `Pollination not required` = used for cultivated bananas because edible bananas are functionally parthenocarpic in home-garden production.

## Important caution
This pass is intentionally conservative.
- Where extension sources were broad but not cultivar-specific, OrchardDex uses species defaults.
- Where cultivar-level claims came only from nursery/community material, those entries should be considered provisional and easy to revise.
- Pollination compatibility can still depend on bloom overlap, ploidy, and local conditions even when two cultivars are both present in the catalog.
