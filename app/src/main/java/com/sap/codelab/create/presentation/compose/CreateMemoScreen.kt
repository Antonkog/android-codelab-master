package com.sap.codelab.create.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.create.presentation.compose.components.SelectableMap
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    vm: CreateMemoNewViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val locationNotSetError = stringResource(R.string.memo_location_empty_error)
    val titleNotSetError = stringResource(R.string.memo_title_empty_error)
    val descriptionNotSetError = stringResource(R.string.memo_text_empty_error)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_memo)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if(vm.validate()){
                            vm.onAction(CreateMemoAction.OnSave)
                            onSave()
                        }
                    }) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = { vm.onAction(CreateMemoAction.OnTitleChange(it)) },
                label = { Text(stringResource(R.string.memo_title)) },
                isError = state.titleError,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.titleError)
                Text(
                    titleNotSetError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )


            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { vm.onAction(CreateMemoAction.OnDescriptionChange(it)) },
                label = { Text(stringResource(R.string.memo_text)) },
                isError = state.descriptionError,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.descriptionError)
                Text(
                    descriptionNotSetError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )

            Spacer(Modifier.height(16.dp))

            SelectableMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onLocationSetListener = { lat, lng ->
                    vm.onAction(CreateMemoAction.OnLocationSelected(lat, lng))
                }
            )

            if (state.locationError)
                Text(
                    locationNotSetError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
        }
    }
}
