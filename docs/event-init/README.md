### 资源路径

```
# 数据库初始化 SQL
> cat ./resource/event-postgresql.sql

# 事件数据
> cat ./resource/events.json

# 客户端数据
> cat ./resource/clients.json

```

### 打包

```
# 依赖 go 和 goreleaser
> ./build.sh

# 打包目录 dist

```

### 同步命令

```
# linux
> ./event-init-linux -h

# Mac
> ./event-init-mac -h

# Windows
> ./event-init.exe -h

# 参数说明
-host       数据同步 Host，如：http://localhost:8080
-token      数据同步 Token
-events     开启事件信息初始化
-clients    开启客户端信息初始化
-db         数据库连接字符，用于表结构初始化,如：postgres://user:password@localhost:5432/spear-event?sslmode=disable
-tables     开启表结构初始化

# 例如，初始化事件信息
> ./event-init -host={HOST} -events
```