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

data class DashboardModel(
    val totalTreeCount: Int = 0,
    val activeCultivarCount: Int = 0,
    val upcoming7Count: Int = 0,
    val upcoming30Count: Int = 0,
    val speciesCount: Int = 0,
    val wishlistCount: Int = 0,
    val firstFruitCount: Int = 0,
    val recentActivity: List<RecentActivityItem> = emptyList(),
    val recentHarvests: List<HarvestEntity> = emptyList()
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
    val defaultCustomLeadHours: Int
)
