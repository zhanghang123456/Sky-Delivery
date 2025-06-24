# 苍穹外卖项目 - 后端服务

## 项目简介
苍穹外卖是一个餐饮外卖平台的后端服务系统，提供商家管理、菜品展示、购物车管理、订单处理及配送状态追踪等核心功能，支持多角色用户（顾客、商家、管理员）协同操作。

## 技术栈
- **核心框架**: Spring Boot 2.7.18
- **Java版本**: JDK 17
- **数据库**: 
  - MySQL 8.0 (主数据存储)
  - Redis 7.x (缓存和会话管理)
- **持久层**: MyBatis-Plus 3.5.5
- **安全框架**: Spring Security + JWT
- **其他组件**: 
  - Lombok (简化代码)
  - Swagger (API文档)
  - Spring Data Redis
  - Spring Task (定时任务)
  - Apache POI (Excel操作)

---

## 如何运行

### 环境要求
1. MySQL 8.0+ ([下载地址](https://dev.mysql.com/downloads/))
2. Redis 7.x ([下载地址](https://redis.io/download))
3. JDK 11 ([下载地址](https://www.oracle.com/java/technologies/downloads/))

### 数据库初始化
1. 创建数据库：
   ```sql
   CREATE DATABASE sky_takeout CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 执行初始化脚本：[schema.sql](./sql/schema.sql)
   ```bash
   mysql -u root -p sky_takeout < sql/schema.sql
   ```
3. 导入示例数据（可选）：[data.sql](./sql/data.sql)
   ```bash
   mysql -u root -p sky_takeout < sql/data.sql
   ```



## API接口说明
通过 Swagger 查看完整接口文档：  
🔗 **http://localhost:8080/doc.html**

### 核心接口示例
| 功能模块       | 接口路径               | 方法 | 说明                     |
|----------------|------------------------|------|--------------------------|
| 用户登录       | `/user/login`          | POST | 用户登录获取JWT令牌      |
| 菜品管理       | `/admin/dish`          | POST | 创建新菜品               |
| 购物车操作     | `/user/shoppingCart`   | GET  | 获取当前用户购物车       |
| 订单创建       | `/user/order/submit`   | POST | 提交新订单               |
| 订单状态更新   | `/admin/order/status`  | PUT  | 修改订单状态（商家端）   |
| 配送状态查询   | `/user/order/status`   | GET  | 获取订单配送状态         |

---

## 项目结构
```
D:.
├─.idea
├─sky-common
│  ├─src
│  │  ├─main
│  │  │  ├─java
│  │  │  │  └─com
│  │  │  │      └─sky
│  │  │  │          ├─constant
│  │  │  │          ├─context
│  │  │  │          ├─enumeration
│  │  │  │          ├─exception
│  │  │  │          ├─json
│  │  │  │          ├─properties
│  │  │  │          ├─result
│  │  │  │          └─utils
│  │  │  └─resources
│  │  └─test
│  │      └─java
│  └─target
│      ├─classes
│      │  ├─com
│      │  │  └─sky
│      │  │      ├─constant
│      │  │      ├─context
│      │  │      ├─enumeration
│      │  │      ├─exception
│      │  │      ├─json
│      │  │      ├─properties
│      │  │      ├─result
│      │  │      └─utils
│      │  └─META-INF
│      ├─generated-sources
│      │  └─annotations
│      ├─generated-test-sources
│      │  └─test-annotations
│      └─maven-status
│          └─maven-compiler-plugin
│              ├─compile
│              │  └─default-compile
│              └─testCompile
│                  └─default-testCompile
├─sky-pojo
│  ├─src
│  │  ├─main
│  │  │  ├─java
│  │  │  │  └─com
│  │  │  │      └─sky
│  │  │  │          ├─dto
│  │  │  │          ├─entity
│  │  │  │          └─vo
│  │  │  └─resources
│  │  └─test
│  │      └─java
│  └─target
│      ├─classes
│      │  └─com
│      │      └─sky
│      │          ├─dto
│      │          ├─entity
│      │          └─vo
│      ├─generated-sources
│      │  └─annotations
│      └─maven-status
│          └─maven-compiler-plugin
│              └─compile
│                  └─default-compile
└─sky-server
    ├─src
    │  ├─main
    │  │  ├─java
    │  │  │  └─com
    │  │  │      └─sky
    │  │  │          ├─annotation
    │  │  │          ├─aspect
    │  │  │          ├─config
    │  │  │          ├─controller
    │  │  │          │  ├─admin
    │  │  │          │  ├─notify
    │  │  │          │  └─user
    │  │  │          ├─handler
    │  │  │          ├─interceptor
    │  │  │          ├─mapper
    │  │  │          ├─service
    │  │  │          │  └─impl
    │  │  │          ├─task
    │  │  │          └─websocket
    │  │  └─resources
    │  │      ├─mapper
    │  │      └─template
    │  └─test
    │      └─java
    │          └─com
    │              └─sky
    │                  └─test
    └─target
        ├─classes
        │  ├─com
        │  │  └─sky
        │  │      ├─annotation
        │  │      ├─aspect
        │  │      ├─config
        │  │      ├─controller
        │  │      │  ├─admin
        │  │      │  ├─notify
        │  │      │  └─user
        │  │      ├─handler
        │  │      ├─interceptor
        │  │      ├─mapper
        │  │      ├─service
        │  │      │  └─impl
        │  │      ├─task
        │  │      └─websocket
        │  ├─mapper
        │  └─template
        ├─generated-sources
        │  └─annotations
        └─maven-status
            └─maven-compiler-plugin
                └─compile
                    └─default-compile
```

> 提示：运行前请确保 Redis 和 MySQL 服务已启动
