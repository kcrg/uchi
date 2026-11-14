package eu.tryniecki.uchi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eu.tryniecki.uchi.UchiApp
import eu.tryniecki.uchi.data.apps.AppRepository
import eu.tryniecki.uchi.data.apps.LaunchableApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val apps: List<LaunchableApp>) : HomeUiState
}

class HomeViewModel(val repository: AppRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.apps
        .map { apps -> if (apps == null) HomeUiState.Loading else HomeUiState.Ready(apps) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as UchiApp
                HomeViewModel(app.container.appRepository)
            }
        }
    }
}
