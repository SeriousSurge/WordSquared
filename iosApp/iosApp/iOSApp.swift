import SwiftUI
import shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has its own keyboard handling
                .background(Color(red: 253/255, green: 246/255, blue: 227/255)) // App background color
                .ignoresSafeArea(.container, edges: .all) // Extend background to edges
        }
    }
}
