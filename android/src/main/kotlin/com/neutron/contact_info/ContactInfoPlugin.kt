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
    }else {
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
    //新建一个联系人实例
    val temp = HashMap<String,String>()
    val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
    //获取联系人姓名
    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
    val time = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP))
    temp["other_name"]=(name)
    temp["last_time"]=(time)

    //获取联系人电话号码
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
    //记得要把cursor给close掉
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

fun isSystemApp(pInfo: PackageInfo): Boolean {
  //判断是否是系统软件
  return pInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}

fun formatTime(date: Long, format: String?): String? {
  val formatter = SimpleDateFormat(format)
  return formatter.format(date).toString()
}