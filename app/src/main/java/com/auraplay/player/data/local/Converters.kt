package com.auraplay.player.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String = value?.joinToString(",") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> = value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
}
