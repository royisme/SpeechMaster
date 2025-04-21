package com.example.speechmaster.ui.screens.my.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.ui.navigation.navigateToCreateCourse
import com.example.speechmaster.R
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.navigation.navigateToEditCourse
import com.example.speechmaster.ui.navigation.navigateToManageCards
import com.example.speechmaster.ui.state.BaseUIState
import timber.log.Timber

@Composable
fun MyCoursesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MyCoursesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()

    // Handle Delete Confirmation Dialog
    if (deleteConfirmationState.show) {
        DeleteCourseConfirmationDialog(
            courseTitle = deleteConfirmationState.courseTitleToDelete ?: "",
            onConfirm = { viewModel.confirmDeleteCourse() },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.new_course)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_course)) },
                onClick = {
                    navController.navigateToCreateCourse()
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BaseUIState.Loading -> {
                    LoadingView(Modifier.fillMaxSize())
                }
                is BaseUIState.Error -> {
                    ErrorView(
                        message = stringResource(id = state.messageResId),
                        onRetry = { viewModel.retryLoadCourses() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is BaseUIState.Success -> {
                    when (val data = state.data) {
                        is MyCoursesData.Empty -> {
                            EmptyState(
                                onCreateClick = { navController.navigateToCreateCourse() },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        // Pass List<Course> directly
                        is MyCoursesData.Success -> {
                            UserCourseList(
                                courses = data.courses, // Pass List<Course>
                                onEdit = { courseId ->
                                    //Timber.d("Edit course with ID: $courseId")
                                    navController.navigateToEditCourse(courseId)
                                         },
                                onManageCards = { courseId ->
                                    navController.navigateToManageCards(courseId)
                                                },
                                onDelete = { courseId, courseTitle -> viewModel.requestDeleteCourse(courseId, courseTitle) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCourseList(
    courses: List<Course>, // Accepts List<Course>
    onEdit: (Long) -> Unit,
    onManageCards: (Long) -> Unit,
    onDelete: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(courses, key = { it.id }) { course -> // Iterate over List<Course>
            MyCourseListCard(
                course = course, // Pass Course object
                onEdit = { onEdit(course.id) },
                onManageCards = { onManageCards(course.id) },
                onDelete = { onDelete(course.id, course.title) }
            )
        }
    }
}

@Composable
private fun MyCourseListCard(
    course: Course, // Accepts Course object
    onEdit: () -> Unit,
    onManageCards: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Use properties directly from Course object
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            course.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.edit))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onManageCards) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = stringResource(R.string.manage_cards_button), Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.manage_cards_button))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}


// EmptyState and DeleteCourseConfirmationDialog remain unchanged
@Composable
private fun EmptyState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School, // Or another relevant icon
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_courses_created),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateClick) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.create_first_course))
        }
    }
}

@Composable
fun DeleteCourseConfirmationDialog(
    courseTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_delete_course_title)) },
        text = { Text(stringResource(R.string.confirm_delete_course_message, courseTitle)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}