import Foundation
import Shared

@Observable
final class FerryViewModel {
    var ferryInfo: FerryInfo?
    var isLoading = false
    var errorMessage: String?

    private let repository = FerryRepository.companion.create()

    private var deviceLocale: String {
        Locale.current.language.languageCode?.identifier ?? "en"
    }

    func loadData() async {
        isLoading = true
        errorMessage = nil

        do {
            let result = try await repository.getFerryInfo(locale: deviceLocale)

            switch onEnum(of: result) {
            case .success(let success):
                self.ferryInfo = success.data
                self.errorMessage = nil
            case .error(let error):
                self.errorMessage = error.message
            }
        } catch {
            self.errorMessage = String(localized: "An unexpected error occurred.")
        }

        isLoading = false
    }

    func refresh() async {
        do {
            let result = try await repository.refresh(locale: deviceLocale)

            switch onEnum(of: result) {
            case .success(let success):
                self.ferryInfo = success.data
                self.errorMessage = nil
            case .error(let error):
                self.errorMessage = error.message
            }
        } catch {
            // Silently ignore refresh errors if we already have data
        }
    }
}
