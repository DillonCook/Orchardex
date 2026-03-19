# Papaya OrchardDex notes

This note records the first lean OrchardDex pass for papaya.

## Modeling choice

Papaya is wired onto the existing reusable everbearing / repeat-bearing path.

Current implementation uses:
- species catalog entry + aliases
- cultivar catalog entries + aliases
- graded pollination metadata
- existing `MANUAL_ONLY` / separate dashboard everbearing listing

It does **not** fake a normal monthly bloom forecast.
It does **not** add a papaya-only schema field right now.

## Species boundary used in app

This lane is for **`Carica papaya`**.

Accepted aliases:
- `papaya`
- `Carica papaya`
- `mamão`
- `mamao`
- `lechosa`

Deliberately excluded as species aliases because of collision risk:
- `papaw`
- `pawpaw`

## Species baseline used in app

Current OrchardDex behavior:
- forecast behavior: tracked as `Continuous / repeat-bearing`
- automatic monthly bloom windows: disabled
- pollination default: `Unknown`

Why this is the honest default:
- papaya includes male, female, and hermaphrodite plants
- hermaphrodite lines can fruit on their own
- female plants need pollen
- male plants generally do not set normal fruit
- so a single species-wide yes/no fertility label is misleading

## Cultivar seed set added

Lean first-pass seed set:
- Kapoho Solo
- Sunrise Solo (`Sunrise`)
- Improved Sunrise Solo 72/12 (`Sunrise Solo 72/12`)
- Waimanalo
- Sunset
- Rainbow
- SunUp (`UH SunUp`)
- Tainung No. 1 (`Tainung 1`, `Tainung No.1`)
- Tainung No. 2 (`Tainung 2`, `Tainung No.2`)
- Known You No. 1 (`Known You 1`, `Known-You 1`)
- Known You No. 2 (`Known You 2`, `Known-You 2`)
- Red Lady 786 (`Red Lady`, `786`)
- Maradol (`Maradol Roja`)
- Caribbean Red
- Eksotika
- Pusa Delicious
- Pusa Majesty
- Pusa Nanha
- Pusa Dwarf
- Ranchi Dwarf
- Washington
- Honey Dew (`Honeydew`)
- Coorg Honey Dew
- CO.2 (`CO 2`)
- CO.3 (`CO 3`)
- CO.5 (`CO 5`)
- CO.6 (`CO 6`)
- CO.7 (`CO 7`)
- CO.8 (`CO 8`)
- Arka Surya
- Arka Prabhath (`Arka Prabhat`)
- Golden
- Calimosa
- UENF/Caliman 01 (`Caliman 01`, `UENF-Caliman 01`)

## Pollination mapping

Species default:
- `Unknown`

Strong cultivar overrides used in this pass:
- clear hermaphrodite / gynodioecious lines like `Kapoho Solo`, `Sunrise Solo`, `Improved Sunrise Solo 72/12`, `Waimanalo`, `Sunset`, `Rainbow`, `SunUp`, `Tainung No. 1`, `Tainung No. 2`, `Red Lady 786`, `Eksotika`, `Pusa Delicious`, `Pusa Majesty`, `Honey Dew`, `Coorg Honey Dew`, `CO.3`, `CO.7`, `CO.8`, `Arka Surya`, `Arka Prabhath` → `Self-fertile`
- clear dioecious lines like `Pusa Nanha`, `Pusa Dwarf`, `Ranchi Dwarf`, `Washington`, `CO.2`, `CO.5`, `CO.6` → `Needs cross-pollination`
- cultivar families with seed-line / sex-expression ambiguity like `Maradol`, `Caribbean Red`, `Known You No. 1`, `Known You No. 2`, `Golden`, `Calimosa`, `UENF/Caliman 01` remain `Unknown`

## Known simplifications

- OrchardDex is not yet storing a dedicated `sex_expression` field.
- Several commercial papaya names are seed-line families, not clone-clean selections, so cultivar name alone does not fully determine sex behavior.
- If OrchardDex adds a generalizable `sex_expression` field later, papaya is the best first species to backfill into it.
