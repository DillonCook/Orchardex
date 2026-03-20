package com.dillon.orcharddex.sample

import com.dillon.orcharddex.data.local.EventEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistPriority
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

data class SamplePayload(
    val trees: List<TreeEntity>,
    val events: List<EventEntity>,
    val harvests: List<HarvestEntity>,
    val reminders: List<ReminderEntity>,
    val wishlist: List<WishlistCultivarEntity>
)

class SampleDataSeeder {
    fun build(nowMillis: Long = System.currentTimeMillis()): SamplePayload {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val trees = listOf(
            sampleTree("Sunridge", "South fence row", "Mango", "Carrie", plantedDaysAgo = 680),
            sampleTree("Sunridge", "South fence row", "Mango", "Sweet Tart", plantedDaysAgo = 540),
            sampleTree("Sunridge", "Container pad", "Avocado", "Reed", plantedDaysAgo = 400, plantType = PlantType.CONTAINER, container = "25 gal"),
            sampleTree("Sunridge", "North trellis", "Passionfruit", "Panama Red", plantedDaysAgo = 220),
            sampleTree("Backyard", "Patio", "Guava", "Ruby Supreme", plantedDaysAgo = 365),
            sampleTree("Backyard", "Greenhouse", "Jaboticaba", "Sabara", plantedDaysAgo = 120, plantType = PlantType.CONTAINER, container = "15 gal"),
            sampleTree("Backyard", "East berm", "Lychee", "Mauritius", plantedDaysAgo = 860),
            sampleTree("Backyard", "East berm", "Loquat", "Big Jim", plantedDaysAgo = 620),
            sampleTree("Trial Block", "Row A", "Dragon fruit", "American Beauty", plantedDaysAgo = 210),
            sampleTree("Trial Block", "Row B", "Fig", "Black Madeira", plantedDaysAgo = 190),
            sampleTree("Trial Block", "Row C", "Sapodilla", "Makok", plantedDaysAgo = 280),
            sampleTree("Trial Block", "Container row", "Mulberry", "Pakistan", plantedDaysAgo = 145, plantType = PlantType.CONTAINER, container = "20 gal")
        )

        val events = trees.flatMapIndexed { index, tree ->
            listOf(
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    treeId = tree.id,
                    eventType = EventType.PLANTED,
                    eventDate = tree.plantedDate,
                    notes = "Planted into ${tree.sectionName.lowercase()}",
                    cost = null,
                    quantityValue = null,
                    quantityUnit = null,
                    photoPath = null,
                    createdAt = tree.createdAt
                ),
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    treeId = tree.id,
                    eventType = if (index % 2 == 0) EventType.FERTILIZED else EventType.PRUNED,
                    eventDate = atHour(today.minusDays((index + 3).toLong()), 8, zone),
                    notes = if (index % 2 == 0) "Applied spring feeding." else "Shaped to open the canopy.",
                    cost = if (index % 2 == 0) 4.5 else null,
                    quantityValue = if (index % 2 == 0) 0.5 else null,
                    quantityUnit = if (index % 2 == 0) "lb" else null,
                    photoPath = null,
                    createdAt = nowMillis
                )
            )
        }

        val harvests = listOf(
            sampleHarvest(trees[0].id, today.minusDays(20), 14.0, "fruit", 5, true, "First clean Carrie crop."),
            sampleHarvest(trees[4].id, today.minusDays(6), 8.5, "lb", 4, false, "Sweet with low seed count."),
            sampleHarvest(trees[7].id, today.minusDays(30), 3.0, "lb", 4, true, "First fruit after a strong bloom."),
            sampleHarvest(trees[8].id, today.minusDays(11), 12.0, "fruit", 3, false, "Needs another week for peak flavor.")
        )

        val reminders = listOf(
            sampleReminder(trees[0].id, "Fertilize Carrie mango", today.plusDays(3), RecurrenceType.MONTHLY, null),
            sampleReminder(trees[2].id, "Check container moisture", today.plusDays(1), RecurrenceType.EVERY_X_DAYS, 4),
            sampleReminder(trees[8].id, "Harvest check", today.plusDays(5), RecurrenceType.WEEKLY, null),
            sampleReminder(null, "Spray inventory review", today.plusDays(9), RecurrenceType.NONE, null)
        )

        val wishlist = listOf(
            WishlistCultivarEntity(UUID.randomUUID().toString(), "Mango", "Coconut Cream", WishlistPriority.HIGH, "Priority for summer planting.", false, null, nowMillis),
            WishlistCultivarEntity(UUID.randomUUID().toString(), "Avocado", "Sharwil", WishlistPriority.MEDIUM, "Good flavor benchmark.", false, null, nowMillis),
            WishlistCultivarEntity(UUID.randomUUID().toString(), "Lychee", "Sweetheart", WishlistPriority.MEDIUM, "Need a better pollination pair.", false, null, nowMillis),
            WishlistCultivarEntity(UUID.randomUUID().toString(), "Fig", "Smith", WishlistPriority.LOW, "Trial against Black Madeira.", false, null, nowMillis)
        )

        return SamplePayload(
            trees = trees,
            events = events,
            harvests = harvests,
            reminders = reminders,
            wishlist = wishlist
        )
    }

    private fun sampleTree(
        orchard: String,
        section: String,
        species: String,
        cultivar: String,
        plantedDaysAgo: Long,
        plantType: PlantType = PlantType.IN_GROUND,
        container: String = ""
    ): TreeEntity {
        val now = System.currentTimeMillis()
        val plantedAt = System.currentTimeMillis() - plantedDaysAgo * 24 * 60 * 60 * 1000
        return TreeEntity(
            id = UUID.randomUUID().toString(),
            orchardName = orchard,
            sectionName = section,
            nickname = null,
            species = species,
            cultivar = cultivar,
            rootstock = "",
            source = "Sample nursery",
            purchaseDate = plantedAt - 14 * 24 * 60 * 60 * 1000,
            plantedDate = plantedAt,
            plantType = plantType,
            containerSize = container.ifBlank { null },
            sunExposure = "Full sun",
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = "",
            irrigationNote = "2x weekly deep soak",
            status = TreeStatus.ACTIVE,
            notes = "Sample orchard record for onboarding.",
            tags = "sample,starter",
            createdAt = now,
            updatedAt = now
        )
    }

    private fun sampleHarvest(
        treeId: String,
        date: java.time.LocalDate,
        quantity: Double,
        unit: String,
        quality: Int,
        firstFruit: Boolean,
        notes: String
    ): HarvestEntity = HarvestEntity(
        id = UUID.randomUUID().toString(),
        treeId = treeId,
        harvestDate = atHour(date, 9, ZoneId.systemDefault()),
        quantityValue = quantity,
        quantityUnit = unit,
        qualityRating = quality,
        firstFruit = firstFruit,
        verified = true,
        notes = notes,
        photoPath = null,
        createdAt = System.currentTimeMillis()
    )

    private fun sampleReminder(
        treeId: String?,
        title: String,
        date: java.time.LocalDate,
        recurrenceType: RecurrenceType,
        recurrenceIntervalDays: Int?
    ): ReminderEntity {
        val now = System.currentTimeMillis()
        return ReminderEntity(
            id = UUID.randomUUID().toString(),
            treeId = treeId,
            title = title,
            notes = "",
            dueAt = atHour(date, 8, ZoneId.systemDefault()),
            hasTime = false,
            recurrenceType = recurrenceType,
            recurrenceIntervalDays = recurrenceIntervalDays,
            enabled = true,
            completedAt = null,
            leadTimeMode = LeadTimeMode.SAME_DAY,
            customLeadTimeHours = null,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun atHour(date: java.time.LocalDate, hour: Int, zone: ZoneId): Long =
        ZonedDateTime.of(date.atTime(hour, 0), zone).toInstant().toEpochMilli()
}
