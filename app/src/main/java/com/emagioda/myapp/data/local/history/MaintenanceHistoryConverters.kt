package com.emagioda.myapp.data.local.history

import androidx.room.TypeConverter
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.MaintenanceStatus

class MaintenanceHistoryConverters {

    @TypeConverter
    fun fromEndResult(value: EndResult): String = value.name

    @TypeConverter
    fun toEndResult(value: String): EndResult = EndResult.valueOf(value)

    @TypeConverter
    fun fromMaintenanceStatus(value: MaintenanceStatus): String = value.name

    @TypeConverter
    fun toMaintenanceStatus(value: String): MaintenanceStatus = MaintenanceStatus.valueOf(value)

    @TypeConverter
    fun fromMaintenanceEventType(value: MaintenanceEventType): String = value.name

    @TypeConverter
    fun toMaintenanceEventType(value: String): MaintenanceEventType =
        MaintenanceEventType.valueOf(value)
}
