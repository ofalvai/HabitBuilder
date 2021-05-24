package com.ofalvai.habittracker.ui.model

import androidx.annotation.FloatRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

data class SingleStats(
    val firstDay: LocalDate?,
    val actionCount: Int,
    val weeklyActionCount: Int,
    @FloatRange(from = 0.0, to = 1.0) val completionRate: Float,
)

data class ActionCountByWeek(
    val year: Year,
    val weekOfYear: Int,
    val actionCount: Int
)

data class ActionCountByMonth(
    val yearMonth: YearMonth,
    val actionCount: Int
)

typealias BucketIndex = Int

data class HeatmapMonth(
    val yearMonth: YearMonth,
    val dayMap: Map<LocalDate, BucketInfo>,
    val totalHabitCount: Int,
    val bucketCount: Int,
    val bucketMaxValues: List<Pair<BucketIndex, Int>>
) {
    data class BucketInfo(
        val bucketIndex: BucketIndex,
        val value: Int // Actual value (habit count on day) that bucketing is based on
    )
}

data class TopHabitItem(
    val habitId: HabitId,
    val name: String,
    val count: Int,
    @FloatRange(from = 0.0, to = 1.0) val progress: Float
)

data class TopDayItem(
    val habitId: HabitId,
    val name: String,
    val day: DayOfWeek,
    val count: Int
)