package com.example.speechmaster.ui.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.screens.home.HomeScreen
import com.example.speechmaster.AppRouteList.HOME_ROUTE

// 定义应用中的路由

@Composable
fun AppNav(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){

        composable(HOME_ROUTE){
            HomeScreen(navController = navController)
        }
    }
}

