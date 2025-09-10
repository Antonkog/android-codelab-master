package com.sap.codelab.detail.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import org.koin.androidx.compose.koinViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMemoScreen(
    memoId: Long,
    onBack: () -> Unit,
    vm: ViewMemoViewModel = koinViewModel()
) {
    vm.loadMemo(memoId = memoId)
    val state by vm.memo.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.view_memo)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { inner ->
        state?.let {
            Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(16.dp)
            ) {

                Text(text = it.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(text = it.description)
            }
        } ?: run {
            Text(text = stringResource(R.string.loading))
        }
    }
}
