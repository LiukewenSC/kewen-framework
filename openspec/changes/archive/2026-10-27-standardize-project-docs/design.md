# Design

## Context

See `proposal.md` — Why for the motivation. This design fixes the target layout and migration rules; it does not change runtime behavior.

Current constraints that shape the approach:

- Multi-module Maven repo: `basic`, `basic-support`, `boot` (5 starters), `auth` (5 modules), `sample` (6 examples), `other/code-generator`. OpenSpec has 16 runtime specs, none about documentation; this change sets `skip_specs: true`.
- Root `README.md` is 31K and covers product intro, annotation reference, config guide, usage example and FAQ. Root `README_Auth.md` is 39K of older auth manual; it still links to an external sample repo, uses old config keys, and contains a real-looking RDS host/user/password.
- Login docs are split across a 36K starter README, a 24K `README-SSO.md`, `docs/说明文档/oidc接入踩坑记录.md` and `docs/说明文档/SAML2_SP_Metadata配置指南.md`. They disagree with the code on enum values, session storage, and supported modes.
- Most module READMEs are config appendices (`自动装配 / 配置项 / 前置条件 / 完整示例`) with no "what is this / how to run in 5 minutes". `basic`, `basic-support`, `auth-rbac`, `magic`, 4 samples, `code-generator` and `scripts` have no README.
- Framework DDL lives in `docs/sql/`; sample DDL lives in `sample/*/sql/`; the Spring Session script lives in `boot/login-security-spring-boot-starter/relation/sql/`; a stale `auth/relation/properties/application-sample.yml` still uses old keys.
- Tooling: `scripts/check-sql-compat.sh` scans `docs/sql/*.sql` and `sample/*/sql/*.sql` with H2 in MySQL mode; `scripts/check-config-docs.sh` validates `docs/application.yml` against every module's `spring-configuration-metadata.json`; `sample/pom.xml` mounts `docs/sql` to `classpath:sql/`, while `auth-rbac-boot-sample` and `auth-core-boot-sample` override `<resources>`.
- Open-source constraints: public Gitee repo, Chinese readers, no CI, and `.qoder/repowiki` is generated content that must not be edited by hand.

## Goals / Non-Goals

**Goals:**

- One mechanical ownership rule: each Maven module has exactly one `README.md` entry; advanced topics live in `<module>/docs/`; root `README.md` is only a landing page; root `docs/` only holds cross-module material.
- Framework DDL has exactly one home, `relation/{module}/`; sample DDL stays with its sample, and the boundary is stated in the same place.
- Login docs become one reader-friendly overview plus three protocol detail docs, rewritten against current code.
- Every module, starter and sample has a README; every starter README takes a reader from dependency to a running application.
- Style is consistent enough that review is mechanical, and all links/config/SQL paths are updated in this change.

**Non-Goals:**

- No Java code, dependency or runtime behavior changes.
- No new capability specs and no edits to the existing 16 runtime specs.
- No English translation, no docs site generator, no CI pipeline.
- No new `CONTRIBUTING.md` or `CHANGELOG.md`; the root README already has contribution steps, and git history is the changelog.
- No merge of sample DDL into `relation/`; no sample table changes.
- No hand edits to `.qoder/repowiki` or existing `openspec/` content other than this change.

## Decisions

### D1. One `README.md` per module; details go to `<module>/docs/`

- Rule:
  - `README.md` is the only entry for a module. It should fit one screen and answer: what it is, when to use it, how to run it in 5 minutes, where to read next.
  - Advanced material goes to `<module>/docs/<topic>.md`, lowercase kebab-case filenames, one topic per file. The README links to each file.
  - Root `docs/` is reserved for cross-module material only (`application.yml` config reference and `decisions/`).
- Rationale: stable links (`<module>/README.md`), a scannable module root, and a single place to start. The login module alone would otherwise need five `README-*.md` files.
- Alternative considered: flat `README-*.md` files. Rejected because it multiplies entry points and repeats the current SSO problem. Central `docs/<module>/` rejected because it moves ownership away from the module again.

### D2. Root README is a landing page; `README_Auth.md` is removed after migration

- Root README sections: positioning, feature list with honest status, module map with one-line descriptions and links, two quick starts, documentation index, contribution and license.
- Two quick starts: (a) run an H2 sample with zero external services; (b) add the framework to an existing MySQL project. The first path is the open-source first-run experience; the second is the production path.
- `README_Auth.md` content handling:
  - Annotation semantics and known limits → `auth/docs/annotations.md`.
  - RBAC model, auth-string encoding, department inheritance and extension points → `auth/docs/rbac-and-extension.md`.
  - Links to the external sample repo, old config keys and the RDS credentials are not migrated.
