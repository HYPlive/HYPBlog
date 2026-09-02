# HYPBlog 生产部署

本项目使用 GitHub Actions 构建并推送三个 GHCR 镜像，服务器使用 Docker Compose 运行。服务器统一使用 `/home/ubuntu/hypblog-deploy` 作为 HYPBlog 的生产部署目录；旧的 `/home/ubuntu/hypblog` 目录在迁移完成前保留，不由新 Compose 使用。

## 服务器目录

```text
/home/ubuntu/hypblog-deploy/
├── compose/
│   └── docker-compose.yml
├── .env
├── config/
│   ├── api.env
│   └── application-prod.properties
├── data/upload/
└── log/
```

`config/api.env` 和 `config/application-prod.properties` 包含生产配置或密钥，只保存在服务器，不要提交到 Git。

## 首次准备服务器

先从仓库根目录将 Compose 文件复制到统一部署目录：

```bash
ssh ubuntu@SERVER_IP
mkdir -p /home/ubuntu/hypblog-deploy/compose
exit
scp deploy/compose/docker-compose.yml ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/compose/
```

登录服务器创建目录和配置文件：

```bash
ssh ubuntu@SERVER_IP
mkdir -p /home/ubuntu/hypblog-deploy/config
mkdir -p /home/ubuntu/hypblog-deploy/data/upload
mkdir -p /home/ubuntu/hypblog-deploy/log
```

从本仓库复制模板创建配置文件：

```bash
scp deploy/compose/.env.example ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/.env
scp deploy/compose/api.env.example ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/config/api.env
scp deploy/compose/application-prod.properties.example ubuntu@SERVER_IP:/home/ubuntu/hypblog-deploy/config/application-prod.properties
```

编辑三个配置文件，填写真实数据库、Redis、站点、Token 和通知配置：

```bash
nano /home/ubuntu/hypblog-deploy/.env
nano /home/ubuntu/hypblog-deploy/config/api.env
nano /home/ubuntu/hypblog-deploy/config/application-prod.properties
chmod 600 /home/ubuntu/hypblog-deploy/.env
chmod 600 /home/ubuntu/hypblog-deploy/config/api.env
chmod 600 /home/ubuntu/hypblog-deploy/config/application-prod.properties
```

`.env` 中的路径必须保持如下关系：

```dotenv
GHCR_OWNER=HYPlive
IMAGE_TAG=已发布的完整 commit SHA
API_ENV_PATH=/home/ubuntu/hypblog-deploy/config/api.env
UPLOAD_PATH=/home/ubuntu/hypblog-deploy/data/upload
LOG_PATH=/home/ubuntu/hypblog-deploy/log
API_CONFIG_PATH=/home/ubuntu/hypblog-deploy/config/application-prod.properties
```

API 容器内路径仍是 `/opt/hypblog/upload` 和 `/opt/hypblog/log`，这是容器路径，不是服务器路径。

## 迁移旧上传文件

确认旧目录中的上传文件位置后，先备份，再复制到新目录。不要直接删除旧目录：

```bash
cp -a /home/ubuntu/hypblog/upload /home/ubuntu/hypblog/upload.backup
rsync -a /home/ubuntu/hypblog/upload/ /home/ubuntu/hypblog-deploy/data/upload/
```

如果旧 API 仍在写入文件，迁移期间应短暂停止旧 API，或在迁移后再次执行 `rsync`。确认新容器和外部访问正常后，再保留旧目录作为回滚备份。

## 手动首次启动

先登录 GHCR，再使用 `.env` 中的 SHA 标签启动：

```bash
read -rsp 'GHCR token: ' GHCR_TOKEN; echo
printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u HYPlive --password-stdin
unset GHCR_TOKEN
cd /home/ubuntu/hypblog-deploy/compose
docker compose --env-file /home/ubuntu/hypblog-deploy/.env config --quiet
docker compose --env-file /home/ubuntu/hypblog-deploy/.env pull
docker compose --env-file /home/ubuntu/hypblog-deploy/.env up -d --remove-orphans
docker compose --env-file /home/ubuntu/hypblog-deploy/.env ps
```

本机验证：

```bash
curl --fail http://127.0.0.1:18080/
curl --fail http://127.0.0.1:18079/
curl --fail http://127.0.0.1:18090/site
```

## Nginx 切换

确认三个本机地址都成功后，在现有 Nginx 配置中仅替换上游地址，保留原有域名、TLS 和其他 location：

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
}

# hyptest.cn
location / {
    proxy_pass http://127.0.0.1:18080/;
}
```

修改前备份配置，然后检查并重载：

```bash
sudo cp /etc/nginx/sites-available/实际配置文件 /etc/nginx/sites-available/实际配置文件.before-hypblog
sudo nginx -t
sudo systemctl reload nginx
```

验证外部地址：

```bash
curl --fail https://hyptest.cn/
curl --fail https://admin.hyptest.cn/
curl --fail https://api.hyptest.cn/blog/site
```

## GitHub Actions 配置

在仓库进入：

```text
Settings -> Actions -> General
```

确认 Actions 允许执行，并将 Workflow permissions 设置为 **Read and write permissions**，因为工作流需要使用 `GITHUB_TOKEN` 推送 GHCR 镜像。

在：

```text
Settings -> Secrets and variables -> Actions -> Secrets
```

创建：

- `DEPLOY_HOST`：服务器 IP 或 SSH 主机名，不带协议和端口。
- `DEPLOY_USER`：SSH 用户名，例如 `ubuntu`。
- `DEPLOY_SSH_KEY`：部署用 SSH 私钥完整内容，不是 `.pub` 公钥。
- `GHCR_READ_TOKEN`：有 `read:packages` 权限的 GitHub classic PAT，供服务器拉取私有镜像。

首次手动部署完成前，不要创建或启用 `DEPLOY_ENABLED=true`。手动部署、Nginx 切换和外部访问全部成功后，再进入：

```text
Settings -> Secrets and variables -> Actions -> Variables
```

创建：

```text
Name: DEPLOY_ENABLED
Value: true
```

工作流后续会 SSH 到 `/home/ubuntu/hypblog-deploy/compose`，检查三个服务器配置文件，按当前 commit SHA 拉取并启动镜像，然后检查 18080、18079、18090。

## 自动更新与回滚

每次推送到 `main` 会先执行 API 测试和两个前端构建，再推送三个使用 commit SHA 的镜像。自动部署不会生成、覆盖或删除服务器上的生产配置和上传数据。

回滚时修改 `/home/ubuntu/hypblog-deploy/.env` 的 `IMAGE_TAG` 为上一个正常的 commit SHA，然后执行：

```bash
cd /home/ubuntu/hypblog-deploy/compose
docker compose --env-file /home/ubuntu/hypblog-deploy/.env pull
docker compose --env-file /home/ubuntu/hypblog-deploy/.env up -d --remove-orphans
```

如果问题出在 Nginx，恢复备份配置后执行 `sudo nginx -t && sudo systemctl reload nginx`。

## 注意事项

- 不要提交 `.env`、`api.env`、`application-prod.properties`、PAT 或 SSH 私钥。
- 不要使用 `latest`，当前发布流程使用 commit SHA 标签。
- 不要执行 `docker compose down -v`，本项目不需要删除数据卷。
- 不要删除旧 `/home/ubuntu/hypblog` 目录，直到新部署完成验证并经过回滚观察期。
- MySQL 和 Redis 不由本 Compose 创建，API 通过 host network 访问服务器上的 `127.0.0.1`。
