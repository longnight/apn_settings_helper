package io.github.ln.apnsettingshelper.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.ln.apnsettingshelper.R

/**
 * Placeholder preset list. M-B supplies real preset data; M-D builds the
 * favorites section, heart toggle, and last-applied note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListScreen(onPresetClick: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
        ) {
            ListItem(
                headlineContent = { Text("Sample preset") },
                supportingContent = { Text("Tap to open detail (M-D wires real presets)") },
                modifier = Modifier.clickable { onPresetClick("sample") },
            )
        }
    }
}
