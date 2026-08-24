# HYPBlog Compose 部署

这套部署适合 2C2G 服务器：GitHub Actions 负责构建镜像，服务器只负责拉取和运行。它不会覆盖现有 `/home/ubuntu/hypblog`、`/home/ubuntu/blog-view`、`/home/ubuntu/blog-cms` 目录；新容器使用独立目录 `/home/ubuntu/hypblog-cicd`，现有 Nginx 继续对外提供 80/443。

## 首次准备服务器

从本仓库根目录执行：

```bash
sudo mkdir -p /home/ubuntu/hypblog-cicd
sudo cp deploy/compose/docker-compose.yml /home/ubuntu/hypblog-cicd/
sudo cp deploy/compose/api.env.example /home/ubuntu/hypblog-cicd/api.env
sudo cp deploy/compose/.env.example /home/ubuntu/hypblog-cicd/.env
```

编辑 `/home/ubuntu/hypblog-cicd/api.env`、`/home/ubuntu/hypblog-cicd/.env`，并确认 `/home/ubuntu/hypblog/config/application-prod.properties` 使用的是线上数据库和 Redis 配置。API 使用宿主机网络，因此 `127.0.0.1` 会继续指向服务器已有的 MySQL 和 Redis；API 本身改为监听宿主机 `18090`。不要把这些文件提交到 Git。数据库和 Redis 继续使用你现有线上实例，不由这套 Compose 重建。

启动新容器：

```bash
cd /home/ubuntu/hypblog-cicd
docker compose up -d
```

然后参考 `nginx-existing-server.conf.example`，分别在 `api.hyptest.cn`、`admin.hyptest.cn` 和 `hyptest.cn` 的现有 Nginx server 块中替换对应 `proxy_pass` 上游，执行 `sudo nginx -t && sudo systemctl reload nginx`。先确认 `curl http://127.0.0.1:18080/`、`curl http://127.0.0.1:18079/` 和 `curl http://127.0.0.1:18090/` 正常，再切换 Nginx 流量。

## GitHub 配置

在仓库 Settings → Secrets and variables → Actions 中配置：

- `DEPLOY_HOST`
- `DEPLOY_USER`
- `DEPLOY_SSH_KEY`
- `GHCR_READ_TOKEN`（服务器拉取私有 GHCR 镜像时需要）

首次迁移完成前，不要设置 `DEPLOY_ENABLED` 变量；此时推送到 `main` 只会构建和推送镜像。确认新容器与 Nginx 均正常后，在 Settings → Secrets and variables → Actions → Variables 添加 `DEPLOY_ENABLED=true`。之后推送到 `main`，Actions 才会通过 SSH 更新服务器。现有 JAR 和前端目录保留，出现问题时可把 Nginx location 改回原配置进行回滚。
