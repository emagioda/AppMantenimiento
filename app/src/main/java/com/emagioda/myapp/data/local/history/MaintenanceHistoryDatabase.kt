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
    version = 3,
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

val MAINTENANCE_HISTORY_MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE maintenance_cases
            ADD COLUMN caseCode TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )
        database.execSQL(
            """
            UPDATE maintenance_cases
            SET caseCode = UPPER(machineId) || '-' ||
                strftime('%d%m%Y%H%M', openedAt / 1000, 'unixepoch', 'localtime')
            WHERE TRIM(caseCode) = ''
            """.trimIndent()
        )
    }
}
