package com.neutron.contact_info

import androidx.annotation.NonNull

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

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "contact_info")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if(call.method == "getContact"){

      result.success(getAllContacts(channel.context))

    }else{
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }


}

data class PContacts(
  var other_name   : String? = null,
  var other_mobile  : String? = null,
  var last_time     : String? = null
)

fun getAllContacts(context: Context): ArrayList<PContacts>? {
  val contacts: ArrayList<PContacts> = ArrayList<PContacts>()
  val cursor = context.contentResolver.query(
    ContactsContract.Contacts.CONTENT_URI, null, null, null, null
  )
  while (cursor!!.moveToNext()) {
    //新建一个联系人实例
    val temp = PContacts()
    val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
    //获取联系人姓名
    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
    val time = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP))
    temp.other_name=(name)
    temp.last_time=(time)

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
      temp.other_mobile=phone
    }

    contacts.add(temp)
    //记得要把cursor给close掉
    phoneCursor.close()
  }
  cursor.close()
  return contacts
}
