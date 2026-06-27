package io.github.ln.apnsettingshelper.ui.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import io.github.ln.apnsettingshelper.ui.common.displayName
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
    PresetDetailContent(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onRecordApplied = viewModel::recordApplied,
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
    onRecordApplied: () -> Unit,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val editorUnavailable = stringResource(R.string.apn_editor_unavailable)
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
        item {
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
    }
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

private data class CopyFieldUi(
    val labelRes: Int,
    val value: String,
)

private data class ChecklistFieldUi(
    val key: String,
    val labelRes: Int,
    val value: String,
)

/** Non-blank, non-dropdown fields, in system-APN-editor order. APN/MCC/MNC are always present. */
private fun copyableFields(preset: Preset): List<CopyFieldUi> =
    buildList {
        add(CopyFieldUi(R.string.field_apn, preset.apn))
        if (preset.username.isNotBlank()) add(CopyFieldUi(R.string.field_username, preset.username))
        if (preset.password.isNotBlank()) add(CopyFieldUi(R.string.field_password, preset.password))
        add(CopyFieldUi(R.string.field_mcc, preset.mcc))
        add(CopyFieldUi(R.string.field_mnc, preset.mnc))
        if (preset.mvnoValue.isNotBlank()) add(CopyFieldUi(R.string.field_mvno_value, preset.mvnoValue))
        if (preset.apnType.isNotBlank()) add(CopyFieldUi(R.string.field_apn_type, preset.apnType))
        if (preset.proxy.isNotBlank()) add(CopyFieldUi(R.string.field_proxy, preset.proxy))
        if (preset.port.isNotBlank()) add(CopyFieldUi(R.string.field_port, preset.port))
        if (preset.mmsc.isNotBlank()) add(CopyFieldUi(R.string.field_mmsc, preset.mmsc))
        if (preset.mmsProxy.isNotBlank()) add(CopyFieldUi(R.string.field_mms_proxy, preset.mmsProxy))
        if (preset.mmsPort.isNotBlank()) add(CopyFieldUi(R.string.field_mms_port, preset.mmsPort))
        if (preset.server.isNotBlank()) add(CopyFieldUi(R.string.field_server, preset.server))
    }

/** The dropdown fields, always shown as "set to X" checklist items. */
private fun checklistFields(preset: Preset): List<ChecklistFieldUi> =
    listOf(
        ChecklistFieldUi("authType", R.string.field_auth_type, preset.authType.displayName()),
        ChecklistFieldUi("protocol", R.string.field_protocol, preset.protocol.displayName()),
        ChecklistFieldUi("roamingProtocol", R.string.field_roaming_protocol, preset.roamingProtocol.displayName()),
        ChecklistFieldUi("mvnoType", R.string.field_mvno_type, preset.mvnoType.displayName()),
    )
