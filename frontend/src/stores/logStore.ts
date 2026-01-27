import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface LogEntry {
    id: number
    timestamp: string
    type: 'info' | 'success' | 'error' | 'warning'
    message: string
    details?: any
}

/**
 * 日志 Store
 * 管理应用运行时的日志记录
 */
export const useLogStore = defineStore('log', () => {
    const logs = ref<LogEntry[]>([])
    const isOpen = ref(false)
    let nextId = 1

    /**
     * 添加一条日志
     * @param type 日志类型 (info, success, error, warning)
     * @param message 日志消息
     * @param details 详细信息对象
     */
    function addLog(type: LogEntry['type'], message: string, details?: any) {
        const entry: LogEntry = {
            id: nextId++,
            timestamp: new Date().toLocaleTimeString(),
            type,
            message,
            details
        }
        logs.value.unshift(entry)
        // 发生错误或成功且有详情时自动打开面板
        if (type === 'error' || (type === 'success' && details)) {
            isOpen.value = true
        }
    }

    /** 清空日志 */
    function clearLogs() {
        logs.value = []
    }

    /** 切换日志面板的显示状态 */
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
