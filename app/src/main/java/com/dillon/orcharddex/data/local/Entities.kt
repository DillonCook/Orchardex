package com.dillon.orcharddex.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.MicroclimateFlag
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.PollinationMode
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.SelfCompatibility
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistPriority
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "growing_locations",
    indices = [Index("name")]
)
data class GrowingLocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val countryCode: String,
    val timezoneId: String,
    val hemisphere: Hemisphere,
    val latitudeDeg: Double?,
    val longitudeDeg: Double?,
    val elevationM: Double?,
    val usdaZoneCode: String?,
    val chillHoursBand: ChillHoursBand,
    val microclimateFlags: Set<MicroclimateFlag>,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
@Entity(
    tableName = "trees",
    foreignKeys = [
        ForeignKey(
            entity = GrowingLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("species"),
        Index("cultivar"),
        Index("orchardName"),
        Index("status"),
        Index("locationId")
    ]
)
data class TreeEntity(
    @PrimaryKey val id: String,
    val locationId: String? = null,
    val orchardName: String,
    val sectionName: String,
    val nickname: String?,
    val species: String,
    val cultivar: String,
    val rootstock: String?,
    val source: String?,
    val purchaseDate: Long?,
    val plantedDate: Long,
    val plantType: PlantType,
    val containerSize: String?,
    val sunExposure: String?,
    val frostSensitivity: FrostSensitivityLevel,
    val frostSensitivityNote: String?,
    val irrigationNote: String?,
    val status: TreeStatus,
    val hasFruitedBefore: Boolean = false,
    val notes: String,
    val tags: String,
    val bloomTimingMode: BloomTimingMode = BloomTimingMode.AUTO,
    val customBloomStartMonth: Int? = null,
    val customBloomStartDay: Int? = null,
    val customBloomDurationDays: Int? = null,
    val selfCompatibilityOverride: SelfCompatibility? = null,
    val pollinationModeOverride: PollinationMode? = null,
    val pollinationOverrideNote: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
@Entity(
    tableName = "tree_photos",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("treeId")]
)
data class TreePhotoEntity(
    @PrimaryKey val id: String,
    val treeId: String,
    val relativePath: String,
    val caption: String?,
    val createdAt: Long,
    val sortOrder: Int,
    val isHero: Boolean = false
)

@Serializable
@Entity(
    tableName = "activity_photos",
    indices = [Index(value = ["ownerKind", "ownerId"]), Index("relativePath")]
)
data class ActivityPhotoEntity(
    @PrimaryKey val id: String,
    val ownerKind: String,
    val ownerId: String,
    val relativePath: String,
    val caption: String?,
    val createdAt: Long,
    val sortOrder: Int
)

@Serializable
@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("treeId"), Index("eventDate")]
)
data class EventEntity(
    @PrimaryKey val id: String,
    val treeId: String,
    val eventType: EventType,
    val eventDate: Long,
    val notes: String,
    val cost: Double?,
    val quantityValue: Double?,
    val quantityUnit: String?,
    val photoPath: String?,
    val createdAt: Long
)

@Serializable
@Entity(
    tableName = "harvests",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("treeId"), Index("harvestDate")]
)
data class HarvestEntity(
    @PrimaryKey val id: String,
    val treeId: String,
    val harvestDate: Long,
    val quantityValue: Double,
    val quantityUnit: String,
    val qualityRating: Int,
    val firstFruit: Boolean,
    val verified: Boolean = true,
    val notes: String,
    val photoPath: String?,
    val createdAt: Long
)

@Serializable
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TreeEntity::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("treeId"), Index("dueAt")]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val treeId: String?,
    val title: String,
    val notes: String,
    val dueAt: Long,
    val hasTime: Boolean,
    val recurrenceType: RecurrenceType,
    val recurrenceIntervalDays: Int?,
    val enabled: Boolean,
    val completedAt: Long?,
    val leadTimeMode: LeadTimeMode,
    val customLeadTimeHours: Int?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
@Entity(
    tableName = "wishlist_cultivars",
    indices = [Index("species"), Index("cultivar")]
)
data class WishlistCultivarEntity(
    @PrimaryKey val id: String,
    val species: String,
    val cultivar: String,
    val priority: WishlistPriority,
    val notes: String,
    val acquired: Boolean,
    val linkedTreeId: String?,
    val createdAt: Long
)

data class TreeWithPhotos(
    @Embedded val tree: TreeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "treeId"
    )
    val photos: List<TreePhotoEntity>
)

fun GrowingLocationEntity.toForecastLocationProfile(): ForecastLocationProfile = ForecastLocationProfile(
    name = name,
    countryCode = countryCode,
    timezoneId = timezoneId,
    hemisphere = hemisphere,
    latitudeDeg = latitudeDeg,
    longitudeDeg = longitudeDeg,
    elevationM = elevationM,
    usdaZoneCode = usdaZoneCode,
    chillHoursBand = chillHoursBand,
    microclimateFlags = microclimateFlags,
    notes = notes
)
