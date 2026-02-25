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
                    quickInfoCards
                    locationsSection
                    linksSection
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

    private var quickInfoCards: some View {
        HStack(spacing: 12) {
            InfoCard(
                icon: "clock",
                title: "Crossing",
                value: "\(ferryInfo.schedule.crossingDurationMinutes) min"
            )
            InfoCard(
                icon: "car.2",
                title: "Capacity",
                value: "~\(ferryInfo.schedule.approximateCapacity) vehicles"
            )
            InfoCard(
                icon: "dollarsign.circle",
                title: "Cost",
                value: "FREE"
            )
        }
    }

    private var locationsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Ferry Locations")
                .font(.headline)

            LocationRow(
                label: "Iowa",
                location: ferryInfo.locations.iowa
            )
            LocationRow(
                label: "Wisconsin",
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
