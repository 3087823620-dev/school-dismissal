# 可视化放学系统 - Spring Boot 3 + MyBatis-Plus 版后端

与原 Node.js/Express 版**同库同 API**，前端 Vue3 零改动。

## 技术栈
- JDK 17 + Spring Boot 3.2.5
- MyBatis-Plus 3.5.7（`mybatis-plus-spring-boot3-starter`）
- MySQL 8 + HikariCP
- JJWT 0.11.5（JWT 24h，密钥与原 Node 版一致）
- spring-security-crypto（BCryptPasswordEncoder，salt=10，与 bcryptjs 兼容）

## 目录
```
src/main/java/com/school/dismissal/
├── SchoolDismissalApplication.java
├── common/        Result / BusinessException / GlobalExceptionHandler
├── config/        CorsConfig / MybatisPlusConfig / BeanConfig / WebConfig
├── security/      JwtUtil / RateLimitInterceptor
├── entity/        6 张表对应实体（@TableName / @TableId(IdType.AUTO)）
├── mapper/        6 个 Mapper（extends BaseMapper<T>，复杂联表用 @Select）
└── controller/    Auth / User / Classes / Student / Dismissal / Notice / Health
```

## 与原 Node 版的对应
| 原 Node 版 | Spring Boot 版 |
|---|---|
| `mysql2/promise` 手写 SQL | `BaseMapper` 单表 CRUD + `@Select` 注解写联表 SQL |
| `bcryptjs.hash/compare` | `BCryptPasswordEncoder` |
| `jsonwebtoken` | `jjwt` |
| `express-rate-limit` | `RateLimitInterceptor`（内存计数，阈值 200/20/50 与原版一致）|
| `winston` | Spring Boot 默认 logback |
| `pool.query` 批量插入（事务） | `@Transactional` + 循环 `mapper.insert` |

## 启动
1. 确保 MySQL 已建库 `school_dismissal`（执行 `src/main/resources/schema.sql`）。
2. 如需初始数据，先在旧 Node 目录跑一次 `npm run init-db`，或自行插入 bcrypt 密码。
3. 修改 `src/main/resources/application.yml` 中的数据库账号密码。
4. 启动：
   ```
   mvn spring-boot:run
   ```
   或打包：
   ```
   mvn -DskipTests package
   java -jar target/school-dismissal.jar
   ```
5. 服务监听 `http://localhost:3000`，前端 Vite 代理 `/api` 已指向该端口，无需改前端。

## 默认账号（与原 Node 版一致）
- admin / admin123（管理员）
- teacher / teacher123（教师）
