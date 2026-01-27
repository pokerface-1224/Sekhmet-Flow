import { createApp } from 'vue'
import { createPinia } from 'pinia' // 1. 导入 Pinia 状态管理库
import './style.css'
import App from './App.vue'

const app = createApp(App) // 2. 创建 Vue 应用实例

app.use(createPinia()) // 3. 安装 Pinia 插件
app.mount('#app') // 4. 挂载应用到 DOM
