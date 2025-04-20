package com.example.speechmaster.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use AutoMirrored
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar // Or TopAppBar based on preference
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // For default descriptions
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onNavigationClick: () -> Unit, // Typically navController.navigateUp()
    onMenuClick: () -> Unit,       // Typically scope.launch { drawerState.open() }
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    viewModel: TopBarViewModel // Passed shared instance
) {
    // Collect the combined state (base or override)
    val state by viewModel.state.collectAsState()

    CenterAlignedTopAppBar( // Or TopAppBar if alignment preference changes
        title = { Text(text = state.title) },
        navigationIcon = {
            // Determine navigation icon based on state flags
            when {
                state.showBackButton -> {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Use AutoMirrored
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
                state.showMenuButton -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.open_menu)
                        )
                    }
                }
                // Add else block or handle case where neither is true if necessary
            }
        },
        actions = {
            // Iterate through the list of actions defined in the state
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(), // Adjust colors if needed
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}