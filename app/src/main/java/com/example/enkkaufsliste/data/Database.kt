package com.example.enkkaufsliste.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "items")
data class ShoppingItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: String = "1",
    val checked: Boolean = false,
    val deleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val dirty: Boolean = true
)

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE deleted = 0 ORDER BY checked ASC, updatedAt DESC")
    fun observeItems(): Flow<List<ShoppingItem>>

    @Upsert
    suspend fun upsert(item: ShoppingItem)

    @Query("UPDATE items SET checked = :checked, updatedAt = :now, dirty = 1 WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean, now: Long)

    @Query("UPDATE items SET deleted = 1, updatedAt = :now, dirty = 1 WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)

    @Query("UPDATE items SET deleted = 1, updatedAt = :now, dirty = 1 WHERE checked = 1 AND deleted = 0")
    suspend fun clearChecked(now: Long)

    @Query("SELECT DISTINCT name FROM items WHERE name LIKE :prefix || '%' ORDER BY name LIMIT 5")
    suspend fun suggestions(prefix: String): List<String>

    @Query("SELECT * FROM items WHERE dirty = 1")
    suspend fun getDirty(): List<ShoppingItem>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: String): ShoppingItem?

    @Query("UPDATE items SET dirty = 0 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markClean(id: String, updatedAt: Long)

    @Query("UPDATE items SET dirty = 1")
    suspend fun markAllDirty()

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}

@Database(entities = [ShoppingItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "einkaufsliste.db"
            ).build().also { instance = it }
        }
    }
}