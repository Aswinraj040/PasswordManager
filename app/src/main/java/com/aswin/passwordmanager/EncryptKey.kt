package com.aswin.passwordmanager

import android.util.Base64
import android.util.Log
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES"

object EncryptionUtils {
    private val secretKey: Key = generateKey()

    private fun generateKey(): Key {
        val key = "MySecretKey12345" // Ensure this key is 16 bytes for AES
        return SecretKeySpec(key.toByteArray(), ALGORITHM)
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedValue = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedValue = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
        Log.d("Tag", "Decrypted value: ${String(decryptedValue)}")
        return String(decryptedValue)
    }
}
