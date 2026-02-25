import SwiftUI
import Shared

struct FAQView: View {
    let faqs: [FAQ]

    var body: some View {
        NavigationStack {
            List(faqs, id: \.question) { faq in
                DisclosureGroup {
                    Text(faq.answer)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } label: {
                    Text(faq.question)
                        .font(.subheadline)
                }
            }
            .navigationTitle("FAQ")
        }
    }
}
