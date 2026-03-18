# Pome and Berry Bloom Research Notes

The added apple and blueberry cultivars use the same forecast model as the rest of the app: a species bloom baseline tied to a USDA reference zone, plus cultivar phase adjustments for early, mid, mid-late, and late bloom timing.

## Sources

- Stark Bro's apple bloom and pollination chart: https://www.starkbros.com/page/apple-tree-bloom-and-pollination-chart
- University of Georgia / Southern Region Small Fruit Consortium, *Blueberry Cultivars for Georgia*: https://smallfruits.org/files/2019/06/06bbcvproc_Nov0206.pdf
- UF/IFAS blueberry cultivar topic pages: https://edis.ifas.ufl.edu/topics/blueberry_varieties

## Implementation note

Blueberry bloom timing in the app is inferred from official chilling and relative flowering guidance rather than an explicit cultivar-by-zone date table. Cultivars documented as earlier or later flowering than Climax or other reference cultivars were placed into earlier or later bloom phases, then shifted by USDA zone in the forecast engine.
