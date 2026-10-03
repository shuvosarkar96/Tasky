@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tasky.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tasky.Filter
import com.tasky.TaskViewModel
import com.tasky.data.Task
import java.time.LocalDate

private val categories = listOf(
    "Personal",
    "Work",
    "Study",
    "Health"
)

private val priorities = listOf(
    "Low",
    "Medium",
    "High"
)

private val priorityColors = listOf(
    Color(0xFF22C55E),
    Color(0xFFF59E0B),
    Color(0xFFEF4444)
)

private fun dayLabel(day: Long): String {
    val today = LocalDate.now().toEpochDay()

    return when (day) {
        today -> "Today"
        today + 1 -> "Tomorrow"
        today - 1 -> "Yesterday"
        else -> {
            val date = LocalDate.ofEpochDay(day)
            "${date.month.name.take(3)} ${date.dayOfMonth}"
        }
    }
}

@Composable
fun TaskyScreen(viewModel: TaskViewModel) {

    val tasks by viewModel.visible.collectAsStateWithLifecycle()
    val allTasks by viewModel.all.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val completed = allTasks.count { it.done }
    val total = allTasks.size
    val progress = if (total == 0) 0f else completed.toFloat() / total

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskyLogo(
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Tasky",
                                fontWeight = FontWeight.Bold
                            )

                            if (total > 0) {
                                Text(
                                    text = "$completed of $total completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (completed > 0) {
                        TextButton(
                            onClick = {
                                viewModel.clearDone()
                            }
                        ) {
                            Text("Clear done")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingTask = null
                    showSheet = true
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New task"
                    )
                },
                text = {
                    Text("New task")
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ProgressCard(
                    progress = progress,
                    completed = completed,
                    total = total
                )
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        viewModel.query.value = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text("Search tasks")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.query.value = ""
                                }
                            ) {
                                Text("Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                FilterRow(
                    selected = filter,
                    onSelected = {
                        viewModel.filter.value = it
                    }
                )
            }

            if (tasks.isEmpty()) {

                item {
                    EmptyState(
                        hasTasks = allTasks.isNotEmpty(),
                        onAdd = {
                            editingTask = null
                            showSheet = true
                        }
                    )
                }

            } else {

                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->

                    TaskCard(
                        task = task,
                        onToggle = {
                            viewModel.toggle(task)
                        },
                        onDelete = {
                            viewModel.delete(task)
                        },
                        onEdit = {
                            editingTask = task
                            showSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showSheet) {
        TaskSheet(
            task = editingTask,
            onDismiss = {
                showSheet = false
            },
            onSave = { task ->
                viewModel.save(task)
                showSheet = false
            }
        )
    }
}

@Composable
private fun ProgressCard(
    progress: Float,
    completed: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Your progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (total == 0) {
                            "No tasks yet"
                        } else {
                            "$completed completed out of $total"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}

@Composable
private fun FilterRow(
    selected: Filter,
    onSelected: (Filter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(Filter.values().toList()) { filter ->

            FilterChip(
                selected = selected == filter,
                onClick = {
                    onSelected(filter)
                },
                label = {
                    Text(filter.label)
                }
            )
        }
    }
}

@Composable
private fun EmptyState(
    hasTasks: Boolean,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (hasTasks) {
                    "Nothing here"
                } else {
                    "No tasks yet"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasTasks) {
                    "Try another filter or search."
                } else {
                    "Create your first task to get started."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!hasTasks) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAdd
                ) {
                    Text("Create task")
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onEdit
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = task.done,
                onCheckedChange = {
                    onToggle()
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.done) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = task.notes,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (task.dueDay != null) {
                        Text(
                            text = "  •  ${dayLabel(task.dueDay)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Canvas(
                        modifier = Modifier.size(8.dp)
                    ) {
                        drawCircle(
                            color = priorityColors[
                                task.priority.coerceIn(0, 2)
                            ]
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete
            ) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun TaskSheet(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var title by remember(task) {
        mutableStateOf(task?.title ?: "")
    }

    var notes by remember(task) {
        mutableStateOf(task?.notes ?: "")
    }

    var priority by remember(task) {
        mutableStateOf(task?.priority ?: 1)
    }

    var category by remember(task) {
        mutableStateOf(task?.category ?: "Personal")
    }

    var dueDay by remember(task) {
        mutableStateOf(task?.dueDay)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp
                )
        ) {

            Text(
                text = if (task == null) {
                    "New task"
                } else {
                    "Edit task"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Title")
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Notes")
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {

                priorities.forEachIndexed { index, label ->

                    SegmentedButton(
                        selected = priority == index,
                        onClick = {
                            priority = index
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = priorities.size
                        )
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(categories) { item ->

                    FilterChip(
                        selected = category == item,
                        onClick = {
                            category = item
                        },
                        label = {
                            Text(item)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dueDay?.let { dayLabel(it) } ?: "No date",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = {
                    Text("Due date")
                },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            showDatePicker = true
                        }
                    ) {
                        Text("Pick")
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {

                    if (title.isNotBlank()) {

                        onSave(
                            Task(
                                id = task?.id ?: 0,
                                title = title.trim(),
                                notes = notes.trim(),
                                priority = priority,
                                category = category,
                                dueDay = dueDay,
                                done = task?.done ?: false,
                                createdAt = task?.createdAt
                                    ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(
                    text = if (task == null) {
                        "Create task"
                    } else {
                        "Save changes"
                    }
                )
            }
        }
    }

    if (showDatePicker) {

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis =
                dueDay?.times(86_400_000L)
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        dueDay = datePickerState
                            .selectedDateMillis
                            ?.div(86_400_000L)

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

@Composable
private fun TaskyLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {

        // Main rounded square
        drawRoundRect(
            color = Color(0xFF0F766E),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                10.dp.toPx(),
                10.dp.toPx()
            )
        )

        // Back paper
        drawRoundRect(
            color = Color(0xFF99F6E4),
            topLeft = androidx.compose.ui.geometry.Offset(
                size.width * 0.24f,
                size.height * 0.20f
            ),
            size = androidx.compose.ui.geometry.Size(
                size.width * 0.48f,
                size.height * 0.58f
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                3.dp.toPx(),
                3.dp.toPx()
            )
        )

        // Main white paper
        drawRoundRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(
                size.width * 0.30f,
                size.height * 0.25f
            ),
            size = androidx.compose.ui.geometry.Size(
                size.width * 0.48f,
                size.height * 0.58f
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                3.dp.toPx(),
                3.dp.toPx()
            )
        )

        // Check mark
        val stroke = 2.5.dp.toPx()

        drawLine(
            color = Color(0xFF0F766E),
            start = androidx.compose.ui.geometry.Offset(
                size.width * 0.40f,
                size.height * 0.54f
            ),
            end = androidx.compose.ui.geometry.Offset(
                size.width * 0.48f,
                size.height * 0.63f
            ),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        drawLine(
            color = Color(0xFF0F766E),
            start = androidx.compose.ui.geometry.Offset(
                size.width * 0.48f,
                size.height * 0.63f
            ),
            end = androidx.compose.ui.geometry.Offset(
                size.width * 0.68f,
                size.height * 0.43f
            ),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}