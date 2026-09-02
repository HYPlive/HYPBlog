# HYPBlog GitHub Actions 与服务器部署手册

本文对应当前仓库的 CI/CD 配置：

- 工作流：[.github/workflows/ci-cd.yml](../.github/workflows/ci-cd.yml)
- Compose 文件：[deploy/compose/docker-compose.yml](../deploy/compose/docker-compose.yml)
- Nginx 上游示例：[deploy/compose/nginx-existing-server.conf.example](../deploy/compose/nginx-existing-server.conf.example)

生产部署统一放在服务器的 `/home/ubuntu/hypblog-deploy`。旧的 `/home/ubuntu/hypblog` 仅作为旧部署和原始上传文件的保留位置，不能再与新部署混用。

## 1. 最终目录结构

```text
/home/ubuntu/hypblog-deploy/
├── .env                                  # Compose 变量，不含数据库或应用密钥
├── compose/
│   └── docker-compose.yml                # 从仓库复制的 Compose 文件
├── config/
│   ├── api.env                           # 数据库、Redis、监听端口
│   └── application-prod.properties       # 站点、JWT、邮件等应用配置
├── data/
│   └── upload/                           # 上传图片，持久化数据
└── log/                                  # API 文件日志
```

容器中的路径不等于服务器路径：

```text
服务器 /home/ubuntu/hypblog-deploy/data/upload -> 容器 /opt/hypblog/upload
服务器 /home/ubuntu/hypblog-deploy/log         -> 容器 /opt/hypblog/log
服务器 /home/ubuntu/hypblog-deploy/config/application-prod.properties
                                                   -> 容器 /config/application-prod.properties
```

## 2. 配置 GitHub Actions 权限

打开 GitHub 仓库后进入：

```text
Settings -> Actions -> General
```

确认：

1. 仓库允许运行 GitHub Actions。
2. **Workflow permissions** 选择 **Read and write permissions**。
3. 点击保存。

工作流用 GitHub 提供的 `GITHUB_TOKEN` 推送镜像到 GHCR；若保持只读权限，镜像发布会失败。

## 3. 创建 GitHub Secrets

进入：

```text
Settings -> Secrets and variables -> Actions -> Secrets
```

点击 **New repository secret**，创建以下四项。

### `DEPLOY_HOST`

服务器公网 IP 或 SSH 主机名，例如：

```text
203.0.113.10
```

不要填写 `http://`、`https://` 或 Nginx 端口。

### `DEPLOY_USER`

服务器登录用户，当前目录结构假设为：

```text
ubuntu
```

该用户必须能在不使用交互式 `sudo` 的情况下执行：

```bash
docker compose version
docker ps
```

若需要授予 Docker 权限，在服务器执行一次：

```bash
sudo usermod -aG docker ubuntu
```

之后重新登录 SSH。

### `DEPLOY_SSH_KEY`

填写专用部署私钥的完整内容：

```text
-----BEGIN OPENSSH PRIVATE KEY-----
...
-----END OPENSSH PRIVATE KEY-----
```

不要填写 `.pub` 公钥。推荐在本地生成独立部署密钥：

```bash
ssh-keygen -t ed25519 -f ~/.ssh/hypblog_deploy -C "hypblog-github-actions" -N ""
ssh-copy-id -i ~/.ssh/hypblog_deploy.pub ubuntu@SERVER_IP
ssh -i ~/.ssh/hypblog_deploy ubuntu@SERVER_IP
```

确认最后一条命令能登录后，将 `~/.ssh/hypblog_deploy` 私钥全文写入 Secret。

### `GHCR_READ_TOKEN`

用于服务器从私有 GHCR 拉取镜像。

创建步骤：

1. GitHub 右上角头像 -> **Settings**。
2. **Developer settings** -> **Personal access tokens** -> **Tokens (classic)**。
3. **Generate new token (classic)**。
4. 勾选 `read:packages`。
5. 创建后立即复制 Token，并保存到 `GHCR_READ_TOKEN` Secret。

Token 所属 GitHub 用户必须拥有 `HYPlive/HYPBlog` 及三个 Package 的读取权限。

## 4. 不要提前开启自动部署

在以下流程完成前，不要创建 GitHub Variable `DEPLOY_ENABLED=true`：

1. PR 的 CI 验证成功。
2. 合并到 `main` 后三个镜像成功发布。
3. 服务器手动启动容器成功。
4. Nginx 切换后外部域名访问成功。

未设置该 Variable 时，`main` 只会测试、构建、推送镜像，不会 SSH 连接服务器。

## 5. 首次发布镜像

当前 CI/CD 分支为：

```text
codex/cicd-docker-compose
```

先创建它到 `main` 的 Pull Request。在 PR 的 **Checks** 中确认以下步骤全部通过：

```text
Initialize test database
Test API
Build frontends
Build CMS
```

通过后合并到 `main`。合并会触发 `release`，将以下镜像推送到 GHCR：