- Rationale: a root README that duplicates module detail becomes stale first and is the most visible page. A second auth manual at root guarantees two sources of truth.
- Alternative considered: keep `README_Auth.md` as an advanced root doc. Rejected; it preserves the duplicate manual and the stale links.

### D3. Starter README uses a fixed seven-section skeleton with a real 5-minute path

Skeleton:

1. 这是什么
2. 何时使用 / 不适用
3. 5 分钟接入：Maven 坐标 → 必需表 → 最小配置 → 启动命令 → 一条验证请求
4. 自动装配
5. 配置项
6. 前置条件与已知空转项
7. 相关文档

Per-starter specifics:

- `basic-spring-boot-starter`: DataSource is a hard prerequisite; show an H2 local path; explain p6spy/MyBatis-Plus transitive effects.
- `storage-spring-boot-starter`: list the four required keys, three tables, endpoint table, and what can be skipped (download-only vs upload).
- `tenant-spring-boot-starter`: no tables; `tenantId` header; Feign interceptor only when Feign is present.
- `magic-spring-boot-starter`: mark as demo/experimental; `SampleMagicApiApp` lives in main sources and `HelloController` returns fixed `hello`.
- `login-security-spring-boot-starter`: `SecurityUserDetailsService` is mandatory; password login is the minimal path; OAuth2/OIDC/SAML are opt-in; session requires JDBC/DataSource.
- `auth-core-spring-boot-starter`: `AuthCurrentUserContextHolder` is mandatory; seven column mappings; no user/role model.
- `auth-rbac-spring-boot-starter`: 10 auth tables, `PasswordEncoder`, `AuthCurrentUserContextHolder`; security starter supplies the last two.
- `auth-rbac-security-spring-boot-starter`: no own config; supplies the default `UserDetailsService` and current-user endpoint; warn about duplicate `AuthCurrentUserContextHolder` beans.

Rationale: a user landing on a starter README needs to run something, not read an auto-config table first. The tables remain, but after the quick start.

### D4. Framework DDL moves to root `relation/{module}/`; sample DDL stays

Target mapping:

| Current | Target |
| --- | --- |
| `docs/sql/auth-rbac.sql` | `relation/auth-rbac/auth-rbac.sql` |
| `docs/sql/basic-support.sql` | `relation/basic-support/basic-support.sql` |
| `boot/login-security-spring-boot-starter/relation/sql/spring-session-mysql.sql` | `relation/login-security/spring-session-mysql.sql` |
| `sample/auth-core-boot-sample/sql/*` | unchanged |
| `sample/auth-rbac-boot-sample/sql/*` | unchanged |
| `auth/relation/properties/application-sample.yml` | delete (stale keys) |

Rules:

- `relation/` holds product DDL only. Sample-specific schema and seed data stay in `sample/*/sql/`; `relation/README.md` states this boundary explicitly.
- `relation/auth-rbac/auth-rbac.sql` is the table set for the auth domain. `auth-core` uses `sys_auth_data` but does not own the script; the auth-core README links to this file and says so.
- `relation/login-security/spring-session-mysql.sql` is MySQL-only. H2 samples use the schema shipped inside the spring-session-jdbc jar, so this script must not be loaded by `spring.sql.init`.
- `scripts/check-sql-compat.sh` scans only H2-compatible scripts: `relation/auth-rbac/*.sql`, `relation/basic-support/*.sql`, and `sample/*/sql/*.sql`. It must exclude `relation/login-security/` and say why in its header.
- Keep framework filenames (`auth-rbac.sql`, `basic-support.sql`) so existing `classpath:sql/<file>.sql` references stay valid. Mount each relation subdirectory to `targetPath=sql`; do not mount the parent `relation/` as a tree, which would change classpath paths.
- `sample/pom.xml`: replace the single `../../docs/sql` resource with three resources pointing at `../../relation/auth-rbac`, `../../relation/basic-support`, `../../relation/login-security`; include `*.sql`; target path `sql`.
- `sample/auth-rbac-boot-sample/pom.xml`: replace `../../docs/sql` with `../../relation/auth-rbac`; keep its own `sql/` resource. Its `schema-locations` (`classpath:sql/auth-rbac.sql, classpath:sql/schema.sql`) and `data-locations` (`classpath:sql/seed-data.sql`) stay unchanged.
- `sample/auth-core-boot-sample/pom.xml`: keep mounting only its own `sql/`; update the comment that mentions `docs/sql` to say `relation/`.
- `docs/application.yml` and sample `application.yml` comments: replace `docs/sql/...` paths with `relation/...`.
- `docs/decisions/已废弃实现与决策记录.md`: append one migration entry with date, old/new paths and the reason; do not rewrite the 2026-10-15 decision entry.
- Delete `docs/sql/`, the stale `auth/relation/` directory, and the now-empty `boot/login-security-spring-boot-starter/relation/` directory.

