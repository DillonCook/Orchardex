package com.dillon.orcharddex.data.model

import android.net.Uri

data class TreeInput(
    val id: String? = null,
    val locationId: String? = null,
    val orchardName: String = "",
    val sectionName: String = "",
    val nickname: String = "",
    val species: String,
    val cultivar: String,
    val rootstock: String = "",
    val source: String = "",
    val purchaseDate: Long? = null,
    val plantedDate: Long,
    val plantType: PlantType = PlantType.IN_GROUND,
    val containerSize: String = "",
    val sunExposure: String = "",
    val frostSensitivity: FrostSensitivityLevel = FrostSensitivityLevel.MEDIUM,
    val frostSensitivityNote: String = "",
    val irrigationNote: String = "",
    val status: TreeStatus = TreeStatus.ACTIVE,
    val hasFruitedBefore: Boolean = false,
    val notes: String = "",
    val tags: String = "",
    val bloomTimingMode: BloomTimingMode = BloomTimingMode.AUTO,
    val bloomPatternOverride: BloomPatternType? = null,
    val manualBloomProfile: List<Int> = emptyList(),
    val alternateYearAnchor: Int? = null,
    val customBloomStartMonth: Int? = null,
    val customBloomStartDay: Int? = null,
    val customBloomDurationDays: Int? = null,
    val selfCompatibilityOverride: SelfCompatibility? = null,
    val pollinationModeOverride: PollinationMode? = null,
    val pollinationOverrideNote: String = "",
    val nurseryStage: NurseryStage = NurseryStage.NONE,
    val parentTreeId: String? = null,
    val originType: TreeOriginType = TreeOriginType.UNKNOWN,
    val propagationMethod: PropagationMethod? = null,
    val propagationDate: Long? = null,
    val quantity: Int = 1,
    val newPhotoUris: List<Uri> = emptyList(),
    val removedPhotoIds: List<String> = emptyList()
)

data class GrowingLocationInput(
    val id: String? = null,
    val name: String,
    val countryCode: String = "",
    val timezoneId: String,
    val hemisphere: Hemisphere = Hemisphere.NORTHERN,
    val latitudeDeg: Double? = null,
    val longitudeDeg: Double? = null,
    val elevationM: Double? = null,
    val usdaZoneCode: String? = null,
    val chillHoursBand: ChillHoursBand = ChillHoursBand.UNKNOWN,
    val microclimateFlags: Set<MicroclimateFlag> = emptySet(),
    val notes: String = ""
)

data class EventInput(
    val treeId: String,
    val eventType: EventType,
    val eventDate: Long,
    val notes: String = "",
    val cost: Double? = null,
    val quantityValue: Double? = null,
    val quantityUnit: String = "",
    val photoUris: List<Uri> = emptyList()
)

data class HarvestInput(
    val treeId: String,
    val harvestDate: Long,
    val quantityValue: Double,
    val quantityUnit: String,
    val qualityRating: Int = 3,
    val firstFruit: Boolean = false,
    val verified: Boolean = true,
    val notes: String = "",
    val photoUris: List<Uri> = emptyList()
)

data class SaleInput(
    val treeId: String,
    val saleKind: SaleKind,
    val linkedHarvestId: String? = null,
    val soldAt: Long,
    val quantityValue: Double,
    val quantityUnit: String,
    val unitPrice: Double,
    val currencyCode: String = "USD",
    val saleChannel: SaleChannel = SaleChannel.DIRECT,
    val notes: String = ""
)

data class ReminderInput(
    val id: String? = null,
    val treeId: String? = null,
    val title: String,
    val notes: String = "",
    val dueAt: Long,
    val hasTime: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceIntervalDays: Int? = null,
    val enabled: Boolean = true,
    val leadTimeMode: LeadTimeMode = LeadTimeMode.SAME_DAY,
    val customLeadTimeHours: Int? = null
)

data class WishlistInput(
    val id: String? = null,
    val species: String,
    val cultivar: String,
    val priority: WishlistPriority = WishlistPriority.MEDIUM,
    val notes: String = ""
)
