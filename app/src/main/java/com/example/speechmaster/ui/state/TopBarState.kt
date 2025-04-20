package com.example.speechmaster.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class TopBarState(
    val title: String = "",
    val showBackButton: Boolean = false,
    val showMenuButton: Boolean = true,
    val actions: List<TopBarAction> = emptyList()
){
    // 修改标题并返回新的实例
    fun changeTitle(newTitle: String): TopBarState {
        return this.copy(title = newTitle)
    }

    // 修改操作列表并返回新的实例
    fun changeAction(newActions: List<TopBarAction>): TopBarState {
        return this.copy(actions = newActions)
    }

    // 修改显示返回按钮选项
    fun showBack(show: Boolean): TopBarState {
        return this.copy(showBackButton = show)
    }

    // 修改显示菜单按钮选项
    fun showMenu(show: Boolean): TopBarState {
        return this.copy(showMenuButton = show)
    }
}

/**
 * Represents a single action item in the TopAppBar.
 *
 * @param icon The vector graphic for the action button.
 * @param contentDescription Accessibility description for the icon.
 * @param onClick The lambda function to execute when the action is clicked.
 */
@Immutable
data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
    // Add enabled: Boolean = true if needed
)

/**
 * Default initial state, potentially showing app name and menu.
 * Adjust as needed for your app's very first screen.
 */
val initialTopBarState = TopBarState(
    // title = "SpeechMaster", // title might be set later by Home screen default
    showBackButton = false,
    showMenuButton = true, // Assuming Home screen shows menu
    actions = emptyList()
)
