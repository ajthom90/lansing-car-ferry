import SwiftUI

struct ContentView: View {
    var body: some View {
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
    }
}
