# NBlog to HYPBlog Migration Design

## Goal

Create a clean copy of `D:\Java_project\NBlog-master` in `D:\Java_project\HYPBlog`, using `HYPBlog` as the project root and changing the Java package namespace from `top.hyp` to `top.hyp`.

## Copy Scope

Copy project source, resources, frontend project files, scripts, images, upload examples, SQL, and documentation.

Exclude generated or local-only files:

- `.idea`
- `blog-api/target`
- `blog-api/log`
- `blog-cms/node_modules`
- `blog-view/node_modules`

## Backend Namespace Migration

Move Java source directories:

- `blog-api/src/main/java/top/naccl` to `blog-api/src/main/java/top/hyp`
- `blog-api/src/test/java/top/naccl` to `blog-api/src/test/java/top/hyp`

Update source references:

- `package top.hyp...` to `package top.hyp...`
- `import top.hyp...` to `import top.hyp...`
- `pom.xml` groupId from `top.hyp` to `top.hyp`
- Documentation references to `top.hyp` where they refer to the local Java package.

## Verification

After migration:

- Search the new project for remaining `top.hyp` references.
- Run Maven backend verification from `blog-api`.
- Keep frontend dependencies uninstalled; `package-lock.json` remains for later `npm install`.
