# OrchardDex Catalog Research Pass Template

Use this template for any new species pass or grouped crop-family backfill.

If the pass covers several related species, repeat the `Species record` section once per species and keep shared sources in one note.

## Pass summary

- scope:
- reason for this pass:
- author/date:
- target status after this pass:
  - `baseline_only`
  - `reviewed`
  - `strong`

## Sources used

List the main sources first. Use this order when possible:
- extension publications
- research papers
- breeding program or germplasm program material
- government or institute references
- nursery/grower pages only for cautious alias fill-in

For each major source:
- citation:
- url:
- source type:
- confidence:
- what claim(s) it supports:

## Species record

### Identity

- key:
- display name:
- scientific name:
- family/crop group:
- aliases:

### Forecast modeling

- growth form:
- pattern type:
- model type:
- baseline bloom:
  - reference zone:
  - start month/day:
  - duration:
- trigger signals:
- location shift policy:
  - supports hemisphere shift:
  - supports USDA shift:

### Pollination

- species default pollination requirement:
- self-compatibility:
- pollination mode:
- sex expression, if materially relevant:
- hand pollination need/helpfulness, if materially relevant:
- pollination notes:

### Learning support

- preferred learning signals:
- does local history need to outrank quickly:

### Uncertainty

- uncertainty note:
- what was intentionally not modeled:

### Implementation decisions

- asset entry added or updated:
- tests added or updated:
- research status:
- last reviewed date:

### Checklist

- identity captured:
- aliases reviewed:
- pattern chosen:
- model chosen:
- baseline timing recorded:
- pollination decision recorded:
- uncertainty logged:
- research note linked:
- cultivar audit started:
  - if no named cultivars are modeled yet, record that explicit decision here
- tests present:
- status:

### Baseline packet gate

Before marking a species `baseline_only` or higher, confirm:
- forecast core exists:
  - pattern, model, timing/coarse rationale, trigger signals, shift policy
- fertility core exists:
  - pollination requirement, self-compatibility, pollination mode
- fertility nuance is captured when operationally important:
  - sex expression
  - hand pollination helpful/required
  - short compatibility note
- uncertainty is explicit rather than implied
- at least one test or matching assertion exists

## Cultivar records

Repeat only for cultivars that need more than alias support.

### Cultivar

- species key:
- cultivar:
- aliases:
- status:
  - `catalog-only`
  - `phase-adjusted`
  - `pollination override`
  - `pattern override`
- evidence summary:
- phase offset or phase bucket:
- pollination override:
- uncertainty note:

## Evidence log

Add one bullet per meaningful claim:
- subject:
- claim type:
- source:
- evidence strength:
- note:

## Follow-up

- future species-model upgrades:
- cultivar cleanup still needed:
- climate-model limitations:
- UI or wording follow-up:
