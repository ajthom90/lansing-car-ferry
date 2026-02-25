import SwiftUI
import Shared

struct InfoView: View {
    let ferryInfo: FerryInfo

    var body: some View {
        NavigationStack {
            List {
                scheduleSection
                vehicleRestrictionsSection
                sizeLimitsSection
            }
            .navigationTitle(String(localized: "Ferry Info"))
        }
    }

    private var scheduleSection: some View {
        Section(String(localized: "Schedule")) {
            LabeledContent(String(localized: "Wisconsin Side")) {
                Text(formatRange(ferryInfo.schedule.regularHours.wisconsinDeparture.start, ferryInfo.schedule.regularHours.wisconsinDeparture.end))
            }
            LabeledContent(String(localized: "Iowa Side")) {
                Text(formatRange(ferryInfo.schedule.regularHours.iowaDeparture.start, ferryInfo.schedule.regularHours.iowaDeparture.end))
            }
            LabeledContent(String(localized: "Holiday Hours")) {
                Text(formatRange(ferryInfo.schedule.holidayHours.start, ferryInfo.schedule.holidayHours.end))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(String(localized: "Commuter Priority"))
                    .font(.subheadline)
                ForEach(Array(ferryInfo.schedule.commuterPriorityWindows), id: \.start) { window in
                    Text(formatRange(window.start, window.end))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    private var vehicleRestrictionsSection: some View {
        Section(String(localized: "Vehicles")) {
            DisclosureGroup(String(localized: "Allowed")) {
                ForEach(Array(ferryInfo.vehicleRestrictions.allowed), id: \.self) { vehicle in
                    Label(vehicle, systemImage: "checkmark.circle.fill")
                        .foregroundStyle(.green)
                }
            }
            DisclosureGroup(String(localized: "Prohibited")) {
                ForEach(Array(ferryInfo.vehicleRestrictions.prohibited), id: \.self) { vehicle in
                    Label(vehicle, systemImage: "xmark.circle.fill")
                        .foregroundStyle(.red)
                }
            }
        }
    }

    private var sizeLimitsSection: some View {
        Section(String(localized: "Size Limits")) {
            LabeledContent(String(localized: "Height"), value: String(localized: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.heightFeet)) ft"))
            LabeledContent(String(localized: "Length"), value: String(localized: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.lengthFeet)) ft"))
            LabeledContent(String(localized: "Width"), value: ferryInfo.vehicleRestrictions.sizeLimits.widthFeetInches)
            LabeledContent(String(localized: "Weight"), value: String(localized: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.weightTons)) tons"))
        }
    }

    private func formatTime(_ time: String) -> String {
        let parts = time.split(separator: ":")
        guard parts.count == 2,
              let hour = Int(parts[0]),
              let minute = Int(parts[1]) else { return time }
        let period = hour >= 12 ? String(localized: "PM") : String(localized: "AM")
        let displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour)
        return minute == 0 ? "\(displayHour) \(period)" : "\(displayHour):\(String(format: "%02d", minute)) \(period)"
    }

    private func formatRange(_ start: String, _ end: String) -> String {
        "\(formatTime(start)) – \(formatTime(end))"
    }
}
