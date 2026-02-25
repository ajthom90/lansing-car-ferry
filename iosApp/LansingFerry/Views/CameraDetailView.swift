import SwiftUI
import Shared

struct CameraDetailView: View {
    let camera: Camera
    @State private var isFullScreen = false

    var body: some View {
        VStack {
            if let url = URL(string: camera.streamUrl) {
                VideoPlayerView(url: url)
                    .aspectRatio(16/9, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding()
                    .onTapGesture {
                        isFullScreen = true
                    }
            }

            Spacer()
        }
        .navigationTitle(camera.name)
        .fullScreenCover(isPresented: $isFullScreen) {
            if let url = URL(string: camera.streamUrl) {
                VideoPlayerView(url: url)
                    .ignoresSafeArea()
                    .overlay(alignment: .topTrailing) {
                        Button {
                            isFullScreen = false
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.title)
                                .foregroundStyle(.white)
                                .padding()
                        }
                    }
            }
        }
    }
}
