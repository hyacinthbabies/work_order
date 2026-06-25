import request from './request'

export const login = (data) => request.post('/auth/login', data)

export const getDashboard = () => request.get('/dashboard')

export const getTicketList = (params) => request.get('/tickets', { params })

export const getTicketById = (id) => request.get(`/tickets/${id}`)

export const createTicket = (data) => request.post('/tickets', data)

export const updateTicket = (id, data) => request.put(`/tickets/${id}`, data)

export const deleteTicket = (id) => request.delete(`/tickets/${id}`)

export const getAnalytics = () => request.get('/analytics')

export const getPermissions = () => request.get('/permissions')

export const analyzeTitle = (title) => request.post('/ai/analyze', { title })

/**
 * SSE流式分析工单标题
 */
export const analyzeTitleStream = async (title, onEvent) => {
  const url = '/api/ai/analyze/stream'
  const token = localStorage.getItem('token')

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({ title })
  })

  if (!response.ok) {
    throw new Error('流式请求失败')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // SSE事件以 \n\n 分隔，按双换行切分完整事件
    const events = buffer.split('\n\n')
    buffer = events.pop() || ''

    for (const eventBlock of events) {
      if (!eventBlock.trim()) continue
      const lines = eventBlock.split('\n')
      let eventName = ''
      let data = ''
      for (const line of lines) {
        const trimmed = line.trim()
        if (trimmed.startsWith('event:')) {
          eventName = trimmed.slice(6).trim()
        } else if (trimmed.startsWith('data:')) {
          data += (data ? '\n' : '') + trimmed.slice(5)
        }
      }
      if (eventName) {
        onEvent(eventName, data)
      }
    }
  }

  // 处理缓冲区中可能残留的最后事件
  if (buffer.trim()) {
    const lines = buffer.split('\n')
    let eventName = ''
    let data = ''
    for (const line of lines) {
      const trimmed = line.trim()
      if (trimmed.startsWith('event:')) {
        eventName = trimmed.slice(6).trim()
      } else if (trimmed.startsWith('data:')) {
        data += (data ? '\n' : '') + trimmed.slice(5)
      }
    }
    if (eventName) onEvent(eventName, data)
  }
}

export const getKnowledgeKeywords = () => request.get('/knowledge/keywords')
export const getKnowledgeKeywordsByCategory = (category) => request.get(`/knowledge/keywords/category/${category}`)
export const saveKnowledgeKeyword = (data) => request.post('/knowledge/keywords', data)
export const deleteKnowledgeKeyword = (id) => request.delete(`/knowledge/keywords/${id}`)

export const getKnowledgeSolutions = () => request.get('/knowledge/solutions')
export const getKnowledgeSolutionsByCategory = (category) => request.get(`/knowledge/solutions/category/${category}`)
export const saveKnowledgeSolution = (data) => request.post('/knowledge/solutions', data)
export const deleteKnowledgeSolution = (id) => request.delete(`/knowledge/solutions/${id}`)