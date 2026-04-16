param(
    [string]$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$phenologyPath = Join-Path $RepoRoot "app/src/main/assets/phenology_profiles.json"
$speciesOutPath = Join-Path $RepoRoot "docs/catalog-species-ledger.csv"
$cultivarOutPath = Join-Path $RepoRoot "docs/catalog-cultivar-ledger.csv"
$testPath = Join-Path $RepoRoot "app/src/test/java/com/dillon/orcharddex/BloomForecastEngineTest.kt"

$phenology = Get-Content $phenologyPath -Raw | ConvertFrom-Json
$speciesProfiles = @($phenology.speciesProfiles)
$cultivarProfiles = @($phenology.cultivarProfiles)
$catalogOnlyCultivars = @($phenology.catalogOnlyCultivars)

$today = Get-Date -Format "yyyy-MM-dd"
$testContent = if (Test-Path $testPath) { Get-Content $testPath -Raw } else { "" }

$researchMap = @{
    "abiu" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "ambarella" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "apple" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "apricot" = @{ note = "stonefruit-bloom-research.md"; status = "reviewed" }
    "asian pear" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "atemoya" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "avocado" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "banana" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "barbados cherry" = @{ note = "barbados-cherry-research.md"; status = "reviewed" }
    "black sapote" = @{ note = "black-sapote-research.md"; status = "reviewed" }
    "blackberry" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "blueberry" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "caimito" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "canistel" = @{ note = "canistel-research.md"; status = "reviewed" }
    "cashew" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "citrus" = @{ note = "citrus-bloom-research.md"; status = "reviewed" }
    "coconut" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "dragon fruit" = @{ note = "pollination-research.md"; status = "strong" }
    "fig" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "grape" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "grapefruit" = @{ note = "citrus-bloom-research.md"; status = "reviewed" }
    "green sapote" = @{ note = "green-sapote-research.md"; status = "reviewed" }
    "guava" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "jaboticaba" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "jackfruit" = @{ note = "jackfruit-research.md"; status = "reviewed" }
    "jamaican cherry" = @{ note = "jamaican-cherry-research.md"; status = "reviewed" }
    "lemon" = @{ note = "citrus-bloom-research.md"; status = "reviewed" }
    "lime" = @{ note = "citrus-bloom-research.md"; status = "reviewed" }
    "longan" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "loquat" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "lychee" = @{ note = "lychee-bloom-research.md"; status = "reviewed" }
    "mamey sapote" = @{ note = "mamey-sapote-research.md"; status = "reviewed" }
    "mamoncillo" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "mandarin" = @{ note = "citrus-bloom-research.md"; status = "reviewed" }
    "mango" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "mulberry" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "nectarine" = @{ note = "stonefruit-bloom-research.md"; status = "reviewed" }
    "orange" = @{ note = "citrus-bloom-research.md"; status = "reviewed" }
    "papaya" = @{ note = "papaya-research.md"; status = "reviewed" }
    "passionfruit" = @{ note = "passion-fruit-research.md"; status = "reviewed" }
    "peach" = @{ note = "stonefruit-bloom-research.md"; status = "reviewed" }
    "pear" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "persimmon" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "pineapple" = @{ note = "pineapple-research.md"; status = "reviewed" }
    "plum" = @{ note = "stonefruit-bloom-research.md"; status = "reviewed" }
    "pomegranate" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "raspberry" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "saccharum spp." = @{ note = "sugar-cane-research.md"; status = "reviewed" }
    "sapodilla" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "sour cherry" = @{ note = "stonefruit-bloom-research.md"; status = "reviewed" }
    "soursop" = @{ note = "tropical-catalog-backfill-research.md"; status = "reviewed" }
    "star fruit" = @{ note = "star-fruit-research.md"; status = "reviewed" }
    "strawberry" = @{ note = "pome-berry-bloom-research.md"; status = "reviewed" }
    "sugar apple" = @{ note = "sugar-apple-research.md"; status = "reviewed" }
    "sweet cherry" = @{ note = "stonefruit-bloom-research.md"; status = "reviewed" }
    "tamarind" = @{ note = "tamarind-research.md"; status = "reviewed" }
    "white sapote" = @{ note = "white-sapote-research.md"; status = "reviewed" }
}

function Get-PollinationProfile {
    param([string]$Requirement)
    if ([string]::IsNullOrWhiteSpace($Requirement)) {
        $Requirement = "UNKNOWN"
    }
    switch ($Requirement) {
        "SELF_FERTILE" { return @{ selfCompatibility = "SELF_FERTILE"; pollinationMode = "SELF_POLLINATING" } }
        "SELF_FERTILE_CROSS_BENEFITS" { return @{ selfCompatibility = "SELF_FERTILE"; pollinationMode = "INSECT" } }
        "NEEDS_CROSS_POLLINATION" { return @{ selfCompatibility = "SELF_STERILE"; pollinationMode = "HAND_HELPFUL" } }
        "CROSS_POLLINATION_RECOMMENDED" { return @{ selfCompatibility = "PARTIAL"; pollinationMode = "INSECT" } }
        "PARTIAL_SELF_INCOMPATIBILITY" { return @{ selfCompatibility = "PARTIAL"; pollinationMode = "HAND_HELPFUL" } }
        "POLLINATION_NOT_REQUIRED" { return @{ selfCompatibility = "UNKNOWN"; pollinationMode = "UNKNOWN" } }
        default { return @{ selfCompatibility = "UNKNOWN"; pollinationMode = "UNKNOWN" } }
    }
}

function Get-LearningSignals {
    param([string]$PatternType)
    switch ($PatternType) {
        "SUPPRESSED" { return "" }
        default { return "BUD|BLOOM|FRUIT_SET|HARVEST" }
    }
}

function Get-DisplayName {
    param([object]$Species)
    if ($Species.catalogSpeciesLabel) {
        return $Species.catalogSpeciesLabel
    }
    $parts = ($Species.key -split " ")
    return ($parts | ForEach-Object {
        if ($_.Length -eq 0) { return $_ }
        $_.Substring(0,1).ToUpperInvariant() + $_.Substring(1)
    }) -join " "
}

function Get-SpeciesMetadata {
    param([string]$Key)
    switch ($Key) {
        "abiu" { return @{ scientificName = "Pouteria caimito"; familyOrCropGroup = "Sapotaceae"; growthForm = "tree" } }
        "ambarella" { return @{ scientificName = "Spondias dulcis"; familyOrCropGroup = "Anacardiaceae"; growthForm = "tree" } }
        "apple" { return @{ scientificName = "Malus domestica"; familyOrCropGroup = "Rosaceae / pome fruit"; growthForm = "tree" } }
        "apricot" { return @{ scientificName = "Prunus armeniaca"; familyOrCropGroup = "Rosaceae / stone fruit"; growthForm = "tree" } }
        "asian pear" { return @{ scientificName = "Pyrus pyrifolia"; familyOrCropGroup = "Rosaceae / pome fruit"; growthForm = "tree" } }
        "atemoya" { return @{ scientificName = "Annona x atemoya"; familyOrCropGroup = "Annonaceae"; growthForm = "tree" } }
        "avocado" { return @{ scientificName = "Persea americana"; familyOrCropGroup = "Lauraceae"; growthForm = "tree" } }
        "banana" { return @{ scientificName = "Musa spp."; familyOrCropGroup = "Musaceae"; growthForm = "herbaceous" } }
        "barbados cherry" { return @{ scientificName = "Malpighia emarginata"; familyOrCropGroup = "Malpighiaceae"; growthForm = "shrub" } }
        "black sapote" { return @{ scientificName = "Diospyros digyna"; familyOrCropGroup = "Ebenaceae"; growthForm = "tree" } }
        "blackberry" { return @{ scientificName = "Rubus spp."; familyOrCropGroup = "Rosaceae / cane berry"; growthForm = "cane" } }
        "blueberry" { return @{ scientificName = "Vaccinium spp."; familyOrCropGroup = "Ericaceae / berry"; growthForm = "shrub" } }
        "caimito" { return @{ scientificName = "Chrysophyllum cainito"; familyOrCropGroup = "Sapotaceae"; growthForm = "tree" } }
        "canistel" { return @{ scientificName = "Pouteria campechiana"; familyOrCropGroup = "Sapotaceae"; growthForm = "tree" } }
        "cashew" { return @{ scientificName = "Anacardium occidentale"; familyOrCropGroup = "Anacardiaceae"; growthForm = "tree" } }
        "citrus" { return @{ scientificName = "Citrus spp."; familyOrCropGroup = "Citrus"; growthForm = "tree" } }
        "coconut" { return @{ scientificName = "Cocos nucifera"; familyOrCropGroup = "Arecaceae"; growthForm = "palm" } }
        "dragon fruit" { return @{ scientificName = "Selenicereus spp."; familyOrCropGroup = "Cactaceae"; growthForm = "vine" } }
        "fig" { return @{ scientificName = "Ficus carica"; familyOrCropGroup = "Moraceae"; growthForm = "tree" } }
        "grape" { return @{ scientificName = "Vitis spp."; familyOrCropGroup = "Vitaceae"; growthForm = "vine" } }
        "grapefruit" { return @{ scientificName = "Citrus x paradisi"; familyOrCropGroup = "Citrus"; growthForm = "tree" } }
        "green sapote" { return @{ scientificName = "Pouteria viridis"; familyOrCropGroup = "Sapotaceae"; growthForm = "tree" } }
        "guava" { return @{ scientificName = "Psidium guajava"; familyOrCropGroup = "Myrtaceae"; growthForm = "tree" } }
        "jaboticaba" { return @{ scientificName = "Plinia spp."; familyOrCropGroup = "Myrtaceae"; growthForm = "tree" } }
        "jackfruit" { return @{ scientificName = "Artocarpus heterophyllus"; familyOrCropGroup = "Moraceae"; growthForm = "tree" } }
        "jamaican cherry" { return @{ scientificName = "Muntingia calabura"; familyOrCropGroup = "Muntingiaceae"; growthForm = "tree" } }
        "lemon" { return @{ scientificName = "Citrus limon"; familyOrCropGroup = "Citrus"; growthForm = "tree" } }
        "lime" { return @{ scientificName = "Citrus spp."; familyOrCropGroup = "Citrus"; growthForm = "tree" } }
        "longan" { return @{ scientificName = "Dimocarpus longan"; familyOrCropGroup = "Sapindaceae"; growthForm = "tree" } }
        "loquat" { return @{ scientificName = "Eriobotrya japonica"; familyOrCropGroup = "Rosaceae / pome fruit"; growthForm = "tree" } }
        "lychee" { return @{ scientificName = "Litchi chinensis"; familyOrCropGroup = "Sapindaceae"; growthForm = "tree" } }
        "mamey sapote" { return @{ scientificName = "Pouteria sapota"; familyOrCropGroup = "Sapotaceae"; growthForm = "tree" } }
        "mamoncillo" { return @{ scientificName = "Melicoccus bijugatus"; familyOrCropGroup = "Sapindaceae"; growthForm = "tree" } }
        "mandarin" { return @{ scientificName = "Citrus spp."; familyOrCropGroup = "Citrus"; growthForm = "tree" } }
        "mango" { return @{ scientificName = "Mangifera indica"; familyOrCropGroup = "Anacardiaceae"; growthForm = "tree" } }
        "mulberry" { return @{ scientificName = "Morus spp."; familyOrCropGroup = "Moraceae"; growthForm = "tree" } }
        "nectarine" { return @{ scientificName = "Prunus persica"; familyOrCropGroup = "Rosaceae / stone fruit"; growthForm = "tree" } }
        "orange" { return @{ scientificName = "Citrus spp."; familyOrCropGroup = "Citrus"; growthForm = "tree" } }
        "papaya" { return @{ scientificName = "Carica papaya"; familyOrCropGroup = "Caricaceae"; growthForm = "herbaceous" } }
        "passionfruit" { return @{ scientificName = "Passiflora edulis"; familyOrCropGroup = "Passifloraceae"; growthForm = "vine" } }
        "peach" { return @{ scientificName = "Prunus persica"; familyOrCropGroup = "Rosaceae / stone fruit"; growthForm = "tree" } }
        "pear" { return @{ scientificName = "Pyrus communis"; familyOrCropGroup = "Rosaceae / pome fruit"; growthForm = "tree" } }
        "persimmon" { return @{ scientificName = "Diospyros spp."; familyOrCropGroup = "Ebenaceae"; growthForm = "tree" } }
        "pineapple" { return @{ scientificName = "Ananas comosus"; familyOrCropGroup = "Bromeliaceae"; growthForm = "herbaceous" } }
        "plum" { return @{ scientificName = "Prunus spp."; familyOrCropGroup = "Rosaceae / stone fruit"; growthForm = "tree" } }
        "pomegranate" { return @{ scientificName = "Punica granatum"; familyOrCropGroup = "Lythraceae"; growthForm = "shrub" } }
        "raspberry" { return @{ scientificName = "Rubus spp."; familyOrCropGroup = "Rosaceae / cane berry"; growthForm = "cane" } }
        "saccharum spp." { return @{ scientificName = "Saccharum spp."; familyOrCropGroup = "Poaceae"; growthForm = "cane" } }
        "sapodilla" { return @{ scientificName = "Manilkara zapota"; familyOrCropGroup = "Sapotaceae"; growthForm = "tree" } }
        "sour cherry" { return @{ scientificName = "Prunus cerasus"; familyOrCropGroup = "Rosaceae / stone fruit"; growthForm = "tree" } }
        "soursop" { return @{ scientificName = "Annona muricata"; familyOrCropGroup = "Annonaceae"; growthForm = "tree" } }
        "star fruit" { return @{ scientificName = "Averrhoa carambola"; familyOrCropGroup = "Oxalidaceae"; growthForm = "tree" } }
        "strawberry" { return @{ scientificName = "Fragaria x ananassa"; familyOrCropGroup = "Rosaceae / berry"; growthForm = "herbaceous" } }
        "sugar apple" { return @{ scientificName = "Annona squamosa"; familyOrCropGroup = "Annonaceae"; growthForm = "tree" } }
        "sweet cherry" { return @{ scientificName = "Prunus avium"; familyOrCropGroup = "Rosaceae / stone fruit"; growthForm = "tree" } }
        "tamarind" { return @{ scientificName = "Tamarindus indica"; familyOrCropGroup = "Fabaceae"; growthForm = "tree" } }
        "white sapote" { return @{ scientificName = "Casimiroa edulis"; familyOrCropGroup = "Rutaceae"; growthForm = "tree" } }
        default { return @{ scientificName = ""; familyOrCropGroup = ""; growthForm = "" } }
    }
}

function Get-TriggerSignals {
    param([object]$Species)
    switch ($Species.modelType) {
        "CHILL_HEAT" { return "chill|spring_heat" }
        "WARM_SEASON_PHOTOPERIOD" { return "warm_season|photoperiod" }
        "TROPICAL_REPEAT" { return "warm_season|rainfall" }
        "MANUAL_ONLY" { return "warm_season" }
        default {
            $zoneMatch = [regex]::Match("$($Species.referenceZoneCode)", '^\d+')
            if ($zoneMatch.Success -and [int]$zoneMatch.Value -le 8) {
                return "chill|spring_heat"
            }
            return "spring_heat|warm_season"
        }
    }
}

function Get-TestCoverage {
    param([string]$SpeciesKey)
    if ([string]::IsNullOrWhiteSpace($testContent)) { return "false" }
    $escaped = [regex]::Escape($SpeciesKey)
    if ($testContent -match $escaped) {
        return "true"
    }
    return "false"
}

$catalogOnlyLookup = @{}
foreach ($row in $catalogOnlyCultivars) {
    $catalogOnlyLookup["$($row.species)|$($row.cultivar)".ToLowerInvariant()] = $true
}

$speciesLookup = @{}
foreach ($species in $speciesProfiles) {
    $speciesLookup[$species.key] = $species
}

$speciesRows = foreach ($species in $speciesProfiles | Sort-Object key) {
    $research = if ($researchMap.ContainsKey($species.key)) { $researchMap[$species.key] } else { @{ note = ""; status = "baseline_only" } }
    $pollinationRequirement = if ($species.pollinationRequirement) { $species.pollinationRequirement } else { "UNKNOWN" }
    $forecastBehavior = if ($species.forecastBehavior) { $species.forecastBehavior } else { "WINDOW" }
    $shiftDaysPerHalfZone = if ($null -ne $species.shiftDaysPerHalfZone -and "$($species.shiftDaysPerHalfZone)" -ne "") { [int64]$species.shiftDaysPerHalfZone } else { 4 }
    $displayName = Get-DisplayName $species
    $metadata = Get-SpeciesMetadata $species.key
    $pollination = Get-PollinationProfile $pollinationRequirement
    $timedCultivars = @($cultivarProfiles | Where-Object { $_.speciesKey -eq $species.key })
    $catalogOnlyForSpecies = @($catalogOnlyCultivars | Where-Object { $_.species -eq $displayName })
    $pollinationOverrideCultivars = @(
        $timedCultivars | Where-Object {
            $_.pollinationRequirement -and $_.pollinationRequirement -ne $pollinationRequirement
        }
    )
    [pscustomobject]@{
        key = $species.key
        displayName = $displayName
        scientificName = $metadata.scientificName
        familyOrCropGroup = $metadata.familyOrCropGroup
        growthForm = $metadata.growthForm
        patternType = $species.patternType
        modelType = $species.modelType
        referenceZoneCode = $species.referenceZoneCode
        startMonth = $species.startMonth
        startDay = $species.startDay
        durationDays = $species.durationDays
        triggerSignals = Get-TriggerSignals $species
        supportsHemisphereShift = if ($forecastBehavior -eq "WINDOW" -and $species.key -ne "dragon fruit") { "true" } else { "false" }
        supportsUsdaShift = if ($forecastBehavior -eq "WINDOW" -and $shiftDaysPerHalfZone -ne 0) { "true" } else { "false" }
        pollinationRequirement = $pollinationRequirement
        selfCompatibility = $pollination.selfCompatibility
        pollinationMode = $pollination.pollinationMode
        preferredLearningSignals = Get-LearningSignals $species.patternType
        researchStatus = $research.status
        researchNoteId = $research.note
        lastReviewedDate = $today
        uncertaintyNote = $species.uncertaintyNote
        checklist_identityCaptured = if ($metadata.scientificName -and $metadata.familyOrCropGroup -and $metadata.growthForm) { "true" } else { "false" }
        checklist_aliasesReviewed = if (@($species.aliases).Count -gt 0) { "true" } else { "false" }
        checklist_patternChosen = if ($species.patternType) { "true" } else { "false" }
        checklist_modelChosen = if ($species.modelType) { "true" } else { "false" }
        checklist_baselineTimingRecorded = if ($species.referenceZoneCode -and $species.startMonth -and $species.startDay -and $species.durationDays) { "true" } else { "false" }
        checklist_pollinationDecisionRecorded = "true"
        checklist_uncertaintyLogged = if ($species.uncertaintyNote) { "true" } else { "false" }
        checklist_researchNoteLinked = if ($research.note) { "true" } else { "false" }
        checklist_cultivarAuditStarted = if (($timedCultivars.Count + $catalogOnlyForSpecies.Count) -gt 0 -or $research.note) { "true" } else { "false" }
        checklist_testsPresent = $(Get-TestCoverage $species.key)
        checklist_status = $research.status
        timedCultivarCount = $timedCultivars.Count
        catalogOnlyCultivarCount = $catalogOnlyForSpecies.Count
        pollinationOverrideCultivarCount = $pollinationOverrideCultivars.Count
    }
}

$cultivarRows = @()
foreach ($cultivar in $cultivarProfiles | Sort-Object speciesKey, cultivar) {
    $species = $speciesLookup[$cultivar.speciesKey]
    $research = if ($researchMap.ContainsKey($cultivar.speciesKey)) { $researchMap[$cultivar.speciesKey] } else { @{ note = ""; status = "baseline_only" } }
    $hasPollinationOverride = $false
    if ($cultivar.pollinationRequirement -and $species) {
        $hasPollinationOverride = $cultivar.pollinationRequirement -ne $species.pollinationRequirement
    }
    $cultivarRows += [pscustomobject]@{
        speciesKey = $cultivar.speciesKey
        speciesDisplayName = if ($cultivar.catalogSpeciesLabel) { $cultivar.catalogSpeciesLabel } elseif ($species) { Get-DisplayName $species } else { "" }
        cultivar = $cultivar.cultivar
        aliases = (@($cultivar.aliases) -join "|")
        catalogOnly = "false"
        hasPhaseAdjustment = if ($cultivar.phase) { "true" } else { "false" }
        phaseBucket = $cultivar.phase
        hasPollinationOverride = if ($hasPollinationOverride) { "true" } else { "false" }
        pollinationRequirement = $cultivar.pollinationRequirement
        researchStatus = $research.status
        researchNoteId = $research.note
        lastReviewedDate = $today
    }
}

foreach ($cultivar in $catalogOnlyCultivars | Sort-Object species, cultivar) {
    $speciesKey = ($speciesProfiles | Where-Object { $_.catalogSpeciesLabel -eq $cultivar.species } | Select-Object -First 1).key
    if (-not $speciesKey) {
        $speciesKey = ($speciesProfiles | Where-Object { (Get-DisplayName $_) -eq $cultivar.species } | Select-Object -First 1).key
    }
    $research = if ($speciesKey -and $researchMap.ContainsKey($speciesKey)) { $researchMap[$speciesKey] } else { @{ note = ""; status = "baseline_only" } }
    $cultivarRows += [pscustomobject]@{
        speciesKey = $speciesKey
        speciesDisplayName = $cultivar.species
        cultivar = $cultivar.cultivar
        aliases = (@($cultivar.aliases) -join "|")
        catalogOnly = "true"
        hasPhaseAdjustment = "false"
        phaseBucket = ""
        hasPollinationOverride = if ($cultivar.pollinationRequirement) { "true" } else { "false" }
        pollinationRequirement = $cultivar.pollinationRequirement
        researchStatus = $research.status
        researchNoteId = $research.note
        lastReviewedDate = $today
    }
}

$speciesRows | Export-Csv -NoTypeInformation -Encoding UTF8 $speciesOutPath
$cultivarRows | Sort-Object speciesDisplayName, cultivar, catalogOnly | Export-Csv -NoTypeInformation -Encoding UTF8 $cultivarOutPath

Write-Host "Exported $($speciesRows.Count) species rows to $speciesOutPath"
Write-Host "Exported $($cultivarRows.Count) cultivar rows to $cultivarOutPath"
