import SwiftUI
import Shared

struct LiveCamerasView: View {
    let cameras: [Camera]

    var body: some View {
        NavigationStack {
            List(cameras, id: \.id) { camera in
                NavigationLink(destination: CameraDetailView(camera: camera)) {
                    HStack(spacing: 12) {
                        AsyncImage(url: URL(string: camera.snapshotUrl)) { image in
                            image
                                .resizable()
                                .aspectRatio(16/9, contentMode: .fill)
                        } placeholder: {
                            Rectangle()
                                .fill(.quaternary)
                                .aspectRatio(16/9, contentMode: .fill)
                                .overlay {
                                    ProgressView()
                                }
                        }
                        .frame(width: 120, height: 68)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                        Text(camera.name)
                            .font(.subheadline)
                    }
                }
            }
            .navigationTitle(String(localized: "Live Cameras"))
        }
    }
}
