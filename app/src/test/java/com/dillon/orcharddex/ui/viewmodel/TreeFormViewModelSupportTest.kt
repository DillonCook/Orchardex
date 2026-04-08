package com.dillon.orcharddex.ui.viewmodel

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.NurseryStage
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.PollinationMode
import com.dillon.orcharddex.data.model.PropagationMethod
import com.dillon.orcharddex.data.model.SelfCompatibility
import com.dillon.orcharddex.data.model.TreeOriginType
import com.dillon.orcharddex.data.model.TreeStatus
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class TreeFormViewModelSupportTest {
    @Test
    fun applyPropagationParentState_copiesCultivarAndAdvancedProfileToChildDraft() {
        val today = LocalDate.of(2026, 4, 3)

        val propagatedState = applyPropagationParentState(
            currentState = TreeFormState(),
            parentTree = testTree(
                id = "parent-1",
                species = "Mango",
                cultivar = "Pickering",
                orchardName = "North Grove",
                sectionName = "Block A",
                rootstock = "Turpentine",
                source = "Local nursery",
                plantType = PlantType.CONTAINER,
                containerSize = "7 gal",
                sunExposure = "Full sun",
                frostSensitivity = FrostSensitivityLevel.CUSTOM,
                frostSensitivityNote = "Protect below 35F",
                irrigationNote = "Deep soak weekly",
                bloomTimingMode = BloomTimingMode.CUSTOM,
                bloomPatternOverride = BloomPatternType.MULTI_WAVE,
                manualBloomProfile = listOf(0, 0, 1, 2, 3, 2, 0, 0, 0, 0, 0, 0),
                customBloomStartMonth = 3,
                customBloomStartDay = 15,
                customBloomDurationDays = 75,
                selfCompatibilityOverride = SelfCompatibility.SELF_FERTILE,
                pollinationModeOverride = PollinationMode.HAND_HELPFUL,
                pollinationOverrideNote = "Better yield with a nearby partner"
            ),
            parentOrchardName = "North Grove",
            defaultLocationId = "fallback-loc",
            fallbackOrchardName = "Home Orchard",
            propagationMethod = PropagationMethod.CUTTING,
            today = today
        )

        assertThat(propagatedState.species).isEqualTo("Mango")
        assertThat(propagatedState.cultivar).isEqualTo("Pickering")
        assertThat(propagatedState.sectionName).isEqualTo("Block A")
        assertThat(propagatedState.rootstock).isEqualTo("Turpentine")
        assertThat(propagatedState.source).isEqualTo("Local nursery")
        assertThat(propagatedState.plantType).isEqualTo(PlantType.CONTAINER)
        assertThat(propagatedState.containerSize).isEqualTo("7 gal")
        assertThat(propagatedState.sunExposure).isEqualTo("Full sun")
        assertThat(propagatedState.frostSensitivity).isEqualTo(FrostSensitivityLevel.CUSTOM)
        assertThat(propagatedState.frostSensitivityNote).isEqualTo("Protect below 35F")
        assertThat(propagatedState.irrigationNote).isEqualTo("Deep soak weekly")
        assertThat(propagatedState.bloomTimingMode).isEqualTo(BloomTimingMode.CUSTOM)
        assertThat(propagatedState.bloomPatternOverride).isEqualTo(BloomPatternType.MULTI_WAVE)
        assertThat(propagatedState.manualBloomProfile).containsExactly(0, 0, 1, 2, 3, 2, 0, 0, 0, 0, 0, 0)
        assertThat(propagatedState.customBloomStartMonth).isEqualTo("3")
        assertThat(propagatedState.customBloomStartDay).isEqualTo("15")
        assertThat(propagatedState.customBloomDurationDays).isEqualTo("75")
        assertThat(propagatedState.selfCompatibilityOverride).isEqualTo(SelfCompatibility.SELF_FERTILE)
        assertThat(propagatedState.pollinationModeOverride).isEqualTo(PollinationMode.HAND_HELPFUL)
        assertThat(propagatedState.pollinationOverrideNote).isEqualTo("Better yield with a nearby partner")
        assertThat(propagatedState.nurseryStage).isEqualTo(NurseryStage.PROPAGATING)
        assertThat(propagatedState.parentTreeId).isEqualTo("parent-1")
        assertThat(propagatedState.originType).isEqualTo(TreeOriginType.PROPAGATED)
        assertThat(propagatedState.propagationMethod).isEqualTo(PropagationMethod.CUTTING)
        assertThat(propagatedState.propagationDate).isEqualTo(today)
        assertThat(propagatedState.notes).isEmpty()
        assertThat(propagatedState.hasFruitedBefore).isFalse()
    }

    private fun testTree(
        id: String,
        species: String,
        cultivar: String,
        orchardName: String,
        sectionName: String,
        rootstock: String,
        source: String,
        plantType: PlantType,
        containerSize: String,
        sunExposure: String,
        frostSensitivity: FrostSensitivityLevel,
        frostSensitivityNote: String,
        irrigationNote: String,
        bloomTimingMode: BloomTimingMode,
        bloomPatternOverride: BloomPatternType,
        manualBloomProfile: List<Int>,
        customBloomStartMonth: Int,
        customBloomStartDay: Int,
        customBloomDurationDays: Int,
        selfCompatibilityOverride: SelfCompatibility,
        pollinationModeOverride: PollinationMode,
        pollinationOverrideNote: String
    ) = TreeEntity(
        id = id,
        orchardName = orchardName,
        sectionName = sectionName,
        nickname = null,
        species = species,
        cultivar = cultivar,
        rootstock = rootstock,
        source = source,
        purchaseDate = null,
        plantedDate = 0L,
        plantType = plantType,
        containerSize = containerSize,
        sunExposure = sunExposure,
        frostSensitivity = frostSensitivity,
        frostSensitivityNote = frostSensitivityNote,
        irrigationNote = irrigationNote,
        status = TreeStatus.ACTIVE,
        notes = "",
        tags = "",
        bloomTimingMode = bloomTimingMode,
        bloomPatternOverride = bloomPatternOverride,
        manualBloomProfile = manualBloomProfile,
        customBloomStartMonth = customBloomStartMonth,
        customBloomStartDay = customBloomStartDay,
        customBloomDurationDays = customBloomDurationDays,
        selfCompatibilityOverride = selfCompatibilityOverride,
        pollinationModeOverride = pollinationModeOverride,
        pollinationOverrideNote = pollinationOverrideNote,
        createdAt = 0L,
        updatedAt = 0L
    )
}
