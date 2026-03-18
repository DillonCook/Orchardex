package com.dillon.orcharddex.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistPriority
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "trees",
    indices = [
        Index("species"),
        Index("cultivar"),
        Index("orchardName"),
        Index("status")
    ]
)
data class TreeEntity(
    @PrimaryKey val id: String,
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
