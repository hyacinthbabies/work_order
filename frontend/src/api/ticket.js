import request from './request'

export const login = (data) => request.post('/auth/login', data)

export const getDashboard = () => request.get('/dashboard')

export const getTicketList = (params) => request.get('/tickets', { params })

export const getTicketById = (id) => request.get(`/tickets/${id}`)

export const createTicket = (data) => request.post('/tickets', data)

export const createTicketWithAttachments = (formData) => {
  return request.post('/tickets/with-attachments', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

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

export const getKnowledgeArticles = () => request.get('/knowledge/articles')
export const getKnowledgeArticlesByCategory = (category) => request.get(`/knowledge/articles/category/${category}`)
export const getKnowledgeArticleById = (id) => request.get(`/knowledge/articles/${id}`)
export const saveKnowledgeArticle = (data) => request.post('/knowledge/articles', data)
export const deleteKnowledgeArticle = (id) => request.delete(`/knowledge/articles/${id}`)
export const searchKnowledgeArticles = (keyword) => request.get('/knowledge/articles/search', { params: { keyword } })
export const findRelatedArticles = (keyword) => request.get('/knowledge/articles/related', { params: { keyword } })

export const uploadAttachment = (ticketId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/attachments/upload/${ticketId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getAttachmentsByTicketId = (ticketId) => request.get(`/attachments/ticket/${ticketId}`)
export const getAttachments = (ticketId) => request.get(`/attachments/ticket/${ticketId}`)

export const deleteAttachment = (id) => request.delete(`/attachments/${id}`)

export const revokeTicket = (id, data) => request.put(`/tickets/${id}/revoke`, data)

export const transferTicket = (id, data) => request.put(`/tickets/${id}/transfer`, data)

export const escalateTicket = (id) => request.put(`/tickets/${id}/escalate`)

export const completeTicket = (id, data) => request.put(`/tickets/${id}/complete`, data)

export const getUsersByDepartment = (department) => request.get(`/users/department/${encodeURIComponent(department)}`)

export const getTicketLogs = (ticketId) => request.get(`/ticket-logs/ticket/${ticketId}`)

export const addTicketLog = (data) => request.post('/ticket-logs', data)