package com.example.presence

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EncryptDecrypt {
    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val secureRandom = SecureRandom()
        keyGenerator.init(128, secureRandom)
        return keyGenerator.generateKey()
    }
    // Encrypt text with AES encryption using a given key
    fun encryptText(text: String, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(text.toByteArray(Charsets.UTF_8))
    }
    // Decrypt bytes with AES decryption using a given key
    fun decryptBytes(bytes: ByteArray, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(bytes).toString(Charsets.UTF_8)
    }

}