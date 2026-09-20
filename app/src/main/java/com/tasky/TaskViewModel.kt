package com.tasky

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasky.data.Task
import com.tasky.data.TaskDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Filter(val label: String) { ALL("All"), TODAY("Today"), UPCOMING("Upcoming"), OVERDUE("Overdue"), DONE("Done") }

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = TaskDatabase.get(app).dao()
    val query = MutableStateFlow("")
    val filter = MutableStateFlow(Filter.ALL)

    val all = dao.all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visible = combine(all, query, filter) { list, q, f ->
        val today = LocalDate.now().toEpochDay()
        list.filter { t ->
            (q.isBlank() || t.title.contains(q, true) || t.notes.contains(q, true) || t.category.contains(q, true)) &&
                    when (f) {
                        Filter.ALL -> true
                        Filter.TODAY -> t.dueDay == today && !t.done
                        Filter.UPCOMING -> (t.dueDay ?: -1) > today && !t.done
                        Filter.OVERDUE -> (t.dueDay ?: Long.MAX_VALUE) < today && !t.done
                        Filter.DONE -> t.done
                    }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(t: Task) = viewModelScope.launch { dao.upsert(t) }
    fun toggle(t: Task) = save(t.copy(done = !t.done))
    fun delete(t: Task) = viewModelScope.launch { dao.delete(t) }
    fun clearDone() = viewModelScope.launch { dao.clearDone() }
}