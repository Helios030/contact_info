package com.neutron.contact_info
import android.text.TextUtils
import androidx.annotation.NonNull
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import java.text.SimpleDateFormat


import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs


/** ContactInfoPlugin */
class ContactInfoPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context :Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context=flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "contact_info")
    channel.setMethodCallHandler(this)
  }
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if(call.method == "getContact"){
      result.success(getAllContacts(context))
    }else if(call.method == "getAPPs"){
      result.success(getAppList(context))
    }else f(call.method == "getDeviceInfo"){
      result.success(getDeviceInfo(context))
    }else{
      result.notImplemented()
    }
  }
  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}

fun getAllContacts(context: Context): ArrayList<HashMap<String,String>>? {
  val contacts: ArrayList<HashMap<String,String>> = ArrayList<HashMap<String,String>>()
  val cursor = context.contentResolver.query(
    ContactsContract.Contacts.CONTENT_URI, null, null, null, null
  )
  while (cursor!!.moveToNext()) {
    //???????????????????????????
    val temp = HashMap<String,String>()
    val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
    //?????????????????????
    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
    val time = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP))
    temp["other_name"]=(name)
    temp["last_time"]=(time)

    //???????????????????????????
    val phoneCursor = context.contentResolver.query(
      ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
      null,
      ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
      null,
      null
    )
    while (phoneCursor!!.moveToNext()) {
      var phone =   phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
      phone = phone.replace("-", "")
      phone = phone.replace(" ", "")
      temp["other_mobile"]=(phone)
    }
    contacts.add(temp)
    //????????????cursor???close???
    phoneCursor.close()
  }
  cursor.close()
  return contacts
}

fun getAppList(context: Context): List<HashMap<String,String>>? {
  val appLists: MutableList<HashMap<String,String>> = ArrayList<HashMap<String,String>>()
  var map: HashMap<String,String>
  val pm = context.packageManager
  val list = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)
  for (packageInfo in list) {
    val appName =  packageInfo.applicationInfo.loadLabel(context.packageManager).toString()
    val packageName = packageInfo.packageName
    val isSYS: Boolean =
      isSystemApp(packageInfo)
    if (!TextUtils.isEmpty(appName) && !TextUtils.isEmpty(packageName)) {
      map = HashMap<String,String>()
      map["firstTime"]=formatTime(packageInfo.firstInstallTime, "yyyy-MM-dd HH:mm:ss").toString()
      map["lastTime"]=   formatTime(
        packageInfo.lastUpdateTime,
        "yyyy-MM-dd HH:mm:ss"
      ).toString()
      map["name"]=appName
      map["packageName"]=packageName
      map["versionCode"]=packageInfo.versionName
      map["systemApp"]=if (isSYS) "1" else "2"
      appLists.add(map)
    }
  }

  return appLists
}


fun getDeviceInfo (context: Context): HashMap<String,String>? {
 val  map = HashMap<String,String>()
//  ??????




  return map;
}

fun isSystemApp(pInfo: PackageInfo): Boolean {
  //???????????????????????????
  return pInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}

fun formatTime(date: Long, format: String?): String? {
  val formatter = SimpleDateFormat(format)
  return formatter.format(date).toString()
}



fun getBrands(): String {
  return android.os.Build.BRAND
}

/**
 * ??????????????????
 *
 * @return
 */
fun getMobil(): String {
  return android.os.Build.MODEL
}

/**
 * CPU??????
 *
 * @return
 */
fun getCpuModel(): String {
  return android.os.Build.CPU_ABI
}


/**
 * ?????????????????????????????????
 */
fun getSystemVersion(): String {
  return Build.VERSION.RELEASE
}


**
* ???????????????
* @return
*/
fun getResolution(): String {
  val metric = DisplayMetrics()
  val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  windowManager.defaultDisplay.getRealMetrics(metric)
  val width = metric.widthPixels
  val height = metric.heightPixels;
  val densityDpi = metric.densityDpi;
  return "$width??$height -$densityDpi"
}

/**
 * ??????wifi??????
 */
