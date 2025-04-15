package com.example.speechmaster.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.speechmaster.R
import com.example.speechmaster.ui.viewmodels.TopBarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onNavigationClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TopBarViewModel = hiltViewModel(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val state by viewModel.state.collectAsState()

    CenterAlignedTopAppBar(
        title = { Text(text = state.title) },
        navigationIcon = {
            if (state.showBackButton) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            } else if (state.showMenuButton) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_menu)
                    )
                }
            }
        },
        actions = { state.actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
} 