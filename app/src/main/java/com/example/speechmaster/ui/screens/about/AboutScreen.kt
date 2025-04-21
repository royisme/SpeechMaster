package com.example.speechmaster.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.R // Import your R file
import com.example.speechmaster.ui.navigation.AppRoutes // Import your Routes
import com.example.speechmaster.ui.theme.AppTheme
import timber.log.Timber

@Composable
fun AboutScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    // Fetch URLs from string resources for better management
    val repositoryUrl = stringResource(R.string.repository_url) // Add this string
    val licenseUrl = stringResource(R.string.license_url)       // Add this string (URL to license text)
    val licenseName = stringResource(R.string.license_name)     // Add this string (e.g., "Apache License 2.0")
    val contactEmail = stringResource(R.string.contact_email)   // Add this string

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp) // Adjusted spacing
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_launcher), // Replace with your actual logo
            contentDescription = stringResource(R.string.app_logo),
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = stringResource(R.string.version_format, uiState.appVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Optional App Description
        Text(
            text = stringResource(R.string.app_description_about), // Example
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Modifier.padding(vertical = 16.dp)
        HorizontalDivider(modifier, 1.dp, MaterialTheme.colorScheme.onSurfaceVariant)

        // --- Links Section ---

        // Source Code Link
        ClickableLinkText( // Modified: Use the new ClickableLinkText implementation
            text = stringResource(R.string.source_code), // Add this string
            onClick = { safeOpenUri(uriHandler, repositoryUrl) }
        )

        // License Link/Text
        ClickableLinkText(
            // Display license name, link to full text
            text = stringResource(R.string.view_license_format, licenseName),
            onClick = { safeOpenUri(uriHandler, licenseUrl) }
        )

        // Contact/Feedback (Consider linking to Issues page if preferred)
        ClickableLinkText(
            text = stringResource(R.string.report_issue_or_feedback),
            onClick = {
                // Option 1: Link to Issues (often same as repo URL + "/issues")
                safeOpenUri(uriHandler, "$repositoryUrl/issues")
                // Option 2: Mailto link
                // safeOpenUri(uriHandler, "mailto:$contactEmail?subject=SpeechMaster Feedback")
            }
        )

        // Privacy Policy Link
        ClickableLinkText(
            text = stringResource(R.string.privacy_policy),
            onClick = { navController.navigate(AppRoutes.PRIVACY_POLICY_ROUTE) }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        // Data Storage Note
        Text(
            text = stringResource(R.string.data_storage_notice_about),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// Helper function to safely open URIs
private fun safeOpenUri(uriHandler: androidx.compose.ui.platform.UriHandler, uriString: String) {
    try {
        uriHandler.openUri(uriString)
    } catch (e: Exception) {
        // Log error or inform user if URI opening fails
        Timber.tag("AboutScreen").e(e, "Failed to open URI: $uriString")
        // Optionally show a Snackbar message
    }
}

// Improved ClickableLinkText helper composable
@Composable
private fun ClickableLinkText(
    text: String,
    tag: String = "LINK_TAG",
    onClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = tag, annotation = "link")
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )) {
            append(text)
        }
        pop()
    }

// 使用 remember 来存储 TextLayoutResult
    var textLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }
// 使用 rememberUpdatedState 来确保在 lambda 变化时也能获取最新的 onClick
    val currentOnClick by rememberUpdatedState(onClick)

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        // 1. 获取 TextLayoutResult
        onTextLayout = { textLayoutResult ->
            textLayoutResultState = textLayoutResult
        },
        modifier = Modifier
            // 2. 使用 pointerInput 检测点击手势
            .pointerInput(Unit) { // 可以传入 annotatedString 作为 key，如果字符串变化需要重新设置手势
                detectTapGestures { offset ->
                    // 3. 使用 TextLayoutResult 将点击位置 (Offset) 转换为文本索引 (Int)
                    textLayoutResultState?.let { textLayoutResult ->
                        val position = textLayoutResult.getOffsetForPosition(offset)

                        // 4. 查找在点击位置的、带有特定 tag 的注解
                        annotatedString
                            .getStringAnnotations(tag = tag, start = position, end = position)
                            .firstOrNull()
                            ?.let { annotation ->
                                // 5. 如果找到了匹配的注解，则调用 onClick
                                // 因为 getStringAnnotations 已经按 tag 过滤了, 不再需要 annotation.tag == tag 的检查
                                currentOnClick()
                            }
                    }
                }
            }
    )
}


@Preview(showBackground = true)
@Composable
private fun AboutScreenOpenSourcePreview() {
    AppTheme {
        AboutScreen(navController = rememberNavController())
    }
}