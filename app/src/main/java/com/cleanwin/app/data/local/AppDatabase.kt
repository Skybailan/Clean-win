package com.cleanwin.app.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cleanwin.app.data.local.dao.MessageDao
import com.cleanwin.app.data.local.entity.MessageEntity
import com.cleanwin.app.data.local.entity.RuleEntity

@Database(entities = [MessageEntity::class, RuleEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clean_win_database"
                )
                .fallbackToDestructiveMigration() // 允许在版本升级时重建数据库以防止字段冲突导致的崩溃
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
