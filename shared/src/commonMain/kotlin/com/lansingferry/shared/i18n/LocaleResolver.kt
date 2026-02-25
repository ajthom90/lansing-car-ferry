package com.lansingferry.shared.i18n

import com.lansingferry.shared.model.*
import com.lansingferry.shared.model.raw.*

object LocaleResolver {

    fun resolve(translations: LocalizedString, locale: String): String {
        translations[locale]?.let { return it }
        val lang = locale.split("-", "_").first()
        if (lang != locale) {
            translations[lang]?.let { return it }
        }
        return translations["en"] ?: translations.values.firstOrNull() ?: ""
    }

    fun resolve(raw: RawFerryInfo, locale: String): FerryInfo {
        return FerryInfo(
            schedule = resolveSchedule(raw.schedule, locale),
            locations = Locations(
                iowa = resolveLocation(raw.locations.iowa, locale),
                wisconsin = resolveLocation(raw.locations.wisconsin, locale),
            ),
            cameras = raw.cameras.map { resolveCamera(it, locale) },
            vehicleRestrictions = resolveVehicleRestrictions(raw.vehicleRestrictions, locale),
            faqs = raw.faqs.map { resolveFAQ(it, locale) },
            contact = Contact(name = raw.contact.name, email = raw.contact.email),
            links = Links(
                facebook = raw.links.facebook,
                traffic = raw.links.traffic,
                iowadot = raw.links.iowadot,
            ),
        )
    }

    private fun resolveSchedule(raw: RawSchedule, locale: String): Schedule {
        return Schedule(
            regularHours = RegularHours(
                wisconsinDeparture = HoursWindow(
                    start = raw.regularHours.wisconsinDeparture.start,
                    end = raw.regularHours.wisconsinDeparture.end,
                ),
                iowaDeparture = HoursWindow(
                    start = raw.regularHours.iowaDeparture.start,
                    end = raw.regularHours.iowaDeparture.end,
                ),
            ),
            holidayHours = HoursWindow(
                start = raw.holidayHours.start,
                end = raw.holidayHours.end,
            ),
            commuterPriorityWindows = raw.commuterPriorityWindows.map {
                HoursWindow(start = it.start, end = it.end)
            },
            holidays = raw.holidays,
            crossingDurationMinutes = raw.crossingDurationMinutes,
            approximateCapacity = raw.approximateCapacity,
            serviceNote = resolve(raw.serviceNote, locale),
        )
    }

    private fun resolveLocation(raw: RawLocation, locale: String): Location {
        return Location(
            name = resolve(raw.name, locale),
            description = resolve(raw.description, locale),
            latitude = raw.latitude,
            longitude = raw.longitude,
        )
    }

    private fun resolveCamera(raw: RawCamera, locale: String): Camera {
        return Camera(
            id = raw.id,
            name = resolve(raw.name, locale),
            streamUrl = raw.streamUrl,
            snapshotUrl = raw.snapshotUrl,
        )
    }

    private fun resolveVehicleRestrictions(raw: RawVehicleRestrictions, locale: String): VehicleRestrictions {
        return VehicleRestrictions(
            allowed = raw.allowed.map { resolve(it, locale) },
            prohibited = raw.prohibited.map { resolve(it, locale) },
            sizeLimits = SizeLimits(
                heightFeet = raw.sizeLimits.heightFeet,
                lengthFeet = raw.sizeLimits.lengthFeet,
                widthFeetInches = resolve(raw.sizeLimits.widthFeetInches, locale),
                weightTons = raw.sizeLimits.weightTons,
            ),
        )
    }

    private fun resolveFAQ(raw: RawFAQ, locale: String): FAQ {
        return FAQ(
            question = resolve(raw.question, locale),
            answer = resolve(raw.answer, locale),
        )
    }
}
