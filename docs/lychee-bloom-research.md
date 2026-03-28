# Lychee bloom + fertility notes

This note records the first OrchardDex lychee packet shape.

## Bloom modeling

Lychee should not use one national bloom window.
The app now treats lychee bloom as **orchard region + USDA zone**:

- **South Florida (10b–11a):** late Dec–Apr overall, usually strongest around Feb–Mar.
- **Hawaii (10b–12a):** flower induction often starts Oct–Feb, with visible bloom roughly Jan–Apr.
- **California (10a–10b):** spring bloom, but this profile is still lower-confidence and should stay easy to revise.

Current app implementation keeps a general fallback if orchard region is unset, then applies the regional override when the user sets orchard region in onboarding or Settings.

## Fertility modeling

Lychee fertility is graded, not binary.

- **Species default:** treat lychee as able to fruit alone, but often improved by cross-pollination.
  - OrchardDex label: `Self-fertile, cross-pollination helps`
- **Mauritius / Tai So:** can self-fruit, but cross-pollination often improves set and retention.
  - OrchardDex label: `Self-fertile, cross-pollination helps`
- **Fei Zi Xiao / Fay Zee Siu:** strongest current signal for partial self-incompatibility.
  - OrchardDex label: `Partial self-incompatibility`

## Follow-up

- Tighten California source coverage before treating that window as high-confidence.
- Add more cultivar-level lychee entries as packets land.
