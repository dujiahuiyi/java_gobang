# 实时五子棋对战平台

[![Language](https://img.shields.io/badge/Language-Java-blue.svg)](https://www.java.com)
[![Framework](https://img.shields.io/badge/Framework-Spring%20Boot-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL-orange.svg)](https://www.mysql.com)
[![WebSocket](https://img.shields.io/badge/Protocol-WebSocket-blueviolet.svg)](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API)

一个基于 Spring Boot 和 WebSocket 技术栈实现的在线五子棋对战平台。实现了用户认证、实时匹配、在线对战、战绩统计等核心功能，并特别关注了后端系统在高并发场景下的稳定性和性能。

---

## 📸 项目预览

在这里放置一个项目的主要流程GIF动图，会非常吸引眼球！
*(**提示**: 使用 [ScreenToGif](https://www.screentogif.com/) 或类似工具录制一个从登录、大厅匹配到开始对战的完整流程GIF，然后上传到仓库或图床，在这里引用)*

![项目演示GIF](https://raw.githubusercontent.com/user-attachments/assets/b4614e21-0a6a-4d40-9f2d-74d3d81b2190.gif) 

**(登录与注册)**
![登录注册](https://raw.githubusercontent.com/user-attachments/assets/81c4e7ab-18e9-4e0d-b94f-f230b0800d92.png)

**(游戏大厅)**
![游戏大厅](https://raw.githubusercontent.com/user-attachments/assets/36b81313-2d2c-4734-b26a-9a997d5cc394.png)

**(游戏对战)**
![游戏对战](https://raw.githubusercontent.com/user-attachments/assets/516e9b25-0955-46b5-827c-652f1e405a8b.png)


## ✨ 主要功能

- **用户系统**:
  - [x] 用户注册与登录功能
  - [x] 基于`Session`的会话管理与登录拦截
  - [x] 密码采用 `MD5 + Salt` 方式加密存储，保证数据安全
- **匹配系统**:
  - [x] 基于WebSocket的实时匹配/取消匹配功能
  - [x] 根据用户天梯分数划分不同等级的匹配队列，优先匹配实力相近的对手
  - [x] 多线程处理匹配逻辑，保证匹配效率
  - [x] 严格的并发安全控制，防止用户重复匹配或“自己匹配自己”
- **对战系统**:
  - [x] 基于WebSocket的实时游戏对战功能
  - [x] 实时同步双方落子、回合状态等信息
  - [x] 后端进行严谨的五子连珠胜负判断（横、竖、左斜、右斜）
- **核心体验**:
  - [x] 玩家断线后自动判负，保证对局公平性
  - [x] 游戏结束后自动更新双方的天梯分数与战绩统计
  - [x] 全局异常处理和统一的API响应格式，提升前后端协作效率和健壮性

## 🛠️ 技术栈

| 分类 | 技术 |
| :--- | :--- |
| **后端核心框架** | Spring Boot, Spring MVC |
| **实时通信** | Spring WebSocket |
| **数据持久层** | MyBatis-Plus |
| **数据库** | MySQL 8.0 |
| **构建工具** | Maven |
| **前端** | HTML5, CSS3, JavaScript (原生) |
| **其他依赖** | Lombok, Jackson, HikariCP |


## 🚀 快速开始

1.  **克隆项目到本地**
    ```bash
    git clone https://github.com/dujiahuiyi/java_gobang.git
    cd java_gobang
    ```

2.  **准备数据库**
    - 在你的MySQL中创建一个新的数据库，例如 `java_gobang`。
    - 将项目根目录下的 `java_gobang.sql` 文件导入到你创建的数据库中，以创建所需的表结构。

3.  **修改配置**
    - 打开 `src/main/resources/application.properties` 文件。
    - 根据你的本地环境，修改以下数据库连接信息：
      ```properties
      spring.datasource.url=jdbc:mysql://localhost:3306/java_gobang?serverTimezone=UTC
      spring.datasource.username=root
      spring.datasource.password=your_password
      ```

4.  **运行后端服务**
    - 使用 IntelliJ IDEA 或 Eclipse 打开项目。
    - 等待 Maven 自动下载所有项目依赖。
    - 找到 `com.dujia.java_gobang.JavaGobangApplication.java` 文件，运行 `main` 方法。

5.  **访问项目**
    - 打开浏览器，访问 `http://localhost:8080/login.html`。
    - 注册两个不同的账号，在两个不同的浏览器（或一个普通窗口、一个隐身窗口）中登录，即可开始匹配对战。

## 🎯 核心设计与难点剖析

这个项目不仅是一个简单的CRUD应用，我在设计和开发过程中特别关注了实时性和并发安全问题。

#### 1. 高并发匹配系统的设计

- **挑战**: 如何设计一个高效且线程安全的匹配系统，以应对大量用户同时请求匹配的场景，同时避免使用重量级的`synchronized`锁带来的性能开销？
- **我的解决方案**:
    - **生产者-消费者模型**: 我将匹配系统抽象为经典的生产者-消费者模型。用户（前端）点击匹配是**生产者**，将自己（`User`对象）放入匹配池；后台独立的匹配线程是**消费者**，不断地从池中取出玩家进行配对。
    - **`BlockingDeque`的应用**: 我选择了Java并发包中的`BlockingDeque`作为匹配池。它是一个线程安全的双端阻塞队列，其`take()`方法在队列为空时会自动阻塞，`put()`方法在队列满时也会阻塞。这完美地契合了我的模型，使得匹配线程可以安全、高效地等待玩家，无需任何手动的`wait/notify`或`lock`操作，代码简洁且性能优越。

#### 2. “自己匹配自己”竞态条件的解决方案

- **问题**: 在用户进行快速操作（如：点击匹配 -> 立即取消 -> 再次点击匹配）的边界场景下，由于网络延迟和线程调度的不确定性，可能会导致一个用户的实例被先后两次放入匹配队列，并被匹配线程取出，造成“自己和自己对战”的罕见bug。
- **我的解决方案**: 我实施了双重保障来根除此问题：
    1.  **原子性检查**: 在用户加入匹配队列前，使用`ConcurrentHashMap`的`putIfAbsent`方法，确保同一时刻一个用户ID只在匹配表中存在一次。
    2.  **最终校验**: 我认为前端和入口的防护总是不够的，最可靠的防线必须在核心逻辑里。因此，在匹配线程成功取出两个玩家(`player1`, `player2`)后，我增加了一道**最终的、不可绕过的校验**：`if (player1.getUserId().equals(player2.getUserId()))`。如果ID相同，则直接丢弃本次“无效”匹配。这道防线彻底解决了该竞态条件问题。

#### 3. 游戏状态的实时与可靠同步

- **挑战**: 在对战中，如何确保一个玩家的落子操作能够被**实时**、**可靠**地同步给对手，并正确切换双方的回合状态？
- **我的解决方案**:
    - **WebSocket长连接**: 我采用 WebSocket 在客户端和服务器之间建立全双工的长连接，避免了传统HTTP轮询的延迟和资源浪费。
    - **服务端权威仲裁**: 所有的游戏逻辑，包括落子合法性验证、回合切换、胜负判断，都**严格由服务端**进行。前端只负责渲染和发送用户操作。这保证了即使有恶意用户篡改前端代码，也无法破坏游戏规则和状态的一致性。
    - **消息广播**: 当一个玩家落子并经由服务器验证通过后，服务器会将包含新棋子位置、下一回合归属、胜负结果（如果有）的`putChess`消息**广播**给房间内的**所有玩家**。这样，对战双方总能接收到完全一致的、由权威方（服务器）认证过的游戏状态。

---
## 📝 未来的迭代方向
- [ ] **排行榜功能**: 增加一个天梯分排行榜，展示Top玩家。
- [ ] **断线重连**: 允许玩家在短暂断线后，重新连接回未结束的对局。
- [ ] **好友系统与邀请对战**: 增加好友功能，并允许玩家向好友发起指定的对战邀请。
- [ ] **容器化部署**: 使用 Docker 将应用和数据库打包成镜像，实现一键部署。


## 🙏 致谢
感谢您花时间阅读我的项目！如果这个项目对您有帮助，欢迎 Star！