package com.example.flashcardstudy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Category::class, Flashcard::class, ReviewLog::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun flashcardDao(): FlashcardDao

    abstract fun reviewLogDao(): ReviewLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Adds the `source` column (TEXT, default 'MANUAL') introduced in v3. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE flashcards ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'"
                )
            }
        }

        /** Adds the `sourceLabel` column (TEXT, nullable) introduced in v4. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE flashcards ADD COLUMN sourceLabel TEXT"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashcards.db",
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}