package com.example.speechmaster.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.data.model.RecentPractice
import com.example.speechmaster.data.model.User
import com.example.speechmaster.data.model.UserProgress
import com.example.speechmaster.data.repository.MockPracticeRepository
import com.example.speechmaster.domain.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// 主页UI状态
data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val userProgress: UserProgress? = null,
    val featuredSession: PracticeSession? = null,
    val recentPractices: List<RecentPractice> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSessionManager: UserSessionManager,

    private val practiceRepository: MockPracticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        // 这里可以调用practiceRepository来获取数据

        // 将用户状态和练习数据合并
        viewModelScope.launch {
            // 监听当前用户变化
            userSessionManager.currentUserFlow.collect { user ->
                _uiState.update { it.copy(user = user) }

                user?.let { currentUser ->
                    // 基于用户ID加载进度
                    practiceRepository.getUserProgress(currentUser.id).collect { progress ->
                        _uiState.update { it.copy(userProgress = progress) }
                    }
                }
            }
        }


// 加载用户的练习历史
        viewModelScope.launch {
            // 监听用户和练习历史
            combine(
                userSessionManager.currentUserFlow,
                practiceRepository.getAvailablePracticeSessions()
            ) { user, sessions ->
                Pair(user, sessions)
            }.collect { (user, sessions) ->
                if (user != null) {
                    // 加载最近练习
                    practiceRepository.getRecentPractices(user.id).collect { practices ->
                        _uiState.update {
                            it.copy(
                                recentPractices = practices,
                                isLoading = false
                            )
                        }
                    }
                }

                // 设置特色会话
                if (sessions.isNotEmpty()) {
                    _uiState.update { it.copy(featuredSession = sessions[0]) }
                }
            }
        }
    }

    fun startPractice(sessionId: String) {
        viewModelScope.launch {
            val user = userSessionManager.currentUserFlow.value
            user?.let { currentUser ->
                practiceRepository.startPracticeSession(sessionId)
            }
            // 此处应该导航到练习界面，但暂不实现
        }
    }
}