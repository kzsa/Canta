package org.samo_lego.canta.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.samo_lego.canta.APP_NAME
import org.samo_lego.canta.ui.component.AppList
import org.samo_lego.canta.ui.viewmodel.AppListViewModel
import org.samo_lego.canta.ui.viewmodel.ShizukuViewModel
import org.samo_lego.canta.util.Filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CantaApp(
    launchShizuku: () -> Unit,
    uninstallApp: (String) -> Boolean,
    reinstallApp: (String) -> Boolean,
) {
    var selectedAppsType by remember { mutableStateOf(AppsType.INSTALLED) }
    val shizukuModel = viewModel<ShizukuViewModel>()
    val appListViewModel = viewModel<AppListViewModel>()
    var showMoreOptionsPanel by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = APP_NAME) },
                actions = {
                    IconButton(
                        onClick = { showMoreOptionsPanel = !showMoreOptionsPanel },
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    MoreOptionsMenu(
                        showMoreOptionsPanel = showMoreOptionsPanel,
                        onDismiss = { showMoreOptionsPanel = false },
                    )
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            // Add floating action button
            FloatingActionButton(
                containerColor = when (selectedAppsType) {
                    AppsType.INSTALLED -> MaterialTheme.colorScheme.errorContainer
                    AppsType.UNINSTALLED -> MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.padding(16.dp),
                onClick = {
                    coroutineScope.launch {
                        // Check shizuku permission
                        val permission = shizukuModel.checkShizukuPermission()
                        println("Shizuku permission: $permission")

                        if (!permission) {
                            Toast.makeText(
                                context,
                                "Please start Shizuku and authorise Canta.",
                                Toast.LENGTH_SHORT
                            ).show()
                            launchShizuku()
                        } else {
                            // Proceed with the action
                            when (selectedAppsType) {
                                AppsType.INSTALLED -> {
                                    launchShizuku()
                                    appListViewModel.selectedAppsForRemoval.forEach {
                                        val uninstalled = uninstallApp(it.key)
                                        if (uninstalled) {
                                            appListViewModel.changeAppStatus(it.key)
                                        }
                                    }
                                }

                                AppsType.UNINSTALLED -> {
                                    appListViewModel.selectedAppsForRemoval.forEach {
                                        val installed = reinstallApp(it.key)
                                        if (installed) {
                                            appListViewModel.changeAppStatus(it.key)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            ) {
                when (selectedAppsType) {
                    AppsType.INSTALLED -> Icon(
                        Icons.Default.Delete,
                        contentDescription = "Uninstall"
                    )

                    AppsType.UNINSTALLED -> Icon(
                        Icons.Default.InstallMobile,
                        contentDescription = "ReInstall"
                    )
                }
            }

        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TabRow(
                selectedTabIndex = selectedAppsType.ordinal,
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Tab(
                    selected = selectedAppsType == AppsType.INSTALLED,
                    onClick = {
                        selectedAppsType = AppsType.INSTALLED
                        appListViewModel.showUninstalled = false
                        appListViewModel.selectedFilter = Filter.any
                    },
                    icon = {
                        Icon(
                            Icons.Default.AutoDelete,
                            contentDescription = AppsType.INSTALLED.toString()
                        )
                    },
                )
                Tab(
                    selected = selectedAppsType == AppsType.UNINSTALLED,
                    onClick = {
                        selectedAppsType = AppsType.UNINSTALLED
                        appListViewModel.showUninstalled = true
                        appListViewModel.selectedFilter = Filter.any
                    },
                    icon = {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = AppsType.UNINSTALLED.toString()
                        )
                    },
                )
            }
            AppList()
        }
    }
}

@Composable
fun MoreOptionsMenu(
    showMoreOptionsPanel: Boolean,
    onDismiss: () -> Unit,
) {
    val appListViewModel = viewModel<AppListViewModel>()
    DropdownMenu(
        expanded = showMoreOptionsPanel,
        onDismissRequest = onDismiss,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Only system")
            Checkbox(
                checked = appListViewModel.showSystem,
                onCheckedChange = {
                    appListViewModel.showSystem = it
                },
            )
        }
        // Filters
        var filtersMenu by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { filtersMenu = !filtersMenu }
                .padding(8.dp)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(appListViewModel.selectedFilter.name)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Filters")
        }

        if (filtersMenu) {
            Filter.availableFilters.forEach { filter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            appListViewModel.selectedFilter = filter
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(filter.name)
                }
            }
        }
    }
}

enum class AppsType {
    INSTALLED,
    UNINSTALLED,
}