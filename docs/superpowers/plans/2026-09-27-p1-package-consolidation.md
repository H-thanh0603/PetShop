# P1 Package Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move 6 legacy top-level packages (101 main files) into `com.petshop.*` with zero behaviour change.

**Architecture:** Six sequential move commits, leaf-first (constant → context → util → model → dao → controller); each commit moves one package via `git mv`, rewrites its `package` decl + all consumer imports with exact `sed` commands, then proves compile + full suite green before the next commit starts.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Gradle 8.14, git mv + sed import rewrite.

**Spec:** `docs/superpowers/specs/2026-09-27-p1-package-consolidation-design.md`

## Global Constraints

- Baseline suite: TOTAL=451 test, PROPERTY=160, 0 fail/error, 1 skip (`CheckoutConcurrencyTest`, Docker off); cấm hạ số test; cấm thêm/bớt/sửa test logic (test files chỉ đổi `package`/`import`).
- Pure move: cấm sửa logic, đổi tên class/method/field, sửa annotation/string literal.
- Rule package-private gãy: nới member lên `public` tối thiểu, liệt kê trong commit message body.
- Không đụng `services`, `db`, `com.petshop.*` hiện tại.
- Không commit secret (`secrets.properties`, `.env` luôn gitignored).
- Mỗi commit phải tự compile + xanh suite; diff chỉ rename + import (+ widening đã log).
- Nhánh thực thi: `refactor/p1-packages` (tạo từ `main` tại `b0ced1d`).

## Review Focus

- Filter đăng ký tường minh bị sót import sau move → filter âm thầm rớt khỏi chain (auth bypass). Người dùng trông đợi mọi filter vẫn chạy. Test ghim: `controller/filter/*Test` xanh trong Task 6 + gate grep `WebRegistrationConfig` import package mới.
- OAuth login (`FaceBookLogin`, `GoogleLogin`) gãy wiring sau move mà không test nào cover (không có OAuth test). Người dùng trông đợi login Google/Facebook vẫn hoạt động. Test ghim: Task 6 step liệt kê mọi consumer của 2 class (grep) + compile xanh + suite xanh.
- Class mang annotation Spring lọt vào component-scan sau khi vào `com.petshop.*`. Đã xác minh 0 annotation (2026-09-27); người dùng trông đợi số bean không đổi. Test ghim: Task 7 chạy lại grep annotation = 0 hit.
- Session cũ deserialize Model sau đổi package → `InvalidClassException`. Người dùng deploy restart (bootWar, single instance) nên session ephemeral — chấp nhận; ghi chú trong Task 7 report, không test.
- Widening `public` mở rộng API surface âm thầm. Người dùng trông đợi danh sách widening đầy đủ để audit. Test ghim: mỗi task liệt kê widening trong commit body; Task 7 aggregate toàn bộ.

---

### Task 1: Move `Constant` → `com.petshop.constant`

**Files:**
- Move: `src/main/java/Constant/IConstant.java` → `src/main/java/com/petshop/constant/IConstant.java`
- Modify (import updates, files found by grep at plan time): consumers importing `Constant.` (3 files — re-run grep, exact list at execution)
- Test: no test mirror (package `Constant` has no tests); full suite is the test

**Interfaces:**
- Consumes: nothing (first task; leaf package)
- Produces: `com.petshop.constant.IConstant` (same simple name, new package) for Tasks 2-6 consumer updates

- [ ] **Step 1: Move the file**

```bash
mkdir -p src/main/java/com/petshop/constant && git mv src/main/java/Constant/IConstant.java src/main/java/com/petshop/constant/IConstant.java && ls src/main/java/Constant 2>&1 || echo "Constant dir gone"
```

Expected: `Constant dir gone` (empty dir auto-removed by git mv).

- [ ] **Step 2: Fix the package declaration**

```bash
sed -i 's/^package Constant;/package com.petshop.constant;/' src/main/java/com/petshop/constant/IConstant.java && head -1 src/main/java/com/petshop/constant/IConstant.java
```

Expected: `package com.petshop.constant;`

- [ ] **Step 3: Update all consumer imports**

```bash
grep -rln "^import Constant\." src/ | xargs sed -i 's/^import Constant\./import com.petshop.constant./' && grep -rn "^import Constant\." src/ | wc -l
```

Expected: `0`

- [ ] **Step 4: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL`. If error mentions `is not public` / package-private access across old boundary → widen that member to `public` (minimal), record it for Step 6.

- [ ] **Step 5: Full suite**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor(pkg): move Constant to com.petshop.constant"
```

