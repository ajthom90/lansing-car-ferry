import SwiftUI

struct ContentView: View {
    @State private var viewModel = FerryViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.ferryInfo == nil {
                ProgressView("Loading ferry info...")
            } else if let ferryInfo = viewModel.ferryInfo {
                TabView {
                    Text("Home")
                        .tabItem {
                            Label("Home", systemImage: "house")
                        }

                    Text("Cameras")
                        .tabItem {
                            Label("Cameras", systemImage: "video")
                        }

                    Text("Info")
                        .tabItem {
                            Label("Info", systemImage: "info.circle")
                        }

                    Text("FAQ")
                        .tabItem {
                            Label("FAQ", systemImage: "questionmark.circle")
                        }
                }
            } else if let error = viewModel.errorMessage {
                ContentUnavailableView(
                    "Unable to Load",
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
