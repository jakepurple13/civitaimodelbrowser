import SwiftUI
import FirebaseCore
import SwiftUI
import common
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate {
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()
      //MainViewControllerKt.doInitKoinIos(extraModules: [])
    return true
  }
}

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        // Register background tasks
        registerBackgroundTasks()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func registerBackgroundTasks() {
        // let koinIos = KoinIOS()

        // Register periodic sync task
        /* BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "restore",
            using: nil
        ) { task in
            koinIos.getScheduler().handleTask(
                task: task,
                taskIdentifier: "restore"
            )
        } */
    }
}

extension iOSApp {
    func scenePhase(_ phase: ScenePhase) {
        if phase == .background {
            // iOS will execute scheduled tasks when app is in background
            print("App entered background - BGTasks can now execute")
        }
    }
}