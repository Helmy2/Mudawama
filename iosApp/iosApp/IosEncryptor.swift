import Foundation
import CryptoKit
import MudawamaUI

class IosEncryptor: Encryptor {
    private let keychainAccount = "mudawama_session_key"
    
    // Get existing key from Keychain, or create & save a new one
    private var symmetricKey: SymmetricKey {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: keychainAccount,
            kSecReturnData as String: true
        ]
        
        var item: CFTypeRef?
        if SecItemCopyMatching(query as CFDictionary, &item) == errSecSuccess,
           let keyData = item as? Data {
            return SymmetricKey(data: keyData)
        }
        
        // If no key exists, generate and save one
        let newKey = SymmetricKey(size: .bits256)
        let keyData = newKey.withUnsafeBytes { Data($0) }
        
        let addQuery: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: keychainAccount,
            kSecValueData as String: keyData,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]
        SecItemAdd(addQuery as CFDictionary, nil)
        
        return newKey
    }

    func encrypt(plain: String) -> String {
        let data = Data(plain.utf8)
        let sealedBox = try! AES.GCM.seal(data, using: symmetricKey)
        return sealedBox.combined!.base64EncodedString()
    }

    func decrypt(encrypted: String) -> String {
        guard let data = Data(base64Encoded: encrypted),
              let sealedBox = try? AES.GCM.SealedBox(combined: data),
              let decryptedData = try? AES.GCM.open(sealedBox, using: symmetricKey),
              let result = String(data: decryptedData, encoding: .utf8) else {
            return ""
        }
        return result
    }
}
