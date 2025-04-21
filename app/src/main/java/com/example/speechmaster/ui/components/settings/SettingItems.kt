package com.example.speechmaster.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role

/**
 * 一个通用的设置项布局，包含标题、可选描述和内容区域。
 */
@Composable
fun BaseSettingItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // 调整垂直内边距增加空间感
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 16.dp), // 给右侧内容留出空间
            verticalArrangement = Arrangement.Center
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                title()
            }
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                ProvideTextStyle(value = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    description()
                }
            }
        }
        Box { // 将内容放在 Box 中，方便对齐
            content()
        }
    }
}


/**
 * 下拉菜单设置项
 * @param labelResId 标签的字符串资源 ID
 * @param optionsMap 显示文本到实际值的映射 (例如 "亮色模式" -> ThemeMode.LIGHT)
 * @param selectedValue 当前选中的实际值
 * @param onValueSelected 当用户选择新值时调用的回调函数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSettingItem(
    label: String,
    optionsMap: Map<String, T>, // Map of Display String -> Actual Value
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null // 可选的描述文字
) {
    var expanded by remember { mutableStateOf(false) }
    // 找到当前选中值对应的显示文本
    val selectedDisplay = optionsMap.entries.find { it.value == selectedValue }?.key ?: ""
    val displayOptions = optionsMap.keys.toList()

    BaseSettingItem(
        modifier = modifier.clickable { expanded = true }, // 使整个条目可点击以展开
        title = { Text(label) },
        description = description?.let { { Text(it) } }
    ) {
        // 使用 ExposedDropdownMenuBox 实现下拉菜单
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.width(IntrinsicSize.Min) // 根据内容调整宽度
        ) {
            // 只读的 TextField 显示当前选项
            OutlinedTextField(
                value = selectedDisplay,
                onValueChange = {}, // 不需要改变，因为是 readOnly
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, // 图标也考虑 enabled
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    // 当 disabled 时，颜色会自动调整，但也可以自定义
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier.menuAnchor(PrimaryNotEditable, true), // *** 修正：仅使用 menuAnchor() ***
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            // 实际的下拉菜单
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                displayOptions.forEach { selectionDisplay ->
                    DropdownMenuItem(
                        text = { Text(selectionDisplay) },
                        onClick = {
                            val selectedActualValue = optionsMap[selectionDisplay]
                            if (selectedActualValue != null) {
                                onValueSelected(selectedActualValue) // 回调实际值
                            }
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

/**
 * 带 Switch 开关的设置项
 */
@Composable
fun SwitchSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true // 添加 enabled 参数
) {
    BaseSettingItem(
        modifier = modifier.clickable(
            enabled = enabled, // 根据 enabled 状态决定是否可点击
            onClick = { if (enabled) onCheckedChange(!checked) }, // 点击整个条目切换状态
            role = Role.Switch // 辅助功能角色
        ),
        title = { Text(title) },
        description = description?.let { { Text(it) } }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null, // 点击事件由 clickable Modifier 处理
            enabled = enabled // 控制 Switch 的可用状态
        )
    }
}

/**
 * 可点击的设置项，用于显示信息并触发操作（如打开对话框）
 */
@Composable
fun ClickableSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    currentValue: String, // 显示当前设置的值，例如时间
    onClick: () -> Unit,
    enabled: Boolean = true // 添加 enabled 参数
) {
    BaseSettingItem(
        modifier = modifier.clickable(
            enabled = enabled,
            onClick = onClick,
            role = Role.Button // 辅助功能角色
        ),
        title = { Text(title) },
        description = description?.let { { Text(it) } }
    ) {
        // 在右侧显示当前值
        Text(
            text = currentValue,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // 禁用时变灰
        )
        // 可以考虑在此处添加一个向右的箭头图标 (Trailing Icon)
        // Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}