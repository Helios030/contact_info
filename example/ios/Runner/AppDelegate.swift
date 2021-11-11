import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    // 初始化
    _ = AppDataConfig.config
    
    GeneratedPluginRegistrant.register(with: self)
    
    let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
    if let messenger = controller as? FlutterBinaryMessenger {
        let batteryChannel = FlutterMethodChannel.init(name: "contact_info",
                                                       binaryMessenger: messenger );
        
        batteryChannel.setMethodCallHandler { call, result in
            print(call.method)
            switch call.method {
            case "getContact":
                ContactTools.tool.getContact { lists in
                    
                    result(lists)
                    
                }
            case "getAPPs":
                result(AppDataConfig.config.appMessage)
                
                
                
            default:
                break
            }
           
        
            
        }
    }
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
