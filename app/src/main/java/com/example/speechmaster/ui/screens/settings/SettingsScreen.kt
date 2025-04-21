package com.example.speechmaster.ui.screens.settings


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.theme.AppTheme
import kotlinx.coroutines.launch

// --- 新增或确保存在的 imports ---
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew // 或者其他合适的图标
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.speechmaster.domain.settings.user.ThemeMode
import com.example.speechmaster.ui.components.settings.DropdownSettingItem
import androidx.compose.material3.rememberTimePickerState // 导入 TimePickerState
import androidx.compose.material3.TimePicker // 导入 TimePicker

import androidx.compose.animation.AnimatedVisibility // 用于条件显示时间设置
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.speechmaster.ui.components.settings.SwitchSettingItem // 导入 SwitchSettingItem
import com.example.speechmaster.ui.components.settings.ClickableSettingItem // 导入 ClickableSettingItem
import com.example.speechmaster.ui.components.settings.TextFieldSettingItem
import com.example.speechmaster.ui.components.settings.TimePickerDialog
import com.example.speechmaster.ui.components.settings.ResetSettingsConfirmationDialog
import timber.log.Timber
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 处理错误消息显示 (保持不变)
    LaunchedEffect(uiState.errorMessageResId) {
        uiState.errorMessageResId?.let { messageResId ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(messageResId),
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearErrorMessage()
        }
    }

    // --- 时间选择器状态和逻辑 ---
    val initialHour: Int
    val initialMinute: Int
    if (uiState.reminderTime != null && uiState.reminderTime!!.matches(Regex("\\d{2}:\\d{2}"))) {
        val parts = uiState.reminderTime!!.split(":")
        initialHour = parts[0].toIntOrNull() ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        initialMinute = parts[1].toIntOrNull() ?: Calendar.getInstance().get(Calendar.MINUTE)
    } else {
        // 设置默认时间，例如当前时间或上午9点
        val calendar = Calendar.getInstance()
        initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        initialMinute = calendar.get(Calendar.MINUTE)
    }
    // 使用 rememberTimePickerState，并用 uiState 中的时间初始化
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true // 或者根据需要设置为 false
    )
    // 当对话框需要显示时，更新 TimePickerState (防止重组时重置回旧时间)
    LaunchedEffect(uiState.showTimePickerDialog, uiState.reminderTime) {
        if (uiState.showTimePickerDialog) {
            if (uiState.reminderTime != null && uiState.reminderTime!!.matches(Regex("\\d{2}:\\d{2}"))) {
                val parts = uiState.reminderTime!!.split(":")
                timePickerState.hour = parts[0].toIntOrNull() ?: timePickerState.hour
                timePickerState.minute = parts[1].toIntOrNull() ?: timePickerState.minute
            } else {
                // 如果没有设置时间，可以选择重置为当前时间
                val calendar = Calendar.getInstance()
                timePickerState.hour = calendar.get(Calendar.HOUR_OF_DAY)
                timePickerState.minute = calendar.get(Calendar.MINUTE)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        if (uiState.isLoading) {
            LoadingView(Modifier.fillMaxSize().padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- 用户资料 Section ---
                item {
                    SettingSectionHeader(title = stringResource(R.string.profile))
                }
                // 头像显示和生成按钮
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp), // 调整垂直 padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 使用 Coil 加载头像
                        val imageUrl = uiState.avatarUrl

                        Timber.d("SettingsScreen: Passing URL to AsyncImage: $imageUrl")

                        AsyncImage(
                            model = imageUrl, // 从 ViewModel State 获取 URL
                            contentDescription = stringResource(R.string.user_avatar),
                            placeholder = painterResource(id = R.drawable.baseline_account_circle_24), // 你的默认头像资源
                            error = painterResource(id = R.drawable.error_baseline_account_circle_24),       // 加载错误时显示的头像
                            modifier = Modifier
                                .size(80.dp) // 稍微增大头像尺寸
                                .clip(CircleShape),
                            onLoading = { state ->
                                Timber.d("Coil: Loading image...")
                            },
                            onSuccess = { state ->
                                Timber.d("Coil: Successfully loaded image from ${state.result.dataSource}")
                            },
                            onError = { state ->
                                Timber.e(state.result.throwable, "Coil: Error loading image")
                            }

                        )

                        // 随机生成按钮，考虑加入加载状态
                        Button(
                            onClick = { viewModel.generateNewAvatar() },
                            enabled = !uiState.isSavingProfile // 当不在保存资料时可用
                        ) {
                            // 在按钮内部显示加载指示器或图标
                            if (uiState.isSavingProfile) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = LocalContentColor.current // 适配按钮颜色
                                )
                            } else {
                                Icon(Icons.Filled.Autorenew, contentDescription = null)
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.generate_new_avatar))
                            }
                        }
                    }
                }
                // 用户名编辑
                item {
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = { newValue ->
                            // 可以在这里添加一些基本的客户端验证，或者完全依赖ViewModel
                            viewModel.updateUsername(newValue)
                        },
                        label = { Text(stringResource(R.string.username)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp), // 底部加点间距
                        singleLine = true,
                        enabled = !uiState.isSavingProfile, // 保存时禁用
                        // 可以根据需要添加 isError 状态
                        // isError = uiState.errorMessageResId == R.string.error_username_cannot_be_empty
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(top = 8.dp)) }

                // --- 外观 Section (占位符) ---
                item {
                    SettingSectionHeader(title = stringResource(R.string.appearance))
                }
                item {
                    // 使用新的 DropdownSettingItem
                    val themeOptions = remember { // 使用 remember 避免每次重组都创建 Map
                        mapOf(
                            context.getString(R.string.theme_mode_system) to ThemeMode.SYSTEM,
                            context.getString(R.string.theme_mode_light) to ThemeMode.LIGHT,
                            context.getString(R.string.theme_mode_dark) to ThemeMode.DARK
                        )
                    }
                    DropdownSettingItem(
                        label = stringResource(R.string.theme),
                        optionsMap = themeOptions,
                        selectedValue = uiState.themeMode,
                        onValueSelected = { selectedMode -> viewModel.updateTheme(selectedMode) }
                        // modifier = Modifier.padding(vertical = 4.dp) // 可选：为整个条目添加垂直 padding
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(top = 8.dp)) }
                // --- 通知 Section ---
                item {
                    SettingSectionHeader(title = stringResource(R.string.notifications))
                }
                item {
                    // 使用 SwitchSettingItem
                    SwitchSettingItem(
                        title = stringResource(R.string.daily_reminder),
                        description = stringResource(R.string.daily_reminder_description),
                        checked = uiState.isReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderEnabled(it) }
                    )
                }
                item {
                    // 使用 AnimatedVisibility 控制时间设置的显示与隐藏
                    AnimatedVisibility(
                        visible = uiState.isReminderEnabled,
                        enter = slideInVertically { it / 2 } + fadeIn(),
                        exit = slideOutVertically { it / 2 } + fadeOut()
                    ) {
                        // 使用 ClickableSettingItem 显示时间并触发 TimePicker
                        ClickableSettingItem(
                            title = stringResource(R.string.reminder_time),
                            currentValue = uiState.reminderTime ?: stringResource(R.string.time_not_set),
                            onClick = { viewModel.showTimePicker() },
                            enabled = uiState.isReminderEnabled // 根据开关状态启用/禁用
                        )
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                // --- API 密钥 Section (占位符) ---
                item {
                    SettingSectionHeader(title = stringResource(R.string.api_keys_optional))
                }
                item {
                    // 说明文字
                    Text(
                        text = stringResource(R.string.api_keys_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp) // 说明文字和第一个输入框的间距
                    )
                }
                item {
                    // Azure Key 输入框
                    TextFieldSettingItem(
                        label = stringResource(R.string.azure_speech_key),
                        value = uiState.azureKey,
                        onValueChange = { viewModel.updateAzureKey(it) },
                        placeholder = stringResource(R.string.paste_your_key_here),
                        visualTransformation = PasswordVisualTransformation(), // 使用密码遮挡
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // 设置键盘类型
                        modifier = Modifier.padding(bottom = 8.dp) // 输入框之间的间距
                    )
                }
                item {
                    // Azure Region 输入框
                    TextFieldSettingItem(
                        label = stringResource(R.string.azure_speech_region),
                        value = uiState.azureRegion,
                        onValueChange = { viewModel.updateAzureRegion(it) },
                        placeholder = stringResource(R.string.enter_region_here),
                        // keyboardOptions 可以设置为 KeyboardType.Text
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }



                // --- 数据管理 Section (占位符) ---
                item {
                    SettingSectionHeader(title = stringResource(R.string.data_management))
                }
                item {
                    Button(onClick = { viewModel.showResetDialog() }) { // TODO: Step 8
                        Text(stringResource(R.string.reset_settings))
                    }
                    if (uiState.showResetConfirmationDialog) {
                        Text("确认重置对话框应在此显示") // 临时占位
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(top = 8.dp)) }



                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            // --- 时间选择器对话框 ---
            if (uiState.showTimePickerDialog) {
                TimePickerDialog(
                    onDismiss = { viewModel.hideTimePicker() },
                    onConfirm = {
                        viewModel.updateReminderTime(timePickerState.hour, timePickerState.minute)
                        // viewModel.hideTimePicker() // updateReminderTime 内部会调用 hide
                    }
                ) {
                    TimePicker(state = timePickerState)
                }
            }

            // --- 重置确认对话框 ---
            if (uiState.showResetConfirmationDialog) {
                ResetSettingsConfirmationDialog(
                    onConfirm = {
                        // 调用 ViewModel 执行重置，ViewModel 内部会隐藏对话框
                        viewModel.resetAllSettings()
                    },
                    onDismiss = { viewModel.hideResetDialog() } // 直接隐藏对话框
                )
            }
        }
    }
}

// SettingSectionHeader Composable (保持不变)
@Composable
fun SettingSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(top = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
    )
}

// 预览 (保持不变)
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(navController = rememberNavController())
    }
}