# HYPBlog 部署 SOP（直接 jar + Nginx）

> 目标：从一台**全新 Ubuntu 服务器**，到博客完整上线，照这份文档一步步做即可。
> 适用范围：不用 Docker / 不用 GitHub Actions，本地打包 → 上传服务器 → Nginx 反代。
> 最后更新：2026-11-02

---

## 0. 部署架构速览（先看懂这张图）

三个应用，三个子域名，一个 Nginx 统一入口：

| 域名 | 内容 | Nginx 处理方式 |
|------|------|----------------|
| `hyptest.cn`（+ `www`） | 前台 blog-view 的 `dist` | 静态文件，`root /var/www/blog-view` |
| `admin.hyptest.cn` | 后台 blog-cms 的 `dist` | 静态文件，`root /var/www/blog-cms` |
| `api.hyptest.cn/blog` | 后端 Spring Boot | 反代到 `127.0.0.1:8090` |

```text
浏览器
  │  https://hyptest.cn            → Nginx → /var/www/blog-view（静态）
  │  https://admin.hyptest.cn      → Nginx → /var/www/blog-cms（静态）
  │  https://api.hyptest.cn/blog/* → Nginx → 127.0.0.1:8090（Java，剥掉 /blog/ 前缀）
```

关键约定：

- 后端只监听 `127.0.0.1:8090`，**不对外**，必须靠 Nginx 转发。
- 后端 **没有** Spring context-path，`/blog` 这个前缀是 Nginx 在 `location /blog/` 里剥掉的。
- 前端是 Vue Router history 模式，Nginx 必须配 `try_files ... /index.html`，否则刷新子页面会 404。

---

## 1. 当前服务器信息（这台，供参考）

| 项 | 值 |
|----|----|
| 系统 | Ubuntu（用户 `ubuntu`，主机名 `VM-0-5-ubuntu`） |
| 公网 IP | `152.136.54.32` |
| 域名 | `hyptest.cn`、`www.hyptest.cn`、`admin.hyptest.cn`、`api.hyptest.cn` |
| 后端 jar | `/home/ubuntu/app/blog-api-0.0.1.jar` |
| 后端日志目录 | `/home/ubuntu/hypblog/log/`（文件名 `blog-api.log-<日期>-<序号>.log`） |
| 图片上传目录 | `/home/ubuntu/hypblog/upload/` |
| 前端目录 | `/var/www/blog-view`、`/var/www/blog-cms` |
| Nginx 站点配置 | `/etc/nginx/sites-enabled/default` |
| SSL 证书 | `/etc/letsencrypt/live/hyptest.cn/`（一张证书覆盖 4 个域名） |

> 注意：后端日志没有裸的 `blog-api.log`，因为 `logback-spring.xml` 里 `<file>` 那行被注释了，日志直接按 `blog-api.log-<日期>-<序号>.log` 命名滚动（保留 365 天，单个超 10MB 切序号）。

---

## 2. 新服务器初始化（第一次做）

### 2.1 安装软件

```bash
sudo apt update

# JDK 8（项目是 Java 8 / Spring Boot 2.2.7）
sudo apt install -y openjdk-8-jdk
java -version   # 确认 1.8.x

# MySQL
sudo apt install -y mysql-server
# Redis
sudo apt install -y redis-server
# Nginx
sudo apt install -y nginx
# Certbot（HTTPS 证书）
sudo apt install -y certbot python3-certbot-nginx
```

> 也可以用更高版本 MySQL/Redis，不影响。MySQL 字符集必须支持 `utf8mb4`。

### 2.2 建目录

```bash
mkdir -p /home/ubuntu/app            # 放 jar 和日志
sudo mkdir -p /var/www/blog-view     # 前台 dist
sudo mkdir -p /var/www/blog-cms      # 后台 dist
```

### 2.3 建库、导数据

```bash
# 建库（utf8mb4，支持 emoji）
sudo mysql -e "CREATE DATABASE hypblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 导入基础表结构和种子数据（在本地仓库 blog-api/nblog.sql）
mysql -uroot -p hypblog < blog-api/nblog.sql

# 再按顺序应用 docs/sql/ 里 nblog.sql 之外的增量迁移（如果有）
# 例：mysql -uroot -p hypblog < docs/sql/todo.sql
```

> 数据库默认管理员账号是 `Admin` / `123456`（见 nblog.sql 种子数据）。
> 迁移脚本命名 `日期-功能-up.sql`（正向）/ `-down.sql`（回滚），按日期从小到大执行。

---

## 3. 后端部署（blog-api）

### 3.1 本地打包

在仓库 `blog-api` 目录执行：

```bash
mvn clean package -Dmaven.test.skip=true
```

产物：`blog-api/target/blog-api-0.0.1.jar`

