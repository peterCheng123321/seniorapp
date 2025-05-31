package com.example.seniorapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.seniorapp.service.VoiceAssistant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var voiceAssistant: VoiceAssistant
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SET_ALARM
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        voiceAssistant = VoiceAssistant(this)
        
        if (!hasAllPermissions()) {
            requestPermissions()
        }
        
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                SeniorVoiceAssistantScreen(voiceAssistant)
            }
        }
    }
    
    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        voiceAssistant.release()
    }
}

@Composable
fun SeniorVoiceAssistantScreen(voiceAssistant: VoiceAssistant) {
    val isListening by voiceAssistant.isListening.collectAsState()
    val lastResult by voiceAssistant.lastResult.collectAsState()
    val volumeLevel by voiceAssistant.volumeLevel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Title
            Text(
                text = "语音助手",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Volume indicator (only show when listening)
            if (isListening) {
                VolumeIndicator(volumeLevel)
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Result display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lastResult.ifEmpty { "按下按钮开始说话" },
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Main voice button
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (isListening) {
                            voiceAssistant.stopListening()
                        } else {
                            voiceAssistant.startListening()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isListening) "停止" else "开始说话",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Emergency contact button
            OutlinedButton(
                onClick = {
                    // TODO: Open emergency contact setup
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "设置紧急联系人",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun VolumeIndicator(volumeLevel: Float) {
    val normalizedVolume = ((volumeLevel + 80f) / 80f).coerceIn(0f, 1f)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "音量",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LinearProgressIndicator(
            progress = { normalizedVolume },
            modifier = Modifier
                .width(200.dp)
                .height(8.dp)
                .padding(top = 8.dp),
            color = when {
                normalizedVolume < 0.3f -> MaterialTheme.colorScheme.error
                normalizedVolume < 0.7f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}