```text
ghcr.io/hyplive/hypblog-api:<完整 commit SHA>
ghcr.io/hyplive/hypblog-view:<完整 commit SHA>
ghcr.io/hyplive/hypblog-cms:<完整 commit SHA>
```

当前工作流不发布 `latest`。记下第一个成功发布的完整 commit SHA，首次手动部署需要使用它。

在 GitHub 的 **Packages** 页面确认三个 Package 存在；保持 Private 时，确认部署 Token 对它们有读取权限。

## 6. 准备服务器

登录服务器：

```bash
ssh ubuntu@SERVER_IP
```

确认 Docker、Compose、MySQL、Redis 已运行：

```bash
docker --version
docker compose version
docker ps
mysql -h 127.0.0.1 -u hypblog -p hypblog
redis-cli -h 127.0.0.1 -p 6379 ping
```

Redis 应返回：

```text
PONG
```

新 Compose 不创建 MySQL 或 Redis。API 使用 host network，因此其 `127.0.0.1` 指向服务器自己的 MySQL 和 Redis。

创建目录：

```bash
mkdir -p /home/ubuntu/hypblog-deploy/compose
mkdir -p /home/ubuntu/hypblog-deploy/config
mkdir -p /home/ubuntu/hypblog-deploy/data/upload
mkdir -p /home/ubuntu/hypblog-deploy/log
```

从本地仓库将四个模板文件复制到服务器：

```bash
scp deploy/compose/docker-compose.yml ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/compose/
scp deploy/compose/.env.example ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/.env
scp deploy/compose/api.env.example ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/config/api.env
scp deploy/compose/application-prod.properties.example ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/config/application-prod.properties
```

设置配置权限：

```bash
chmod 600 /home/ubuntu/hypblog-deploy/.env
chmod 600 /home/ubuntu/hypblog-deploy/config/api.env
chmod 600 /home/ubuntu/hypblog-deploy/config/application-prod.properties
```

## 7. 填写服务器配置

### `.env`

编辑：

```bash
nano /home/ubuntu/hypblog-deploy/.env
```

填写：

```dotenv
GHCR_OWNER=HYPlive
IMAGE_TAG=填写第一个成功发布的完整 commit SHA
API_ENV_PATH=/home/ubuntu/hypblog-deploy/config/api.env
UPLOAD_PATH=/home/ubuntu/hypblog-deploy/data/upload
LOG_PATH=/home/ubuntu/hypblog-deploy/log
API_CONFIG_PATH=/home/ubuntu/hypblog-deploy/config/application-prod.properties
```

`IMAGE_TAG` 必须是实际已发布的完整 SHA，不能填 `latest`。

### `config/api.env`

编辑：

```bash
nano /home/ubuntu/hypblog-deploy/config/api.env
```

填写真实数据库密码：

```dotenv
SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/hypblog?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=hypblog
SPRING_DATASOURCE_PASSWORD=替换为真实数据库密码
SPRING_REDIS_HOST=127.0.0.1
SPRING_REDIS_PORT=6379
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=18090
UPLOAD_FILE_PATH=/opt/hypblog/upload/
LOGGING_FILE_NAME=/opt/hypblog/log/blog-api.log
```

`SERVER_ADDRESS=127.0.0.1` 和 `SERVER_PORT=18090` 不能改，否则会破坏 Nginx 反代与 GitHub Actions 健康检查。

### `config/application-prod.properties`

编辑：

```bash
nano /home/ubuntu/hypblog-deploy/config/application-prod.properties
```

至少确认以下项：

```properties
server.address=127.0.0.1
server.port=18090
blog.name=HYP's Blog
blog.api=https://api.hyptest.cn/blog
blog.cms=https://admin.hyptest.cn
blog.view=https://hyptest.cn
token.secretKey=填写新的强随机密钥
logging.file.name=/opt/hypblog/log/blog-api.log
upload.channel=local
upload.file.path=/opt/hypblog/upload/
upload.file.access-path=/image/**
upload.file.resources-locations=file:${upload.file.path}
```

生成 JWT 密钥：

```bash
openssl rand -hex 32
```

按实际使用情况补全 SMTP、Telegram、GitHub 图床或又拍云配置。不要把生产密码、Token 或私钥写入 Git。

## 8. 迁移旧上传文件

旧图片通常位于：

```text
/home/ubuntu/hypblog/upload
```

先备份，之后复制，不要直接移动或删除旧目录：

```bash
cp -a /home/ubuntu/hypblog/upload /home/ubuntu/hypblog/upload.backup
rsync -a /home/ubuntu/hypblog/upload/ /home/ubuntu/hypblog-deploy/data/upload/
```

若旧 API 仍在运行且可能上传新图片，先暂停旧 API，或在切换前再执行一次 `rsync`。新容器稳定运行并确认历史图片访问正常前，保留旧目录作为回滚备份。

## 9. 手动启动首次部署

在服务器登录 GHCR：

```bash
read -rsp 'GHCR token: ' GHCR_TOKEN; echo
printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u HYPlive --password-stdin
unset GHCR_TOKEN
```

启动 Compose：