Rationale: SQL is a deployable asset, not an explanation; `docs/` should stop mixing the two. Module-named subdirectories preserve ownership. Keeping sample DDL with the sample follows the user's decision and keeps demo fixtures self-contained.

Alternative considered: move all SQL including samples (rejected by the user). Keep `docs/sql` (rejected because it keeps deployable assets in a docs attic).

### D5. Login docs split into one overview and three protocol docs

Target files under `boot/login-security-spring-boot-starter/docs/`:

- `sso-overview.md`: what each protocol is for; identity-source vs local-account responsibilities; one ASCII sequence per protocol; endpoint table; success/failure delivery (JSON vs 302 with `loginState`); what the session stores; what is not supported.
- `oauth2.md`: authorization-code flow, tokens, scopes, `state`, framework config mapping, endpoints, failure modes, security notes.
- `oidc.md`: OIDC as an identity layer over OAuth2; `openid` scope, `id_token`, discovery, JWKS, `nonce`, claim mapping, startup network dependency, `user-name-attribute-name` and `sub` fallback, troubleshooting.
- `saml2.md`: assertions, AuthnRequest/Response, metadata, signing, Audience, SLO, SP metadata endpoint, failure modes; rewrite the old Spring Security 5.7 metadata guide as "how this framework exposes metadata", not a generic tutorial.

Accuracy rules for the rewrite (do not copy old text):

- `client-authentication-method` values are `client_secret_basic`, `client_secret_post`, `none`; not `basic`/`post`.
- Session storage is spring-session-jdbc, not in-memory. H2 uses the jar schema; MySQL uses `relation/login-security/spring-session-mysql.sql` with `initialize-schema: never`.
- SAML support is SP-initiated unless the code proves otherwise; do not document IdP-initiated SAML as a supported mode without evidence.
- `authorization-grant-type`, `user-info-authentication-method`, `cu.security.remember-me.*`, `cu.security.current-user-url` alone, OIDC RP-initiated logout and `login-page-url` keep their existing status (`[未接线]` / `[已废弃]`); do not present them as active features.
- `RELATIONDEPT__<deptId>` semantics must be verified against the implementation before being stated as strict descendants; if the code still includes the department itself, document the current behavior and the intended behavior separately rather than claiming the target is implemented.

Old files are deleted only after their unique content is merged: `README-SSO.md`, `docs/说明文档/oidc接入踩坑记录.md`, `docs/说明文档/SAML2_SP_Metadata配置指南.md`.

Rationale: the current four documents mix protocol primer, framework configuration and troubleshooting. Readers come for different reasons, and the mixing caused the drift.

Alternative considered: one large SSO document. Rejected because OAuth2, OIDC and SAML2 have distinct config keys and failure modes, and a single document is what produced the current contradictions.

### D6. Style contract

- Chinese prose; English identifiers and config keys. Use "你" for the reader.
- One `#` per document; `##` sections; no numbered headings; no emoji in headings. The root README may keep badges.
- Diagrams are ASCII only (`+`, `-`, `|`, `-->`); no Unicode box drawing.
- Config tables use four columns: `key | 默认值 | 必填 | 说明`. Status is written as `[未接线]`, `[已废弃]`, `[实验性]` inside the description.
- Every document opens with a one-paragraph "这是什么 / 给谁看" and closes with "相关文档". Lead with the conclusion, then details.
- No "主要变化点" or "更新日志" sections; git history is the changelog.
- Example secrets are placeholders. Fixed local-demo values are allowed only when explicitly labeled.
- Links are relative; no absolute local paths. All links are checked after the rewrite.

Rationale: review becomes mechanical and readers stop guessing which style a given file uses. Alternative: per-module style, rejected because it recreates the current inconsistency.

### D7. Full-repo coverage, with honest status labels

New documents: `basic/README.md`, `basic-support/README.md`, `auth/README.md`, `auth/docs/annotations.md`, `auth/docs/rbac-and-extension.md`, `auth/auth-rbac/README.md`, `boot/magic-spring-boot-starter/README.md`, four sample READMEs (`basic`, `login`, `storage`, `tenant`), `other/code-generator/README.md`, `scripts/README.md`, `relation/README.md`.

