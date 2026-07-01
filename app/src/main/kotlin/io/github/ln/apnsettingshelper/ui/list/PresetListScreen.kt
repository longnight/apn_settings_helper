package io.github.ln.apnsettingshelper.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.ln.apnsettingshelper.R
import io.github.ln.apnsettingshelper.ui.common.AppLanguages
import io.github.ln.apnsettingshelper.ui.common.CarrierAvatar
import kotlinx.coroutines.launch

/** VM-wired preset list. */
@Composable
fun PresetListScreen(
    onPresetClick: (String) -> Unit,
    currentLanguageTag: String?,
    onLanguageChange: (String?) -> Unit,
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
        currentLanguageTag = currentLanguageTag,
        onLanguageChange = onLanguageChange,
        modifier = modifier,
    )
}

/**
 * Stateless list content. The app-bar translate action opens a right-side language drawer; the
 * region filter sits at the head of the main preset list (where the old "All presets" header was).
 */
@Composable
fun PresetListContent(
    state: PresetListUiState,
    onPresetClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQueryChange: (String) -> Unit = {},
    onRegionChange: (String?) -> Unit = {},
    currentLanguageTag: String? = null,
    onLanguageChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current

    // Anchor the drawer to the physical right: force the drawer container to RTL so its start edge
    // is the right edge, then restore the real layout direction for the sheet + content so neither
    // is mirrored. In an RTL UI (Arabic) start is already the right edge, so this is a no-op there.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier = modifier,
            // Open via the app-bar action only; allow swipe-to-close once open. This keeps the
            // right edge free for the system back gesture.
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    LanguageDrawerSheet(
                        currentTag = currentLanguageTag,
                        onSelect = { tag ->
                            scope.launch { drawerState.close() }
                            onLanguageChange(tag)
                        },
                    )
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                PresetListScaffold(
                    state = state,
                    onPresetClick = onPresetClick,
                    onToggleFavorite = onToggleFavorite,
                    onQueryChange = onQueryChange,
                    onRegionChange = onRegionChange,
                    onOpenLanguages = { scope.launch { drawerState.open() } },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetListScaffold(
    state: PresetListUiState,
    onPresetClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onRegionChange: (String?) -> Unit,
    onOpenLanguages: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenLanguages) {
                        Icon(
                            painter = painterResource(R.drawable.ic_translate),
                            contentDescription = stringResource(R.string.cd_language),
                        )
                    }
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
                item(key = "region-filter") {
                    RegionSelector(
                        regions = state.regions,
                        selected = state.selectedRegion,
                        onSelect = onRegionChange,
                    )
                }
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
    Box(modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
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
}

/** Right-side drawer that lists every UI language (README parity) plus a system-default choice. */
@Composable
private fun LanguageDrawerSheet(
    currentTag: String?,
    onSelect: (String?) -> Unit,
) {
    // Take a fraction of the width so a scrim strip always stays visible — it reads as a right-side
    // panel, not a full-screen takeover (the AVD/phone width varies).
    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.82f)) {
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.language_system_default)) },
                selected = currentTag == null,
                onClick = { onSelect(null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
            AppLanguages.supported.forEach { language ->
                NavigationDrawerItem(
                    label = { Text(language.endonym) },
                    selected = currentTag == language.tag,
                    onClick = { onSelect(language.tag) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
            }
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
