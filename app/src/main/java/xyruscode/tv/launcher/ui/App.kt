package xyruscode.tv.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import xyruscode.tv.launcher.appContainer
import xyruscode.tv.launcher.ui.home.HomeScreen
import xyruscode.tv.launcher.ui.home.HomeViewModel
import xyruscode.tv.launcher.ui.settings.SettingsScreen
import xyruscode.tv.launcher.ui.setup.SetupScreen
import xyruscode.tv.launcher.util.Launchers

private enum class Screen { Home, Setup, Settings }

@Composable
fun App() {
    val context = LocalContext.current
    val container = remember { context.appContainer() }
    var screen by remember { mutableStateOf(Screen.Home) }

    val homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    appRepository = container.appRepository,
                    jellyfinRepository = container.jellyfinRepository,
                    settings = container.settings,
                )
            }
        },
    )
    val state by homeViewModel.state.collectAsState()

    // On the home screen, Back is a no-op (this is the launcher). Elsewhere it returns home.
    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    when (screen) {
        Screen.Home -> HomeScreen(
            state = state,
            onMediaClick = { Launchers.playJellyfinItem(context, it.id) },
            onAppClick = { Launchers.launchApp(context, it) },
            onOpenSettings = { screen = Screen.Settings },
            onConnectJellyfin = { screen = Screen.Setup },
        )

        Screen.Setup -> SetupScreen(
            onConnected = {
                screen = Screen.Home
                homeViewModel.refresh()
            },
            onSkip = { screen = Screen.Home },
        )

        Screen.Settings -> SettingsScreen(
            onBack = { screen = Screen.Home },
            onReconnect = { screen = Screen.Setup },
            onChanged = { homeViewModel.refresh() },
        )
    }
}
