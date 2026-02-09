import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface LogEntry {
    id: number
    timestamp: string
    type: 'info' | 'success' | 'error' | 'warning'
    message: string
    details?: any
}

// BroadcastChannel 消息类型
interface LogBroadcastMessage {
    action: 'add' | 'clear'
    entry?: LogEntry
}

// 创建广播频道用于跨窗口通信
const logChannel = new BroadcastChannel('sekhmet-logs')

/**
 * 日志 Store
 * 管理应用运行时的日志记录，支持跨窗口同步
 */
export const useLogStore = defineStore('log', () => {
    const logs = ref<LogEntry[]>([])
    const isOpen = ref(false)
    let nextId = 1

    // 监听来自其他窗口的日志消息
    logChannel.onmessage = (event: MessageEvent<LogBroadcastMessage>) => {
        const { action, entry } = event.data
        if (action === 'add' && entry) {
            // 从其他窗口接收到的日志，直接添加（不再广播）
            logs.value.unshift(entry)
            // 更新本地 nextId 以避免 ID 冲突
            if (entry.id >= nextId) {
                nextId = entry.id + 1
            }
        } else if (action === 'clear') {
            logs.value = []
        }
    }

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

        // 广播日志到其他窗口
        logChannel.postMessage({ action: 'add', entry } as LogBroadcastMessage)

        // 发生错误或成功且有详情时自动打开面板
        if (type === 'error' || (type === 'success' && details)) {
            isOpen.value = true
        }
    }

    /** 清空日志 */
    function clearLogs() {
        logs.value = []
        // 广播清空操作到其他窗口
        logChannel.postMessage({ action: 'clear' } as LogBroadcastMessage)
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

