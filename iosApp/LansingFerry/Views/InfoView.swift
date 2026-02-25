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
                contactSection
            }
            .navigationTitle("Ferry Info")
        }
    }

    private var scheduleSection: some View {
        Section("Schedule") {
            LabeledContent("Wisconsin Side") {
                Text("\(ferryInfo.schedule.regularHours.wisconsinDeparture.start) – \(ferryInfo.schedule.regularHours.wisconsinDeparture.end)")
            }
            LabeledContent("Iowa Side") {
                Text("\(ferryInfo.schedule.regularHours.iowaDeparture.start) – \(ferryInfo.schedule.regularHours.iowaDeparture.end)")
            }
            LabeledContent("Holiday Hours") {
                Text("\(ferryInfo.schedule.holidayHours.start) – \(ferryInfo.schedule.holidayHours.end)")
            }

            VStack(alignment: .leading, spacing: 4) {
                Text("Commuter Priority")
                    .font(.subheadline)
                ForEach(Array(ferryInfo.schedule.commuterPriorityWindows), id: \.start) { window in
                    Text("\(window.start) – \(window.end)")
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
            LabeledContent("Height", value: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.heightFeet)) ft")
            LabeledContent("Length", value: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.lengthFeet)) ft")
            LabeledContent("Width", value: ferryInfo.vehicleRestrictions.sizeLimits.widthFeetInches)
            LabeledContent("Weight", value: "\(Int(ferryInfo.vehicleRestrictions.sizeLimits.weightTons)) tons")
        }
    }

    private var contactSection: some View {
        Section("Contact") {
            LabeledContent("Name", value: ferryInfo.contact.name)
            Link(ferryInfo.contact.email, destination: URL(string: "mailto:\(ferryInfo.contact.email)")!)
        }
    }
}
