import Foundation
import UserNotifications
import MudawamaUI

/// Swift implementation of the KMP `NotificationPermissionProvider` interface.
/// Requests `UNUserNotificationCenter` authorisation on first call and schedules
/// a one-shot welcome notification when the user grants permission.
///
/// Also acts as `UNUserNotificationCenterDelegate` so that the welcome notification
/// is displayed even while the app is foregrounded (the common case — the user just
/// tapped "Allow" and is still looking at the screen).
@MainActor
class IosNotificationProvider: NSObject, NotificationPermissionProvider, UNUserNotificationCenterDelegate {

    private let center = UNUserNotificationCenter.current()

    override init() {
        super.init()
        // Register as delegate before any notification can fire.
        center.delegate = self
    }

    // MARK: - NotificationPermissionProvider

    nonisolated func __requestPermissionAndNotifyIfGranted(completionHandler: @escaping @Sendable ((any Error)?) -> Void) {
        Task { @MainActor in
            let center = UNUserNotificationCenter.current()
            let settings = await center.notificationSettings()

            switch settings.authorizationStatus {
            case .notDetermined:
                do {
                    let granted = try await center.requestAuthorization(options: [.alert, .sound, .badge])
                    if granted {
                        await MainActor.run {
                            self.scheduleWelcomeNotification()
                        }
                    }
                    completionHandler(nil)
                } catch {
                    completionHandler(error)
                }

            case .authorized, .provisional, .ephemeral:
                // Already granted on a previous launch — nothing to do.
                completionHandler(nil)

            default:
                // .denied — user has to go to Settings; nothing we can do here.
                completionHandler(nil)
            }
        }
    }

    // Convenience wrapper matching the Kotlin suspend function
    func requestPermissionAndNotifyIfGranted() async {
        await withCheckedContinuation { continuation in
            __requestPermissionAndNotifyIfGranted { _ in
                continuation.resume()
            }
        }
    }

    // MARK: - UNUserNotificationCenterDelegate

    /// Allow notifications to appear as banners even while the app is foregrounded.
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }

    // MARK: - Private

    private func scheduleWelcomeNotification() {
        let content = UNMutableNotificationContent()
        content.title = NSLocalizedString("notification_welcome_title",
                                          value: "Notifications enabled",
                                          comment: "Welcome notification title")
        content.body  = NSLocalizedString("notification_welcome_body",
                                          value: "You'll receive daily Athkar reminders. May Allah accept your dhikr.",
                                          comment: "Welcome notification body")
        content.sound = .default

        // Fire 1 second from now — the delegate ensures it shows in-app.
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(
            identifier: "mudawama.welcome",
            content: content,
            trigger: trigger
        )

        center.add(request) { _ in /* ignore delivery error */ }
    }
}
