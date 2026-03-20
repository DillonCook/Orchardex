package com.dillon.orcharddex.data.model

import com.dillon.orcharddex.data.local.EventEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import kotlinx.serialization.Serializable

data class TreeListItem(
    val tree: TreeEntity,
    val mainPhotoPath: String?
)

data class TreeDetailModel(
    val tree: TreeEntity,
    val photos: List<TreePhotoEntity>,
    val events: List<EventEntity>,
    val harvests: List<HarvestEntity>,
    val reminders: List<ReminderEntity>
)

enum class ActivityKind {
    EVENT,
    HARVEST
}

data class RecentActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: Long,
    val kind: ActivityKind,
    val treeId: String?
)

data class HistoryEntryModel(
    val id: String,
    val kind: ActivityKind,
    val treeId: String,
    val treeLabel: String,
    val orchardName: String,
    val species: String,
    val cultivar: String,
    val date: Long,
    val createdAt: Long,
    val title: String,
    val preview: String,
    val notes: String,
    val eventType: EventType? = null,
    val quantityValue: Double? = null,
    val quantityUnit: String? = null,
    val cost: Double? = null,
    val qualityRating: Int? = null,
    val firstFruit: Boolean = false,
    val verified: Boolean = false,
    val photoPath: String? = null
)

data class DashboardDetailItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val date: Long? = null,
    val treeId: String? = null
)

data class DashboardModel(
    val totalTreeCount: Int = 0,
    val cultivarCount: Int = 0,
    val upcoming7Count: Int = 0,
    val upcoming30Count: Int = 0,
    val speciesCount: Int = 0,
    val wishlistCount: Int = 0,
    val awaitingFirstFruitCount: Int = 0,
    val recentActivity: List<RecentActivityItem> = emptyList(),
    val recentHarvests: List<HarvestEntity> = emptyList(),
    val treeItems: List<DashboardDetailItem> = emptyList(),
    val cultivarItems: List<DashboardDetailItem> = emptyList(),
    val speciesItems: List<DashboardDetailItem> = emptyList(),
    val wishlistItems: List<DashboardDetailItem> = emptyList(),
    val awaitingFirstFruitItems: List<DashboardDetailItem> = emptyList(),
    val upcoming7Items: List<DashboardDetailItem> = emptyList()
)

data class DexCultivarEntry(
    val species: String,
    val cultivar: String,
    val activeTreeCount: Int,
    val inactiveTreeCount: Int,
    val firstFruitAchieved: Boolean,
    val wishlist: Boolean,
    val linkedTreeId: String?
)

data class DexSpeciesGroup(
    val species: String,
    val cultivars: List<DexCultivarEntry>
)

data class DexModel(
    val ownedGroups: List<DexSpeciesGroup> = emptyList(),
    val wishlistEntries: List<WishlistCultivarEntity> = emptyList(),
    val ownedCultivarCount: Int = 0,
    val wishlistCount: Int = 0,
    val firstFruitCount: Int = 0
)

data class ReminderListItem(
    val reminder: ReminderEntity,
    val treeLabel: String?,
    val species: String?
)

@Serializable
data class SettingsSnapshot(
    val themeMode: String,
    val dynamicColor: Boolean,
    val defaultLeadTimeMode: String,
    val defaultCustomLeadHours: Int,
    val orchardName: String = "",
    val usdaZone: String = "",
    val orchardRegion: String = "",
    val onboardingComplete: Boolean = false
)
