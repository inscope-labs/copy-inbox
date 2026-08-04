package com.inscopelabs.abx.clipinbox.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inscopelabs.abx.clipinbox.domain.queue.QueueDao
import com.inscopelabs.abx.clipinbox.domain.queue.QueueEntity

@Database(
    entities = [ClipEntity::class, QueueEntity::class, SafPath::class, NamingMacro::class],
    version = 4,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao
    abstract fun queueDao(): QueueDao
    abstract fun safPathDao(): SafPathDao
    abstract fun namingMacroDao(): NamingMacroDao

    companion object {
        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clips ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE clips ADD COLUMN isRead INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `queue` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `suggestedName` TEXT NOT NULL,
                      `type` TEXT NOT NULL,
                      `content` TEXT NOT NULL,
                      `mime` TEXT,
                      `sourceUri` TEXT,
                      `createdAt` INTEGER NOT NULL,
                      `attempts` INTEGER NOT NULL DEFAULT 0,
                      `lastError` TEXT,
                      `state` TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saf_paths` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `label` TEXT NOT NULL,
                      `treeUri` TEXT NOT NULL,
                      `lastUsedAt` INTEGER NOT NULL DEFAULT 0,
                      `seqCounter` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `naming_macros` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `label` TEXT NOT NULL,
                      `template` TEXT NOT NULL,
                      `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    "clipinbox_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
