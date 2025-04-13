package com.example.speechmaster.domain.session

import com.example.speechmaster.data.model.User
import com.example.speechmaster.domain.repository.IUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话状态
 */
sealed class SessionState {
    // 初始加载中状态
    data object Loading : SessionState()

    // 已登录状态，包含用户信息
    data class LoggedIn(val user: User) : SessionState()

    // 使用默认本地用户（未登录）
    data class UsingLocalUser(val user: User) : SessionState()

    // 未经身份验证状态（无用户）
    data object Unauthenticated : SessionState()
}

/**
 * 用户会话管理器 - 负责全局用户状态
 */
@Singleton
class UserSessionManager @Inject constructor(
    private val userRepository: IUserRepository
) {
    // 应用级协程作用域
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 会话状态流
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState

    // 当前用户便捷访问
    val currentUser: Flow<User?> = sessionState.map { state ->
        when (state) {
            is SessionState.LoggedIn -> state.user
            is SessionState.UsingLocalUser -> state.user
            else -> null
        }
    }

    // 是否登录状态便捷访问
    val isLoggedIn: Flow<Boolean> = sessionState.map { state ->
        state is SessionState.LoggedIn
    }

    // 用于外部访问的便捷属性
    val currentUserFlow: StateFlow<User?> = currentUser.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        // 初始化会话
        initializeSession()
    }

    private fun initializeSession() {
        scope.launch {
            try {
                // 检查是否有已保存的认证用户
                val user = userRepository.getCurrentUser().first()

                if (user.isAuthenticated) {
                    // 已认证用户
                    _sessionState.value = SessionState.LoggedIn(user)
                } else {
                    // 未认证但有本地用户
                    _sessionState.value = SessionState.UsingLocalUser(user)
                }
            } catch (e: Exception) {
                // 初始化失败，设为未认证状态
                _sessionState.value = SessionState.Unauthenticated
            }
        }
    }

    // 模拟登录
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 这里将来会调用实际的登录API
            // 现在我们只是模拟成功并更新状态
            val localUser = userRepository.getCurrentUser().first()

            // 模拟远程用户数据
            val remoteUser = User(
                id = "remote-" + localUser.id,
                username = email.substringBefore("@"),
                avatarUrl = null,
                email = email,
                isAuthenticated = true,
                remoteId = "server-user-id-12345"
            )

            // 合并本地用户与远程用户数据
            val result = userRepository.mergeWithRemoteUser(remoteUser)

            if (result.isSuccess) {
                // 更新会话状态
                _sessionState.value = SessionState.LoggedIn(result.getOrThrow())
                Result.success(result.getOrThrow())
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 注销
    suspend fun logout(): Result<Boolean> {
        return try {
            // 将当前用户重置为本地默认用户（不删除）
            val user = userRepository.getCurrentUser().first()?.copy(
                isAuthenticated = false,
                email = null,
                remoteId = null
            )

            if (user != null) {
                userRepository.updateUserInfo(user.username, user.avatarUrl)
                _sessionState.value = SessionState.UsingLocalUser(user)
            } else {
                // 如果没有用户，确保创建一个
                val defaultUser = userRepository.ensureLocalUser()
                _sessionState.value = SessionState.UsingLocalUser(defaultUser)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 更新用户信息
    suspend fun updateProfile(username: String, avatarUrl: String?): Result<User> {
        return try {
            val result = userRepository.updateUserInfo(username, avatarUrl)

            if (result.isSuccess) {
                val updatedUser = result.getOrThrow()
                // 根据当前状态更新会话状态
                when (_sessionState.value) {
                    is SessionState.LoggedIn -> {
                        _sessionState.value = SessionState.LoggedIn(updatedUser)
                    }
                    is SessionState.UsingLocalUser -> {
                        _sessionState.value = SessionState.UsingLocalUser(updatedUser)
                    }
                    else -> {
                        // 不更改状态
                    }
                }
                Result.success(updatedUser)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