### 3.2 生产配置（改哪里）

生产配置是 **jar 内置的** `blog-api/src/main/resources/application-prod.properties`。
启动命令里没有外挂配置，所以**改配置 = 改这个文件 → 重新打包 → 重新上传**。

打包前确认这几个值是对的：

```properties
server.port=8090
server.address=127.0.0.1           # 只监听本机，靠 Nginx 转发

blog.api=https://api.hyptest.cn/blog
blog.cms=https://admin.hyptest.cn
blog.view=https://hyptest.cn

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/hypblog?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&serverTimezone=GMT
spring.datasource.username=root
spring.datasource.password=1234   # 当前实际值（个人博客，建议以后换成强密码）

spring.redis.host=127.0.0.1
spring.redis.password=             # 有密码就填
spring.redis.port=6379

token.secretKey=【保留你原有的随机值，别用 dev 默认的 abcdefg...】

logging.level.root=info
logging.level.top.hyp=debug      # ⚠️ 必须 top.hyp，不是旧的 top.naccl（包名已改，写错 DEBUG 日志会失效）

logging.file.name=/home/ubuntu/hypblog/log/blog-api.log
upload.channel=local
upload.file.path=/home/ubuntu/hypblog/upload/
upload.file.access-path=/image/**
upload.file.resources-locations=file:${upload.file.path}

# 评论提醒（二选一：mail 或 tg），下面按 mail 举例
comment.notify.channel=mail
spring.mail.host=smtp.qq.com
spring.mail.port=465
spring.mail.username=【你的邮箱】
spring.mail.password=【邮箱授权码】
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
```

> `token.secretKey` 和邮箱授权码是敏感信息，别提交到 git；这份文档里也用占位符，不要写真实值。

### 3.3 上传 jar

在**本地**（Windows）把 jar 传到服务器。有几种方式，任选：

**方式 A：scp（命令行）**

在 Git Bash / WSL / PowerShell 里执行（把 IP 换成你的）：

```bash
scp blog-api/target/blog-api-0.0.1.jar ubuntu@152.136.54.32:/home/ubuntu/app/
```

**方式 B：图形化工具（推荐，不用记命令）**

- WinSCP / Xftp / FileZilla / MobaXterm：SSH 登录后，左边选本地文件，右边进 `/home/ubuntu/app/`，拖过去即可。

### 3.4 启动脚本 run.sh

在服务器上建 `/home/ubuntu/app/run.sh`：

```bash
#!/bin/bash
# 用法：sh run.sh 启动；更新时先 kill 旧进程再执行
cd /home/ubuntu/app

# 杀掉旧进程（按 jar 名精确匹配，不误伤其他 java）
OLD=$(pgrep -f "blog-api-0.0.1.jar")
if [ -n "$OLD" ]; then
  kill $OLD
  sleep 3
fi

nohup java -Xms256m -Xmx512m \
  -jar blog-api-0.0.1.jar \
  --spring.profiles.active=prod \
  > blog-api.out.log 2>&1 &

echo "started, pid=$(pgrep -f blog-api-0.0.1.jar)"
```

> `--spring.profiles.active=prod` 必须带，否则默认走 `dev` 配置。

### 3.5 更新 / 重启流程

1. 本地 `mvn clean package`（改过配置或代码的话）
2. 上传新 jar 到 `/home/ubuntu/app/`
3. `sh /home/ubuntu/app/run.sh`（脚本会先 kill 旧进程再起）

---

## 4. 前端部署（blog-view / blog-cms）

### 4.1 本地打包（先确认环境变量）

两个项目各有一个 `.env.production`，打包前确认 API 地址：

- `blog-view/.env.production` → `VUE_APP_API_BASE_URL=https://api.hyptest.cn/blog`
- `blog-cms/.env.production` → `VUE_APP_API_BASE_URL=https://api.hyptest.cn/blog/admin`

> 换新域名时，改这里的值再重新 build。前台结尾是 `/blog`，后台是 `/blog/admin`，别写错。
> 两个项目的 `publicPath` 都是 `/`（子域名部署，不是子路径），不用改。

```bash
cd blog-view && npm ci && npm run build   # 产物 blog-view/dist
cd blog-cms && npm ci && npm run build    # 产物 blog-cms/dist
```

### 4.2 上传 dist

把 **dist 目录里的内容** 上传到对应目录：

- `blog-view/dist/*` → 服务器 `/var/www/blog-view/`
- `blog-cms/dist/*` → 服务器 `/var/www/blog-cms/`

scp（在本地执行）：

```bash
scp -r blog-view/dist/* ubuntu@152.136.54.32:/var/www/blog-view/
scp -r blog-cms/dist/* ubuntu@152.136.54.32:/var/www/blog-cms/
```