If Step 4 required widening, append to message body: `Widened: <file>:<member> (package-private broke across move)`. Message subject must match exactly when no widening.

---

### Task 2: Move `Context` → `com.petshop.context`

**Files:**
- Move: `src/main/java/Context/DBContext.java`, `src/main/java/Context/LegacySchemaMigrator.java` → `src/main/java/com/petshop/context/`
- Modify: consumers importing `Context.` (43 files at plan time — re-run grep, exact list at execution)
- Test: no test mirror; every DAO test exercises `DBContext` via the suite

**Interfaces:**
- Consumes: `com.petshop.constant.*` (Task 1 — only if `Context` references it; otherwise none)
- Produces: `com.petshop.context.DBContext`, `com.petshop.context.LegacySchemaMigrator` for Tasks 3-6

- [ ] **Step 1: Move the files**

```bash
mkdir -p src/main/java/com/petshop/context && git mv src/main/java/Context/DBContext.java src/main/java/Context/LegacySchemaMigrator.java src/main/java/com/petshop/context/ && ls src/main/java/Context 2>&1 || echo "Context dir gone"
```

Expected: `Context dir gone`.

- [ ] **Step 2: Fix package declarations**

```bash
sed -i 's/^package Context;/package com.petshop.context;/' src/main/java/com/petshop/context/*.java && head -1 src/main/java/com/petshop/context/DBContext.java
```

Expected: `package com.petshop.context;`

- [ ] **Step 3: Update all consumer imports**

```bash
grep -rln "^import Context\." src/ | xargs sed -i 's/^import Context\./import com.petshop.context./' && grep -rn "^import Context\." src/ | wc -l
```

Expected: `0`

- [ ] **Step 4: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL` (widening rule as in Task 1 Step 4 if needed).

- [ ] **Step 5: Full suite**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor(pkg): move Context to com.petshop.context"
```

Append widening list to body if any; subject exact otherwise.

---

### Task 3: Move `Util` → `com.petshop.util`

**Files:**
- Move: all 21 files `src/main/java/Util/*.java` (AppConfig, AuthRedirectUtil, CertificateGenerator, DigitalSigner, EmailConfig, EmailUtil, FileUploadUtil, FileUploadValidator, FormHelper, Json, LoginLockout, OTPUtil, PasswordUtil, RSAKeyGenerator, SecretConfig, ShippingConfig, SocialAuthUtil, UploadConfig, ValidationUtil, VnpayConfig, VnpayUtil) → `src/main/java/com/petshop/util/`
- Move (test mirror): all 10 files `src/test/java/Util/*.java` (AppConfigPrecedenceTest, AppConfigTest, AuthRedirectUtilTest, CertificateGeneratorTest, DigitalSignerTest, JsonBehaviorTest, PasswordUtilTest, ValidationUtilPasswordTest, ValidationUtilRecipientTest, VnpayUtilTest) → `src/test/java/com/petshop/util/`
- Modify: consumers importing `Util.` (50 files at plan time — re-run grep)

**Interfaces:**
- Consumes: `com.petshop.constant.*`, `com.petshop.context.*` (Tasks 1-2)
- Produces: `com.petshop.util.*` (incl. `Util.Json.MAPPER`, `Util.AppConfig`) for Tasks 4-6

- [ ] **Step 1: Move main + test files**

```bash
mkdir -p src/main/java/com/petshop/util src/test/java/com/petshop/util && for f in src/main/java/Util/*.java; do git mv "$f" src/main/java/com/petshop/util/; done && for f in src/test/java/Util/*.java; do git mv "$f" src/test/java/com/petshop/util/; done && ls src/main/java/Util src/test/java/Util 2>&1 || echo "Util dirs gone"
```

Expected: `Util dirs gone`.

- [ ] **Step 2: Fix package declarations (main + test)**

```bash
sed -i 's/^package Util;/package com.petshop.util;/' src/main/java/com/petshop/util/*.java src/test/java/com/petshop/util/*.java && head -1 src/main/java/com/petshop/util/Json.java && head -1 src/test/java/com/petshop/util/JsonBehaviorTest.java
```

Expected: both print `package com.petshop.util;`

- [ ] **Step 3: Update all consumer imports**

```bash
grep -rln "^import Util\." src/ | xargs sed -i 's/^import Util\./import com.petshop.util./' && grep -rn "^import Util\." src/ | wc -l
```

Expected: `0`

- [ ] **Step 4: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL` (widening rule if needed).

- [ ] **Step 5: Full suite**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor(pkg): move Util to com.petshop.util"
```

