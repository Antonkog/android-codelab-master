package com.sap.codelab.core.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.sap.codelab.core.data.db.AppDatabase
import com.sap.codelab.core.domain.Memo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class, sdk = [29, 34])
class RepositoryRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: Repository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        stopKoin()
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = Repository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun save_and_query_flows_work_and_notNotified_filters() = testScope.runTest {
        repository.saveMemo(
            Memo(
                title = "Groceries",
                description = "buy something",
                reminderLatitude = 121.0,
                reminderLongitude = 89.0
            )
        )
        repository.saveMemo(
            Memo(
                title = "Buy shoes",
                description = "shopping",
                reminderLatitude = 123.0,
                reminderLongitude = 59.0
            )
        )

        // Let Room/flows propagate
        advanceUntilIdle()

        val all = repository.getAllMemoAsFlow().first()
        assertThat(all.map { it.title }).containsExactly("Groceries", "Buy shoes")

        val open = repository.getOpenMemoAsFlow().first()
        assertThat(open.size).isEqualTo(2)

        // Mark one as notified
        repository.saveMemo(all.first().copy(isNotificationShown = true))
        advanceUntilIdle()

        val notNotified = repository.getNotNotifiedMemosAsFlow().first()
        assertThat(notNotified.map { it.title }).containsExactly("Buy shoes")
    }
}
class TestApp : Application()//to avoid koin started error, when running all tests together.