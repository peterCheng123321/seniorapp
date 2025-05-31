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
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = MaterialTheme.colorScheme.primary,
                    onPrimary = MaterialTheme.colorScheme.onPrimary,
                    surface = MaterialTheme.colorScheme.surface,
                    onSurface = MaterialTheme.colorScheme.onSurface
                )
            ) {
                AccessibleVoiceAssistantScreen(voiceAssistant)
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
fun AccessibleVoiceAssistantScreen(voiceAssistant: VoiceAssistant) {
    val isListening by voiceAssistant.isListening.collectAsState()
    val lastResult by voiceAssistant.lastResult.collectAsState()
    val volumeLevel by voiceAssistant.volumeLevel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = "语音助手",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Volume indicator (only show when listening)
            if (isListening) {
                AccessibleVolumeIndicator(volumeLevel)
            }
            
            // Result display
            AccessibleResultCard(lastResult)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Main voice button
            AccessibleButton(
                onClick = {
                    coroutineScope.launch {
                        if (isListening) {
                            voiceAssistant.stopListening()
                        } else {
                            voiceAssistant.startListening()
                        }
                    }
                },
                isListening = isListening
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Emergency contact button
            OutlinedButton(
                onClick = { /* TODO: Open emergency contact setup */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "设置紧急联系人",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccessibleVolumeIndicator(volumeLevel: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "音量",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LinearProgressIndicator(
            progress = { ((volumeLevel + 80f) / 80f).coerceIn(0f, 1f) },
            modifier = Modifier
                .width(280.dp)
                .height(12.dp)
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun AccessibleResultCard(result: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.ifEmpty { "请按下按钮开始说话" },
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 32.sp
            )
        }
    }
}

@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    isListening: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isListening) 
                MaterialTheme.colorScheme.error
            else 
                MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = if (isListening) "停止" else "开始说话",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}