@SuppressLint("WifiManagerPotentialLeak", "MissingPermission")
//    @Deprecated("WifiName")
fun getWifiName(): String {
  val wifiMgr: WifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  val info: WifiInfo = wifiMgr.connectionInfo

  val networkId: Int = info.networkId
  val configuredNetworks: MutableList<WifiConfiguration> = wifiMgr.configuredNetworks
  var ssid = ""
  run breaking@{
    configuredNetworks.forEach {
      if (it.networkId == networkId) {
        ssid = it.SSID
        return@breaking
      }
    }
  }
  if (ssid.contains("\"")) {
    ssid = ssid.replace("\"", "")
  }
  return ssid
}

@Suppress("DEPRECATION")
fun getNewWifiName(): String {
  val ssid = "unknown id"

  if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    val wifiMgr: WifiManager =
      mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val info = wifiMgr.connectionInfo
    return info.ssid.replace("\"", "")
  } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
    val connManager: ConnectivityManager = mContext
      .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo: NetworkInfo? = connManager.activeNetworkInfo
    if (networkInfo?.isConnected == true) {
      if (networkInfo.extraInfo != null) {
        return networkInfo.extraInfo.replace("\"", "")
      }
    }
  }
  return ssid
}

/**
 * ??????wifi mac ??????
 */
fun getWifiMac(): String {
  val networkInterfaces: Enumeration<NetworkInterface> =
    NetworkInterface.getNetworkInterfaces()
  while (networkInterfaces.hasMoreElements()) {
    val element: NetworkInterface? = networkInterfaces.nextElement()
    val address: ByteArray = element?.hardwareAddress ?: continue
    if (element.name == "wlan0") {
      val builder = StringBuilder()
      address.forEach {
        builder.append(String.format("%02X:", it))
      }
      if (builder.isNotEmpty()) {
        builder.deleteCharAt(builder.length - 1)
      }
      return builder.toString()
    }
  }
  return ""
}

/**
 * ??????wifi???????????? state
 */
@SuppressLint("WifiManagerPotentialLeak")
fun getWifiState(): String {
  val wifiMgr: WifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  val info: WifiInfo = wifiMgr.connectionInfo
  if (info.ssid != null) {
    val strength: Int = WifiManager.calculateSignalLevel(info.rssi, 5)
    return strength.toString()
  }
  return ""
}


/*
 * ??????App??????
 * @return [Int] ??????
 */
var openAppBatteryLevel: Int? = 0

/**
 * ????????????
 * @return [Int] ??????
 */
fun getBatteryLevel(): Int? {
  val intent: Intent? =
    ContextWrapper(mContext).registerReceiver(
      null,
      IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )
  return intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! * 100 /
          intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
}

/**
 * ??????root???????????????
 * 1???root???????????????
 * 0??????root???????????????
 */
fun isSuEnableRoot(): Int {
  var file: File? = null
  val paths = arrayOf(
    "/system/bin/",
    "/system/xbin/",
    "/system/sbin/",
    "/sbin/",
    "/vendor/bin/",
    "/su/bin/"
  )
  try {
    for (path in paths) {
      file = File(path + "su")
      if (file.exists() && file.canExecute()) {
        Log.i("TAG", "find su in : $path")
        return 1
      }
    }
  } catch (x: Exception) {
    x.printStackTrace()
  }
  return 0
}

var virtualMachine: Int = 1
fun checkDeviceInfo(): Boolean {
  var result: Boolean = (Build.FINGERPRINT.startsWith("generic") // ???????????????
          || Build.MODEL.contains("google_sdk") // ?????? ??????????????????????????????
          || Build.MODEL.toLowerCase().contains("droid4x")
          || Build.MODEL.contains("Emulator")
          || Build.MODEL.contains("Android SDK built for x86")
          || Build.MANUFACTURER.contains("Genymotion") // ???????????????
          || Build.HARDWARE == "goldfish" || Build.HARDWARE == "vbox86" || Build.PRODUCT == "sdk" || Build.PRODUCT == "google_sdk" || Build.PRODUCT == "sdk_x86" || Build.PRODUCT == "vbox86p" || Build.BOARD.toLowerCase()
    .contains("nox") // ??????
          || Build.BOOTLOADER.toLowerCase().contains("nox") // ???????????????????????????
          || Build.HARDWARE.toLowerCase().contains("nox")
          || Build.PRODUCT.toLowerCase().contains("nox")
          || Build.SERIAL.toLowerCase().contains("nox")) // ???????????????
  if (result) return true
  result =
    result or (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith(
      "generic"
    ))
  if (result) return true
  result = result or ("google_sdk" == Build.PRODUCT)
  return result
}
