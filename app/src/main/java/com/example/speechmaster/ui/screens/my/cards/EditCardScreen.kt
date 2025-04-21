package com.example.speechmaster.ui.screens.my.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.state.BaseUIState
import com.example.speechmaster.ui.state.TopBarAction
import com.example.speechmaster.ui.state.TopBarState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EditCardViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel() // Inject TopBarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Navigation & Snackbar Effects ---
    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { state ->
            if (state.saveSuccess) {
                navController.popBackStack()
                viewModel.resetSaveSuccess()
            }
            state.transientErrorResId?.let { messageResId ->
                val message = context.getString(
                    messageResId,
                    *(state.transientErrorFormatArgs?.toTypedArray() ?: emptyArray()) // <--- 使用存储的参数
                )
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearErrorMessage()
            }
        }
    }

    val titleResId = if (viewModel.isEditMode) R.string.edit_card else R.string.add_card
    val title = stringResource(id = titleResId)
    // Save action enabled state depends on current mode and states
    val isSaveEnabled = remember(uiState.currentMode, uiState.isSaving, uiState.singleCardContent, uiState.bulkProcessingState) {
        !uiState.isSaving && // Not currently saving
                when (uiState.currentMode) {
                    CardCreationMode.SINGLE -> uiState.singleCardContent.isNotBlank() // Single card has content
                    CardCreationMode.BULK -> { // Bulk mode has processed cards
                        val procState = uiState.bulkProcessingState
                        (procState is BaseUIState.Success && !procState.data.previewCards.isNullOrEmpty())
                    }
                }
    }

    LaunchedEffect(title, isSaveEnabled) { // Update TopBar when title or enabled state changes
        topBarViewModel.overrideState(
            TopBarState(
                title = title,
                showBackButton = true,
                showMenuButton = false,
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Check,
                        contentDescription = context.getString(R.string.save),
                        onClick = { if (isSaveEnabled) viewModel.save() }
                    )
                )
            )
        )

    }

    DisposableEffect(Unit) {
        onDispose { topBarViewModel.overrideState(null) }
    }

    // --- Main UI ---
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Tabs for Mode Selection
            ModeTabs(
                selectedMode = uiState.currentMode,
                onModeSelected = viewModel::changeMode,
                enabled = !uiState.isSaving // Disable tabs while saving
            )

            // 2. Content Area based on Mode
            when (uiState.currentMode) {
                CardCreationMode.SINGLE -> SingleCardContent(viewModel = viewModel)
                CardCreationMode.BULK -> BulkImportContent(viewModel = viewModel)
            }
            // Global Saving Indicator Overlay (Optional)
            if (uiState.isSaving) {
                Box(modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// --- Tab Row Composable ---
@OptIn(ExperimentalMaterial3Api::class) // Might be needed for PrimaryTabRow
@Composable
private fun ModeTabs(
    selectedMode: CardCreationMode,
    onModeSelected: (CardCreationMode) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val titles = listOf(
        stringResource(R.string.add_card_mode_single),
        stringResource(R.string.add_card_mode_bulk)
    )
    val selectedIndex = selectedMode.ordinal

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth()
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { if (enabled) onModeSelected(CardCreationMode.entries[index]) },
                text = { Text(title) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SingleCardContent(
    viewModel: EditCardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle initial loading state specifically for edit mode
    val editLoadState = uiState.editLoadState
    if (viewModel.isEditMode && editLoadState is BaseUIState.Loading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingView()
        }
        return // Show only loading indicator while loading card data
    }
    if (editLoadState is BaseUIState.Error) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Error loading card: ${stringResource(id = editLoadState.messageResId ?: R.string.error_unknown)}",
                color = MaterialTheme.colorScheme.error
            )
        }
        return // Show error if loading failed
    }
    val isContentError = remember(uiState.transientErrorResId) {
        uiState.transientErrorResId == R.string.error_card_content_required ||
                uiState.transientErrorResId == R.string.error_card_content_too_long
        // Add other relevant field errors
    }



    // Main content area for single card input
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Allow scrolling for hint text etc.
    ) {
        OutlinedTextField(
            value = uiState.singleCardContent,
            onValueChange = viewModel::onSingleCardContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp), // Ensure decent height
            label = { Text(stringResource(R.string.card_content_label)) },
            placeholder = { Text(stringResource(R.string.card_content_placeholder)) },
            // Show error state based on specific validation error
            isError = uiState.transientErrorResId == R.string.error_card_content_required,
            enabled = !uiState.isSaving,
            supportingText = {
                if (isContentError && uiState.transientErrorResId != null) {
                    Text(
                        stringResource(
                            id = uiState.transientErrorResId!!,
                            // *** 使用 state 中的 formatArgs ***
                            *(uiState.transientErrorFormatArgs?.toTypedArray() ?: emptyArray()) // <--- 使用存储的参数
                        )
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Hint Text Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_card_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // "View Example" Button (Optional)
        TextButton(onClick = { /* TODO: Implement view example logic */ }) {
            Text(stringResource(R.string.add_card_view_example))
        }
        // Add Spacer at the bottom if content is short, to push hint/example up
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BulkImportContent(
    viewModel: EditCardViewModel, // Pass ViewModel or relevant state parts
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val processingState = uiState.bulkProcessingState // 获取批量处理的状态

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp) // 内边距
    ) {
        // 1. Text Input Area (保持不变)
        OutlinedTextField(
            value = uiState.bulkRawText,
            onValueChange = viewModel::onBulkRawTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                // 使用 weight 控制比例，给下方结果区域留出更多空间
                .weight(0.35f), // 调整比例，例如 35%
            label = { Text(stringResource(R.string.import_paste_text_label)) },
            placeholder = { Text(stringResource(R.string.import_paste_text_placeholder)) },
            isError = uiState.transientErrorResId == R.string.error_import_text_empty,
            // 当处理中或保存中时禁用输入
            enabled = !(processingState is BaseUIState.Loading || uiState.isSaving)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Process Button (保持不变)
        Button(
            onClick = viewModel::processBulkText,
            enabled = uiState.bulkRawText.isNotBlank() && !(processingState is BaseUIState.Loading || uiState.isSaving),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.ContentPasteGo, contentDescription = null)
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.import_process_button))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Results Area Title (保持不变)
        Text(stringResource(R.string.import_preview_title), style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 4. Results Area Content (根据 processingState 显示不同内容)
        Box(
            modifier = Modifier
                .weight(0.65f) // 调整比例，例如 65%
                .fillMaxWidth()
        ) {
            when (processingState) {
                // 4a. 加载中状态
                is BaseUIState.Loading -> {
                    LoadingView(Modifier.align(Alignment.Center))
                }
                // 4b. 错误状态
                is BaseUIState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp), // 添加内边距
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning, // 或 Error 图标
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = processingState.messageResId),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge // 让文字更清晰
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // 可选：添加重试按钮，如果适用
                        // Button(onClick = viewModel::processBulkText) { Text("Retry Processing") }
                    }
                }
                // 4c. 成功状态 (包含空列表和有数据列表)
                is BaseUIState.Success -> {
                    val previewCards = processingState.data.previewCards
                    if (previewCards.isEmpty()) {
                        // 区分是初始状态还是处理后无结果
                        if (uiState.bulkRawText.isBlank()) {
                            // 初始状态 - 等待输入和处理
                            Text(
                                stringResource(R.string.import_awaiting_processing),
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // 处理完成但未生成卡片
                            Text(
                                stringResource(R.string.import_no_cards_generated),
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge // 让提示更明显
                            )
                        }
                    } else {
                        // 成功生成预览卡片，显示列表
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(), // 填满 Box
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp) // 给列表本身添加边距
                        ) {
                            itemsIndexed(previewCards, key = { index, _ -> index }) { index, cardText ->
                                PreviewCardItem(
                                    index = index + 1,
                                    text = cardText,
                                    // TODO: Add edit/delete actions here later
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
