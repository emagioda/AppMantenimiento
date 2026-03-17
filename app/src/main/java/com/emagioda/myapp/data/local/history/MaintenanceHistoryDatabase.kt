package com.emagioda.myapp.data.local.history

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MaintenanceCaseEntity::class,
        MaintenanceEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(MaintenanceHistoryConverters::class)
abstract class MaintenanceHistoryDatabase : RoomDatabase() {
    abstract fun maintenanceHistoryDao(): MaintenanceHistoryDao
}

val MAINTENANCE_HISTORY_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE maintenance_cases
            ADD COLUMN canceledAt INTEGER
            """.trimIndent()
        )
        database.execSQL(
            """
            ALTER TABLE maintenance_cases
            ADD COLUMN cancellationReason TEXT
            """.trimIndent()
        )
    }
}
