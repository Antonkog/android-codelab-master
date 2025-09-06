package com.sap.codelab.home.presentation

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.sap.codelab.R
import com.sap.codelab.create.presentation.CreateMemo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivityInstrumentedTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var scenario: ActivityScenario<Home>

    @Before
    fun setUp() {
        // Initialize Espresso-Intents to capture navigation
        Intents.init()
        scenario = ActivityScenario.launch(Home::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
        scenario.close()
    }

    @Test
    fun views_exist_after_permissions_granted() {
        // RecyclerView is visible
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        // FAB should be visible after permissions granted by rule
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
    }

    @Test
    fun menu_items_exist_after_permissions_granted() {
        // The toolbar action "Show all" should be visible initially
        onView(withId(R.id.action_show_all)).check(matches(isDisplayed()))
        // Toggle to Show open and ensure it appears
        onView(withId(R.id.action_show_all)).perform(click())
        onView(withId(R.id.action_show_open)).check(matches(isDisplayed()))
    }

    @Test
    fun fab_navigates_to_create_memo_activity() {
        onView(withId(R.id.fab)).perform(click())
        Intents.intended(hasComponent(CreateMemo::class.java.name))
    }
}
