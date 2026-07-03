import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      const error = new Error(res.message || 'Error')
      error.response = { data: res }
      return Promise.reject(error)
    }
    return res
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      if (error.response.data && error.response.data.message) {
        error.message = error.response.data.message
      }
    }
    return Promise.reject(error)
  }
)

export default service