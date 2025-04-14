package com.example.speechmaster.utils.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.speechmaster.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * 权限请求组件
 *
 * 处理单个权限的请求过程，包括显示理由和处理权限结果
 *
 * @param permission 要请求的权限（例如 android.Manifest.permission.RECORD_AUDIO）
 * @param rationale 向用户解释为什么需要此权限的文本
 * @param permissionNotAvailableContent 当用户拒绝并选择"不再询问"时显示的内容
 * @param content 获得权限后显示的内容
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequest(
    permission: String,
    rationale: String,
    permissionNotAvailableContent: @Composable () -> Unit = { },
    content: @Composable () -> Unit = { }
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission)

    // 是否显示永久拒绝对话框
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    if (permissionState.status.isGranted) {
        // 已有权限，直接显示内容
        content()
    } else {
        // 显示权限请求对话框或理由
        if (permissionState.status.shouldShowRationale) {
            // 用户拒绝过，但没有选择"不再询问"，显示理由对话框
            RationaleDialog(
                rationale = rationale,
                onRequestPermission = { permissionState.launchPermissionRequest() },
                onDismiss = { showPermissionDeniedDialog = true }
            )
        } else {
            // 首次请求或用户已选择"不再询问"
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }

        // 处理永久拒绝的情况
        if (showPermissionDeniedDialog) {
            PermissionDeniedDialog(
                onGoToSettings = {
                    // 跳转到应用设置页面
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    showPermissionDeniedDialog = false
                },
                onDismiss = { showPermissionDeniedDialog = false }
            )
        }

        // 显示无权限内容
        permissionNotAvailableContent()
    }
}

/**
 * 权限理由对话框
 *
 * 向用户解释为什么应用需要请求的权限
 */
@Composable
private fun RationaleDialog(
    rationale: String,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.permission_required))
        },
        text = {
            Text(text = rationale)
        },
        confirmButton = {
            TextButton(
                onClick = onRequestPermission
            ) {
                Text(stringResource(R.string.grant_permission))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.deny))
            }
        }
    )
}

/**
 * 权限永久拒绝对话框
 *
 * 当用户拒绝权限并选择"不再询问"时显示，
 * 提供前往设置页面手动授予权限的选项
 */
@Composable
private fun PermissionDeniedDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.permission_denied))
        },
        text = {
            Text(text = stringResource(R.string.permission_denied_message))
        },
        confirmButton = {
            TextButton(
                onClick = onGoToSettings
            ) {
                Text(stringResource(R.string.go_to_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
