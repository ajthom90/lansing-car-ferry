import SwiftUI

struct ContentView: View {
    @State private var viewModel = FerryViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.ferryInfo == nil {
                ProgressView(String(localized: "Loading ferry info..."))
            } else if let ferryInfo = viewModel.ferryInfo {
                TabView {
                    HomeView(ferryInfo: ferryInfo) {
                            await viewModel.refresh()
                        }
                        .tabItem {
                            Label(String(localized: "Home"), systemImage: "house")
                        }

                    LiveCamerasView(cameras: ferryInfo.cameras)
                        .tabItem {
                            Label(String(localized: "Cameras"), systemImage: "video")
                        }

                    InfoView(ferryInfo: ferryInfo)
                        .tabItem {
                            Label(String(localized: "Info"), systemImage: "info.circle")
                        }

                    FAQView(faqs: ferryInfo.faqs)
                        .tabItem {
                            Label(String(localized: "FAQ"), systemImage: "questionmark.circle")
                        }
                }
            } else if let error = viewModel.errorMessage {
                ContentUnavailableView(
                    String(localized: "Unable to Load"),
                    systemImage: "wifi.slash",
                    description: Text(error)
                )
            }
        }
        .task {
            await viewModel.loadData()
        }
    }
}
