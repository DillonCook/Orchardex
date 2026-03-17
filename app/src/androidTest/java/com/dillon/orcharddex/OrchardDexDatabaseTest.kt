package com.dillon.orcharddex

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchardDexDatabaseTest {
    private lateinit var database: OrchardDexDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OrchardDexDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTree_persistsAndReadsBack() = runBlocking {
        val tree = sampleTree()
        database.treeDao().insert(tree)

        val saved = database.treeDao().getTree(tree.id)

        assertThat(saved).isNotNull()
        assertThat(saved?.cultivar).isEqualTo("Carrie")
        assertThat(saved?.plantType).isEqualTo(PlantType.IN_GROUND)
    }

    private fun sampleTree() = TreeEntity(
        id = "tree-1",
        orchardName = "Home",
        sectionName = "South row",
        nickname = "Front favorite",
        species = "Mango",
        cultivar = "Carrie",
        rootstock = null,
        source = "Local nursery",
        purchaseDate = null,
        plantedDate = 1_700_000_000_000,
        plantType = PlantType.IN_GROUND,
        containerSize = null,
        sunExposure = "Full sun",
        frostSensitivity = FrostSensitivityLevel.MEDIUM,
        frostSensitivityNote = null,
        irrigationNote = null,
        status = TreeStatus.ACTIVE,
        notes = "",
        tags = "",
        createdAt = 1L,
        updatedAt = 1L
    )
}
