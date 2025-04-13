package com.example.speechmaster.ui.components.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.CourseSource
import com.example.speechmaster.common.enums.Difficulty
import com.example.speechmaster.domain.model.FilterState


@Composable
fun FilterBar(
    filterState: FilterState,
    onSourceSelected: (CourseSource) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceOptions = listOf(
        CourseSource.ALL to stringResource(R.string.all),
        CourseSource.BUILT_IN to stringResource(R.string.built_in),
        CourseSource.USER_CREATED to stringResource(R.string.user_created)
    )

    // difficulty options
    val difficultyOptions  = listOf(
        Difficulty.ALL to stringResource(R.string.all),
        Difficulty.BEGINNER to stringResource(R.string.beginner),
        Difficulty.INTERMEDIATE to stringResource(R.string.intermediate),
        Difficulty.ADVANCED to stringResource(R.string.advanced)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 来源筛选
        FilterDropDown(
            label = stringResource(R.string.source),
            options = sourceOptions.map { it.second },
            selectedOption = when(filterState.source) {
                CourseSource.ALL -> stringResource(R.string.all)
                CourseSource.BUILT_IN -> stringResource(R.string.built_in)
                CourseSource.USER_CREATED -> stringResource(R.string.user_created)
            },
            onOptionSelected = { sourceName ->
                val source = sourceOptions.first { it.second == sourceName }.first
                onSourceSelected(source)
            },
            modifier = Modifier.weight(1f)
        )

        // 难度筛选
        FilterDropDown(
            label = stringResource(R.string.difficulty),
            options = difficultyOptions.map { it.second },
            selectedOption = when(filterState.difficulty) {
                Difficulty.ALL -> stringResource(R.string.all)
                Difficulty.BEGINNER -> stringResource(R.string.beginner)
                Difficulty.INTERMEDIATE -> stringResource(R.string.intermediate)
                Difficulty.ADVANCED -> stringResource(R.string.advanced)
            },
            onOptionSelected = { difficultyName ->
                val difficulty = difficultyOptions.first { it.second == difficultyName }.first
                onDifficultySelected(difficulty)
            },
            modifier = Modifier.weight(1f)
        )

        // 分类筛选 (假设从ViewModel获取可用分类)
        val defaultCategory = stringResource(R.string.all)
        FilterDropDown(
            label = defaultCategory,
            options = listOf(
                stringResource(R.string.all),
                stringResource(R.string.business),
                stringResource(R.string.daily),
                stringResource(R.string.academic)
            ),
            selectedOption = filterState.category ?: defaultCategory,
            onOptionSelected = { category ->
                val actualCategory = if (category == defaultCategory) null else category
                onCategorySelected(actualCategory)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FilterDropDown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(horizontal = 4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


