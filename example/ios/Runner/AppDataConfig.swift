//
//  AppDataConfig.swift
//  Runner
//
//  Created by JcKeats on 2021/11/10.
//

import Foundation
import SystemConfiguration.CaptiveNetwork
import CoreTelephony
class AppDataConfig:NSObject {
    static let config = AppDataConfig()
    
    var currentDeviceJson:[String:String]?
    
    var back_num = 0
    var screen_Num = 0
    private override init() {
        super.init()
        currentDeviceJson = deviceJson.first { dict in
            return deviceName.contains(dict["model_id"] ?? "")
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(appBecomeActive), name: UIApplication.willEnterForegroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(takeScreenShot), name: UIApplication.userDidTakeScreenshotNotification, object: nil)

    }
    
    
    private lazy var deviceJson:[[String:String]] = {
       
        guard let path = Bundle.main.path(forResource: "device", ofType: "json") , let data = try? Data(contentsOf: URL(fileURLWithPath: path)) , let json = try? JSONSerialization.jsonObject(with: data, options: JSONSerialization.ReadingOptions.allowFragments) as? [[String:String]] else {
            return []
        }
        
        return json
        
    }()
    
    @objc func appBecomeActive() {
        back_num += 1
    }
    @objc func takeScreenShot() {
        screen_Num += 1
    }
    
    /// 启动时间
    private let launchTime = Date()
    
    /// 启动时电量
    private let startPower = UIDevice.current.batteryLevel
    
    
    /// 品牌
    private var brand:String {
        
        let device = UIDevice.current.userInterfaceIdiom
        
        switch device {
        case .carPlay:
            return "CarPlay"
        case .mac:
            return "Mac"
        case .pad:
            return "iPad"
        case .phone:
            return "iPhone"
        case .tv:
            return "AppleTV"
        default:
            return ""
        }
    }
    
    /// 设备型号
    private var deviceName:String {
        let device = UIDevice.current
        return device.name
    }
    
    /// cpu
    private var cpu:String {
        return currentDeviceJson?["cpu"] ?? ""
    }
    
    /// cpu 核心数
    private var cpuCores:String {
        var ncpu: UInt = UInt(0)
        var len: size_t = MemoryLayout.size(ofValue: ncpu)
        sysctlbyname("hw.ncpu", &ncpu, &len, nil, 0)
        return String(ncpu)
        
    }
    
    /// 运行内存
    var ram:String {
        return fileSizeToString(fileSize: Int64(ProcessInfo.processInfo.physicalMemory))
        
    }
    
    /// rom
    var rom:String {
        return fileSizeToString(fileSize: UIDevice.current.TotalDiskSize+UIDevice.current.AvailableDiskSize)
    }
   
    
    /// 将大小转换成字符串用以显示
    func fileSizeToString(fileSize:Int64) -> String{
      
      let fileSize1 = CGFloat(fileSize)
      
      let KB:CGFloat = 1024
      let MB:CGFloat = KB*KB
      let GB:CGFloat = MB*KB
      
      if fileSize < 10
      {
        return "0 B"
        
      }else if fileSize1 < KB
      {
        return "< 1 KB"
      }else if fileSize1 < MB
      {
        return String(format: "%.1f KB", CGFloat(fileSize1)/KB)
      }else if fileSize1 < GB
      {
        return String(format: "%.1f MB", CGFloat(fileSize1)/MB)
      }else
      {
        return String(format: "%.1f GB", CGFloat(fileSize1)/GB)
      }
    }
    
    /// 屏幕分辨率
    var resolution:String {
        let width = UIScreen.main.bounds.size.width
        let height = UIScreen.main.bounds.size.height
        let scale = UIScreen.main.scale
        return String(format: "%.0f x %.0f", width*scale,height*scale)
    }
    
    /// 开启时间
    var open_time:String {
        let dataFormater = DateFormatter()
        dataFormater.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return dataFormater.string(from: launchTime)
    }
    
    /// 当前系统版本
    var currentVersion:String {
        return UIDevice.current.systemVersion
    }
    
    /// 设备当前电量
    var currentPower:String {
        return String(format: "%.0f", UIDevice.current.batteryLevel*100)
    }
    
    /// wifi名称
    var wifiNameAndMac:(wifiName:String,mac:String) {
        
        guard let wifiInterfaces = CNCopySupportedInterfaces() else {
            return ("","")
        }
        
        guard let interfaceArr = CFBridgingRetain(wifiInterfaces) as? [String] , interfaceArr.count > 0 else {
            return ("","")
        }
        let interfaceName = interfaceArr[0] as CFString
        guard let ussafeInterfaceData = CNCopyCurrentNetworkInfo(interfaceName) as? [String:Any] else {
            return ("","")
        }
        
        guard let interfaceData = ussafeInterfaceData["SSID"] as? String , let mac = ussafeInterfaceData["BSSID"] as? String else {
            return ("","")
        }
        
        return (interfaceData,mac)
        
        
    }
    
    
    var netName:String {
        var netConntype = ""
        let reachability = Reachability.init(hostName: "www.apple.com")
        let netState = reachability?.currentReachabilityStatus().rawValue
        switch netState {
        case 0:
            netConntype = "no network"
        case 1:
            netConntype = "wifi"
        case 2:
            let info = CTTelephonyNetworkInfo()
            if let currentStatus = info.currentRadioAccessTechnology {
                netConntype = currentStatus
            }
            
        default:
            break
        }
        return netConntype
    }
    
    var currentTimeZone:String {
        
        let localZone = NSTimeZone.local
        let second = localZone.secondsFromGMT()
        
        return String(format: "%d", second / 3600)
        
    }
    
    
    var appMessage:[[String:Any]] {
       
        let wifiTump = wifiNameAndMac
        
        return  [[
            "brands":brand,
            "mobile_model":deviceName,
            "cpu_model":cpu,
            "cpu_cores":cpuCores,
            "ram":ram,
            "rom":rom,
            "resolution":resolution,
            "open_power":String(format: "%.0f", startPower),
            "open_time":open_time,
            "version":currentVersion,
            "complete_apply_power":currentPower,
            "back_num": back_num,
            "screen":screen_Num,
            "root":0,
            "wifi_name":wifiTump.wifiName,
            "wifi_mac":wifiTump.mac,
            "wifi_state":"",
            "real_machine":1,
            "hit_num":"0",
            "gaid":"",
            "network":netName,
            "time_zone":currentTimeZone
            
        ]]
        
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
}

extension UIDevice {
    
    func blankof<T>(type:T.Type) -> T {
        let ptr = UnsafeMutablePointer<T>.allocate(capacity: MemoryLayout<T>.size)
        let val = ptr.pointee
        ptr.deallocate()
        return val
      }
    
    /// 磁盘总大小
    var TotalDiskSize:Int64{
        var fs = blankof(type: statfs.self)
        if statfs("/var",&fs) >= 0{
          return Int64(UInt64(fs.f_bsize) * fs.f_blocks)
        }
        return -1
      }
   
    /// 磁盘可用大小
      var AvailableDiskSize:Int64{
        var fs = blankof(type: statfs.self)
        if statfs("/var",&fs) >= 0{
          return Int64(UInt64(fs.f_bsize) * fs.f_bavail)
        }
        return -1
      }
      
}
