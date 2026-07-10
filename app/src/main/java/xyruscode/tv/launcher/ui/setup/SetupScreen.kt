package xyruscode.tv.launcher.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyruscode.tv.launcher.R
import xyruscode.tv.launcher.appContainer
import xyruscode.tv.launcher.data.jellyfin.JellyfinRepository
import xyruscode.tv.launcher.data.model.DiscoveredServer
import xyruscode.tv.launcher.data.model.QuickConnectStart

/** Where the setup flow currently is. */
private sealed interface Stage {
    data object Discovering : Stage
    data class Pick(val servers: List<DiscoveredServer>) : Stage
    data object Manual : Stage
    data class Login(val serverUrl: String, val serverName: String?) : Stage
}

@Composable
fun SetupScreen(
    onConnected: () -> Unit,
    onSkip: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { context.appContainer().jellyfinRepository }

    var stage by remember { mutableStateOf<Stage>(Stage.Discovering) }
    // Remembered so "Back" from a login can return to the discovered list.
    var discovered by remember { mutableStateOf<List<DiscoveredServer>>(emptyList()) }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (val s = stage) {
                    is Stage.Discovering -> DiscoveringView(repo) { servers ->
                        discovered = servers
                        stage = if (servers.isEmpty()) Stage.Manual else Stage.Pick(servers)
                    }

                    is Stage.Pick -> PickServerView(
                        servers = s.servers,
                        onSelect = { stage = Stage.Login(it.address, it.name) },
                        onManual = { stage = Stage.Manual },
                        onSearchAgain = { stage = Stage.Discovering },
                        onSkip = onSkip,
                    )

                    is Stage.Manual -> ManualEntryView(
                        onContinue = { stage = Stage.Login(it, null) },
                        onSearchAgain = { stage = Stage.Discovering },
                        onSkip = onSkip,
                    )

                    is Stage.Login -> LoginView(
                        repo = repo,
                        serverUrl = s.serverUrl,
                        serverName = s.serverName,
                        onConnected = onConnected,
                        onBack = {
                            stage = if (discovered.isEmpty()) Stage.Manual else Stage.Pick(discovered)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveringView(
    repo: JellyfinRepository,
    onDone: (List<DiscoveredServer>) -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        onDone(repo.discoverServers())
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.width(24.dp), strokeWidth = 2.dp)
        Text(stringResource(R.string.setup_searching), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun PickServerView(
    servers: List<DiscoveredServer>,
    onSelect: (DiscoveredServer) -> Unit,
    onManual: () -> Unit,
    onSearchAgain: () -> Unit,
    onSkip: () -> Unit,
) {
    Text(stringResource(R.string.setup_pick_server), style = MaterialTheme.typography.headlineSmall)
    servers.forEach { server ->
        Button(onClick = { onSelect(server) }, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(server.name, style = MaterialTheme.typography.titleMedium)
                Text(server.address, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(onClick = onManual) { Text(stringResource(R.string.setup_manual_entry)) }
        OutlinedButton(onClick = onSearchAgain) { Text(stringResource(R.string.setup_search_again)) }
        TextButton(onClick = onSkip) { Text(stringResource(R.string.setup_skip)) }
    }
}

@Composable
private fun ManualEntryView(
    onContinue: (String) -> Unit,
    onSearchAgain: () -> Unit,
    onSkip: () -> Unit,
) {
    var server by remember { mutableStateOf("") }
    Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.setup_no_servers), style = MaterialTheme.typography.bodyMedium)
    OutlinedTextField(
        value = server,
        onValueChange = { server = it },
        label = { Text(stringResource(R.string.setup_server)) },
        placeholder = { Text(stringResource(R.string.setup_server_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { onContinue(server) }, enabled = server.isNotBlank()) {
            Text(stringResource(R.string.setup_continue))
        }
        OutlinedButton(onClick = onSearchAgain) { Text(stringResource(R.string.setup_search_again)) }
        TextButton(onClick = onSkip) { Text(stringResource(R.string.setup_skip)) }
    }
}

/** Chosen server → Quick Connect first, dropping to username/password if it's unavailable. */
@Composable
private fun LoginView(
    repo: JellyfinRepository,
    serverUrl: String,
    serverName: String?,
    onConnected: () -> Unit,
    onBack: () -> Unit,
) {
    // Trying → (QuickConnect | Password); reset whenever the target server changes.
    var quickConnect by remember(serverUrl) { mutableStateOf<QuickConnectStart?>(null) }
    var usePassword by remember(serverUrl) { mutableStateOf(false) }
    var triedQuickConnect by remember(serverUrl) { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(serverUrl) {
        val result = repo.startQuickConnect(serverUrl)
        triedQuickConnect = true
        if (result.isSuccess) quickConnect = result.getOrNull() else usePassword = true
    }

    Text(
        text = serverName ?: serverUrl,
        style = MaterialTheme.typography.headlineSmall,
    )

    when {
        !triedQuickConnect && !usePassword -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.width(24.dp), strokeWidth = 2.dp)
                Text(stringResource(R.string.setup_connecting))
            }
        }

        !usePassword && quickConnect != null ->
            QuickConnectView(
                repo = repo,
                serverUrl = serverUrl,
                start = quickConnect!!,
                onConnected = onConnected,
                onUsePassword = { usePassword = true },
                onBack = onBack,
            )

        else -> PasswordView(repo, serverUrl, onConnected, onBack)
    }
}

@Composable
private fun QuickConnectView(
    repo: JellyfinRepository,
    serverUrl: String,
    start: QuickConnectStart,
    onConnected: () -> Unit,
    onUsePassword: () -> Unit,
    onBack: () -> Unit,
) {
    // Poll until the user approves the code from another Jellyfin client, then exchange it.
    androidx.compose.runtime.LaunchedEffect(start.secret) {
        while (true) {
            delay(3000)
            val approved = repo.pollQuickConnect(serverUrl, start.secret).getOrDefault(false)
            if (approved) {
                if (repo.finishQuickConnect(serverUrl, start.secret).isSuccess) {
                    onConnected()
                    return@LaunchedEffect
                }
            }
        }
    }

    Text(stringResource(R.string.setup_qc_title), style = MaterialTheme.typography.titleLarge)
    Text(stringResource(R.string.setup_qc_instructions))
    Text(
        text = start.code,
        fontFamily = FontFamily.Monospace,
        fontSize = 48.sp,
        color = MaterialTheme.colorScheme.primary,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.width(18.dp), strokeWidth = 2.dp)
        Text(stringResource(R.string.setup_qc_waiting), style = MaterialTheme.typography.bodyMedium)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(onClick = onUsePassword) { Text(stringResource(R.string.setup_use_password)) }
        TextButton(onClick = onBack) { Text(stringResource(R.string.setup_back)) }
    }
}

@Composable
private fun PasswordView(
    repo: JellyfinRepository,
    serverUrl: String,
    onConnected: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = username,
        onValueChange = { username = it; error = false },
        label = { Text(stringResource(R.string.setup_username)) },
        singleLine = true,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it; error = false },
        label = { Text(stringResource(R.string.setup_password)) },
        singleLine = true,
        enabled = !loading,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
    if (error) {
        Text(stringResource(R.string.setup_error), color = MaterialTheme.colorScheme.error)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    error = false
                    val result = repo.authenticate(serverUrl, username, password)
                    loading = false
                    if (result.isSuccess) onConnected() else error = true
                }
            },
            enabled = !loading && username.isNotBlank(),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.setup_connect))
            }
        }
        TextButton(onClick = onBack, enabled = !loading) { Text(stringResource(R.string.setup_back)) }
    }
}
