package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VideoEntity::class, CreatorLikeEntity::class], version = 1, exportSchema = false)
abstract class SaamparanDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun creatorLikeDao(): CreatorLikeDao

    companion object {
        @Volatile
        private var INSTANCE: SaamparanDatabase? = null

        fun getDatabase(context: Context): SaamparanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SaamparanDatabase::class.java,
                    "saamparan_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
