import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface LogEntry {
    id: number
    timestamp: string
    type: 'info' | 'success' | 'error' | 'warning'
    message: string
    details?: any
}

export const useLogStore = defineStore('log', () => {
    const logs = ref<LogEntry[]>([])
    const isOpen = ref(false)
    let nextId = 1

    function addLog(type: LogEntry['type'], message: string, details?: any) {
        const entry: LogEntry = {
            id: nextId++,
            timestamp: new Date().toLocaleTimeString(),
            type,
            message,
            details
        }
        logs.value.unshift(entry)
        // Auto-open on error or success with details
        if (type === 'error' || (type === 'success' && details)) {
            isOpen.value = true
        }
    }

    function clearLogs() {
        logs.value = []
    }

    function togglePanel() {
        isOpen.value = !isOpen.value
    }

    return {
        logs,
        isOpen,
        addLog,
        clearLogs,
        togglePanel
    }
})
