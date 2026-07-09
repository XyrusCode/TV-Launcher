@file:OptIn(ExperimentalTvMaterial3Api::class)

package xyruscode.tv.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import xyruscode.tv.launcher.data.model.AppEntry
import xyruscode.tv.launcher.data.model.MediaItem

/** Poster tile (2:3) for a Jellyfin item. */
@Composable
fun MediaCard(item: MediaItem, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(150.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(150.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

/** Banner tile (16:9) for an installed app. Uses the leanback banner, falling back to the icon. */
@Composable
fun AppCard(entry: AppEntry, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(200.dp)) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            val image = entry.banner ?: entry.icon
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = entry.label,
                    contentScale = if (entry.banner != null) ContentScale.Crop else ContentScale.Fit,
                    modifier = if (entry.banner != null) Modifier.fillMaxSize() else Modifier.padding(24.dp),
                )
            } else {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(200.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}
