import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'editor',
            component: () => import('./pages/EditorPage.vue')
        },
        {
            path: '/logs',
            name: 'logs',
            component: () => import('./pages/LogsPage.vue')
        }
    ]
})

export default router
