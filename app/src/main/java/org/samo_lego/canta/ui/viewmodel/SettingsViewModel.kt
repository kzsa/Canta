package org.samo_lego.canta.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.samo_lego.canta.util.SettingsStore

class SettingsViewModel : ViewModel() {

    // Settings values
    var autoUpdateBloatList by mutableStateOf(true)
    var confirmBeforeUninstall by mutableStateOf(true)
    var latestCommitHash by mutableStateOf("")

    fun loadSettings(settingsStore: SettingsStore) {
        viewModelScope.launch {
            // Collect auto update bloat list
            settingsStore.autoUpdateBloatListFlow.collect {
                autoUpdateBloatList = it
            }
        }

        viewModelScope.launch {
            // Collect confirm before uninstall
            settingsStore.confirmBeforeUninstallFlow.collect {
                confirmBeforeUninstall = it
            }
        }

        viewModelScope.launch {
            // Collect latest commit hash
            settingsStore.latestCommitHashFlow.collect {
                latestCommitHash = it
            }
        }
    }

    fun saveAutoUpdateBloatList(settingsStore: SettingsStore) {
        viewModelScope.launch {
            settingsStore.setAutoUpdateBloatList(autoUpdateBloatList)
        }
    }

    fun saveConfirmBeforeUninstall(settingsStore: SettingsStore) {
        viewModelScope.launch {
            settingsStore.setConfirmBeforeUninstall(confirmBeforeUninstall)
        }
    }

    fun saveSettings(store: SettingsStore) {
        saveAutoUpdateBloatList(store)
        saveConfirmBeforeUninstall(store)
    }
}
