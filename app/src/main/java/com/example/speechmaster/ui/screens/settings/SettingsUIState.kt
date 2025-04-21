package com.example.speechmaster.ui.screens.settings

import androidx.annotation.StringRes
import com.example.speechmaster.domain.settings.user.ThemeMode

data class SettingsUIState(
    // 加载与错误状态
    val isLoading: Boolean = true,
    @StringRes val errorMessageResId: Int? = null, // 用于显示短暂错误，如保存失败

    // 用户资料
    val username: String = "",
    val avatarUrl: String? = null, // DiceBear URL 或 null

    // 外观设置
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    // 通知设置
    val isReminderEnabled: Boolean = true,
    val reminderTime: String? = null, // 格式 "HH:mm" 或 null

    // API Key 覆盖设置
    val azureKey: String = "", // 用户输入的 Key，为空表示未设置
    val azureRegion: String = "", // 用户输入的 Region，为空表示未设置

    // 对话框可见性控制
    val showResetConfirmationDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showAvatarSelectionDialog: Boolean = false, // (为后续头像选择做准备)

    // 操作状态
    val isSavingProfile: Boolean = false, // (为后续保存用户资料做准备)
    val isResettingSettings: Boolean = false // (为后续重置设置做准备)
)