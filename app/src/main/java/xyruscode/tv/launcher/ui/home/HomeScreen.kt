@file:OptIn(ExperimentalTvMaterial3Api::class)

package xyruscode.tv.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import xyruscode.tv.launcher.R
import xyruscode.tv.launcher.data.model.AppEntry
import xyruscode.tv.launcher.data.model.MediaItem
import xyruscode.tv.launcher.ui.components.AppCard
import xyruscode.tv.launcher.ui.components.MediaCard
import xyruscode.tv.launcher.ui.components.RowSection

@Composable
fun HomeScreen(
    state: HomeUiState,
    onMediaClick: (MediaItem) -> Unit,
    onAppClick: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onConnectJellyfin: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { Header(onOpenSettings = onOpenSettings) }

        if (state.jellyfinConnected) {
            item {
                RowSection(
                    title = stringResource(R.string.row_continue_watching),
                    items = state.jellyfin.continueWatching,
                    key = { it.id },
                ) { MediaCard(it) { onMediaClick(it) } }
            }
            item {
                RowSection(
                    title = stringResource(R.string.row_next_up),
                    items = state.jellyfin.nextUp,
                    key = { it.id },
                ) { MediaCard(it) { onMediaClick(it) } }
            }
            item {
                RowSection(
                    title = stringResource(R.string.row_recently_added),
                    items = state.jellyfin.recentlyAdded,
                    key = { it.id },
                ) { MediaCard(it) { onMediaClick(it) } }
            }
            item {
                RowSection(
                    title = stringResource(R.string.row_libraries),
                    items = state.jellyfin.libraries,
                    key = { it.id },
                ) { MediaCard(it) { onMediaClick(it) } }
            }
        } else {
            item { ConnectJellyfinPrompt(onConnectJellyfin) }
        }

        item {
            RowSection(
                title = stringResource(R.string.row_apps),
                items = state.apps,
                key = { it.packageName },
            ) { AppCard(it) { onAppClick(it) } }
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.weight(1f))
        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.settings))
        }
    }
}

@Composable
private fun ConnectJellyfinPrompt(onConnectJellyfin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.jellyfin_not_connected),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.jellyfin_connect_prompt),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onConnectJellyfin) {
            Text(stringResource(R.string.setup_connect))
        }
    }
}
