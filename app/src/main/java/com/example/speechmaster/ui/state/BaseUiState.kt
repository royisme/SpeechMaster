package com.example.speechmaster.ui.state

import androidx.annotation.StringRes


/**
 * 一个通用的密封类，用于描述带有加载状态的数据资源。
 * @param T 成功状态下所持有数据的类型。
 */
sealed class UiState<out T> {
    /**
     * 表示正在加载中。
     */
    data object Loading : UiState<Nothing>()
    /**
     * 表示没有数据的空界面。
     */
    data class Empty(
        @StringRes val messageResId: Int? = null,
        val message: String? = null
    ) : UiState<Nothing>() // 添加空状态

    /**
     * 表示成功状态，并持有具体数据。
     * @param data 成功获取的数据。
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * 表示错误状态，并持有错误详情。
     * @param error 包含错误信息的 ErrorState 对象。
     */
    data class Error(
        @StringRes val messageResId: Int,
        val formatArgs: List<Any>? = null
    ) : UiState<Nothing>()

}
inline fun <reified T> UiState<T>.get(): T? {
    return if (this is UiState.Success) this.data else null
}