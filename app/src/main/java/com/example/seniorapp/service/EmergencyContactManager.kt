package com.example.seniorapp.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.seniorapp.SeniorApp

class EmergencyContactManager(private val context: Context) {
    
    companion object {
        private const val TAG = "EmergencyContact"
        private const val PREFS_NAME = "emergency_contacts"
        
        // Common relationship patterns in Chinese
        private val CONTACT_PATTERNS = mapOf(
            "女儿|闺女" to "daughter",
            "儿子" to "son", 
            "老伴|妻子|丈夫" to "spouse",
            "医生" to "doctor",
            "护士" to "nurse",
            "家人" to "family",
            "孩子" to "child"
        )
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun addEmergencyContact(relationship: String, phoneNumber: String): Boolean {
        return try {
            prefs.edit()
                .putString(relationship.lowercase(), phoneNumber)
                .apply()
            
            showConfirmationNotification("已保存${relationship}的电话号码")
            Log.i(TAG, "Emergency contact added: $relationship -> $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add emergency contact", e)
            false
        }
    }
    
    fun processVoiceCommand(command: String): Boolean {
        Log.d(TAG, "Processing emergency call command: $command")
        
        // Extract relationship from voice command
        val relationship = extractRelationshipFromCommand(command)
        if (relationship != null) {
            val phoneNumber = prefs.getString(relationship, null)
            if (phoneNumber != null) {
                makeCall(phoneNumber, relationship)
                return true
            } else {
                showErrorNotification("未找到${relationship}的电话号码，请先添加联系人")
                return false
            }
        }
        
        return false
    }
    
    private fun extractRelationshipFromCommand(command: String): String? {
        for ((pattern, relationship) in CONTACT_PATTERNS) {
            if (command.contains(Regex(pattern))) {
                return relationship
            }
        }
        
        // Check if command contains saved contact keys
        for (key in prefs.all.keys) {
            if (command.contains(key)) {
                return key
            }
        }
        
        return null
    }
    
    private fun makeCall(phoneNumber: String, relationship: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            Log.i(TAG, "Calling $relationship at $phoneNumber")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to make call", e)
            showErrorNotification("无法拨打电话，请检查权限设置")
        }
    }
    
    fun getEmergencyContacts(): Map<String, String> {
        return prefs.all.mapValues { it.value.toString() }
    }
    
    private fun showConfirmationNotification(message: String) {
        val notification = NotificationCompat.Builder(context, SeniorApp.VOICE_CHANNEL_ID)
            .setContentTitle("紧急联系人")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), notification)
        }
    }
    
    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(context, SeniorApp.VOICE_CHANNEL_ID)
            .setContentTitle("错误")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), notification)
        }
    }
} 