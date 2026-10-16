# 新功能 Git 提交流程

本文用于记录 HYPBlog 开发完一个新功能后，如何手动创建分支、提交代码并推送到远程仓库。

## 1. 检查当前状态

在项目根目录执行：

```bash
git status
git branch --show-current
git remote -v
```

确认：

- 当前目录是项目根目录。
- 当前分支和预期一致。
- 没有误修改或误删除的文件。
- 没有需要保留但尚未处理的其他功能改动。

查看代码差异：

```bash
git diff
git diff --cached
git diff --stat
```

只查看某个文件：

```bash
git diff -- path/to/file
```

## 2. 同步主分支

开始新功能前，先同步远程主分支：

```bash
git switch main
git pull --ff-only origin main
```

如果当前工作区有未提交改动，先不要强行切换分支。应先确认这些改动属于当前功能，或者将它们提交、暂存或另行处理。

## 3. 创建功能分支

从最新的 `main` 创建新分支：

```bash
git switch -c feature_todo
```

分支命名建议：

```text
feature_comment_like
feature_todo
fix_comment_pagination
refactor_blog_cache
docs_project_setup
chore_build_config
```

建议使用英文和下划线，名称直接表达工作内容。

## 4. 开发并验证

完成代码后，按照改动范围执行验证。

后端：

```bash
cd blog-api
mvn test
mvn package -DskipTests
```

前台 Vue：

```bash
cd blog-view
npm run build
```

后台 Vue：

```bash
cd blog-cms
npm run build
```

返回项目根目录：

```bash
cd ..
```

如果项目有数据库迁移，确认同时提供：

- 正向迁移脚本，例如 `docs/sql/todo.sql`
- 回滚脚本，例如 `docs/sql/todo_rollback.sql`
- 初始化数据库是否需要同步修改

## 5. 选择性暂存文件

先查看状态：

```bash
git status --short
```

只暂存当前功能相关文件，不要直接使用 `git add .` 或 `git add -A`：

```bash
git add blog-api/src/main/java/top/hyp/controller/TodoController.java
git add blog-api/src/main/java/top/hyp/service
git add blog-cms/src/views/blog/todo
git add blog-view/src/views/todo
git add docs/sql/todo.sql docs/sql/todo_rollback.sql
```

暂存后再次检查：

```bash
git diff --cached --stat
git diff --cached
git diff --cached --check
```

确认以下内容没有被提交：

- `.env`、密码、Token、私钥等敏感文件
- `node_modules/`
- `target/`
- `dist/`
- `*.out.log` 和 `*.err.log`
- 与当前功能无关的用户改动
- 临时缓存和本地配置

如果误暂存文件，可以移出暂存区但保留本地修改：

```bash
git restore --staged path/to/file
```

## 6. 创建提交

提交前确认暂存区只包含本次功能：

```bash
git status
```

创建新提交：

```bash
git commit -m "$(cat <<'EOF'
feat: 增加 Todo 管理与展示功能

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

提交信息建议使用以下格式：

```text
类型: 简短描述
```

常用类型：

- `feat`：新增功能
- `fix`：修复问题
- `refactor`：重构代码但不改变功能
- `docs`：文档变更
- `test`：测试变更
- `chore`：构建、依赖或工程配置变更

提交后检查：

```bash
git show --stat --summary HEAD
git status
```

## 7. 推送到远程分支

第一次推送当前分支时使用 `-u`：

```bash
git push -u origin feature_todo
```

后续继续开发并产生新提交后，直接执行：

```bash
git push
```

确认远程跟踪关系：

```bash
git status -sb
git branch -vv
```

看到类似以下内容，表示本地分支已跟踪远程分支：

```text
## feature_todo...origin/feature_todo
```

## 8. 创建 Pull Request

推送成功后，可以打开 GitHub 提供的 PR 地址，或者使用 GitHub CLI：

```bash
gh pr create --base main --head feature_todo --title "增加 Todo 管理与展示功能" --body "$(cat <<'EOF'
## Summary
- 增加 Todo 后端接口和数据持久化
- 增加 CMS Todo 看板管理
- 增加前台 Todo 展示页面
- 增加数据库正向和回滚迁移脚本

## Test plan
- [ ] 运行后端测试
- [ ] 构建 blog-view
- [ ] 构建 blog-cms
- [ ] 手动验证 Todo 创建、编辑、拖拽和删除
- [ ] 手动验证前台只展示已发布 Todo
EOF
)"
```

PR 的目标分支应为：

```text
base: main
compare: feature_todo
```

## 9. 后续同步主分支

如果开发期间 `main` 有新的提交：

```bash
git fetch origin
git merge origin/main
```

解决冲突后运行测试，再提交合并结果并推送：

```bash
git add path/to/resolved-file
git commit -m "chore: 同步主分支最新改动"
git push
```

不要使用以下命令覆盖他人或自己的未提交工作：

```bash
git reset --hard
git checkout .
git clean -f
git push --force
```

除非明确确认过影响范围，否则不要使用强制推送。

## 10. 合并完成后的清理

PR 合并到 `main` 后：

```bash
git switch main
git pull --ff-only origin main
git branch -d feature_todo
git fetch --prune
```

如果远程分支也需要删除：

```bash
git push origin --delete feature_todo
```

删除分支前确认 PR 已经合并，并且本地没有未提交改动。

## 快速流程

```bash
# 同步主分支
git switch main
git pull --ff-only origin main

# 创建功能分支
git switch -c feature_todo

# 开发并验证
# ...

# 选择性暂存
git add path/to/changed-file

# 检查暂存区
git diff --cached --check
git diff --cached --stat

# 提交
git commit -m "$(cat <<'EOF'
feat: 增加 Todo 管理与展示功能

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"

# 首次推送
git push -u origin feature_todo

# 创建 PR
# base: main
# compare: feature_todo
```
