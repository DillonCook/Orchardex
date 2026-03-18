package com.dillon.orcharddex.data.model

import android.net.Uri

data class TreeInput(
    val id: String? = null,
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
    val newPhotoUris: List<Uri> = emptyList(),
    val removedPhotoIds: List<String> = emptyList()
)

data class EventInput(
    val treeId: String,
    val eventType: EventType,
    val eventDate: Long,
    val notes: String = "",
    val cost: Double? = null,
    val quantityValue: Double? = null,
    val quantityUnit: String = "",
    val photoUri: Uri? = null
)

data class HarvestInput(
    val treeId: String,
    val harvestDate: Long,
    val quantityValue: Double,
    val quantityUnit: String,
    val qualityRating: Int = 3,
    val firstFruit: Boolean = false,
    val notes: String = "",
    val photoUri: Uri? = null
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
