//
//  ContactTools.swift
//  Runner
//
//  Created by JcKeats on 2021/11/5.
//

import Foundation
import Contacts
class ContactTools: NSObject {
    
    static let tool = ContactTools()
    
    func getContact(complete:@escaping (([[String:Any]]) -> ()))  {
        
        requestContactAuthor(complete: complete)
        
    }

    /// 请求联系人权限
    func requestContactAuthor(complete: (([[String:Any]]) -> ())?) {
        
        let state = CNContactStore.authorizationStatus(for: CNEntityType.contacts)
        
        if state == .notDetermined {
            
            let store = CNContactStore()
            
            store.requestAccess(for: .contacts) {[weak self] granted, error in
                if (error != nil) {
                    print("授权失败")
                    complete?([])
                }else{
                    print("授权成功")
                    // 已授权 下一步
                    complete?(self?.openContact() ?? [])
                }
            }
            
            
        }else if state == .authorized {
            // 已授权 下一步
            complete?(openContact())
            
        }else{
            print("用户拒绝")
            complete?([])
        }
        
    }

    func openContact() -> [[String:Any]] {
        let lists = [CNContactNamePrefixKey,CNContactGivenNameKey,CNContactMiddleNameKey,CNContactFamilyNameKey,CNContactPhoneNumbersKey,CNContactDatesKey] as [CNKeyDescriptor]
        
        let fetchRequest = CNContactFetchRequest(keysToFetch: lists)
        
        let contactStore = CNContactStore()
        
        
        var contactLists:[[String:Any]] = []
        
        do {
            
            
            try contactStore.enumerateContacts(with: fetchRequest) { contact, stop in
                
                var contactDict:[String:Any] = ["last_time":0]
                
                let givenName = contact.givenName
                let familyName = contact.familyName
                let name = familyName + givenName
                let phoneNumbers = contact.phoneNumbers
                contactDict["other_name"] = name
                var phoneNumberStringList:[String] = []
                
                for lableValue in phoneNumbers {
                    //多个号码
                    let phoneString = lableValue.value.stringValue
                    phoneNumberStringList.append(phoneString)
                }
                contactDict["other_mobile"] = phoneNumberStringList
                
                
                contactLists.append(contactDict)
            }
            
        } catch let error {
            
            print("循环遍历出错:\(error)")
            
        }
        
        return contactLists
        
    }

}
