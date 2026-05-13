# NBlog to HYPBlog Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a clean HYPBlog project copy from NBlog-master with the backend Java namespace changed from `top.hyp` to `top.hyp`.

**Architecture:** Copy only source and durable project assets into `D:\Java_project\HYPBlog`, excluding generated artifacts and local dependency directories. Rename backend Java source roots and update package/import/groupId references in text source files.

**Tech Stack:** Spring Boot 2.2.7, Maven, Java 8 source level, Vue 2 frontend projects.

---

## File Structure

- Copy into `D:\Java_project\HYPBlog`: `blog-api`, `blog-cms`, `blog-view`, `pic`, `upload`, `.gitignore`, `deploy.sh`, `LICENSE`, `README.md`.
- Exclude: `.idea`, `blog-api\target`, `blog-api\log`, `blog-cms\node_modules`, `blog-view\node_modules`.
- Move backend source directories after copy:
  - `D:\Java_project\HYPBlog\blog-api\src\main\java\top\naccl` to `D:\Java_project\HYPBlog\blog-api\src\main\java\top\hyp`
  - `D:\Java_project\HYPBlog\blog-api\src\test\java\top\naccl` to `D:\Java_project\HYPBlog\blog-api\src\test\java\top\hyp`
- Modify text files under `D:\Java_project\HYPBlog` to replace `top.hyp` with `top.hyp`.

### Task 1: Clean Copy Project Files

**Files:**
- Create/copy into: `D:\Java_project\HYPBlog`

- [ ] **Step 1: Confirm HYPBlog has no project files except migration docs**

Run: `Get-ChildItem -Force`

Expected: either only `docs` exists, or the directory has no existing project files that would be overwritten.

- [ ] **Step 2: Copy durable project files from NBlog-master**

Run a copy command that excludes `.idea`, `target`, `log`, and `node_modules`.

Expected: `blog-api`, `blog-cms`, `blog-view`, `pic`, `upload`, `.gitignore`, `deploy.sh`, `LICENSE`, and `README.md` appear in `D:\Java_project\HYPBlog`.

- [ ] **Step 3: Confirm excluded directories are absent**

Run: `Test-Path blog-api\target; Test-Path blog-api\log; Test-Path blog-cms\node_modules; Test-Path blog-view\node_modules; Test-Path .idea`

Expected: every value is `False`.

### Task 2: Rename Backend Package Directories

**Files:**
- Move: `D:\Java_project\HYPBlog\blog-api\src\main\java\top\naccl`
- Move: `D:\Java_project\HYPBlog\blog-api\src\test\java\top\naccl`

- [ ] **Step 1: Create destination package directories**

Create `top\hyp` under both `src\main\java` and `src\test\java`.

Expected: both destination directories exist.

- [ ] **Step 2: Move source files**

Move all files and subdirectories from `top\naccl` into `top\hyp`.

Expected: `BlogApiApplication.java` is located at `blog-api\src\main\java\top\hyp\BlogApiApplication.java`, and `BlogApiApplicationTests.java` is located at `blog-api\src\test\java\top\hyp\BlogApiApplicationTests.java`.

- [ ] **Step 3: Remove empty old package directories**

Remove `top\naccl` only after confirming it is empty.

Expected: `blog-api\src\main\java\top\naccl` and `blog-api\src\test\java\top\naccl` do not exist.

### Task 3: Replace Namespace References

**Files:**
- Modify: Java source files under `D:\Java_project\HYPBlog\blog-api\src`
- Modify: `D:\Java_project\HYPBlog\blog-api\pom.xml`
- Modify: documentation/config text files where `top.hyp` appears

- [ ] **Step 1: Replace exact package namespace in text files**

Replace every exact text occurrence of `top.hyp` with `top.hyp` in source and project text files.

Expected: Java `package` declarations and `import` declarations use `top.hyp`.

- [ ] **Step 2: Verify no old namespace remains**

Run: `rg "top\.naccl" -n -g "!**/node_modules/**" -g "!**/target/**" -g "!**/log/**"`

Expected: no matches.

- [ ] **Step 3: Verify new namespace exists in key files**

Run: `rg "top\.hyp" -n blog-api\src blog-api\pom.xml README.md`

Expected: matches include `BlogApiApplication.java`, `BlogApiApplicationTests.java`, and `blog-api\pom.xml`.

### Task 4: Backend Verification

**Files:**
- Read/verify: `D:\Java_project\HYPBlog\blog-api\pom.xml`
- Read/verify: backend Java source under `D:\Java_project\HYPBlog\blog-api\src`

- [ ] **Step 1: Run Maven tests**

Run from `D:\Java_project\HYPBlog\blog-api`: `mvn test`

Expected: Maven compiles the project and test phase completes successfully. If local services such as MySQL or Redis are required for context loading, record the failure and run `mvn -DskipTests package` to verify compilation.

- [ ] **Step 2: Run Maven package fallback if needed**

Run from `D:\Java_project\HYPBlog\blog-api`: `mvn -DskipTests package`

Expected: build completes successfully and generated output appears only under `blog-api\target`.

### Task 5: Final Sanity Check

**Files:**
- Inspect: `D:\Java_project\HYPBlog`

- [ ] **Step 1: List top-level project contents**

Run: `Get-ChildItem -Force`

Expected: project root contains copied project entries plus `docs`.

- [ ] **Step 2: Search for excluded generated directories**

Run: `Get-ChildItem -Recurse -Directory -Force | Where-Object { $_.FullName -match '\\node_modules$|\\log$' }`

Expected: no `node_modules` or old `log` directories. A new `target` directory may exist only if Maven verification generated it.
