package cz.palda97.lpclient.model.db

import androidx.room.TypeConverter
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromExecutionStatus(executionStatus: ExecutionStatus?): Int? {
        return executionStatus?.let {
            when (it) {
                ExecutionStatus.FAILED -> 0
                ExecutionStatus.FINISHED -> 1
                ExecutionStatus.RUNNING -> 2
                ExecutionStatus.CANCELLED -> 3
                ExecutionStatus.DANGLING -> 4
                ExecutionStatus.CANCELLING -> 5
                ExecutionStatus.QUEUED -> 6
            }
        }
    }

    @TypeConverter
    fun toExecutionStatus(value: Int?): ExecutionStatus? {
        return value?.let {
            when (it) {
                0 -> ExecutionStatus.FAILED
                1 -> ExecutionStatus.FINISHED
                2 -> ExecutionStatus.RUNNING
                3 -> ExecutionStatus.CANCELLED
                4 -> ExecutionStatus.DANGLING
                5 -> ExecutionStatus.CANCELLING
                6 -> ExecutionStatus.QUEUED
                else -> null
            }
        }
    }
}