Rewrites: root README, `auth/auth-core/README.md`, three auth starter READMEs, `boot/basic-spring-boot-starter/README.md`, `boot/storage-spring-boot-starter/README.md`, `boot/tenant-spring-boot-starter/README.md`, login-security starter README, `sample/auth-core-boot-sample/README.md`, `sample/auth-rbac-boot-sample/README.md`.

Status labels:

- `magic-spring-boot-starter`: "示例/实验性"; document that `HelloController` returns a fixed `hello` and the starter is not a production-ready module.
- `storage`: current implementation is Qiniu-only; `cu.storage.type` is `[未接线]`.
- `remember-me`: `[未接线]`.
- `@Deprecated`/unimplemented SAML fields: `[已废弃]`.

Rationale: an open-source reader expects an entry for every module; omitting a module is worse than labeling it experimental. The existing `auth-core` and `auth-rbac` sample scenario matrices are unique verification material and stay, restyled to the common skeleton.

### D8. Auth API inventory corrected during apply

实施 3.2 前对照源码发现规划沿用了旧文档的 API 名称，当前代码的事实是：

- 当前只有 4 个注解：`@AuthMenu`、`@AuthMenuDoc`（菜单）、`@AuthDataRange`、`@AuthDataOperation`（数据）。`@AuthDataAuthEdit` 已不存在，数据权限编辑改由 `AuthDataAdaptor.editDataAuths(...)` 完成。
- `AnnotationAuthHandler` 与 `AbstractAuthDatraAnnotationAuthHandler` 已不存在。当前扩展点是 `AuthMenuHandler`（菜单）、`AuthDataHandler`（数据）、`AuthCurrentUserContextHolder`（当前用户）、`MenuApiService`（菜单持久化）以及 `SysUserComposite` / `SysAuthMenuComposite`（RBAC 模型）。
- `RbacAnnotationAuthHandler` 只实现 `AuthMenuHandler`，不再承担数据权限处理。
- `RbacSecurityConfig` 的三个默认 Bean（`SecurityUserDetailsService`、`AuthCurrentUserContextHolder`、`RbacCurrentUserEndpoint`）都带 `@ConditionalOnMissingBean`；旧 README 说的“无条件注册、会冲突”已不成立。
- `RELATIONDEPT__<deptId>` 当前实现按严格上级链（不含自身）展开，与 `auth/dept-inherited-access` 规格一致。

因此 `auth/docs/annotations.md` 与 `auth/docs/rbac-and-extension.md` 只按上述现状编写，`README_Auth.md` 中已删除 API 的章节不迁移。

## Risks / Trade-offs

- [Two SQL rules remain] → `relation/README.md` and each sample README state the boundary; the root quick start names both locations. This is the user's explicit choice, not an accident.
- [Link rot during the move] → do the SQL move and every README link update in the same change; run a link sweep before finishing. A `scripts/check-doc-links.sh` is optional and can be added later.
- [Deleting docs loses unique content] → migrate first, delete after. `README_Auth` only contributes content that still matches current code; the decision log is append-only.
- [Docs drift from code again] → rewrite against code, keep status tags, rely on `docs/application.yml` + `check-config-docs.sh`. Extending that script to compare README config keys is optional and should not block this change.
- [The RDS credentials are already public] → rotate before deletion. Removing them from docs is not remediation.
- [Sample packaging breaks after remounting resources] → update both sample poms, verify `target/classes/sql/` contents, and start the affected samples. Remember that child `<resources>` declarations override the parent rather than append.
- [Checking the MySQL-only Spring Session script with H2 fails] → exclude `relation/login-security/` from `check-sql-compat.sh` and document that it is MySQL-only.
- [Large review surface] → phase tasks P0–P4, no code changes, and verify each phase before moving on.
- [Magic starter is overpromised] → label it experimental/demo and describe actual behavior.
- [Chinese filenames in links] → keep the existing decision-log filename, use English kebab-case for new docs, and verify relative links after moves.
- [Permission semantics are nuanced] → verify `RELATIONDEPT` and null/empty permission behavior against code before writing; do not present a decided target as implemented.

## Migration Plan

### Document migration map

