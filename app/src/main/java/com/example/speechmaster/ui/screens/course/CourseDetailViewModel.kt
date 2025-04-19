package com.example.speechmaster.ui.screens.course
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseCardItem
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.repository.IUserCardCompletionRepository
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.domain.model.CourseDetail
import com.example.speechmaster.ui.state.BaseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject



/**
 * 课程详情页面ViewModel
 */
@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val cardRepository: ICardRepository,
    private val userCourseRelationshipRepository: IUserCourseRelationshipRepository,
    private val userCardCompletionRepository: IUserCardCompletionRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val TAG = "CourseDetailViewModel"
    }
    // 从导航参数获取课程ID
    private val courseId: Long = checkNotNull(savedStateHandle.get<Long>("courseId"))

    // 获取当前用户ID
    private val userId: String? get() = userSessionManager.currentUserFlow.value?.id

    // UI状态
    private val _uiState = MutableStateFlow<BaseUiState<CourseDetailData>>(BaseUiState.Loading)
    val uiState: StateFlow<BaseUiState<CourseDetailData>> = _uiState.asStateFlow()

    // 课程是否已添加(内部状态)
    private val _isAddedInternal = MutableStateFlow<Boolean?>(null)

    // 课程是否已添加(外部状态)
    val isAdded: StateFlow<Boolean> = _isAddedInternal
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        // 初始化时加载数据
        if (courseId != 0L && userId != null) {
            // 加载课程添加状态
            viewModelScope.launch {
                try {
                    _isAddedInternal.value = userCourseRelationshipRepository
                        .isCourseAdded(userId!!, courseId)
                        .first()
                } catch (e: Exception) {
                    _isAddedInternal.value = false
                    Log.e(TAG, "Error checking course addition status", e)
                }
            }

            // 监听isAdded状态变化，触发数据加载
            viewModelScope.launch {
                _isAddedInternal
                    .filterNotNull()
                    .collectLatest { isAdded ->
                        loadCourseDetail(isAdded)
                    }
            }
        } else {
            _uiState.value = BaseUiState.Error(R.string.error_invalid_course_or_not_logged_in)
        }
    }

    /**
     * 加载课程详情和卡片列表
     */
    fun loadCourseDetail(isAdded: Boolean) {
        _uiState.value = BaseUiState.Loading
        viewModelScope.launch {
            try {
                // 加载课程详情
                val course = courseRepository.getCourseById(courseId)
                    .first() // 获取 Flow 发出的第一个值
                if (course == null) {
                    // 如果课程为 null (不存在)
                    _uiState.value = BaseUiState.Error(R.string.error_course_list_not_exist)
                    return@launch // 停止后续执行
                }

                // 加载卡片列表
                val cards = cardRepository.getCardsByCourse(courseId).first()

                // 如果课程已添加，加载已完成的卡片ID
                val completedCardIds = if (isAdded && userId != null) {
                    userCardCompletionRepository.getCompletedCardIds(userId!!, courseId).first()
                } else {
                    emptySet()
                }

                // 转换为UI模型
                val courseDetail = CourseDetail(
                    id = course.id,
                    title = course.title,
                    description = course.description,
                    difficulty = course.difficulty,
                    category = course.category,
                    source = course.source
                )

                val cardItems = cards.sortedBy { it.sequenceOrder }.map { card ->
                    CourseCardItem(
                        id = card.id,
                        sequenceOrder = card.sequenceOrder,
                        textPreview = card.textContent,
                        isCompleted = isAdded && completedCardIds.contains(card.id)
                    )
                }

                // 更新UI状态
                if (cards.isEmpty()) {
                    _uiState.value =BaseUiState.Success(
                        CourseDetailData(
                            course = courseDetail,
                            cards = emptyList(),
                            isAdded = isAdded
                        )
                    )
                } else {
                    _uiState.value = BaseUiState.Success(
                        CourseDetailData(
                            course = courseDetail,
                            cards = cardItems,
                            isAdded = isAdded
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("CourseDetailViewModel", "Error loading course detail for ID: $courseId", e)
                _uiState.value = BaseUiState.Error(
                    messageResId = R.string.error_loading_course_detail_failed,
                    formatArgs = listOf(e.message ?: "")
                )
            }
        }
    }

    /**
     * 添加课程到学习列表
     */
    fun addCourseToLearning() {
        val user = userId ?: return
        viewModelScope.launch {
            try {
                userCourseRelationshipRepository.addRelationship(user, courseId)
                _isAddedInternal.value = true
            } catch (e: Exception) {
                // TODO: 添加错误通知机制
                Log.e(TAG, "Error adding course to learning", e)
            }
        }
    }

    /**
     * 从学习列表移除课程
     */
    fun removeCourseFromLearning() {
        val user = userId ?: return
        viewModelScope.launch {
            try {
                userCourseRelationshipRepository.removeRelationship(user, courseId)
                _isAddedInternal.value = false
            } catch (e: Exception) {
                // TODO: 添加错误通知机制
                Log.e(TAG, "Error removing course from learning", e)
            }
        }
    }
}