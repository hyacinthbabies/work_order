# 银行全域智能工单系统

## 项目概述

本系统是银行全域智能工单管理平台，基于前后端分离架构，支持工单创建、处理、撤销、知识库管理等功能。

## 技术栈

- **前端**: Vue 3 + Element Plus + Vite
- **后端**: Spring Boot 2.7 + Spring Security + JWT
- **数据库**: Oracle 21c
- **容器**: Docker + Docker Compose

## 快速启动

### 使用 Docker Compose（推荐）

```bash
# 进入项目目录
cd /Users/hyacunth/Desktop/workplace/work_order

# 启动所有服务（数据库、后端、前端）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 停止服务
docker-compose down
```

### 手动启动

#### 1. 启动数据库（Oracle）

```bash
docker run -d \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle123 \
  -e APP_USER=ticket_user \
  -e APP_USER_PASSWORD=ticket_password \
  gvenzl/oracle-xe:21-slim
```

```
H2 Web Console 登录：

- URL : http://localhost:8080/h2-console
- JDBC URL : jdbc:h2:file:./data/ticketdb;AUTO_SERVER=TRUE
- 用户名 : sa
- 密码 : (留空)
```

#### 2. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

#### 3. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:3000 | 工单系统首页 |
| 后端API | http://localhost:8080 | RESTful API |
| Oracle数据库 | localhost:1521 | 数据库端口 |

## 数据库连接信息

```
数据库类型: Oracle 21c Express Edition
数据库名: XE
主机: localhost
端口: 1521
用户名: ticket_user
密码: ticket_password
```

## 系统用户

| 用户名 | 密码 | 角色 | 部门 |
|--------|------|------|------|
| admin | admin123 | 系统管理员 | 信息科技部 |
| kefu01 | kefu123 | 一线客服 | 客服中心 |
| zhuguan01 | zhuguan123 | 客服主管 | 客服中心 |

## 功能模块

- **工单管理**: 工单列表、创建、详情、处理、撤销
- **智能分析**: AI问题分析、智能派单、知识库推荐
- **知识库管理**: 知识库文章维护、分类管理、搜索
- **权限管理**: 用户角色权限、数据脱敏配置

## 项目结构

```
work_order/
├── frontend/          # 前端项目
│   ├── src/
│   │   ├── views/     # 页面组件
│   │   ├── api/       # API接口
│   │   └── components/# 公共组件
│   └── package.json
├── backend/           # 后端项目
│   ├── src/main/java/com/njbank/ticket/
│   │   ├── controller/ # REST控制器
│   │   ├── service/    # 业务逻辑
│   │   ├── repository/ # 数据访问
│   │   ├── entity/     # 实体类
│   │   └── dto/        # 数据传输对象
│   └── pom.xml
└── docker-compose.yml # 容器编排配置
```

## 注意事项

1. 首次启动时数据库需要初始化，可能需要等待1-2分钟
2. 前端服务启动时会自动安装依赖，首次启动较慢
3. 系统启动后会自动初始化测试数据和用户账号