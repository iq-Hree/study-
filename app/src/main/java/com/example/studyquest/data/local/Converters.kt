package com.example.studyquest.data.local

import androidx.room.TypeConverter
import com.example.studyquest.data.model.CompanionType
import com.example.studyquest.data.model.Difficulty
import com.example.studyquest.data.model.ItemStatus
import com.example.studyquest.data.model.Priority

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority?): String = value?.name ?: Priority.MEDIUM.name

    @TypeConverter
    fun toPriority(value: String?): Priority =
        try { Priority.valueOf(value ?: Priority.MEDIUM.name) } catch (e: Exception) { Priority.MEDIUM }

    @TypeConverter
    fun fromItemStatus(value: ItemStatus?): String = value?.name ?: ItemStatus.NOT_STARTED.name

    @TypeConverter
    fun toItemStatus(value: String?): ItemStatus =
        try { ItemStatus.valueOf(value ?: ItemStatus.NOT_STARTED.name) } catch (e: Exception) { ItemStatus.NOT_STARTED }

    @TypeConverter
    fun fromDifficulty(value: Difficulty?): String = value?.name ?: Difficulty.MEDIUM.name

    @TypeConverter
    fun toDifficulty(value: String?): Difficulty =
        try { Difficulty.valueOf(value ?: Difficulty.MEDIUM.name) } catch (e: Exception) { Difficulty.MEDIUM }

    @TypeConverter
    fun fromCompanionType(value: CompanionType?): String = value?.name ?: CompanionType.CAT.name

    @TypeConverter
    fun toCompanionType(value: String?): CompanionType =
        try { CompanionType.valueOf(value ?: CompanionType.CAT.name) } catch (e: Exception) { CompanionType.CAT }
}
