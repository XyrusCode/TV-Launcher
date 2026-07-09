package xyruscode.tv.launcher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyruscode.tv.launcher.data.apps.AppRepository
import xyruscode.tv.launcher.data.jellyfin.JellyfinRepository
import xyruscode.tv.launcher.data.model.AppEntry
import xyruscode.tv.launcher.data.model.JellyfinHome
import xyruscode.tv.launcher.data.prefs.SettingsDataStore

data class HomeUiState(
    val loading: Boolean = true,
    val apps: List<AppEntry> = emptyList(),
    val jellyfin: JellyfinHome = JellyfinHome(),
    val jellyfinConnected: Boolean = false,
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val jellyfinRepository: JellyfinRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }

            val hidden = settings.hiddenPackages.first()
            val apps = appRepository.loadApps(hidden)

            val session = jellyfinRepository.session.first()
            val jellyfin = if (session != null) {
                runCatching { jellyfinRepository.loadHome(session) }.getOrDefault(JellyfinHome())
            } else {
                JellyfinHome()
            }

            _state.value = HomeUiState(
                loading = false,
                apps = apps,
                jellyfin = jellyfin,
                jellyfinConnected = session != null,
            )
        }
    }
}
