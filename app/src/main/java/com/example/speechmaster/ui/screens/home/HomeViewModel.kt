package com.example.speechmaster.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.data.model.UserProgress // 确保导入
import com.example.speechmaster.domain.model.CourseItem // 复用或定义 FeaturedCourseItem
import com.example.speechmaster.domain.model.InProgressCourseInfo
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository
// import com.example.speechmaster.domain.repository.IUserProgressRepository // 如果需要整体进度
import com.example.speechmaster.domain.session.UserSessionManager
// import com.example.speechmaster.domain.usecase.home.* // 如果使用了 UseCase
import com.example.speechmaster.ui.state.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// --- UI State Definition ---

// 承载 Home 屏幕成功状态下的所有数据
data class HomeData(
    val userProgress: UserProgress? = null, // 可选的整体进度
    val inProgressCourses: List<InProgressCourseInfo> = emptyList(), // 进行中的课程列表
    val featuredCourses: List<CourseItem> = emptyList(), // 推荐课程列表
    val userDisplayName: String = "Learner" // 用户显示名称
)

// Home 屏幕的整体 UI 状态 (Loading, Success, Error)
typealias HomeUiState = BaseUIState<HomeData>

// Home 屏幕的导航事件
sealed class HomeNavigationEvent {
    data class NavigateToPractice(val courseId: Long, val cardId: Long) : HomeNavigationEvent()
    data class NavigateToCourseDetail(val courseId: Long) : HomeNavigationEvent()
    data object NavigateToMyLearning : HomeNavigationEvent()
    data object NavigateToCourses : HomeNavigationEvent()
}


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSessionManager: UserSessionManager,
    private val userCourseRelationshipRepository: IUserCourseRelationshipRepository,
    private val courseRepository: ICourseRepository, // 用于获取推荐课程
    private val cardRepository: ICardRepository,     // 用于获取下一个卡片 ID
    // private val userProgressRepository: IUserProgressRepository // 如果需要整体进度，注入这个
    // --- 或者注入 UseCases ---
    // private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    // private val getNextCardToPracticeUseCase: GetNextCardToPracticeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(BaseUIState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadHomeScreenData()
    }

    private fun loadHomeScreenData() {
        viewModelScope.launch {
            userSessionManager.currentUser.filterNotNull().collectLatest { user ->
                _uiState.value = BaseUIState.Loading // 每次用户变化时重置为 Loading
                Timber.d("Loading home screen data for user: ${user.id}")

                try {
                    // --- 并行或串行获取数据 ---

                    // 1. 获取进行中的课程 (核心)
                    //    使用 catch 操作符处理单个 Flow 的错误
                    val inProgressFlow = userCourseRelationshipRepository.getInProgressCoursesForUser(user.id)
                        .catch { e ->
                            Timber.e(e, "Error fetching in-progress courses")
                            emit(emptyList()) // 出错时发空列表
                            // 可以考虑设置一个特定的错误状态给 _uiState
                            // _uiState.value = BaseUIState.Error(R.string.error_loading_inprogress_courses)
                        }

                    // 2. 获取推荐课程 (示例：获取前 3 个内置课程)
                    val featuredFlow = courseRepository.getBuiltInCourses()
                        .map { courses -> courses.take(3).map { it.toCourseItem() } } // 转换为 UI Model
                        .catch { e ->
                            Timber.e(e, "Error fetching featured courses")
                            emit(emptyList())
                        }

                    // 3. (可选) 获取用户总体进度
                    // val progressFlow = userProgressRepository.getUserProgress(user.id)
                    //     .catch { e ->
                    //          Timber.e(e, "Error fetching user progress")
                    //          emit(null)
                    //     }

                    // --- 组合数据流 ---
                    // combine(inProgressFlow, featuredFlow, progressFlow) { inProgress, featured, progress ->
                    combine(inProgressFlow, featuredFlow) { inProgress, featured ->
                        Timber.d("Home data combined: InProgress=${inProgress.size}, Featured=${featured.size}")
                        HomeData(
                            userProgress = null, // progress, // 替换为实际的 progress
                            inProgressCourses = inProgress,
                            featuredCourses = featured,
                            userDisplayName = user.username // 使用用户名
                        )
                    }.collect { homeData ->
                        _uiState.value = BaseUIState.Success(homeData)
                        Timber.d("HomeUiState updated to Success")
                    }

                } catch (e: Exception) {
                    Timber.e(e, "Error loading home screen data")
                    _uiState.value = BaseUIState.Error(R.string.error_loading_home_data)
                }
            }
        }
    }

    /**
     * 处理“继续练习”按钮点击
     */
    fun onContinuePractice(courseId: Long) {
        viewModelScope.launch {
            val userId = userSessionManager.currentUserFlow.value?.id
            if (userId == null) {
                Timber.e("Cannot continue practice: User not logged in.")
                // TODO: Show error message via state or event
                return@launch
            }

            Timber.d("Getting next card for user $userId, course $courseId")
            val nextCardId = cardRepository.getFirstUncompletedCardId(userId, courseId)

            if (nextCardId != null) {
                Timber.i("Navigating to practice: course=$courseId, card=$nextCardId")
                _navigationEvent.emit(HomeNavigationEvent.NavigateToPractice(courseId, nextCardId))
            } else {
                // 可能课程已完成，或者没有卡片？应处理这种情况
                Timber.w("No uncompleted card found for course $courseId, cannot continue.")
                // TODO: Show message to user (e.g., "Course completed or no cards found")
                // 可以考虑重新加载数据以更新课程状态
                // loadHomeScreenData() // 或者更精细的刷新
            }
        }
    }

    /**
     * 处理点击推荐课程卡片
     */
    fun onFeaturedCourseSelected(courseId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(HomeNavigationEvent.NavigateToCourseDetail(courseId))
        }
    }

    /**
     * 处理点击“查看全部”（我的学习）
     */
    fun onViewAllLearning() {
        viewModelScope.launch {
            _navigationEvent.emit(HomeNavigationEvent.NavigateToMyLearning)
        }
    }

    /**
     * 处理点击“浏览课程”（空状态下）
     */
    fun onBrowseCourses() {
        viewModelScope.launch {
            _navigationEvent.emit(HomeNavigationEvent.NavigateToCourses)
        }
    }

    // 辅助函数：将 Course 转换为 CourseItem (如果需要不同的模型)
    private fun Course.toCourseItem(): CourseItem {
        return CourseItem(
            id = this.id,
            title = this.title,
            description = this.description,
            difficulty = this.difficulty,
            category = this.category,
            tags = this.tags,
            source = this.source,
            creatorId = this.creatorId
        )
    }
}