package io.github.ln.apnsettingshelper.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.ln.apnsettingshelper.R

/** VM-wired preset list. */
@Composable
fun PresetListScreen(
    onPresetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetListViewModel = viewModel(factory = PresetListViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsState()
    PresetListContent(
        state = state,
        onPresetClick = onPresetClick,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}

/** Stateless list content: ★ Favorites section (if any) then region → carrier groups. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListContent(
    state: PresetListUiState,
    onPresetClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (state.favorites.isNotEmpty()) {
                item(key = "favorites-header") {
                    SectionHeader(stringResource(R.string.favorites_section), leadingStar = true)
                }
                items(state.favorites, key = { "fav-${it.id}" }) { row ->
                    PresetRow(row = row, onClick = onPresetClick, onToggleFavorite = onToggleFavorite)
                }
            }

            state.regions.forEach { region ->
                item(key = "region-${region.region}") { SectionHeader(region.region) }
                region.carriers.forEach { carrier ->
                    item(key = "carrier-${region.region}-${carrier.carrier}") {
                        CarrierHeader(carrier.carrier)
                    }
                    items(carrier.rows, key = { it.id }) { row ->
                        PresetRow(row = row, onClick = onPresetClick, onToggleFavorite = onToggleFavorite)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetRow(
    row: PresetRowUi,
    onClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val favoriteDescription =
        stringResource(if (row.isFavorite) R.string.cd_favorite_remove else R.string.cd_favorite_add)
    ListItem(
        headlineContent = { Text(row.label) },
        supportingContent = {
            Column {
                Text(row.carrier, style = MaterialTheme.typography.bodySmall)
                row.lastAppliedLabel?.let { label ->
                    Text(
                        text = stringResource(R.string.last_applied, label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        trailingContent = {
            IconButton(onClick = { onToggleFavorite(row.id) }) {
                Icon(
                    imageVector = if (row.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = favoriteDescription,
                )
            }
        },
        modifier = Modifier.clickable { onClick(row.id) },
    )
}

@Composable
private fun SectionHeader(
    title: String,
    leadingStar: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingStar) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CarrierHeader(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}
