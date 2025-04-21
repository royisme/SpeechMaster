package com.example.speechmaster.ui.screens.settings

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.data.model.User
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.domain.settings.AppSettingsManager
import com.example.speechmaster.domain.settings.user.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest.e
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsManager: AppSettingsManager,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState(isLoading = true))
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }
    // --- 内部辅助数据类 ---
    // 用户偏好设置的中间结果
    private data class UserPreferencesData(
        val theme: ThemeMode,
        val reminderEnabled: Boolean,
        val reminderTime: String?
    )
    // API Key 设置的中间结果
    private data class ApiKeyData(
        val azureKey: String?,
        val azureRegion: String?
    )
    private fun loadInitialState() {
        viewModelScope.launch {
            // 1. 准备基础 Flow
            val userFlow = userSessionManager.currentUserFlow.filterNotNull() // Flow<User>

            // 2. 组合用户偏好设置 Flow
            val preferencesFlow = combine(
                appSettingsManager.getUserSettings().getThemeMode(),          // Flow<ThemeMode>
                appSettingsManager.getUserSettings().getNotificationEnabled(),// Flow<Boolean>
                appSettingsManager.getUserSettings().getDailyReminderTime()   // Flow<String?>
            ) { theme, reminderEnabled, reminderTime ->
                UserPreferencesData(theme, reminderEnabled, reminderTime)
            } // : Flow<UserPreferencesData>

            // 3. 组合 API Key 设置 Flow
            val apiKeyFlow = combine(
                appSettingsManager.getUserSettings().getAzureKey(),          // Flow<String?>
                appSettingsManager.getUserSettings().getAzureRegion()        // Flow<String?>
            ) { azureKey, azureRegion ->
                ApiKeyData(azureKey, azureRegion)
            } // : Flow<ApiKeyData>

            // 4. 最终组合这三组结果 (userFlow, preferencesFlow, apiKeyFlow)
            //    这次只需要 combine 3 个 Flow，旧版本库也支持

            val finalCombinedFlow = combine(
                userFlow,          // Flow<User>
                preferencesFlow,   // Flow<UserPreferencesData>
                apiKeyFlow         // Flow<ApiKeyData>
            ) { user, prefs, apiKeys ->
                // 使用这三个部分的最新值来构建完整的 UI State
                // 注意：这里我们直接更新整个 state，如果需要保留之前的非数据字段（如对话框状态），
                // 最好是在 collectLatest 中使用 _uiState.update { it.copy(...) }
                Timber.d("SettingsViewModel: collectLatest received loadedData.avatarUrl: ${user.avatarUrl}")

                SettingsUIState(
                    isLoading = false,
                    username = user.username,
                    avatarUrl = user.avatarUrl,
                    themeMode = prefs.theme,
                    isReminderEnabled = prefs.reminderEnabled,
                    reminderTime = prefs.reminderTime,
                    azureKey = apiKeys.azureKey ?: "",
                    azureRegion = apiKeys.azureRegion ?: "",
                    errorMessageResId = null // 清除加载错误
                    // 保留其他状态字段的默认值或当前值
                )
            }
                .catch { e -> // 错误处理
                    Timber.e(e, "Error loading settings state")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageResId = R.string.error_loading_settings
                        )
                    }
                }

            // 5. 收集最终结果并更新 UI State
            finalCombinedFlow.collectLatest { newState ->
                _uiState.value = newState // 或者使用 update { currentState -> newState.copy(...) } 来保留对话框状态等
                Timber.d("SettingsViewModel SettingsUiState updated (grouped): User=${newState.username}," +
                        " Avatar=${newState.avatarUrl}, Theme=${newState.themeMode}")
            }
            Timber.d("SettingsViewModel: _uiState updated. New _uiState.avatarUrl: ${_uiState.value.avatarUrl}")

        }
    }


    // --- 更新函数 (将在后续步骤中实现具体逻辑) ---

    fun updateUsername(newName: String) {
        // TODO: 验证 newName, 调用 userSessionManager.updateProfile(...)
        // 在成功回调或协程完成后更新 _uiState.username (或依赖 UserSessionManager 的 Flow 自动更新)
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true) } // 显示加载状态
            val result = userSessionManager.updateProfile(
                newUsername = newName.trim()
            )
            if (!result.isSuccess) {
                _uiState.update { it.copy(isSavingProfile = false, errorMessageResId = R.string.error_updating_profile) }
                Timber.e(result.exceptionOrNull(), "Failed to update username")
            } else {
                // 不需要手动更新 _uiState.username， 因为 userSessionManager.currentUserFlow 会发出新值
                _uiState.update { it.copy(isSavingProfile = false) } // 清除加载状态
            }
        }
    }


    fun generateNewAvatar() {
        viewModelScope.launch {
            try {
                val style = "bottts" // 或者其他你选定的样式
                val seed = UUID.randomUUID().toString()
                val newAvatarUrl = "https://api.dicebear.com/9.x/$style/webp?seed=$seed"
                _uiState.update { it.copy(isSavingProfile = true) } // 显示加载状态
                val result = userSessionManager.updateProfile(newAvatarUrl = newAvatarUrl)
                if (!result.isSuccess) {
                    _uiState.update { it.copy(isSavingProfile = false, errorMessageResId = R.string.error_updating_profile) }
                    Timber.e(result.exceptionOrNull(), "Failed to update avatar URL")
                } else {
                    // 不需要手动更新 _uiState.avatarUrl， 依赖 userSessionManager.currentUserFlow
                    _uiState.update { it.copy(isSavingProfile = false) }
                    Timber.d("Avatar URL updated to: $newAvatarUrl")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingProfile = false, errorMessageResId = R.string.error_updating_profile) }
                Timber.e(e, "Error generating new avatar")
            }
        }
    }

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            appSettingsManager.getUserSettings().setThemeMode(mode)
            // UI State 会通过 combine 自动更新
        }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsManager.getUserSettings().setNotificationEnabled(enabled)
            // 如果关闭提醒，同时清除提醒时间
            if (!enabled) {
                appSettingsManager.getUserSettings().setDailyReminderTime(null)
            }
            // UI State 会通过 combine 自动更新
        }
    }

    @SuppressLint("DefaultLocale")
    fun updateReminderTime(hour: Int, minute: Int) {
        // 将小时和分钟格式化为 "HH:mm"
        val timeString = String.format("%02d:%02d", hour, minute)
        viewModelScope.launch {
            appSettingsManager.getUserSettings().setDailyReminderTime(timeString)
            // UI State 会通过 combine 自动更新
        }
        // 关闭时间选择器对话框
        hideTimePicker()
    }


    fun updateAzureKey(key: String) {
        viewModelScope.launch {
            appSettingsManager.getUserSettings().setAzureKey(key)
            // UI State 会通过 combine 自动更新
        }
    }

    fun updateAzureRegion(region: String) {
        viewModelScope.launch {
            appSettingsManager.getUserSettings().setAzureRegion(region)
            // UI State 会通过 combine 自动更新
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResettingSettings = true) }
            try {
                appSettingsManager.resetAllSettings()
                // 设置已重置，combine 会自动触发 UI State 更新
                Timber.i("All settings reset successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Error resetting settings")
                _uiState.update { it.copy(errorMessageResId = R.string.error_resetting_settings) } // 定义相应错误字符串
            } finally {
                _uiState.update { it.copy(isResettingSettings = false) }
                hideResetDialog() // 关闭确认对话框
            }
        }
    }


    // --- 对话框控制 ---
    fun showResetDialog() {
        _uiState.update { it.copy(showResetConfirmationDialog = true) }
    }

    fun hideResetDialog() {
        _uiState.update { it.copy(showResetConfirmationDialog = false) }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    fun hideTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    // --- 错误消息清除 ---
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessageResId = null) }
    }
}