Append widening list to body if any; subject exact otherwise.

---

### Task 4: Move `Model` → `com.petshop.model`

**Files:**
- Move: all `src/main/java/Model/*.java` (37 files) + subpackages `src/main/java/Model/FbAccount/`, `src/main/java/Model/GgAccount/` → `src/main/java/com/petshop/model/` (subpackages preserved)
- Move (test mirror): all 5 files `src/test/java/Model/*.java` (OrderLogTest, OrderPaymentExpiryPresentationTest, OrderPaymentPresentationTest, ProductAdminInventoryViewTest, ProductCommerceTest) → `src/test/java/com/petshop/model/`
- Modify: consumers importing `Model.` (99 files at plan time — re-run grep; largest consumer set)

**Interfaces:**
- Consumes: `com.petshop.util.*` (Task 3), `com.petshop.context.*` (Task 2)
- Produces: `com.petshop.model.*` for Tasks 5-6

- [ ] **Step 1: Move main + test files (subpackages preserved)**

```bash
mkdir -p src/main/java/com/petshop/model src/test/java/com/petshop/model && for f in src/main/java/Model/*.java; do git mv "$f" src/main/java/com/petshop/model/; done && git mv src/main/java/Model/FbAccount src/main/java/Model/GgAccount src/main/java/com/petshop/model/ && for f in src/test/java/Model/*.java; do git mv "$f" src/test/java/com/petshop/model/; done && ls src/main/java/Model src/test/java/Model 2>&1 || echo "Model dirs gone"
```

Expected: `Model dirs gone`.

- [ ] **Step 2: Fix package declarations (main + subpackages + test)**

```bash
sed -i 's/^package Model;/package com.petshop.model;/' src/main/java/com/petshop/model/*.java src/test/java/com/petshop/model/*.java && sed -i 's/^package Model\.FbAccount;/package com.petshop.model.FbAccount;/' src/main/java/com/petshop/model/FbAccount/*.java && sed -i 's/^package Model\.GgAccount;/package com.petshop.model.GgAccount;/' src/main/java/com/petshop/model/GgAccount/*.java && grep -rn "^package Model" src/ | wc -l
```

Expected: `0`

- [ ] **Step 3: Update all consumer imports**

```bash
grep -rln "^import Model\." src/ | xargs sed -i 's/^import Model\./import com.petshop.model./' && grep -rn "^import Model\." src/ | wc -l
```

Expected: `0`

- [ ] **Step 4: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL` (widening rule if needed).

- [ ] **Step 5: Full suite**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor(pkg): move Model to com.petshop.model"
```

Append widening list to body if any; subject exact otherwise.

---

### Task 5: Move `DAO` → `com.petshop.dao`

**Files:**
- Move: all 29 files `src/main/java/DAO/*.java` (AddressDao, AdminActionLogDAO, AiChatMessageDAO, AiChatSessionDAO, AiSupportSettingDAO, BankWebhookEventDAO, CartDAO, CertificateDAO, CouponDao, CustomerSupportKnowledgeDAO, DBProperties, InventoryBatchDAO, NotificationDAO, OrderDAO, OrderLogDAO, OrderSignDAO, OrderSignatureDAO, OrderStatusHistoryDAO, PaymentTransactionDAO, PetTypeDAO, ProductDAO, PromotionDAO, RememberTokenDAO, ReportDAO, ReviewDAO, SalesSummaryDAO, SecurityEventDAO, UserDAO, WishlistDAO) → `src/main/java/com/petshop/dao/` (class names unchanged, incl. mixed `*DAO`/`*Dao` — P2 normalizes)
- Move (test mirror): all 5 files `src/test/java/DAO/*.java` (ListProductsScratchTest, OrderDAOLegacySchemaFallbackTest, OrderDAOStatusTransitionTest, PaymentTransactionDAOTest, ProductDAOStockReservationTest) → `src/test/java/com/petshop/dao/`
- Modify: consumers importing `DAO.` (65 files at plan time — re-run grep)

**Interfaces:**
- Consumes: `com.petshop.model.*` (Task 4), `com.petshop.context.*` (Task 2, `DBContext`), `com.petshop.util.*` (Task 3)
- Produces: `com.petshop.dao.*` for Task 6

- [ ] **Step 1: Move main + test files**

```bash
mkdir -p src/main/java/com/petshop/dao src/test/java/com/petshop/dao && for f in src/main/java/DAO/*.java; do git mv "$f" src/main/java/com/petshop/dao/; done && for f in src/test/java/DAO/*.java; do git mv "$f" src/test/java/com/petshop/dao/; done && ls src/main/java/DAO src/test/java/DAO 2>&1 || echo "DAO dirs gone"
```

