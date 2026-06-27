package io.github.ln.apnsettingshelper.ui.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.ln.apnsettingshelper.R
import io.github.ln.apnsettingshelper.domain.model.Preset
import io.github.ln.apnsettingshelper.ui.common.ChecklistItem
import io.github.ln.apnsettingshelper.ui.common.CopyableField
import io.github.ln.apnsettingshelper.ui.common.openApnEditor

/** VM-wired preset detail. */
@Composable
fun PresetDetailScreen(
    presetId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetDetailViewModel = viewModel(factory = PresetDetailViewModel.factory(presetId)),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appliedMessage = stringResource(R.string.applied_ok)
    val notSelectedMessage = stringResource(R.string.applied_not_selected)
    val failedMessage = stringResource(R.string.apply_failed)
    LaunchedEffect(viewModel) {
        viewModel.applyEvents.collect { event ->
            val message =
                when (event) {
                    ApplyEvent.Applied -> {
                        appliedMessage
                    }

                    ApplyEvent.WrittenNotSelected -> {
                        notSelectedMessage
                    }

                    is ApplyEvent.Failed -> {
                        if (event.detail.isNullOrBlank()) {
                            failedMessage
                        } else {
                            context.getString(R.string.apply_failed_detail, event.detail)
                        }
                    }
                }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    PresetDetailContent(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onRecordApplied = viewModel::recordApplied,
        onApplyNow = viewModel::applyNow,
        onSetRootEnabled = viewModel::setRootApplyEnabled,
        modifier = modifier,
    )
}

/** Stateless detail content: copy fields, "set to X" checklist, open-editor + record actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetDetailContent(
    state: PresetDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRecordApplied: () -> Unit,
    onApplyNow: () -> Unit,
    onSetRootEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DetailTopBar(
                title = state.title.ifBlank { stringResource(R.string.app_name) },
                showFavorite = state.preset != null,
                isFavorite = state.isFavorite,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
            )
        },
    ) { innerPadding ->
        val preset = state.preset
        if (state.notFound || preset == null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                if (!state.loading) Text(stringResource(R.string.preset_not_found))
            }
        } else {
            PresetDetailBody(
                preset = preset,
                notes = state.notes,
                lastAppliedLabel = state.lastAppliedLabel,
                rootRequested = state.rootRequested,
                rootChecking = state.rootChecking,
                canApplyRoot = state.canApplyRoot,
                applying = state.applying,
                onApplyNow = onApplyNow,
                onSetRootEnabled = onSetRootEnabled,
                onRecordApplied = onRecordApplied,
                contentPadding = innerPadding,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(
    title: String,
    showFavorite: Boolean,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
        actions = {
            if (showFavorite) {
                val description =
                    stringResource(if (isFavorite) R.string.cd_favorite_remove else R.string.cd_favorite_add)
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = description,
                    )
                }
            }
        },
    )
}

@Composable
private fun PresetDetailBody(
    preset: Preset,
    notes: String,
    lastAppliedLabel: String?,
    rootRequested: Boolean,
    rootChecking: Boolean,
    canApplyRoot: Boolean,
    applying: Boolean,
    onApplyNow: () -> Unit,
    onSetRootEnabled: (Boolean) -> Unit,
    onRecordApplied: () -> Unit,
    contentPadding: PaddingValues,
) {
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    val copyFields = remember(preset) { copyableFields(preset) }
    val checklist = remember(preset) { checklistFields(preset) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
    ) {
        item { OpenApnEditorButton() }

        item { SubHeader(stringResource(R.string.detail_copy_section)) }
        items(copyFields) { field ->
            CopyableField(label = stringResource(field.labelRes), value = field.value)
        }

        item { SubHeader(stringResource(R.string.detail_checklist_section)) }
        items(checklist) { field ->
            ChecklistItem(
                text = stringResource(R.string.set_field_to, stringResource(field.labelRes), field.value),
                checked = checked[field.key] == true,
                onCheckedChange = { checked[field.key] = it },
            )
        }

        if (notes.isNotBlank()) {
            item { SubHeader(stringResource(R.string.notes_label)) }
            item {
                Text(
                    text = notes,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        item {
            RecordAppliedSection(lastAppliedLabel = lastAppliedLabel, onRecordApplied = onRecordApplied)
        }

        item {
            RootApplySection(
                rootRequested = rootRequested,
                rootChecking = rootChecking,
                canApplyRoot = canApplyRoot,
                applying = applying,
                onSetRootEnabled = onSetRootEnabled,
                onApplyNow = onApplyNow,
            )
        }
    }
}

@Composable
private fun OpenApnEditorButton() {
    val context = LocalContext.current
    val editorUnavailable = stringResource(R.string.apn_editor_unavailable)
    Button(
        onClick = {
            if (!openApnEditor(context)) {
                Toast.makeText(context, editorUnavailable, Toast.LENGTH_LONG).show()
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(stringResource(R.string.open_apn_editor))
    }
}

/**
 * Opt-in root section. The toggle is the explicit opt-in: only flipping it on asks the VM to
 * probe for `su` (which may show the superuser-grant dialog). Once root is confirmed the
 * one-tap "Apply now" button appears; otherwise a short status line explains why it didn't.
 */
@Composable
private fun RootApplySection(
    rootRequested: Boolean,
    rootChecking: Boolean,
    canApplyRoot: Boolean,
    applying: Boolean,
    onSetRootEnabled: (Boolean) -> Unit,
    onApplyNow: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.root_apply_toggle), modifier = Modifier.weight(1f))
            Switch(checked = rootRequested, onCheckedChange = onSetRootEnabled)
        }
        if (rootRequested) {
            when {
                rootChecking -> {
                    StatusCaption(stringResource(R.string.root_checking))
                }

                canApplyRoot -> {
                    Button(
                        onClick = onApplyNow,
                        enabled = !applying,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                    ) {
                        if (applying) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.apply_now))
                    }
                    StatusCaption(stringResource(R.string.apply_now_caption))
                }

                else -> {
                    StatusCaption(stringResource(R.string.root_unavailable))
                }
            }
        }
    }
}

@Composable
private fun StatusCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun RecordAppliedSection(
    lastAppliedLabel: String?,
    onRecordApplied: () -> Unit,
) {
    val context = LocalContext.current
    val recordedMessage = stringResource(R.string.applied_recorded)
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedButton(
            onClick = {
                onRecordApplied()
                Toast.makeText(context, recordedMessage, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.record_applied))
        }
        lastAppliedLabel?.let { label ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.last_applied, label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SubHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}
