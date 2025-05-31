package com.example.seniorapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seniorapp.data.AppDatabase
import com.example.seniorapp.data.Memory
import com.example.seniorapp.data.MemoryRepository
import com.example.seniorapp.data.MemoryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MemoryRepository
    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories: StateFlow<List<Memory>> = _memories.asStateFlow()

    init {
        val memoryDao = AppDatabase.getInstance(application).memoryDao()
        repository = MemoryRepository(memoryDao)
        viewModelScope.launch {
            repository.allMemories.collect { memoryList ->
                _memories.value = memoryList
            }
        }
    }

    fun addMemory(content: String, type: Memory.MemoryType) {
        viewModelScope.launch {
            val memory = Memory(
                content = content,
                type = type,
                timestamp = java.util.Date()
            )
            repository.insertMemory(memory)
        }
    }

    fun searchMemories(query: String) {
        viewModelScope.launch {
            repository.searchMemories(query).collect { results ->
                _memories.value = results
            }
        }
    }

    fun deleteMemory(memory: Memory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }
} 