Expected: `DAO dirs gone`.

- [ ] **Step 2: Fix package declarations (main + test)**

```bash
sed -i 's/^package DAO;/package com.petshop.dao;/' src/main/java/com/petshop/dao/*.java src/test/java/com/petshop/dao/*.java && head -1 src/main/java/com/petshop/dao/OrderDAO.java
```

Expected: `package com.petshop.dao;`

- [ ] **Step 3: Update all consumer imports**

```bash
grep -rln "^import DAO\." src/ | xargs sed -i 's/^import DAO\./import com.petshop.dao./' && grep -rn "^import DAO\." src/ | wc -l
```

Expected: `0`

- [ ] **Step 4: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL` (widening rule if needed).

- [ ] **Step 5: Full suite**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor(pkg): move DAO to com.petshop.dao"
```

Append widening list to body if any; subject exact otherwise.

---

### Task 6: Move `controller` → `com.petshop.web.oauth` + `com.petshop.web.filter`

**Files:**
- Move: `src/main/java/controller/FaceBook/FaceBookLogin.java`, `src/main/java/controller/Google/GoogleLogin.java` → `src/main/java/com/petshop/web/oauth/`
- Move: all 7 files `src/main/java/controller/filter/*.java` (AdminAuthFilter, AuthorizationFilter, CookieAttributeFilter, CsrfFilter, PetTypeFilter, RateLimitFilter, StaticAssetCacheFilter) → `src/main/java/com/petshop/web/filter/`
- Move (test mirror): all 3 files `src/test/java/controller/filter/*.java` (CsrfFilterWebhookTest, RateLimitFilterEndpointCoverageTest, CsrfFilterAjaxTest) → `src/test/java/com/petshop/web/filter/`
- Modify: `src/main/java/com/petshop/config/WebRegistrationConfig.java` (explicit filter registration — import updates only), consumers importing `controller.` (1 file at plan time — re-run grep)

**Interfaces:**
- Consumes: `com.petshop.dao.*` (Task 5), `com.petshop.model.*` (Task 4), `com.petshop.util.*` (Task 3)
- Produces: `com.petshop.web.oauth.*`, `com.petshop.web.filter.*` (final layout; Task 7 verifies)

- [ ] **Step 1: Move main + test files**

```bash
mkdir -p src/main/java/com/petshop/web/oauth src/main/java/com/petshop/web/filter src/test/java/com/petshop/web/filter && git mv src/main/java/controller/FaceBook/FaceBookLogin.java src/main/java/controller/Google/GoogleLogin.java src/main/java/com/petshop/web/oauth/ && for f in src/main/java/controller/filter/*.java; do git mv "$f" src/main/java/com/petshop/web/filter/; done && for f in src/test/java/controller/filter/*.java; do git mv "$f" src/test/java/com/petshop/web/filter/; done && ls -R src/main/java/controller src/test/java/controller 2>&1 || echo "controller dirs gone"
```

Expected: `controller dirs gone`.

- [ ] **Step 2: Fix package declarations (three variants)**

```bash
sed -i 's/^package controller\.FaceBook;/package com.petshop.web.oauth;/' src/main/java/com/petshop/web/oauth/FaceBookLogin.java && sed -i 's/^package controller\.Google;/package com.petshop.web.oauth;/' src/main/java/com/petshop/web/oauth/GoogleLogin.java && sed -i 's/^package controller\.filter;/package com.petshop.web.filter;/' src/main/java/com/petshop/web/filter/*.java src/test/java/com/petshop/web/filter/*.java && grep -rn "^package controller" src/ | wc -l
```

Expected: `0`. If a `package` line differs from these three variants (e.g. plain `package controller;`), fix it to the semantically correct new package and record it in the report.

- [ ] **Step 3: Update all consumer imports (three mappings)**

```bash
grep -rln "^import controller\.FaceBook\.\|^import controller\.Google\." src/ | xargs sed -i -e 's/^import controller\.FaceBook\./import com.petshop.web.oauth./' -e 's/^import controller\.Google\./import com.petshop.web.oauth./' ; grep -rln "^import controller\.filter\." src/ | xargs sed -i 's/^import controller\.filter\./import com.petshop.web.filter./' ; grep -rn "^import controller\." src/ | wc -l
```

Expected: `0`. If any `import controller.` (non-subpackage) remains, it is unexpected — escalate (BLOCKED/NEEDS_CONTEXT), do not guess.

