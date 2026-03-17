package com.dillon.orcharddex

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.content.FileProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.Intents.intending
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OrchardDexSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun initIntents() {
        Intents.init()
    }

    @After
    fun releaseIntents() {
        Intents.release()
    }

    @Test
    fun addTree_smoke() {
        val cultivarName = "UITestCarrie${System.currentTimeMillis()}"
        composeRule.onNodeWithTag("add_tree").performClick()
        composeRule.onNodeWithTag("tree_species").performTextInput("Mango")
        composeRule.onNodeWithTag("tree_cultivar").performTextInput(cultivarName)
        composeRule.onNodeWithTag("tree_save").performClick()
        composeRule.onNodeWithText(cultivarName).fetchSemanticsNode()
    }

    @Test
    fun logHarvest_smoke() {
        val cultivarName = "UIHarvest${System.currentTimeMillis()}"
        composeRule.onNodeWithTag("add_tree").performClick()
        composeRule.onNodeWithTag("tree_species").performTextInput("Guava")
        composeRule.onNodeWithTag("tree_cultivar").performTextInput(cultivarName)
        composeRule.onNodeWithTag("tree_save").performClick()
        composeRule.onNodeWithTag("add_harvest").performClick()
        composeRule.onNodeWithTag("harvest_quantity").performTextInput("12")
        composeRule.onNodeWithTag("harvest_save").performClick()
        composeRule.onNodeWithText("12 fruit").fetchSemanticsNode()
    }

    @Test
    fun exportBackup_smoke() {
        val context = composeRule.activity
        val exportFile = File(context.cacheDir, "ui-export.orcharddex.zip").apply {
            delete()
            createNewFile()
        }
        val exportUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
        intending(allOf(hasAction(Intent.ACTION_CREATE_DOCUMENT))).respondWith(
            android.app.Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().apply { data = exportUri }
            )
        )

        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag("export_backup").performScrollTo().performClick()
        composeRule.waitUntil(10_000) { exportFile.length() > 0L }
        assertThat(exportFile.length()).isGreaterThan(0L)
    }
}
