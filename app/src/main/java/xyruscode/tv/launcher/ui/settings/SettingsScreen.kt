package xyruscode.tv.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyruscode.tv.launcher.R
import xyruscode.tv.launcher.appContainer

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onReconnect: () -> Unit,
    onChanged: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { context.appContainer().jellyfinRepository }
    val scope = rememberCoroutineScope()
    val session by repo.session.collectAsState(initial = null)

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.width(520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineSmall,
                )

                val status = session?.let { "Connected: ${it.serverUrl}" }
                    ?: stringResource(R.string.jellyfin_not_connected)
                Text(text = status, style = MaterialTheme.typography.bodyLarge)

                Button(onClick = onReconnect) {
                    val label = if (session != null) {
                        stringResource(R.string.setup_connect)
                    } else {
                        stringResource(R.string.jellyfin_connect_prompt)
                    }
                    Text(label)
                }

                if (session != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                repo.logout()
                                onChanged()
                            }
                        },
                    ) {
                        Text(stringResource(R.string.jellyfin_disconnect))
                    }
                }

                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    }
}
