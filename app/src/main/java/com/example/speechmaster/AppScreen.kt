package com.example.speechmaster

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.components.TopBar
import com.example.speechmaster.ui.navigation.AppDrawer
import com.example.speechmaster.ui.navigation.AppNav
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.navigation.UpdateTopBarState

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val topBarViewModel: TopBarViewModel = hiltViewModel()

    // 获取当前导航状态
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // 根据当前路由更新TopBar状态
    UpdateTopBarState(currentDestination, topBarViewModel, navController)
//    SetDefaultTopBarStateForRoute(currentDestination, topBarViewModel, navController)


    // 添加滚动行为
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopBar(
                    onNavigationClick = { navController.navigateUp() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    scrollBehavior = scrollBehavior,
                    viewModel = topBarViewModel
                )
            }
        ) { paddingValues ->
            AppNav(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                topBarViewModel = topBarViewModel
            )
        }
    }
}
