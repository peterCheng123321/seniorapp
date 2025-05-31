package com.example.seniorapp.data

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<Memory>> = memoryDao.getAllMemories()

    fun getMemoriesByType(type: Memory.MemoryType): Flow<List<Memory>> {
        return memoryDao.getMemoriesByType(type)
    }

    suspend fun insertMemory(memory: Memory): Long {
        return memoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: Memory) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: Memory) {
        memoryDao.deleteMemory(memory)
    }

    fun searchMemories(query: String): Flow<List<Memory>> {
        return memoryDao.searchMemories(query)
    }
} 