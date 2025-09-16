package com.sap.codelab.create.presentation

import com.google.common.truth.Truth.assertThat
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.utils.geofence.GeoFenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMemoViewModelTest {

    private lateinit var viewModel: CreateMemoViewModel
    private val repository = mock<IMemoRepository>()
    private val geoFenceManager = mock<GeoFenceManager>()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        viewModel = CreateMemoViewModel(repository, geoFenceManager)
    }

    @Test
    fun `OnSave with empty fields sets errors and does not pass validation`() =
        testScope.runTest {
            // Start with empty state (default)
            viewModel.onAction(CreateMemoAction.OnSave)

            val state = viewModel.uiState.value
            assertThat(state.titleError).isTrue()
            assertThat(state.descriptionError).isTrue()
            assertThat(state.locationError).isTrue()
            assertThat(state.passedValidation).isFalse()
        }

    @Test
    fun `OnSave with all fields filled passes validation`() = testScope.runTest {
        // Fill in all required fields
        viewModel.onAction(CreateMemoAction.OnTitleChange("My Title"))
        viewModel.onAction(CreateMemoAction.OnDescriptionChange("Some description"))
        viewModel.onAction(CreateMemoAction.OnLocationSelected(10.0, 20.0))

        // after validation repository.saveMemo() is called
        whenever(repository.saveMemo(any())).thenReturn(1L)

        viewModel.onAction(CreateMemoAction.OnSave)

        val state = viewModel.uiState.value
        assertThat(state.titleError).isFalse()
        assertThat(state.descriptionError).isFalse()
        assertThat(state.locationError).isFalse()
        assertThat(state.passedValidation).isTrue()
    }
}