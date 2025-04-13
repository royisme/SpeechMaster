package com.example.speechmaster.ui.layouts

import androidx.annotation.StringRes
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
import com.example.speechmaster.AppRouteList
import com.example.speechmaster.R

@Composable
fun AppDrawer(
    navController: NavController,
    closeDrawer: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val drawerWidth = screenWidth * 0.75f  // 设置为屏幕宽度的3/4
    // 添加背景颜色和固定宽度
    Surface(
        modifier = Modifier
            .width(drawerWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.secondaryContainer,  // 根据主题的表面颜色
        tonalElevation = 1.dp  // 添加轻微阴影效果区分层次
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
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
                selected = navController.currentDestination?.route == AppRouteList.HOME_ROUTE,
                onClick = {
                    navController.navigate(AppRouteList.HOME_ROUTE) {
                        popUpTo(AppRouteList.HOME_ROUTE) { inclusive = true }
                    }
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // 课程列表
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                label = { Text(stringResource(id = R.string.course_list)) },
                selected = navController.currentDestination?.route == AppRouteList.COURSES_ROUTE,
                onClick = {
                    navController.navigate(AppRouteList.COURSES_ROUTE) {
                        popUpTo(AppRouteList.HOME_ROUTE)
                    }
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // 练习历史
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                label = { Text(stringResource(id = R.string.practice_history)) },
                selected = navController.currentDestination?.route == AppRouteList.HISTORY_ROUTE,
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
                selected = navController.currentDestination?.route == AppRouteList.SETTINGS_ROUTE,
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
                selected = navController.currentDestination?.route == AppRouteList.ABOUT_ROUTE,
                onClick = {
                    // 暂未实现
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}