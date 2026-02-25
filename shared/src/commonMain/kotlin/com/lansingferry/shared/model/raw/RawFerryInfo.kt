package com.lansingferry.shared.model.raw

import kotlinx.serialization.Serializable

typealias LocalizedString = Map<String, String>

@Serializable
data class RawFerryInfo(
    val schedule: RawSchedule,
    val locations: RawLocations,
    val cameras: List<RawCamera>,
    val vehicleRestrictions: RawVehicleRestrictions,
    val faqs: List<RawFAQ>,
    val contact: RawContact,
    val links: RawLinks,
)

@Serializable
data class RawSchedule(
    val regularHours: RawRegularHours,
    val holidayHours: RawHoursWindow,
    val commuterPriorityWindows: List<RawHoursWindow>,
    val holidays: List<String>,
    val crossingDurationMinutes: Int,
    val approximateCapacity: Int,
    val serviceNote: LocalizedString,
)

@Serializable
data class RawRegularHours(
    val wisconsinDeparture: RawHoursWindow,
    val iowaDeparture: RawHoursWindow,
)

@Serializable
data class RawHoursWindow(
    val start: String,
    val end: String,
)

@Serializable
data class RawLocations(
    val iowa: RawLocation,
    val wisconsin: RawLocation,
)

@Serializable
data class RawLocation(
    val name: LocalizedString,
    val description: LocalizedString,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class RawCamera(
    val id: String,
    val name: LocalizedString,
    val streamUrl: String,
    val snapshotUrl: String,
)

@Serializable
data class RawVehicleRestrictions(
    val allowed: List<LocalizedString>,
    val prohibited: List<LocalizedString>,
    val sizeLimits: RawSizeLimits,
)

@Serializable
data class RawSizeLimits(
    val heightFeet: Int,
    val lengthFeet: Int,
    val widthFeetInches: LocalizedString,
    val weightTons: Int,
)

@Serializable
data class RawFAQ(
    val question: LocalizedString,
    val answer: LocalizedString,
)

@Serializable
data class RawContact(
    val name: String,
    val email: String,
)

@Serializable
data class RawLinks(
    val facebook: String,
    val traffic: String,
    val iowadot: String,
)
