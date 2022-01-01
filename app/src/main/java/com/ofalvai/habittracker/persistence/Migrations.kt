/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Adds index to foreign key column to avoid full table scan when the parent table changes
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_Action_habit_id` ON `Action` (`habit_id`)")
    }
}

/**
 * Adds order column to Habit table and use Habit ID as initial order value
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Habit` ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("UPDATE `Habit` SET `order` = `id`")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Habit` ADD COLUMN `archived` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Habit` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
)