```bash
cd /home/ubuntu/hypblog-deploy/compose
docker compose --env-file /home/ubuntu/hypblog-deploy/.env config --quiet
docker compose --env-file /home/ubuntu/hypblog-deploy/.env pull
docker compose --env-file /home/ubuntu/hypblog-deploy/.env up -d --remove-orphans
docker compose --env-file /home/ubuntu/hypblog-deploy/.env ps
```

查看 API 启动日志：

```bash
docker compose --env-file /home/ubuntu/hypblog-deploy/.env logs --tail=200 api
```

验证服务器本机服务：

```bash
curl --fail http://127.0.0.1:18080/
curl --fail http://127.0.0.1:18079/
curl --fail http://127.0.0.1:18090/site
```

预期：18080 返回前台 HTML，18079 返回 CMS HTML，18090 的 `/site` 返回 API JSON。

## 10. 切换 Nginx 上游

确认第 9 节三个本机探活都成功后，备份现有 Nginx 配置：

```bash
sudo cp /etc/nginx/sites-available/实际配置文件 /etc/nginx/sites-available/实际配置文件.before-hypblog-deploy
```

仅替换现有 server 块中的 `proxy_pass` 上游，不要覆盖域名、TLS 或其他 location。

```nginx
# api.hyptest.cn
location /blog/ {
    proxy_pass http://127.0.0.1:18090/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

# admin.hyptest.cn
location / {
    proxy_pass http://127.0.0.1:18079/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

# hyptest.cn
location / {
    proxy_pass http://127.0.0.1:18080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

检查并重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

验证外部域名：

```bash
curl --fail https://hyptest.cn/
curl --fail https://admin.hyptest.cn/
curl --fail https://api.hyptest.cn/blog/site
```

随后用浏览器验证前台文章、CMS 登录、历史图片和新图片上传。

## 11. 开启自动部署

手动部署与 Nginx 切换全部成功后，进入：

```text
Settings -> Secrets and variables -> Actions -> Variables
```

添加：

```text
Name: DEPLOY_ENABLED
Value: true
```

之后每次推送到 `main`：

1. Actions 运行 API 测试和前端构建。
2. 推送三个 commit SHA 镜像到 GHCR。
3. SSH 到 `/home/ubuntu/hypblog-deploy/compose`。
4. 检查 `.env`、`config/api.env` 和 `config/application-prod.properties` 是否存在。
5. 使用本次 SHA 拉取并更新容器。
6. 检查 18080、18079、18090。

自动部署不会覆盖服务器配置文件、上传文件或旧部署目录。

## 12. 日常检查与回滚

日常检查：

```bash
cd /home/ubuntu/hypblog-deploy/compose
docker compose --env-file /home/ubuntu/hypblog-deploy/.env ps
docker compose --env-file /home/ubuntu/hypblog-deploy/.env logs --tail=100 api
curl --fail http://127.0.0.1:18090/site
```

当前工作流不会自动回滚。回滚时将 `.env` 的 `IMAGE_TAG` 改成上一个验证正常的完整 SHA，然后执行：

```bash
cd /home/ubuntu/hypblog-deploy/compose
nano /home/ubuntu/hypblog-deploy/.env
docker compose --env-file /home/ubuntu/hypblog-deploy/.env pull
docker compose --env-file /home/ubuntu/hypblog-deploy/.env up -d --remove-orphans
```

再验证三个本机端口和外部域名。若问题来自 Nginx，恢复 Nginx 备份并执行：

```bash
sudo nginx -t && sudo systemctl reload nginx
```

## 13. 常见问题

### GHCR 推送失败

检查 GitHub Actions 的 Workflow permissions 是否为读写，以及工作流保留了 `packages: write`。

### 服务器无法拉取私有镜像

确认 `GHCR_READ_TOKEN` 包含 `read:packages`，且该 Token 的用户有 Package 读取权限。服务器上可重新交互式执行 `docker login ghcr.io` 验证。

### 自动部署 SSH 失败

检查 `DEPLOY_HOST`、`DEPLOY_USER`、`DEPLOY_SSH_KEY` 是否正确，公钥是否已添加到 `~/.ssh/authorized_keys`，部署用户是否能直接执行 `docker compose ps`。

### API 不断重启

执行：

```bash
cd /home/ubuntu/hypblog-deploy/compose
docker compose --env-file /home/ubuntu/hypblog-deploy/.env logs --tail=300 api
```

优先检查数据库、Redis、端口 18090、上传目录权限，以及 `config/api.env`、`config/application-prod.properties` 中的实际值。

## 14. 禁止操作

- 不要提交 `.env`、`api.env`、`application-prod.properties`、PAT 或 SSH 私钥。
- 不要使用 `latest` 镜像标签。
- 不要在首次手动部署验证前设置 `DEPLOY_ENABLED=true`。
- 不要执行 `docker compose down -v`。
- 不要在确认稳定前删除旧 `/home/ubuntu/hypblog` 目录或其上传文件。
- 不要将 API、CMS、前台端口直接绑定到 `0.0.0.0`；公网入口继续由 Nginx 提供。
