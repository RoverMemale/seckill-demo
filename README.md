# 高并发秒杀系统

基于 Spring Boot 实现的秒杀系统，解决了高并发下的超卖、接口防刷、数据库压力等经典问题。

## 技术栈

- Spring Boot 3.2
- MyBatis-Plus
- MySQL 8.0
- Redis 7.0
- RabbitMQ 3.12
- JWT
- JMeter（压测）

## 核心功能

- 用户登录（JWT 无状态认证）
- 秒杀商品列表展示
- 秒杀接口（Redis 预减库存 + RabbitMQ 异步下单）
- 秒杀结果轮询查询

## 快速启动

1. 创建数据库 `seckill_db`，执行 `docs/sql/schema.sql` 建表。
2. （可选）执行 `docs/sql/data.sql` 插入测试数据。
3. 修改 `src/main/resources/application.yml` 中的数据库、Redis、RabbitMQ 连接信息。
4. 启动 MySQL、Redis、RabbitMQ 服务。
5. 运行 `SeckillApplication` 的 `main` 方法。
6. 访问 `http://localhost:8080/index.html`。

## 压测结果

使用 JMeter 模拟 1000 线程并发，系统吞吐量提升约 3 倍，无超卖情况。

## 项目结构

src/main/java/com/seckill/

├── config/ # 配置类（Redis、RabbitMQ、WebMvc）

├── controller/ # 控制器

├── service/ # 业务逻辑

├── mapper/ # MyBatis-Plus Mapper

├── entity/ # 实体类

├── dto/ # 数据传输对象

├── mq/ # 消息队列消息体及消费者

├── interceptor/ # 拦截器

├── util/ # 工具类

└── SeckillApplication.java
