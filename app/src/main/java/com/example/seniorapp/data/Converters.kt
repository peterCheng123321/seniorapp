package com.example.seniorapp.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromImportanceLevel(level: ImportanceLevel): String {
        return level.name
    }

    @TypeConverter
    fun toImportanceLevel(value: String): ImportanceLevel {
        return ImportanceLevel.valueOf(value)
    }

    @TypeConverter
    fun fromMemoryType(value: Memory.MemoryType): String {
        return value.name
    }

    @TypeConverter
    fun toMemoryType(value: String): Memory.MemoryType {
        return Memory.MemoryType.valueOf(value)
    }

    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toFloatArray(value: String?): FloatArray? {
        return value?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }
} 