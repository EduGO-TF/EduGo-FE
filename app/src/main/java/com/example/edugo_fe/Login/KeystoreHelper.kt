package com.example.edugo_fe.Login

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreHelper(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeystore").apply { load(null) }
    private val keyAlias = "REFRESH_TOKEN_KEY"
    private val transformation = "AES/GCM/NoPadding"
    private val ivLength = 12   // OCN 추천 IV 길이

    // 키 생성/확보
    private fun getOrCreateKey(): SecretKey {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
        return keyStore.getKey(keyAlias, null) as SecretKey
    }

    // Refresh Token 암호화 저장
    fun encryptAndSaveToken(token: String): Boolean {
        return try {
            val cipher = Cipher.getInstance(transformation).apply {
                init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            }
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))

            // IV + 암호화 데이터를 Base64로 인코딩하여 SharedPreferences에 저장
            val pref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            pref.edit()
                .putString("encrypted_token", Base64.encodeToString(encryptedBytes + iv, Base64.DEFAULT))
                .apply()
            true
        } catch (e: Exception){
            Log.e("KeystoreHelper", "암호화 실패", e)
            false
        }
    }

    // Refresh Token 복호화
    fun decryptToken(): String? {
        return try {
            val pref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            val combined = pref.getString("encrypted_token", null) ?: return null
            val decoded = Base64.decode(combined, Base64.DEFAULT)

            // IV 와 암호화 데이터 분리
            val iv = decoded.copyOfRange(decoded.size - ivLength, decoded.size)
            val encrypted = decoded.copyOfRange(0, decoded.size - ivLength)

            val cipher = Cipher.getInstance(transformation).apply {
                init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            }
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("KeystoreHelper", "복호화 실패", e)
            null
        }
    }

    // 토큰 삭제
    fun deleteToken() {
        val pref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        pref.edit().remove("encrypted_token").apply()
    }
}