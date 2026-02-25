package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val regularHours: RegularHours,
    val holidayHours: HoursWindow,
    val commuterPriorityWindows: List<HoursWindow>,
    val holidays: List<String>,
    val crossingDurationMinutes: Int,
    val approximateCapacity: Int,
    val serviceNote: String,
)

@Serializable
data class RegularHours(
    val wisconsinDeparture: HoursWindow,
    val iowaDeparture: HoursWindow,
)

@Serializable
data class HoursWindow(
    val start: String,
    val end: String,
)
