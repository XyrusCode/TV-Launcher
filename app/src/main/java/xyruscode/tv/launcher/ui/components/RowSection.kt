@file:OptIn(ExperimentalTvMaterial3Api::class)

package xyruscode.tv.launcher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

private val EDGE = 48.dp

/** A titled horizontal rail. Renders nothing when [items] is empty. */
@Composable
fun <T> RowSection(
    title: String,
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = EDGE, bottom = 10.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = EDGE),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = items, key = { key(it) }) { itemContent(it) }
        }
    }
}
