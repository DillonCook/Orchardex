# Passion fruit OrchardDex notes

This note records the first lean OrchardDex pass for passion fruit.

## Modeling choice

Passion fruit stays inside the current OrchardDex catalog model.

Current implementation uses:
- species baseline bloom window
- cultivar catalog entries + aliases
- graded pollination metadata

It does **not** add a new engine feature.
It does **not** move passion fruit onto the repeat-bearing / `MANUAL_ONLY` path.
It does **not** silently mix other Passiflora species into this row.

## Species boundary used in app

This lane is for **`Passiflora edulis`**.

Accepted species/common-name aliases include:
- `passion fruit`
- `passionfruit`
- `lilikoi`
- `lilikoʻi`
- `maracujá azedo`
- `maracuja azedo`
- `sour passion fruit`
- `Passiflora edulis`

Out of scope for this row:
- sweet granadilla
- giant granadilla
- maypop
- blue passionflower
- other Passiflora species sold casually as passion fruit

## Species baseline used in app

Current OrchardDex baseline:
- reference zone: `10b`
- primary bloom window: mid-March through late November
- pollination default: `Cross-pollination recommended`

Why this baseline is a compromise:
- purple, yellow, and hybrid/passionfruit families do not share the same fertility behavior
- UF timing support is materially different between purple-type and yellow-type material
- the broad baseline is only a fallback; the cultivar-level fertility overrides carry much of the real meaning here

## Cultivar seed set added

Lean first-pass seed set:
- Possum Purple (`Purple Possum`)
- Panama Red
- Sweet Sunrise
- Whitman Yellow (`Whitman`)
- Australian Purple
- Nellie Kelly
- Don's Choice
- Frederick
- Misty Gem
- Sweetheart
- Tango
- Flamenco (`Red Flamenco`, `Red Flemenco`)
- Red Rover
- Tas Black
- Toms Special (`Tom's Special`)
- Waimanalo Selection
- Kapoho Selection
- Mike's Choice
- Panama Gold
- Sevcik Selection
- University Round Selection
- University Selection No. B-74 (`B-74`)
- Yee Selection
- Pandora
- McGuffies Red (`McGuffy`)
- Noel's Special (`Noels Special`)
- IAC-Paulista
- BRS Gigante Amarelo (`BRS GA1`)
- BRS Sol do Cerrado (`BRS SC1`)
- BRS Rubi do Cerrado (`BRS RC`)
- BRS Ouro Vermelho (`BRS OV1`)
- IAC 275 - Wonder (`IAC 275 Wonder`)
- IAC 273 - Monte Alegre (`IAC 273 Monte Alegre`)
- IAC 277 - Jewelry (`IAC 277 Jewelry`)
- FB 200 Yellow Master
- FB 300 Araguari
- SCS437 Catarina (`SCS437 Catherine`)
- UENF Rio Dourado (`UENF Golden River`)
- Solar
- Round Yellow (`Redondo Amarelo`)

## Pollination mapping

Species default:
- `Cross-pollination recommended`

Cultivar overrides used in this pass:
- purple/self-compatible family like `Possum Purple`, `Frederick`, `Misty Gem`, `Sweetheart`, `Tango`, `Flamenco`, `Red Rover`, `Tas Black`, `Toms Special`, `Waimanalo Selection` → `Self-fertile, cross-pollination helps`
- yellow/self-incompatible family like `Sweet Sunrise`, `Whitman Yellow`, `Kapoho Selection`, `Panama Gold`, `Pandora`, `McGuffies Red`, `Noel's Special`, `BRS Gigante Amarelo`, `BRS Sol do Cerrado`, `IAC 273 - Monte Alegre`, `IAC 275 - Wonder`, `IAC 277 - Jewelry`, `FB 200 Yellow Master`, `FB 300 Araguari`, `SCS437 Catarina`, `UENF Rio Dourado`, `Solar`, `Round Yellow` → `Needs cross-pollination`
- hybrid / ambiguous families like `Panama Red`, `IAC-Paulista`, `BRS Rubi do Cerrado`, `BRS Ouro Vermelho` stay in the middle bucket: `Cross-pollination recommended`

## Known simplifications

- OrchardDex is not yet storing a separate purple / yellow / Panama type flag.
- `Panama` as a generic type-family label is intentionally not seeded in this first pass because it is too easy to over-normalize.
- The broad species bloom window is intentionally less precise than the packet’s type-specific interpretation.
