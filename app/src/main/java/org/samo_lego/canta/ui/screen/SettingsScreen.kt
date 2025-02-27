package org.samo_lego.canta.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.samo_lego.canta.BuildConfig
import org.samo_lego.canta.R
import org.samo_lego.canta.ui.component.IconClickButton
import org.samo_lego.canta.ui.component.SettingsItem
import org.samo_lego.canta.ui.viewmodel.SettingsViewModel
import org.samo_lego.canta.util.BLOAT_URL
import org.samo_lego.canta.util.SettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsStore = remember { SettingsStore(context) }

    var latestCommitHash = remember { mutableStateOf("") }

    // Fetch the latest commit hash
    LaunchedEffect(Unit) {
        latestCommitHash.value = settingsStore.getLatestCommitHash()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconClickButton(
                        onClick = onNavigateBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Auto-update bloat list
            SettingsItem(
                title = stringResource(R.string.auto_update_bloat_list),
                description = stringResource(R.string.auto_update_bloat_list_description),
                icon = Icons.Default.Update,
                isSwitch = true,
                checked = settingsViewModel.autoUpdateBloatList,
                onCheckedChange = {
                    settingsViewModel.autoUpdateBloatList = it
                    coroutineScope.launch {
                        settingsViewModel.saveAutoUpdateBloatList(settingsStore)
                    }
                }
            )

            // Confirm before uninstall
            SettingsItem(
                title = stringResource(R.string.confirm_uninstall),
                description = stringResource(R.string.confirm_uninstall_description),
                icon = Icons.Default.Delete,
                isSwitch = true,
                checked = settingsViewModel.confirmBeforeUninstall,
                onCheckedChange = {
                    settingsViewModel.confirmBeforeUninstall = it
                    coroutineScope.launch {
                        settingsViewModel.saveConfirmBeforeUninstall(settingsStore)
                    }
                }
            )


            // Spacer to push footer to bottom
            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            // App info footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Latest commit hash (clickable to go to repository)
                if (latestCommitHash.value.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.bloatware_list_version),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = latestCommitHash.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(BLOAT_URL))
                            context.startActivity(browserIntent)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // App version
                Text(
                    text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // App homepage
                Text(
                    text = "https://samolego.github.io/Canta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://samolego.github.io/Canta"))
                        context.startActivity(browserIntent)
                    }
                )
            }
        }
    }
}
