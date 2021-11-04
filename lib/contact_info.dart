
import 'dart:async';

import 'package:flutter/services.dart';

class ContactInfo {
  static const MethodChannel _channel = MethodChannel('contact_info');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }


  static Future<List<dynamic>?> get getContact async {
    final List<dynamic>? contList = await _channel.invokeMethod('getContact');
    return contList;
  }

  static Future<List<dynamic>?> get getAPPs async {
    final List<dynamic>? APPList = await _channel.invokeMethod('getAPPs');
    return APPList;
  }

}
