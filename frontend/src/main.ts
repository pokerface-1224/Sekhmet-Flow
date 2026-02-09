import { createApp } from 'vue'
import { createPinia } from 'pinia' // 1. 导入 Pinia 状态管理库
import router from './router' // 2. 导入路由配置
import './style.css'
import App from './App.vue'

const app = createApp(App) // 3. 创建 Vue 应用实例

app.use(createPinia()) // 4. 安装 Pinia 插件
app.use(router) // 5. 安装路由插件
app.mount('#app') // 6. 挂载应用到 DOM

