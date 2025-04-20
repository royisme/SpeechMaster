package com.example.speechmaster.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R

@Composable
fun AppDrawer(
    navController: NavController,
    closeDrawer: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val drawerWidth = screenWidth * 0.75f  // 设置为屏幕宽度的3/4
    // 添加背景颜色和固定宽度
    ModalDrawerSheet(
        modifier = Modifier.width(drawerWidth),
        drawerContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        drawerContentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp)
        )
            HorizontalDivider()

            // 首页
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text(stringResource(id = R.string.daily_practice)) },
                selected = navController.currentDestination?.route == AppRoutes.HOME_ROUTE,
                onClick = {
                    navController.navigate(AppRoutes.HOME_ROUTE) {
                        popUpTo(AppRoutes.HOME_ROUTE) { inclusive = true }
                    }
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // 课程列表
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                label = { Text(stringResource(id = R.string.course_list)) },
                selected = navController.currentDestination?.route == AppRoutes.COURSES_ROUTE,
                onClick = {
                    navController.navigate(AppRoutes.COURSES_ROUTE) {
                        popUpTo(AppRoutes.HOME_ROUTE)
                    }
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            // 我的课程
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.LocalLibrary, contentDescription = null) },
                label = { Text(stringResource(id = R.string.my_courses)) },
                selected = navController.currentDestination?.route == AppRoutes.MY_COURSES_ROUTE,
                onClick = {
                    navController.navigate(AppRoutes.MY_COURSES_ROUTE) {
                        popUpTo(AppRoutes.HOME_ROUTE)
                    }
                    closeDrawer()
                }
            )
            // 练习历史
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                label = { Text(stringResource(id = R.string.practice_history)) },
                selected = navController.currentDestination?.route == AppRoutes.HISTORY_ROUTE,
                onClick = {
                    // 暂未实现
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // 设置
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(stringResource(id = R.string.settings)) },
                selected = navController.currentDestination?.route == AppRoutes.SETTINGS_ROUTE,
                onClick = {
                    // 暂未实现
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // 关于应用
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                label = { Text(stringResource(id = R.string.about)) },
                selected = navController.currentDestination?.route == AppRoutes.ABOUT_ROUTE,
                onClick = {
                    // 暂未实现
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

    }
}