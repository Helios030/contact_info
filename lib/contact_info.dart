
import 'dart:async';

import 'package:flutter/services.dart';

class ContactInfo {
  static const MethodChannel _channel = MethodChannel('contact_info');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }


}
