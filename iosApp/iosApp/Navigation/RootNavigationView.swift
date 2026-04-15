import SwiftUI

// MARK: - Push destination routes

enum AppRoute: Hashable {
    case habits
    case tasbeeh
    case settings
    case qibla
}

// MARK: - Tab enum

enum AppTab: Int, CaseIterable {
    case home
    case prayer
    case quran
    case athkar
}

// MARK: - Root navigation shell

struct RootNavigationView: View {
    @StateObject private var appSettings = AppSettingsViewModel()
    @State private var selectedTab: AppTab = .home

    // One NavigationPath per tab so each tab keeps its own back-stack
    @State private var homePath  = NavigationPath()
    @State private var prayerPath = NavigationPath()
    @State private var quranPath  = NavigationPath()
    @State private var athkarPath = NavigationPath()

    var body: some View {
        TabView(selection: $selectedTab) {

            // MARK: Home tab
            NavigationStack(path: $homePath) {
                HomeView(
                    onGoToPrayer:   { selectedTab = .prayer },
                    onGoToAthkar:   { selectedTab = .athkar },
                    onGoToQuran:    { selectedTab = .quran },
                    onGoToHabits:   { homePath.append(AppRoute.habits) },
                    onGoToTasbeeh:  { homePath.append(AppRoute.tasbeeh) },
                    onGoToSettings: { homePath.append(AppRoute.settings) },
                    onGoToQibla:    { homePath.append(AppRoute.qibla) }
                )
                .navigationDestination(for: AppRoute.self) { route in
                    pushDestination(route)
                }
            }
            .tabItem {
                Label(String.loc("nav_home"), systemImage: "house.fill")
            }
            .tag(AppTab.home)

            // MARK: Prayer tab
            NavigationStack(path: $prayerPath) {
                PrayerView()
                    .toolbar { settingsToolbarItem(path: $prayerPath) }
                    .navigationDestination(for: AppRoute.self) { route in
                        pushDestination(route)
                    }
            }
            .tabItem {
                Label(String.loc("nav_prayers"), systemImage: "moon.stars.fill")
            }
            .tag(AppTab.prayer)

            // MARK: Quran tab
            NavigationStack(path: $quranPath) {
                QuranView()
                    .toolbar { settingsToolbarItem(path: $quranPath) }
                    .navigationDestination(for: AppRoute.self) { route in
                        pushDestination(route)
                    }
            }
            .tabItem {
                Label(String.loc("nav_quran"), systemImage: "book.fill")
            }
            .tag(AppTab.quran)

            // MARK: Athkar tab
            NavigationStack(path: $athkarPath) {
                AthkarView()
                    .toolbar { settingsToolbarItem(path: $athkarPath) }
                    .navigationDestination(for: AppRoute.self) { route in
                        pushDestination(route)
                    }
            }
            .tabItem {
                Label(String.loc("nav_athkar"), systemImage: "heart.fill")
            }
            .tag(AppTab.athkar)
        }
        .tint(MudawamaTheme.Colors.primary)
        .environment(\.layoutDirection, appSettings.layoutDirection)
        .environment(\.locale, appSettings.locale)
        .preferredColorScheme(appSettings.colorScheme)
        .environmentObject(appSettings)
        .task { appSettings.observe() }
    }

    // MARK: - Push destination builder

    @ViewBuilder
    private func pushDestination(_ route: AppRoute) -> some View {
        switch route {
        case .habits:
            HabitsView()
                .toolbar(.hidden, for: .tabBar)
        case .tasbeeh:
            TasbeehView()
                .toolbar(.hidden, for: .tabBar)
        case .settings:
            SettingsView()
                .toolbar(.hidden, for: .tabBar)
        case .qibla:
            QiblaView()
                .toolbar(.hidden, for: .tabBar)
        }
    }

    // MARK: - Settings toolbar button (trailing)

    @ToolbarContentBuilder
    private func settingsToolbarItem(path: Binding<NavigationPath>) -> some ToolbarContent {
        ToolbarItem(placement: .navigationBarTrailing) {
            Button {
                path.wrappedValue.append(AppRoute.settings)
            } label: {
                Image(systemName: "gearshape.fill")
                    .foregroundStyle(MudawamaTheme.Colors.primary)
            }
        }
    }
}

#Preview {
    RootNavigationView()
}