图形化工具同理：打开 `/var/www/blog-view/`，先清掉旧文件，再把本地 `dist` 里的内容拖进去。

> 更新前建议备份旧目录：`sudo cp -r /var/www/blog-view /var/www/blog-view.bak`

---

## 5. Nginx 配置

配置文件：`/etc/nginx/sites-enabled/default`（编辑用 `sudo nano`）。

完整内容（三个域名 + HTTP 跳 HTTPS）：

```nginx
# ── 前台 hyptest.cn ──────────────────────────────
server {
    server_name hyptest.cn www.hyptest.cn;
    root /var/www/blog-view;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;   # history 模式必须
    }

    listen 443 ssl;
    ssl_certificate     /etc/letsencrypt/live/hyptest.cn/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/hyptest.cn/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
}

# ── 后台 admin.hyptest.cn ──────────────────────────
server {
    server_name admin.hyptest.cn;
    root /var/www/blog-cms;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    listen 443 ssl;
    ssl_certificate     /etc/letsencrypt/live/hyptest.cn/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/hyptest.cn/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
}

# ── 后端 api.hyptest.cn ────────────────────────────
server {
    server_name api.hyptest.cn;

    location /blog/ {
        proxy_pass http://127.0.0.1:8090/;   # 尾斜杠 = 剥掉 /blog/ 前缀
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Authorization $http_authorization;
    }
    location / {
        return 404;
    }

    listen 443 ssl;
    ssl_certificate     /etc/letsencrypt/live/hyptest.cn/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/hyptest.cn/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
}

# ── HTTP 80 → HTTPS 跳转（三个域名各一个）──────────
server {
    listen 80;
    server_name hyptest.cn www.hyptest.cn;
    return 301 https://$host$request_uri;
}
server {
    listen 80;
    server_name admin.hyptest.cn;
    return 301 https://$host$request_uri;
}
server {
    listen 80;
    server_name api.hyptest.cn;
    return 301 https://$host$request_uri;
}
```

改完验证 + 重载：

```bash
sudo nginx -t                 # 必须 syntax ok
sudo systemctl reload nginx
```

### ⚠️ 血泪坑：别在 sites-enabled 里留备份文件

nginx 的 `include /etc/nginx/sites-enabled/*;` 会加载目录里**所有文件**，不管后缀。

- ❌ 在 `sites-enabled/` 里存 `default.save`、`default.bak`、`default.disabled` 之类 → 会导致 `nginx -t` 失败、reload 失败，甚至重启后整个站起不来。
- ❌ 更隐蔽：证书自动续期（certbot 的 nginx 插件）也会因为 `nginx -t` 过不了而**静默失败**，最终证书过期。
- ✅ 备份文件要放到 `sites-enabled/` **外面**（比如 `~/` 或 `sites-available/`）。

---

## 6. HTTPS 证书

### 6.1 首次签发（新服务器）

```bash
# 一张证书同时覆盖 4 个域名
sudo certbot --nginx \
  -d hyptest.cn -d www.hyptest.cn \
  -d admin.hyptest.cn -d api.hyptest.cn
```

> 前提：域名 DNS 已解析到服务器 IP，且 80 端口能通（certbot 用 http-01 验证）。

### 6.2 续期 / 自动续期

```bash
# 手动续期
sudo certbot renew
sudo systemctl reload nginx

# 看证书状态和过期时间
sudo certbot certificates

# 确认自动续期定时器是开的（每天跑两次）
sudo systemctl status certbot.timer
```

证书 90 天过期。**如果发现过期了，多半是自动续期之前失败过**（常见原因就是第 5 节的 `.save` 备份文件把 nginx 弄坏了），排查：

```bash
sudo grep -iE "error|failed|challenge" /var/log/letsencrypt/letsencrypt.log | tail -30
```

---

## 7. 日志位置（速查）

| 日志 | 路径 | 命令 |
|------|------|------|
| Nginx 错误日志 | `/var/log/nginx/error.log` | `sudo tail -f /var/log/nginx/error.log` |
| Nginx 访问日志 | `/var/log/nginx/access.log` | `sudo tail -f /var/log/nginx/access.log` |
| 后端实时日志 | `/home/ubuntu/hypblog/log/blog-api.log-<当天日期>-0.log` | `tail -f /home/ubuntu/hypblog/log/blog-api.log-2026-11-02-0.log` |
| 后端 stdout（nohup 输出） | `/home/ubuntu/hypblog/log/app.log` | `tail -f /home/ubuntu/hypblog/log/app.log` |
| 后端历史日志（按天滚动） | `/home/ubuntu/hypblog/log/blog-api.log-*.log` | `ls -la /home/ubuntu/hypblog/log/` |
| 后端 dev 时期旧日志 | `/home/ubuntu/app/log/blog-dev.log-*.log` | `ls -la /home/ubuntu/app/log/` |
| Certbot 日志 | `/var/log/letsencrypt/letsencrypt.log` | `sudo tail -f /var/log/letsencrypt/letsencrypt.log` |