| Current | Target | Action |
| --- | --- | --- |
| `README.md` | `README.md` | rewrite as landing page; move annotation detail to `auth/docs/`, usage example to `sample/auth-rbac-boot-sample/README.md`, config tables to starter READMEs |
| `README_Auth.md` | `auth/README.md`, `auth/docs/annotations.md`, `auth/docs/rbac-and-extension.md` | migrate current-valid content, then delete |
| `auth/auth-core/README.md` | same path | keep model notes, align style, link to auth docs |
| `auth/auth-core-spring-boot-starter/README.md` | same path | add 5-minute path, keep config table, update SQL link |
| `auth/auth-rbac-spring-boot-starter/README.md` | same path | add 5-minute path, keep config table, update SQL link |
| `auth/auth-rbac-security-spring-boot-starter/README.md` | same path | add prerequisites and quick start |
| (none) | `auth/auth-rbac/README.md` | new: RBAC implementation, composites, extension seams |
| (none) | `auth/README.md`, `auth/docs/*` | new: auth overview, annotations, RBAC/extension |
| `boot/basic-spring-boot-starter/README.md` | same path | add 5-minute path and H2 prerequisite |
| `boot/storage-spring-boot-starter/README.md` | same path | add 5-minute path, update SQL link |
| `boot/tenant-spring-boot-starter/README.md` | same path | add 5-minute path |
| (none) | `boot/magic-spring-boot-starter/README.md` | new: experimental demo |
| `boot/login-security-spring-boot-starter/README.md` | same path | slim to overview + protocol map + config index |
| `boot/login-security-spring-boot-starter/README-SSO.md` | `docs/sso-overview.md` | merge, delete old |
| `docs/说明文档/oidc接入踩坑记录.md` | `docs/oidc.md` | merge pitfalls, delete old |
| `docs/说明文档/SAML2_SP_Metadata配置指南.md` | `docs/saml2.md` | rewrite as framework metadata section, delete old |
| (none) | `docs/oauth2.md` | new |
| `docs/说明文档/已废弃实现与决策记录.md` | `docs/decisions/已废弃实现与决策记录.md` | move, append migration entry |
| (none) | `basic/README.md`, `basic-support/README.md` | new |
| `sample/auth-core-boot-sample/README.md` | same path | keep scenario matrix, align style, update SQL links |
| `sample/auth-rbac-boot-sample/README.md` | same path | keep usage cases, align style, update SQL links |
| (none) | `sample/basic-boot-sample/README.md`, `sample/login-boot-sample/README.md`, `sample/storage-boot-sample/README.md`, `sample/tenant-boot-sample/README.md` | new |
| (none) | `other/code-generator/README.md`, `scripts/README.md`, `relation/README.md` | new |
| `docs/sql/*` | `relation/auth-rbac/auth-rbac.sql`, `relation/basic-support/basic-support.sql` | move, delete old |
| `boot/login-security-spring-boot-starter/relation/sql/spring-session-mysql.sql` | `relation/login-security/spring-session-mysql.sql` | move |
| `auth/relation/properties/application-sample.yml` | — | delete (stale keys) |

### Phases

1. **Phase 0 — precondition**: the repository owner rotates the RDS credentials referenced by `README_Auth.md`; confirm before deleting the file.
2. **Phase 1 — layout and tooling**: create `relation/` directories, move framework DDL, delete `docs/sql/`, update `scripts/check-sql-compat.sh`, update both sample poms and SQL path comments, add `relation/README.md`, append the decision-log migration entry, delete the stale auth properties file.
3. **Phase 2 — auth docs**: create `auth/README.md` and `auth/docs/*`, rewrite the three auth starter READMEs and `auth-core/README.md`, add `auth-rbac/README.md`, then delete `README_Auth.md` after content migration.
4. **Phase 3 — remaining modules and samples**: add `basic`, `basic-support`, `magic`, four sample READMEs, `code-generator` and `scripts`; rewrite root README with module map, two quick starts and doc navigation; align the two existing sample READMEs.
5. **Phase 4 — login protocol docs**: rewrite the login starter README, create the four docs, delete the three old protocol files, move the decision log.
6. **Phase 5 — verification**: link sweep; `bash scripts/check-sql-compat.sh`; `bash scripts/check-config-docs.sh` after `mvn -q -DskipTests compile`; start the H2 samples (`auth-core`, `auth-rbac`, `basic`, `login`, `storage`, `tenant` as applicable); `mvn -q -DskipTests package`; review style and status tags.

Rollback: revert the change's commits. There is no runtime state to clean up. If the SQL remount breaks a sample, restore `docs/sql` and the pom resource entries from git.

## Open Questions

- Whether to add `scripts/check-doc-links.sh` in this change or later. It doesn't change the layout or acceptance, so it can be deferred.
- Whether to add a `CONTRIBUTING.md`. The root README already has contribution steps; adding it later won't change this design.
- Exact wording for the magic module label ("示例/实验性" vs "仅本地演示"). This can be adjusted without changing any path or task.
