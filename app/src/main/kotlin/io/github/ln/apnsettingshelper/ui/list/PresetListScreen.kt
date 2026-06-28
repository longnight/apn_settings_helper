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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.ln.apnsettingshelper.R
import io.github.ln.apnsettingshelper.ui.common.CarrierAvatar

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
        onQueryChange = viewModel::setQuery,
        onRegionChange = viewModel::setRegion,
        modifier = modifier,
    )
}

/** Stateless list content: search + region selector, then ★ Favorites and a flat preset-card list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListContent(
    state: PresetListUiState,
    onPresetClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQueryChange: (String) -> Unit = {},
    onRegionChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    RegionSelector(regions = state.regions, selected = state.selectedRegion, onSelect = onRegionChange)
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            SearchField(query = state.query, onQueryChange = onQueryChange)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (state.favorites.isNotEmpty()) {
                    item(key = "favorites-header") {
                        SectionHeader(stringResource(R.string.favorites_section), leadingStar = true)
                    }
                    items(state.favorites, key = { "fav-${it.id}" }) { row ->
                        PresetCard(row = row, onClick = onPresetClick, onToggleFavorite = onToggleFavorite)
                    }
                }
                item(key = "presets-header") { SectionHeader(stringResource(R.string.presets_section)) }
                if (state.presets.isEmpty()) {
                    item(key = "empty") { EmptyHint() }
                } else {
                    items(state.presets, key = { it.id }) { row ->
                        PresetCard(row = row, onClick = onPresetClick, onToggleFavorite = onToggleFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    row: PresetRowUi,
    onClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val favoriteDescription =
        stringResource(if (row.isFavorite) R.string.cd_favorite_remove else R.string.cd_favorite_add)
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onClick(row.id) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            CarrierAvatar(name = row.carrier)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(row.carrier, style = MaterialTheme.typography.titleMedium)
                if (row.subtitle.isNotBlank()) {
                    Text(
                        text = row.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                row.lastAppliedLabel?.let { label ->
                    Text(
                        text = stringResource(R.string.last_applied, label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = { onToggleFavorite(row.id) }) {
                Icon(
                    imageVector = if (row.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = favoriteDescription,
                    tint =
                        if (row.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text(stringResource(R.string.search_hint)) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun RegionSelector(
    regions: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    TextButton(onClick = { expanded = true }) {
        Text(selected ?: stringResource(R.string.region_all))
        Icon(Icons.Filled.ArrowDropDown, contentDescription = stringResource(R.string.cd_select_region))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.region_all)) },
            onClick = {
                onSelect(null)
                expanded = false
            },
        )
        regions.forEach { region ->
            DropdownMenuItem(
                text = { Text(region) },
                onClick = {
                    onSelect(region)
                    expanded = false
                },
            )
        }
    }
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
private fun EmptyHint() {
    Text(
        text = stringResource(R.string.no_presets),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
    )
}