> 后端日志具体文件名和路径由 `application-prod.properties` 里的 `logging.file.name` 决定，用第 9 节命令确认实际值。

**各日志分别看什么（别找错地方）：**

- `access.log`：**每次 HTTP 请求**都记（方法、路径、状态码、IP），看实时访问流量用它。
- `blog-api.log-<日期>-0.log`：只有启动日志、定时任务、异常，以及 DEBUG 级的 SQL 日志（需 `logging.level.top.hyp=debug`）。
- 正常访问（点网站）**不会**写进后端日志——访问记录由 `VisitLogAspect` 写进数据库 `visit_log` 表，不写日志文件。所以 `tail -f` 后端日志时，点网站不滚动是正常的，别误判成故障。

---

## 8. 常见故障排查（按顺序查）

**页面打不开 / 502 / 白屏**

1. 后端活着吗：`ps aux | grep java`
2. 端口监听吗：`sudo ss -tlnp | grep -E '8090|:80|:443'`
3. nginx 活着吗：`systemctl status nginx`
4. 看 nginx 错误日志：`sudo tail -n 50 /var/log/nginx/error.log`

**页面能开但数据（文章列表）出不来**

1. 后端直连测：`curl -s http://127.0.0.1:8090/site`（返回 JSON 且 code 200 就正常）
2. 走完整链路测：`curl -s https://api.hyptest.cn/blog/site`
   - 若 `HTTP=000` 或报 `certificate has expired` → 证书过期，`sudo certbot renew`
3. 查前端 dist 编译的 API 地址对不对：
   ```bash
   grep -roh "https://api.hyptest.cn/blog\|http://localhost:8090" /var/www/blog-view/js/ | sort | uniq -c
   ```
   若出现 `http://localhost:8090` 说明用错环境变量打包了。

**地址栏显示「不安全」**

- 证书过期：`sudo certbot renew` + `sudo systemctl reload nginx`，然后浏览器硬刷新 / 无痕验证。
- 混合内容（页面是 https，但里面嵌了 http 资源）：F12 → Console 找 `Mixed Content` 警告，或 Security 面板看具体 http 地址，改成 https。

**后端日志不滚动（点网站没日志）**

- 先确认是不是正常现象：访问记录写进数据库 `visit_log` 表，不写日志文件；看实时流量用 `sudo tail -f /var/log/nginx/access.log`。
- 想看 SQL/debug 日志却看不到：检查 `application-prod.properties` 里 `logging.level.top.hyp=debug` 是否写成了旧的 `top.naccl`（包名从 `top.naccl` 改成 `top.hyp` 后漏改，写错则 DEBUG 失效），改对后重新打包部署。

---

## 9. 已确认的实际值（这台服务器）

以下是从运行中的 jar 里 `unzip -p ... application-prod.properties` 拿到的真实值，换新服务器时照抄即可（密码类建议替换成新值）：

| 配置项 | 实际值 |
|--------|--------|
| 数据库地址 | `jdbc:mysql://127.0.0.1:3306/hypblog`（`serverTimezone=GMT`） |
| 数据库账号 | `root` / `1234` |
| Redis | `127.0.0.1:6379`，无密码 |
| 上传目录 | `/home/ubuntu/hypblog/upload/` |
| 日志文件 | `/home/ubuntu/hypblog/log/blog-api.log`（实际按天滚动成 `blog-api.log-<日期>-<序号>.log`） |
| 日志级别 | `root=info` + `top.hyp=debug`（包名已改，勿写旧 `top.naccl`） |
| 评论通知 | mail，QQ 邮箱 `smtp.qq.com:465` |
| token.secretKey | 见 `application-prod.properties`（不要用 dev 的默认值） |

> 以后想再核对 jar 里的实际配置，用这条命令：
> ```bash
> unzip -p /home/ubuntu/app/blog-api-0.0.1.jar BOOT-INF/classes/application-prod.properties
> ```

---

## 附：一图流部署清单

```text
新服务器：
  apt 装 JDK8/MySQL/Redis/Nginx/certbot
  mkdir 目录
  建库 hypblog(utf8mb4) + 导入 nblog.sql + 增量迁移
  → certbot 签证书
  → 写 nginx 配置（3 个域名）+ nginx -t + reload

每次发版：
  后端：mvn package → 传 jar → sh run.sh
  前端：npm run build → 传 dist 到 /var/www/ 对应目录
  （nginx 不用动，除非域名/路径变了）
```
