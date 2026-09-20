package com.tasky.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tasky.Filter
import com.tasky.TaskViewModel
import com.tasky.data.Task
import kotlinx.coroutines.launch
import java.time.LocalDate

val categories = listOf("Personal", "Work", "Study", "Health")
val priorities = listOf("Low", "Medium", "High")
private val priorityColor = listOf(Color(0xFF22C55E), Color(0xFFF59E0B), Color(0xFFEF4444))

fun dayLabel(day: Long): String {
    val today = LocalDate.now().toEpochDay()
    return when (day - today) {
        0L -> "Today"; 1L -> "Tomorrow"; -1L -> "Yesterday"
        else -> LocalDate.ofEpochDay(day).let { "${it.month.name.take(3).lowercase().replaceFirstChar(Char::uppercase)} ${it.dayOfMonth}" }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskyScreen(vm: TaskViewModel) {
    val tasks by vm.visible.collectAsStateWithLifecycle()
    val all by vm.all.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var sheetFor by remember { mutableStateOf<Task?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Tasky", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    if (all.any { it.done }) TextButton(onClick = { vm.clearDone() }) { Text("Clear done") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { sheetFor = null; showSheet = true },
                icon = { Icon(Icons.Default.Add, null) }, text = { Text("New task") }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { ProgressCard(all) }
            item {
                OutlinedTextField(
                    value = query, onValueChange = { vm.query.value = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(28.dp),
                    placeholder = { Text("Search tasks") }, leadingIcon = { Icon(Icons.Default.Search, null) }
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Filter.entries) { f ->
                        FilterChip(selected = filter == f, onClick = { vm.filter.value = f }, label = { Text(f.label) })
                    }
                }
            }
            if (tasks.isEmpty()) item {
                Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                    Text("Nothing here. Tap New task to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(tasks, key = { it.id }) { t ->
                TaskCard(t, onToggle = { vm.toggle(t) }, onClick = { sheetFor = t; showSheet = true },
                    onDelete = {
                        vm.delete(t)
                        scope.launch {
                            snack.currentSnackbarData?.dismiss()
                            if (snack.showSnackbar("Task deleted", "Undo") == SnackbarResult.ActionPerformed) vm.save(t)
                        }
                    })
            }
        }
    }
    if (showSheet) TaskSheet(sheetFor, onDismiss = { showSheet = false }, onSave = { vm.save(it); showSheet = false })
}

@Composable
fun ProgressCard(all: List<Task>) {
    val done = all.count { it.done }
    val overdue = all.count { !it.done && (it.dueDay ?: Long.MAX_VALUE) < LocalDate.now().toEpochDay() }
    val p = if (all.isEmpty()) 0f else done / all.size.toFloat()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (all.isEmpty()) "Let's get started" else "$done of ${all.size} done", style = MaterialTheme.typography.titleLarge)
            LinearProgressIndicator(progress = { p }, Modifier.fillMaxWidth().height(8.dp).clip(CircleShape))
            if (overdue > 0) Text("$overdue overdue", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(t: Task, onToggle: () -> Unit, onClick: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
    })
    SwipeToDismissBox(
        state = state, enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.errorContainer).padding(end = 24.dp), Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    ) {
        val overdue = !t.done && t.dueDay != null && t.dueDay < LocalDate.now().toEpochDay()
        Card(onClick = onClick, shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Row(Modifier.padding(8.dp, 8.dp, 16.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(t.done, { onToggle() })
                Column(Modifier.weight(1f)) {
                    Text(t.title, style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (t.done) TextDecoration.LineThrough else null,
                        color = if (t.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(t.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        t.dueDay?.let {
                            Text(dayLabel(it), style = MaterialTheme.typography.labelMedium,
                                color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Box(Modifier.size(12.dp).clip(CircleShape).background(priorityColor[t.priority]))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSheet(initial: Task?, onDismiss: () -> Unit, onSave: (Task) -> Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var priority by remember { mutableIntStateOf(initial?.priority ?: 1) }
    var category by remember { mutableStateOf(initial?.category ?: categories[0]) }
    var due by remember { mutableStateOf(initial?.dueDay) }
    var picker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (initial == null) "New task" else "Edit task", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notes") }, minLines = 2)
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                priorities.forEachIndexed { i, label ->
                    SegmentedButton(priority == i, { priority = i }, SegmentedButtonDefaults.itemShape(i, priorities.size)) { Text(label) }
                }
            }
            Text("Category", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { c -> FilterChip(category == c, { category = c }, { Text(c) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ picker = true }) {
                    Icon(Icons.Default.DateRange, null); Spacer(Modifier.width(8.dp)); Text(due?.let(::dayLabel) ?: "Due date")
                }
                if (due != null) TextButton({ due = null }) { Text("Clear") }
            }
            Button(
                onClick = { onSave((initial ?: Task(title = "")).copy(title = title.trim(), notes = notes.trim(), priority = priority, category = category, dueDay = due)) },
                enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
    if (picker) {
        val ds = rememberDatePickerState(initialSelectedDateMillis = due?.times(86_400_000L))
        DatePickerDialog(
            onDismissRequest = { picker = false },
            confirmButton = { TextButton({ ds.selectedDateMillis?.let { due = it / 86_400_000L }; picker = false }) { Text("OK") } },
            dismissButton = { TextButton({ picker = false }) { Text("Cancel") } }
        ) { DatePicker(ds) }
    }
}