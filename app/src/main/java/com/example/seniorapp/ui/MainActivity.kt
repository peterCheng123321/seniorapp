package com.example.seniorapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.seniorapp.data.Memory
import com.example.seniorapp.service.VoiceProcessingService
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val memories by viewModel.memories.collectAsState()
                MainScreen(
                    memories = memories,
                    onDelete = { viewModel.deleteMemory(it) },
                    onStart = { if (checkPermissions()) startVoiceService() else requestPermissions() },
                    onStop = { stopVoiceService() }
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                startVoiceService()
            } else {
                Toast.makeText(
                    this,
                    "Permissions are required for the app to work properly",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        launcher.launch(requiredPermissions)
    }

    private fun startVoiceService() {
        Intent(this, VoiceProcessingService::class.java).also { intent ->
            startForegroundService(intent)
        }
    }

    private fun stopVoiceService() {
        Intent(this, VoiceProcessingService::class.java).also { intent ->
            stopService(intent)
        }
    }
}

@Composable
fun MainScreen(
    memories: List<Memory>,
    onDelete: (Memory) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "记忆列表",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (!isRecording) { onStart(); isRecording = true } else { onStop(); isRecording = false }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isRecording) "停止录音" else "开始录音")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(memories) { memory ->
                MemoryItem(memory = memory, onDelete = onDelete)
            }
        }
    }
}

@Composable
fun MemoryItem(memory: Memory, onDelete: (Memory) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = memory.content, fontWeight = FontWeight.Medium)
                Text(text = "类型: ${memory.type}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = dateFormat.format(memory.timestamp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDelete(memory) }) {
                Icon(Icons.Default.Remove, contentDescription = "删除")
            }
        }
    }
} 