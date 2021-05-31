package com.ofalvai.habittracker.persistence

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds foreign key constraint and cascading delete to Action table.
 * Fixes the issue when deleting a Habit would leave its Actions in the DB and cause weird stats.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `_new_Action` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habit_id` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`habit_id`) REFERENCES `Habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("INSERT INTO `_new_Action` (habit_id,id,timestamp) SELECT habit_id,id,timestamp FROM `Action`")
        database.execSQL("DROP TABLE `Action`")
        database.execSQL("ALTER TABLE `_new_Action` RENAME TO `Action`")
    }
}

val MIGRATIONS = arrayOf(MIGRATION_1_2)