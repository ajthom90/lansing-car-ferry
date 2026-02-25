package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class FerryInfo(
    val schedule: Schedule,
    val locations: Locations,
    val cameras: List<Camera>,
    val vehicleRestrictions: VehicleRestrictions,
    val faqs: List<FAQ>,
    val contact: Contact,
    val links: Links,
)

@Serializable
data class Locations(
    val iowa: Location,
    val wisconsin: Location,
)

@Serializable
data class Contact(
    val name: String,
    val email: String,
)

@Serializable
data class Links(
    val facebook: String,
    val traffic: String,
    val iowadot: String,
)
