package com.tasky.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: Int = 1,          // 0 Low, 1 Medium, 2 High
    val category: String = "Personal",
    val dueDay: Long? = null,       // epoch day
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, priority DESC, dueDay IS NULL, dueDay ASC")
    fun all(): Flow<List<Task>>
    @Upsert suspend fun upsert(task: Task)
    @Delete suspend fun delete(task: Task)
    @Query("DELETE FROM tasks WHERE done = 1") suspend fun clearDone()
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun dao(): TaskDao
    companion object {
        @Volatile private var inst: TaskDatabase? = null
        fun get(c: Context) = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, TaskDatabase::class.java, "tasky.db")
                .build().also { inst = it }
        }
    }
}