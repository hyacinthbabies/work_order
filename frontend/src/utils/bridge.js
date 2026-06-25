class NJBankBridge {
  constructor() {
    this.bridgeName = 'NJBankTicketBridge'
    this.callbacks = {}
    this.init()
  }

  init() {
    if (typeof window !== 'undefined') {
      window[this.bridgeName] = this.handleMessage.bind(this)
      this.setupMessageListener()
    }
  }

  setupMessageListener() {
    if (window.addEventListener) {
      window.addEventListener('message', this.handlePostMessage.bind(this), false)
    } else if (window.attachEvent) {
      window.attachEvent('onmessage', this.handlePostMessage.bind(this))
    }
  }

  handleMessage(message) {
    try {
      const data = typeof message === 'string' ? JSON.parse(message) : message
      if (data && data.action) {
        this.executeAction(data)
      }
    } catch (e) {
      console.error('Bridge message parse error:', e)
    }
  }

  handlePostMessage(event) {
    if (event.data && event.data.__bridge === this.bridgeName) {
      this.handleMessage(event.data.payload)
    }
  }

  executeAction(data) {
    const { action, params, callbackId } = data
    
    switch (action) {
      case 'getUserInfo':
        this.getUserInfo(callbackId)
        break
      case 'createTicket':
        this.createTicket(params, callbackId)
        break
      case 'getTicketDetail':
        this.getTicketDetail(params, callbackId)
        break
      case 'updateTicket':
        this.updateTicket(params, callbackId)
        break
      case 'closePage':
        this.closePage(callbackId)
        break
      case 'scanQRCode':
        this.scanQRCode(callbackId)
        break
      case 'getLocation':
        this.getLocation(callbackId)
        break
      case 'callPhone':
        this.callPhone(params, callbackId)
        break
      case 'showToast':
        this.showToast(params, callbackId)
        break
      case 'showConfirm':
        this.showConfirm(params, callbackId)
        break
      case 'navigateTo':
        this.navigateTo(params, callbackId)
        break
      case 'getDeviceInfo':
        this.getDeviceInfo(callbackId)
        break
      case 'encryptData':
        this.encryptData(params, callbackId)
        break
      case 'decryptData':
        this.decryptData(params, callbackId)
        break
      default:
        this.sendCallback(callbackId, { success: false, error: 'Unknown action' })
    }
  }

  getUserInfo(callbackId) {
    const userInfo = {
      username: 'kefu01',
      name: '张三',
      role: '一线客服',
      department: '客服中心',
      token: localStorage.getItem('token') || ''
    }
    this.sendCallback(callbackId, { success: true, data: userInfo })
  }

  createTicket(params, callbackId) {
    this.sendCallback(callbackId, {
      success: true,
      data: {
        ticketNo: 'WO' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + String(Math.random()).slice(-4),
        message: '工单创建成功'
      }
    })
  }

  getTicketDetail(params, callbackId) {
    this.sendCallback(callbackId, {
      success: true,
      data: {
        ticketNo: params.ticketNo,
        title: '账户余额异常查询',
        status: '处理中',
        customerName: '张*',
        phone: '138****5678',
        balance: '¥ ****.**'
      }
    })
  }

  updateTicket(params, callbackId) {
    this.sendCallback(callbackId, {
      success: true,
      data: { message: '工单更新成功' }
    })
  }

  closePage(callbackId) {
    if (typeof window.ReactNativeWebView !== 'undefined') {
      window.ReactNativeWebView.postMessage(JSON.stringify({ action: 'closePage' }))
    }
    this.sendCallback(callbackId, { success: true })
  }

  scanQRCode(callbackId) {
    this.sendNativeCommand('scanQRCode', {}, callbackId)
  }

  getLocation(callbackId) {
    this.sendNativeCommand('getLocation', {}, callbackId)
  }

  callPhone(params, callbackId) {
    window.location.href = `tel:${params.phone}`
    this.sendCallback(callbackId, { success: true })
  }

  showToast(params, callbackId) {
    const { message, duration = 2000 } = params
    const toast = document.createElement('div')
    toast.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0,0,0,0.7);
      color: white;
      padding: 12px 24px;
      border-radius: 8px;
      font-size: 14px;
      z-index: 9999;
      pointer-events: none;
    `
    toast.textContent = message
    document.body.appendChild(toast)
    setTimeout(() => {
      toast.remove()
    }, duration)
    this.sendCallback(callbackId, { success: true })
  }

  showConfirm(params, callbackId) {
    const { title, message } = params
    const result = window.confirm(`${title}\n${message}`)
    this.sendCallback(callbackId, { success: true, data: { confirmed: result } })
  }

  navigateTo(params, callbackId) {
    const { url, type = 'push' } = params
    if (type === 'replace') {
      window.location.replace(url)
    } else {
      window.location.href = url
    }
    this.sendCallback(callbackId, { success: true })
  }

  getDeviceInfo(callbackId) {
    const deviceInfo = {
      platform: navigator.platform,
      userAgent: navigator.userAgent,
      appVersion: navigator.appVersion,
      isMobile: /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
    }
    this.sendCallback(callbackId, { success: true, data: deviceInfo })
  }

  encryptData(params, callbackId) {
    const { data } = params
    const encrypted = btoa(encodeURIComponent(data))
    this.sendCallback(callbackId, { success: true, data: encrypted })
  }

  decryptData(params, callbackId) {
    const { data } = params
    const decrypted = decodeURIComponent(atob(data))
    this.sendCallback(callbackId, { success: true, data: decrypted })
  }

  sendNativeCommand(action, params, callbackId) {
    const message = JSON.stringify({
      __bridge: this.bridgeName,
      payload: { action, params, callbackId }
    })
    
    if (window.AndroidBridge) {
      window.AndroidBridge.handleMessage(message)
    } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers[this.bridgeName]) {
      window.webkit.messageHandlers[this.bridgeName].postMessage(message)
    } else if (window.ReactNativeWebView) {
      window.ReactNativeWebView.postMessage(message)
    } else {
      console.warn('Native bridge not found, simulating response')
      setTimeout(() => {
        this.sendCallback(callbackId, { success: true, data: { simulated: true, action } })
      }, 100)
    }
  }

  sendCallback(callbackId, result) {
    if (callbackId) {
      const callback = this.callbacks[callbackId]
      if (callback) {
        callback(result)
        delete this.callbacks[callbackId]
      }
    }
  }

  call(action, params = {}) {
    return new Promise((resolve, reject) => {
      const callbackId = 'cb_' + Date.now() + '_' + Math.random().toString(36).slice(2, 9)
      this.callbacks[callbackId] = (result) => {
        if (result.success) {
          resolve(result.data)
        } else {
          reject(new Error(result.error || 'Bridge call failed'))
        }
      }
      this.executeAction({ action, params, callbackId })
    })
  }

  async initApp() {
    try {
      const userInfo = await this.call('getUserInfo')
      if (userInfo.token) {
        localStorage.setItem('token', userInfo.token)
      }
      return userInfo
    } catch (e) {
      console.error('Bridge init failed:', e)
      return null
    }
  }
}

const bridge = new NJBankBridge()

export default bridge