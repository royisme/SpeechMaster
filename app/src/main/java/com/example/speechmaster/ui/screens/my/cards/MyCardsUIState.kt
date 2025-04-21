package com.example.speechmaster.ui.screens.my.cards

import androidx.annotation.StringRes
import com.example.speechmaster.data.model.Card
import com.example.speechmaster.ui.state.BaseUIState

sealed interface ManageCardsData {
    data class Success(
        val courseTitle: String,
        val cards: List<Card>
    ) : ManageCardsData
    data class Empty(val courseTitle: String) : ManageCardsData
}

typealias ManageCardsUIState = BaseUIState<ManageCardsData>

data class DeleteCardConfirmationState(
    val show: Boolean = false,
    val cardIdToDelete: Long? = null,
    val cardTextToDelete: String? = null // For display in dialog
)

data class EditCardData(
    val cardId: Long?, // Null when creating
    val courseId: Long,
    val initialTextContent: String // Loaded content for comparison or reset
)

typealias EditCardLoadState = BaseUIState<EditCardData>
// 用于批量导入处理过程
data class ImportCardsResultData(
    val previewCards: List<String> = emptyList()
)
typealias TextProcessingState = BaseUIState<ImportCardsResultData> // Loading/Success/Error for text processing

// --- 整体屏幕状态 ---

// 定义创建模式
enum class CardCreationMode {
    SINGLE, // 单卡创建/编辑
    BULK    // 批量导入
}

data class EditCardScreenState(
    // 模式管理
    val currentMode: CardCreationMode = CardCreationMode.SINGLE, // 默认为单卡模式

    // 单卡模式相关状态
    val cardId: Long? = null, // null 表示创建新卡，非 null 表示编辑现有卡
    val courseId: Long,       // 当前课程 ID (从导航参数获取)
    val singleCardContent: String = "", // 单卡模式下的文本输入
    val editLoadState: EditCardLoadState = BaseUIState.Loading, // 编辑模式下加载初始卡片的状态

    // 批量导入模式相关状态
    val bulkRawText: String = "", // 批量模式下的原始粘贴文本
    val bulkProcessingState: TextProcessingState = BaseUIState.Success(ImportCardsResultData()), // 批量文本处理状态

    // 通用状态
    val isSaving: Boolean = false, // 是否正在保存 (通用，因为保存按钮在外部)
    @StringRes val transientErrorResId: Int? = null, // 短暂错误提示 (验证、操作失败等)
    val transientErrorFormatArgs: List<Any>? = null, // <--- 新增

    val saveSuccess: Boolean = false // 保存成功标志，用于导航
) {
    // 便捷属性判断是否处于编辑模式
    val isEditMode: Boolean get() = cardId != null
}

data class ImportCardsScreenState(
    // 用户输入和独立于处理结果的状态
    val rawText: String = "",
    val isSaving: Boolean = false,
    @StringRes val transientErrorResId: Int? = null, // 用于即时反馈，如验证错误
    val saveSuccess: Boolean = false,

    // 使用 BaseUIState 来表示文本处理的状态和结果
    val processingState: TextProcessingState = BaseUIState.Success(ImportCardsResultData(emptyList())) // 初始状态，或者可以是 Idle/Loading
) {
    // 便捷属性，用于直接访问预览卡片（如果成功）
    val previewCards: List<String>?
        get() = (processingState as? BaseUIState.Success)?.data?.previewCards
}