- [ ] **Step 4: Verify filter registration + OAuth consumers (Review Focus pins)**

```bash
grep -n "import com.petshop.web.filter" src/main/java/com/petshop/config/WebRegistrationConfig.java | wc -l; grep -rln "FaceBookLogin\|GoogleLogin" src/main/java src/test/java
```

Expected: first number ≥1 (filter imports present in new package); second lists every consumer file — all must compile in Step 5 (no string-based wiring exists per spec §4).

- [ ] **Step 5: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL` (widening rule if needed).

- [ ] **Step 6: Full suite (filter tests are the pin)**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}' && ls build/test-results/test/ | grep -c Filter
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`, filter test XMLs present.

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "refactor(pkg): move controller to com.petshop.web.oauth/filter"
```

Append widening list to body if any; subject exact otherwise.

---

### Task 7: P1 acceptance gates + bootWar smoke

**Files:**
- Modify: none (verification only; fix only if a gate fails — and then only the minimal change the gate requires, reported as DONE_WITH_CONCERNS)
- Test: full suite + gates below

**Interfaces:**
- Consumes: final layout from Tasks 1-6
- Produces: P1 DONE certificate (gate table in report)

- [ ] **Step 1: Directory gate**

```bash
ls src/main/java/ && ls src/test/java/
```

Expected: main shows only `com db services`; test shows only `audit com services` (moved mirrors live under `com/petshop/{dao,model,util,web}`). If stale empty legacy dirs remain, remove with `git rm -r` only if empty.

- [ ] **Step 2: Package + import grep gates**

```bash
grep -rn "^package \(DAO\|Model\|controller\|Util\|Context\|Constant\)\." src/ | wc -l; grep -rn "^import \(DAO\|Model\|controller\|Util\|Context\|Constant\)\." src/ | wc -l
```

Expected: `0` and `0`.

- [ ] **Step 3: Spring annotation scan-guard (Review Focus)**

```bash
grep -rn "@Component\|@Service\|@Repository\|@Configuration\|@Controller\|@RestController\|@Bean\|@WebFilter\|@WebServlet\|@WebListener" src/main/java/com/petshop/dao src/main/java/com/petshop/model src/main/java/com/petshop/util src/main/java/com/petshop/context src/main/java/com/petshop/constant src/main/java/com/petshop/web/oauth src/main/java/com/petshop/web/filter 2>/dev/null | wc -l
```

Expected: `0` (no class newly visible to component-scan).

- [ ] **Step 4: Full suite with counts**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}' && grep -ho 'failures="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "FAILURES="s}' && ls build/test-results/test/ | grep -c PropertyTest
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=451`, `FAILURES=0`, `18` PropertyTest files.

- [ ] **Step 5: bootWar build + smoke (class-loading sanity after package moves)**

```bash
./gradlew bootWar -x test 2>&1 | tail -2 && ls -la build/libs/petshop-boot.war
```

Expected: `BUILD SUCCESSFUL`, war exists. Then boot it on port 8096 (8080 is occupied) and check health:

```bash
(DB_USERNAME=root DB_PASSWORD="$PETSHOP_DB_PASSWORD" nohup java -jar build/libs/petshop-boot.war --server.port=8096 > /tmp/p1-smoke.log 2>&1 &) && sleep 45 && curl -s http://localhost:8096/actuator/health; pkill -f "petshop-boot.war.*8096" || true
```

Expected: `{"status":"UP"}`. Notes: `PETSHOP_DB_PASSWORD` comes from the existing local environment (same as P0 smoke); never print it. If the app needs other env (`VNPAY_*`, `AI_*` for boot — P0 smoke booted without them, so boot must succeed without them). If smoke fails, diagnose: package-move-caused (ClassNotFound) = fix in scope; environment-caused (DB down) = record and retry, do not change code for environment.

- [ ] **Step 6: Aggregate widening list + report (no commit if green)**

Collect every `Widened:` line from Task 1-6 commit bodies:

```bash
git log --format=%B refactor/p1-packages | grep -i "widened:" || echo "NO_WIDENINGS"
```

Expected: either the full audit list or `NO_WIDENINGS`. Include in the report the accepted session note (Review Focus #4): deserializing pre-P1 sessions breaks after the Model move — accepted because deploys restart via bootWar (single instance, ephemeral sessions). If all gates pass and Step 1 needed no cleanup: no commit — report "no commit, acceptance only". If Step 1 removed stale empty dirs: one commit `chore: drop empty legacy package dirs after P1 moves`.
