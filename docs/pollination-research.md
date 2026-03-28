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
   - Spicy Exotics, Tasty Dragons, Dragons Alley Nursery, and similar specialist grower pages were used only to extend the dragon fruit list beyond the Cal Poly page when there was a reasonably clear pollination claim.
   - These extra cultivars should be treated as easier to revise later than the Cal Poly-backed entries.
   - Used for: Asunta 6, Cosmic Charlie, Edgar's Baby, Fruit Punch, Georges White, Maria Rosa, Medusa, Moroccan Red, Neon, Thai Dragon, Townsend Pink, Tricia, Voodoo Child, White Sapphire, Yellow Thai.
   - Two user-requested entries were added conservatively with unresolved pollination status still marked unknown: Dennis Pale Pink and NOID Lowe's variety.
4. **Focused Asunta-series sources**
   - Dragons Alley Nursery pages were used for explicit self-sterile / cross-pollination-required status on Asunta 1, Asunta 2, Asunta 3, and Asunta 4.
   - Tasty Dragons product/detail pages were used for the numbered/named follow-on hybrids and classifications shown as `CROSS`: Asunta 5 Paco, Asunta 5 Patricia, Asunta 5 Starburst, Asunta 5 Ventura, and Asunta 6 / Wild Berry Skittles.
   - Tasty Dragons category pages plus the Exotic Fruits and Vegetables naming note were used for Asunta 5 Sunset Sherbet, which is also circulated as `Asunta 5 Edgar`.
   - The Exotic Fruits and Vegetables note also claims the official Asunta 5 Paco may circulate under `La Palma` or `Kevin`; OrchardDex accepts those as low-confidence compatibility aliases, not as stronger botanical proof.
5. **Cultivar timing notes for dragon fruit**
   - UF/IFAS HS303 remains the main source for the species-level warm-climate bloom season used by the app.
   - Cultivar pages often mention flowering windows like "during summertime" or "2 to 3 flower cycles starting in June," but they do not provide consistent USDA-zone-normalized bloom tables.
   - OrchardDex therefore keeps dragon fruit on the shared species-level, zone-adjusted bloom window for now, while cultivar entries mainly add pollination metadata and aliases.

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
- User-entered compatibility aliases were added sparingly. A previous temporary alias that conflated `Asunta 6` with `Paco` was removed after correction. OrchardDex now keeps `Asunta 5 Paco` and `Asunta 6` distinct.
- Pollination compatibility can still depend on bloom overlap, ploidy, and local conditions even when two cultivars are both present in the catalog.
