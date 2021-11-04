#import "ContactInfoPlugin.h"
#if __has_include(<contact_info/contact_info-Swift.h>)
#import <contact_info/contact_info-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "contact_info-Swift.h"
#endif

@implementation ContactInfoPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftContactInfoPlugin registerWithRegistrar:registrar];
}
@end
