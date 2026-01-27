import axios from 'axios';

/**
 * Axios 实例配置
 * 统一配置后端 API 基础地址和请求头
 */
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;
