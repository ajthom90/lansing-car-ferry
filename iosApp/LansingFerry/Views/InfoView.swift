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
            .navigationTitle("Ferry Info")
        }
    }

    private var scheduleSection: some View {
        Section("Schedule") {
            LabeledContent("Wisconsin Side") {
                Text(formatRange(ferryInfo.schedule.regularHours.wisconsinDeparture.start, ferryInfo.schedule.regularHours.wisconsinDeparture.end))
            }
            LabeledContent("Iowa Side") {
                Text(formatRange(ferryInfo.schedule.regularHours.iowaDeparture.start, ferryInfo.schedule.regularHours.iowaDeparture.end))
            }
            LabeledContent("Holiday Hours") {
                Text(formatRange(ferryInfo.schedule.holidayHours.start, ferryInfo.schedule.holidayHours.end))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text("Commuter Priority")
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
        Section("Vehicles") {
            DisclosureGroup("Allowed") {
                ForEach(Array(ferryInfo.vehicleRestrictions.allowed), id: \.self) { vehicle in
                    Label(vehicle, systemImage: "checkmark.circle.fill")
                        .foregroundStyle(.green)
                }
            }
            DisclosureGroup("Prohibited") {
                ForEach(Array(ferryInfo.vehicleRestrictions.prohibited), id: \.self) { vehicle in
                    Label(vehicle, systemImage: "xmark.circle.fill")
                        .foregroundStyle(.red)
                }
            }
        }
    }

    private var sizeLimitsSection: some View {
        Section("Size Limits") {
            LabeledContent("Height", value: String(localized: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.heightFeet)) ft"))
            LabeledContent("Length", value: String(localized: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.lengthFeet)) ft"))
            LabeledContent("Width", value: ferryInfo.vehicleRestrictions.sizeLimits.widthFeetInches)
            LabeledContent("Weight", value: String(localized: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.weightTons)) tons"))
        }
    }

    private func formatTime(_ time: String) -> String {
        let parts = time.split(separator: ":")
        guard parts.count == 2,
              let hour = Int(parts[0]),
              let minute = Int(parts[1]) else { return time }
        let period = hour >= 12 ? "PM" : "AM"
        let displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour)
        return minute == 0 ? "\(displayHour) \(period)" : "\(displayHour):\(String(format: "%02d", minute)) \(period)"
    }

    private func formatRange(_ start: String, _ end: String) -> String {
        "\(formatTime(start)) – \(formatTime(end))"
    }
}
