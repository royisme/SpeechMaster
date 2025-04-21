package com.example.speechmaster.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager // 用于清除焦点
import androidx.compose.ui.text.input.VisualTransformation // 用于密码遮挡
import androidx.compose.ui.unit.dp
/**
 * 文本输入设置项
 */
@Composable
fun TextFieldSettingItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    description: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None, // 用于密码遮挡
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    singleLine: Boolean = true // 默认单行
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(vertical = 8.dp)) { // 使用 Column 包含 Label 和 TextField
        // 标签
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // 可选描述
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // 输入框
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions.copy(
                // 可以根据需要设置 imeAction，例如 Done 清除焦点
                // imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() } // 点击 Done 键盘按钮时清除焦点
            ),
            enabled = enabled,
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors( // 可选：自定义颜色
                // unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}