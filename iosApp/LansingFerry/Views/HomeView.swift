import SwiftUI
import MapKit
import Shared

struct HomeView: View {
    let ferryInfo: FerryInfo
    var onRefresh: (() async -> Void)?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    statusBanner
                    facebookNotice
                    quickInfoCards
                    locationsSection
                    linksSection
                    disclaimerNotice
                }
                .padding()
            }
            .refreshable {
                await onRefresh?()
            }
            .navigationTitle("Lansing Car Ferry")
        }
    }

    private var statusBanner: some View {
        VStack(spacing: 4) {
            Text(ferryInfo.schedule.serviceNote)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.blue.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var facebookNotice: some View {
        Link(destination: URL(string: ferryInfo.links.facebook)!) {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundStyle(.orange)
                Text("Check the Facebook page regularly for service updates and schedule changes. No Facebook account is needed to view updates.")
                    .font(.subheadline)
                    .multilineTextAlignment(.leading)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
            .background(.orange.opacity(0.1))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private var quickInfoCards: some View {
        HStack(spacing: 12) {
            InfoCard(
                icon: "clock",
                title: String(localized: "Crossing"),
                value: String(localized: "\(Int(ferryInfo.schedule.crossingDurationMinutes)) min")
            )
            InfoCard(
                icon: "car.2",
                title: String(localized: "Capacity"),
                value: String(localized: "~\(Int(ferryInfo.schedule.approximateCapacity)) vehicles")
            )
            InfoCard(
                icon: "dollarsign.circle",
                title: String(localized: "Cost"),
                value: String(localized: "FREE")
            )
        }
    }

    private var locationsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Ferry Locations")
                .font(.headline)

            LocationRow(
                label: String(localized: "Iowa"),
                location: ferryInfo.locations.iowa
            )
            LocationRow(
                label: String(localized: "Wisconsin"),
                location: ferryInfo.locations.wisconsin
            )
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var linksSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Resources")
                .font(.headline)

            Link(destination: URL(string: ferryInfo.links.facebook)!) {
                Label("Facebook Updates", systemImage: "link")
            }
            Link(destination: URL(string: ferryInfo.links.traffic)!) {
                Label("511 Iowa Traffic", systemImage: "car")
            }
            Link(destination: URL(string: ferryInfo.links.iowadot)!) {
                Label("Iowa DOT Info", systemImage: "globe")
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var disclaimerNotice: some View {
        VStack(spacing: 8) {
            Text("This app is developed by Andrew Thom, an independent developer, and is not affiliated with the States of Iowa or Wisconsin or any government agency. All information is provided on a best-effort basis. Andrew Thom cannot be held liable for any incorrect or outdated information in this application.")
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            HStack(spacing: 16) {
                Link("Privacy Policy", destination: URL(string: "https://cdn.jsdelivr.net/gh/ajthom90/lansing-car-ferry@main/data/privacy-policy.html")!)
                    .font(.caption)
                Link("Terms of Use", destination: URL(string: "https://cdn.jsdelivr.net/gh/ajthom90/lansing-car-ferry@main/data/terms-of-use.html")!)
                    .font(.caption)
            }
        }
        .padding(.top, 8)
    }
}

struct InfoCard: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundStyle(.blue)
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.caption)
                .fontWeight(.semibold)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct LocationRow: View {
    let label: String
    let location: Location

    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(label)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text(location.name)
                    .font(.subheadline)
                Text(location.description_)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Button {
                let coordinate = CLLocationCoordinate2D(
                    latitude: location.latitude,
                    longitude: location.longitude
                )
                let placemark = MKPlacemark(coordinate: coordinate)
                let mapItem = MKMapItem(placemark: placemark)
                mapItem.name = location.name
                mapItem.openInMaps()
            } label: {
                Image(systemName: "map")
                    .font(.title3)
            }
        }
        .padding()
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
