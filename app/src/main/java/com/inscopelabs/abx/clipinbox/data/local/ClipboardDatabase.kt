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
    entities = [ClipEntity::class, CategoryEntity::class, QueueEntity::class, SafPath::class, NamingMacro::class],
    version = 5,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao
    abstract fun categoryDao(): CategoryDao
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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `name` TEXT NOT NULL,
                      `colorHex` TEXT NOT NULL DEFAULT '#5B6EE8',
                      `isDefault` INTEGER NOT NULL DEFAULT 0,
                      `sortOrder` INTEGER NOT NULL DEFAULT 0,
                      `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `categories` (`name`, `colorHex`, `isDefault`, `sortOrder`, `createdAt`)
                    VALUES ('Uncategorized', '#9E9E9E', 1, 0, $now)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `clips_new` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `content` TEXT NOT NULL,
                      `contentHash` TEXT NOT NULL,
                      `detectedType` TEXT NOT NULL DEFAULT 'Text',
                      `categoryId` INTEGER NOT NULL DEFAULT 0,
                      `tags` TEXT NOT NULL DEFAULT '',
                      `isPinned` INTEGER NOT NULL DEFAULT 0,
                      `isFavorite` INTEGER NOT NULL DEFAULT 0,
                      `isArchived` INTEGER NOT NULL DEFAULT 0,
                      `isRead` INTEGER NOT NULL DEFAULT 1,
                      `timestamp` INTEGER NOT NULL,
                      `charCount` INTEGER NOT NULL,
                      `wordCount` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `clips_new` (`id`, `content`, `contentHash`, `detectedType`, `categoryId`, `tags`, `isPinned`, `isFavorite`, `isArchived`, `isRead`, `timestamp`, `charCount`, `wordCount`)
                    SELECT `id`, `content`, `contentHash`, `category` AS `detectedType`,
                           (SELECT `id` FROM `categories` WHERE `isDefault` = 1 LIMIT 1) AS `categoryId`,
                           '' AS `tags`,
                           `isPinned`, `isFavorite`, `isArchived`, `isRead`, `timestamp`, `charCount`, `wordCount`
                    FROM `clips`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `clips` ")
                db.execSQL("ALTER TABLE `clips_new` RENAME TO `clips` ")
            }
        }

        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    "clipinbox_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
