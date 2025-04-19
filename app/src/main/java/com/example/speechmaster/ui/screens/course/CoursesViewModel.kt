package com.example.speechmaster.ui.screens.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.CourseSource
import com.example.speechmaster.common.enums.Difficulty
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.model.CourseItem
import com.example.speechmaster.domain.model.FilterState
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.state.BaseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject



// 排序方式
enum class SortingType(val createComparator: () -> Comparator<Course>) {
    NEWEST({ compareByDescending { it.createdAt } }),
    OLDEST({ compareBy { it.createdAt } }),
    DIFFICULTY({ compareBy { it.difficulty } })
}




@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val userSessionManager: UserSessionManager,
    private val courseRepository: ICourseRepository
) : ViewModel() {
    companion object{
        private const val TAG = "CoursesViewModel"
    }
    // UI状态

    private val _uiState = MutableStateFlow<CourseListUiState>(BaseUiState.Loading)
    val uiState: StateFlow<CourseListUiState> = _uiState.asStateFlow()
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 是否显示搜索框
    private val _showSearch = MutableStateFlow(false)
    val showSearch: StateFlow<Boolean> = _showSearch.asStateFlow()

    // 筛选状态
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // 合并searchQuery和filterState以触发过滤
    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchAndFilterFlow = combine(
        _searchQuery,
        _filterState,
        userSessionManager.currentUserFlow
    ) { query, filter, user ->
        Triple(query, filter, user)
    }.flatMapLatest { (query, filter, user) ->
        user?.id?.let { userId ->
            loadFilteredCourses(userId, query, filter)
        } ?: flowOf(emptyList())
    }.catch { e ->
        emit(emptyList())
        Timber.tag(TAG).e(e, "Error loading courses")
        _uiState.value = BaseUiState.Error(R.string.error_unknown)
    }

    // 初始化
    init {
        Timber.tag(TAG).d("init data")
        loadCourses()
    }

    // 加载课程数据
    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = BaseUiState.Loading
            try {
                // 使用collectLatest而不是collect，这样当有新值时会取消之前的处理
                searchAndFilterFlow.collectLatest { courses ->
                    Timber.d("Loaded ${courses.size} courses")
                    _uiState.value = if (courses.isEmpty()) {
                        BaseUiState.Success(CourseListData.Empty)
                    } else {
                        BaseUiState.Success(CourseListData.Success(courses.map { it.toUiModel() }))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "加载课程失败")
                _uiState.value = BaseUiState.Error(
                    R.string.error_unknown,
                )
            }
        }
    }

    // 更新搜索查询
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 切换搜索框显示状态
    fun toggleSearchVisibility() {
        _showSearch.value = !_showSearch.value
        // 当隐藏搜索框时，清空搜索条件
        if (!_showSearch.value) {
            _searchQuery.value = ""
        }
    }

    // 更新筛选状态
    fun updateFilterState(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    // 更新筛选来源
    fun updateSourceFilter(source: CourseSource) {
        _filterState.value = _filterState.value.copy(source = source)
    }

    // 更新难度筛选
    fun updateDifficultyFilter(difficulty: Difficulty) {
        _filterState.value = _filterState.value.copy(difficulty = difficulty)
    }

    // 更新分类筛选
    fun updateCategoryFilter(category: String?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    // 更新排序方式
    fun updateSorting(sortingType: SortingType) {
        _filterState.value = _filterState.value.copy(sorting = sortingType)
    }

    // 加载筛选后的课程数据
    private fun loadFilteredCourses(
        userId: String,
        query: String,
        filter: FilterState
    ): Flow<List<Course>> {
        return courseRepository.getAccessibleCourses(userId)
            .map { courses ->
                // 应用筛选
                courses.filter { course ->
                    // 1. 来源筛选
                    val sourceMatches = when (filter.source) {
                        CourseSource.ALL -> true
                        CourseSource.BUILT_IN -> course.source == "BUILT_IN"
                        CourseSource.USER_CREATED -> course.source == "UGC"
                    }

                    // 2. 难度筛选
                    val difficultyMatches = when (filter.difficulty) {
                        Difficulty.ALL -> true
                        Difficulty.BEGINNER -> course.difficulty.equals("beginner", ignoreCase = true)
                        Difficulty.INTERMEDIATE -> course.difficulty.equals("intermediate", ignoreCase = true)
                        Difficulty.ADVANCED -> course.difficulty.equals("advanced", ignoreCase = true)
                    }

                    // 3. 分类筛选
                    val categoryMatches = filter.category?.let {
                        course.category.equals(it, ignoreCase = true)
                    } ?: true

                    // 4. 搜索查询
                    val queryMatches = query.isEmpty() ||
                            course.title.contains(query, ignoreCase = true) ||
                            (course.description?.contains(query, ignoreCase = true) ?: false)

                    sourceMatches && difficultyMatches && categoryMatches && queryMatches
                }.let { filteredCourses ->
                    // 应用排序
                    when (filter.sorting) {
                        SortingType.NEWEST -> filteredCourses.sortedByDescending { it.createdAt }
                        SortingType.OLDEST -> filteredCourses.sortedBy { it.createdAt }
                        SortingType.DIFFICULTY -> filteredCourses.sortedBy {
                            when (it.difficulty.lowercase()) {
                                "beginner" -> 1
                                "intermediate" -> 2
                                "advanced" -> 3
                                else -> 4
                            }
                        }
                    }
                }
            }
    }

    // 导航到练习界面
    fun navigateToPractice(courseId: Long) {
        // 导航逻辑将由UI调用，在此处可添加额外的前处理逻辑
    }

    // 导航到创建课程界面
    fun navigateToCreateCourse() {
        // 导航逻辑将由UI调用，在此处可添加额外的前处理逻辑
    }



    // 将数据模型转换为UI模型
    private fun Course.toUiModel(): CourseItem = CourseItem(
        id = id,
        title = title,
        description = description,
        difficulty = difficulty,
        category = category,
        tags = tags,
        source = source,
        creatorId = creatorId
    )
}