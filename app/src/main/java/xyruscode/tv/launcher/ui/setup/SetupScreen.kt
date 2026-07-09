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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyruscode.tv.launcher.R
import xyruscode.tv.launcher.appContainer

@Composable
fun SetupScreen(
    onConnected: () -> Unit,
    onSkip: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { context.appContainer().jellyfinRepository }
    val scope = rememberCoroutineScope()

    var server by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    val errorText = stringResource(R.string.setup_error)

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
                    text = stringResource(R.string.setup_title),
                    style = MaterialTheme.typography.headlineSmall,
                )

                OutlinedTextField(
                    value = server,
                    onValueChange = { server = it; error = false },
                    label = { Text(stringResource(R.string.setup_server)) },
                    placeholder = { Text(stringResource(R.string.setup_server_hint)) },
                    singleLine = true,
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
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
                    Text(text = errorText, color = MaterialTheme.colorScheme.error)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedButton(onClick = onSkip, enabled = !loading) {
                        Text(stringResource(R.string.setup_skip))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                error = false
                                val result = repo.authenticate(server, username, password)
                                loading = false
                                if (result.isSuccess) onConnected() else error = true
                            }
                        },
                        enabled = !loading && server.isNotBlank() && username.isNotBlank(),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.setup_connect))
                        }
                    }
                }
            }
        }
    }
}
