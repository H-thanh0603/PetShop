# P0 — Hạ tầng & Phiên bản (Baseline Upgrade) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Nâng PetShop lên Spring Boot 4.1.1 + MySQL 8.4 LTS, gộp cấu hình vào `application.yml`, thay Gson bằng Jackson 3, bỏ đóng gói WAR legacy — không đổi hành vi người dùng, không hạ số test.

**Architecture:** Mỗi task là một commit riêng trên nhánh `refactor/p0-baseline`; cổng chuyển task là `./gradlew clean test` xanh với **số test không giảm so với baseline 437**. Tầng facade tĩnh (`AppConfig`, `SecretConfig`, `DBProperties`) được giữ nguyên chữ ký — chỉ đổi backing store, để ~100+ chỗ gọi không phải sửa. JSON đi qua một helper `Util.Json.MAPPER` duy nhất để cấu hình lenient nhất quán.

**Tech Stack:** Java 21 · Gradle 8.14 · Spring Boot 4.1.1 (Spring 7, Jakarta EE 11, Jackson 3.1.5, JUnit Jupiter 6.0.3) · MySQL 8.4 LTS · JSP/JSTL (giữ tới phase sau) · Flyway 12.4.0 · HikariCP

**Spec:** `docs/superpowers/specs/2026-09-25-p0-baseline-upgrade-design.md`

## Global Constraints

- **Demo luôn chạy được**: không đổi hành vi người dùng (giao diện, luồng mua, API, schema DB).
- `./gradlew clean test` phải xanh với **≥437 test case** — cấm hạ số test; đếm bằng `build/test-results/test/*.xml`.
- Spring Boot **4.1.1** (không phải 4.0.x — 4.0 hết OSS 31/12/2026), MySQL image **`mysql:8.4`**, Java **21**, Gradle **8.14** (đã được Boot 4 hỗ trợ).
- **Giữ nguyên tên biến env var** mà docker-compose đang set: `DB_PASSWORD`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_PASSWORD`, `PETSHOP_DB_PASSWORD`, `DEEPSEEK_API_KEY`, `AI_*`, `VNPAY_*`.
- **Giữ facade tĩnh** (`Util.AppConfig`, `Util.SecretConfig`, `DAO.DBProperties`, `Util.VnpayConfig`, `Util.ShippingConfig`, `services.ai.AiConfig`) và thứ tự ưu tiên đọc: **Spring Environment (system property > env var > application.yml) → file legacy `secrets.properties`**.
- Không còn import `com.google.gson` ở cuối P0; không còn file `.properties` cấu hình nào ngoài `*.example`.
- Giữ task `bootWar` (sinh `petshop-boot.war` — JSP cần WAR); bỏ task `war`/`ROOT.war` ở cuối.
- **Không bao giờ commit secret**: `secrets.properties` và `.env` luôn gitignored; không ghi giá trị secret vào plan, code hay commit message.
- Giữ nguyên `src/main/webapp/**/*.jsp` (P3 mới chuyển Thymeleaf) và toàn bộ schema DB.

## Review Focus

Năm điểm spec ngầm định nhưng test của task khác không chạm tới, dễ làm hỏng người dùng thật:

1. **160 jqwik property test có thể "im lặng" không chạy** trên JUnit Platform 6 (Gradle vẫn báo build xanh nếu engine không discover được) → **Task 3, bước đếm test bằng XML**.
2. **Thứ tự ưu tiên đọc config bị đảo** sau khi gộp → app đọc nhầm secret, start fail hoặc kết nối DB sai → **Task 5, `AppConfigPrecedenceTest`**.
3. **Gson escape HTML (`<` → `\u003c`), Jackson không** → JSON response đổi chuỗi, client có thể vỡ → **Task 6, `JsonBehaviorTest` pins hành vi sau khi migrate**.
4. **Gson parse lenient, Jackson strict mặc định** → webhook/`ShippingService` nhận JSON hỏng từ GHN là throw khi trước giờ không → **Task 6, `ShippingServiceJsonLenientTest`** (chạy xanh cả trước lẫn sau khi đổi thư viện).
5. **Định dạng số/tiền trong JSON đổi** (BigDecimal ghi dạng khoa học) → VNPAY/Sepay amount sai → **Task 6, `JsonBehaviorTest` money pin**.

---

### Task 1: Dọn cảnh báo deprecated trên Boot 3.5.16 (Bước 0)

Boot 4 xoá toàn bộ API deprecated trong 3.x, nên phải dọn trước khi nâng.

**Files:**
- Modify: `build.gradle:80-82` (thêm `-Xlint:deprecation`)
- Modify: `src/main/java/services/OrderEmailService.java:16`
- Modify: `src/main/java/services/ShippingService.java:357-371`
- Modify: `src/main/java/com/petshop/web/BankWebhookController.java:111`
- Modify: `src/main/java/com/petshop/web/UserAiSupportController.java:177,328,367`

**Interfaces:**
- Consumes: không có.
- Produces: `./gradlew compileJava` không còn warning `[deprecation]`; các file trên giữ nguyên chữ ký method (chỉ đổi cách gọi API nội bộ).

- [ ] **Step 1: Bật lint và đo baseline cảnh báo**

`build.gradle`, trong `tasks.withType(JavaCompile).configureEach`:

```gradle
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.compilerArgs += ['-Xlint:deprecation']
}
```

Run: `./gradlew clean compileJava --console=plain 2>&1 | grep -c "warning: \[deprecation\]"`
Expected: **14** (4 chỗ `new JsonParser().parse` ở `ShippingService:362,367`, `BankWebhookController:111`, `UserAiSupportController:177,328,367`; 1 chỗ `JsonReader.setLenient` ở `ShippingService:366`; 1 chỗ `new Locale("vi","VN")` ở `OrderEmailService:16` — mỗi chỗ đếm 2 warning cho constructor + method).

- [ ] **Step 2: Sửa `Locale` deprecated**

`src/main/java/services/OrderEmailService.java:16`:

```java
private static final NumberFormat VND = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));
```

(thêm `import java.util.Locale;` nếu chưa có). `Locale.of` có từ Java 19, project đang ở Java 21.

- [ ] **Step 3: Sửa `JsonParser` deprecated (static API, cùng hành vi)**

`src/main/java/com/petshop/web/BankWebhookController.java:111`:

```java
JsonObject json = JsonParser.parseString(rawPayload).getAsJsonObject();
```

`src/main/java/com/petshop/web/UserAiSupportController.java:177`:

```java
JsonObject reqJson = rawBody == null || rawBody.isBlank()
        ? new JsonObject() : JsonParser.parseString(rawBody).getAsJsonObject();
```

`src/main/java/com/petshop/web/UserAiSupportController.java:328`:

```java
responseJson.add("cards", JsonParser.parseString(aiRes.getCardsJson()));
```

`src/main/java/com/petshop/web/UserAiSupportController.java:367`:

```java
JsonObject reqJson = JsonParser.parseString(rawBody).getAsJsonObject();
```

- [ ] **Step 4: Sửa nhánh lenient trong `ShippingService`**

`src/main/java/services/ShippingService.java:357-371` — thay toàn bộ method:

```java
private static JsonObject parseJsonLenient(String json) {
    try {
        return JsonParser.parseString(json).getAsJsonObject();
    } catch (JsonSyntaxException | IllegalStateException e) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse JSON response: " + json, ex);
        }
    }
}
```

`JsonParser.parseReader(JsonReader)` tự bật lenient trước khi parse, nên hành vi giữ nguyên; xoá dòng `reader.setLenient(true);`.

- [ ] **Step 5: Verify 0 warning**

Run: `./gradlew clean compileJava --console=plain 2>&1 | grep -c "warning: \[deprecation\]"`
Expected: `0`

- [ ] **Step 6: Chạy toàn bộ test**

Run: `./gradlew clean test --console=plain`
Expected: `BUILD SUCCESSFUL`, tổng test trong `build/test-results/test/*.xml` = **437** (chạy lệnh đếm ở Task 12 Step 2).

- [ ] **Step 7: Commit**

```bash
git add build.gradle src/main/java/services/OrderEmailService.java src/main/java/services/ShippingService.java src/main/java/com/petshop/web/BankWebhookController.java src/main/java/com/petshop/web/UserAiSupportController.java
git commit -m "chore: clear deprecation warnings ahead of Spring Boot 4 (lint on)"
```

---

### Task 2: Nâng Spring Boot 4.1.1 — compile xanh

**Files:**
- Modify: `build.gradle` (toàn bộ phần `plugins`, `dependencies`)

**Interfaces:**
- Consumes: Task 1 (không còn deprecated API).
- Produces: classpath Boot 4.1.1 với starter tên mới — Task 3 chạy test trên đó, Task 6+ viết code dùng Jackson 3 (`tools.jackson.*`) có sẵn trên classpath.

- [ ] **Step 1: Đổi version và starter trong `build.gradle`**

```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'war'
    id 'org.springframework.boot' version '4.1.1'
}

dependencies {
    implementation platform('org.springframework.boot:spring-boot-dependencies:4.1.1')
    providedRuntime platform('org.springframework.boot:spring-boot-dependencies:4.1.1')

    providedCompile 'jakarta.servlet:jakarta.servlet-api:6.1.0'

    // Spring Boot 4: web -> webmvc
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
    providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'

    // Spring Boot 4: flyway-core + flyway-mysql -> spring-boot-starter-flyway
    implementation 'org.springframework.boot:spring-boot-starter-flyway'

    implementation 'jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api'
    implementation 'org.glassfish.web:jakarta.servlet.jsp.jstl'
    implementation 'com.mysql:mysql-connector-j'
    implementation 'jakarta.mail:jakarta.mail-api'
    implementation 'org.eclipse.angus:jakarta.mail'
    implementation 'com.google.code.gson:gson'          // gỡ ở Task 10
    implementation 'org.springframework.security:spring-security-crypto'
    implementation 'com.zaxxer:HikariCP'
    implementation 'org.slf4j:slf4j-api'
    implementation 'ch.qos.logback:logback-classic'
    implementation 'org.bouncycastle:bcpkix-jdk18on:1.86'

    // Tạm thời: diagnostic property đổi tên, GỠ ở Task 11
    runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'

    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.mockito:mockito-core'
    testImplementation 'org.mockito:mockito-junit-jupiter'
    testImplementation 'net.jqwik:jqwik:1.10.1'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:mysql'
}
```

Giữ nguyên các block `java {}`, `repositories`, `test {}`, `tasks.withType(JavaCompile)`, `springBoot {}`, `war`/`bootWar` (Task 11 mới sửa).

- [ ] **Step 2: Compile main — đọc từng lỗi, sửa import/package**

Run: `./gradlew compileJava --console=plain`
Expected: có thể lỗi ở các class `org.springframework.boot.web.*` (ErrorPage, FilterRegistrationBean, ServletContextInitializer) nếu Boot 4 chuyển package. Quy tắc sửa: mở link lỗi, tìm package mới theo thông báo compiler, đổi `import`. **Cấm đoán** — luôn đọc message lỗi.

Nếu số lỗi import quá lớn (>30 file), thêm cầu tạm rồi mới sửa dần:

```gradle
    implementation 'org.springframework.boot:spring-boot-starter-classic'
```

xong chuyển sang starter chuẩn ở Step 4 rồi gỡ `starter-classic`.

- [ ] **Step 3: Compile test**

Run: `./gradlew compileTestJava --console=plain`
Expected: xanh. Nếu lỗi `@MockBean`/`@SpyBean` → đổi sang `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`); nếu lỗi `@WebMvcTest`/`@AutoConfigureMockMvc` package → thêm `spring-boot-starter-webmvc-test` (testImplementation) và import từ `org.springframework.boot.webmvc.test.autoconfigure`. **Lưu ý: khảo sát cho thấy project không dùng các annotation này**, nên bước này dự kiến không có lỗi.

- [ ] **Step 4: Xác minh dependency tree đúng**

Run: `./gradlew dependencies --configuration runtimeClasspath | grep -E "spring-boot-starter-webmvc|spring-boot-starter-flyway|gson|jackson-databind" | head`
Expected: có `spring-boot-starter-webmvc`, `spring-boot-starter-flyway`, `tools.jackson.core:jackson-databind:3.1.5`, còn `com.google.code.gson:gson`.

- [ ] **Step 5: Commit**

```bash
git add build.gradle
git commit -m "build: upgrade Spring Boot 3.5.16 -> 4.1.1 with modular starters"
```

---

### Task 3: Test suite xanh trên Boot 4 + đảm bảo jqwik chạy (Review Focus #1)

Boot 4 dùng JUnit Jupiter **6.0.3** (JUnit Platform 6.x) trong khi jqwik **1.10.1** (bản mới nhất, chưa có bản JUnit 6) vẫn dựng trên platform **1.14.4**. Nguy cơ: Gradle báo `BUILD SUCCESSFUL` nhưng **160 property test không được discover**.

**Files:**
- Modify: `build.gradle` (chỉ khi cần contingency)
- Test: `build/test-results/test/*.xml` (đếm), `build/reports/tests/test/index.html`

**Interfaces:**
- Consumes: Task 2 (classpath Boot 4.1.1).
- Produces: một trong hai cấu hình — (a) `tasks.test` chạy đủ Jupiter + jqwik, hoặc (b) `tasks.test` chạy Jupiter + task `propertyTest` riêng cho jqwik. Task 12 dùng đúng cấu hình này khi nghiệm thu.

- [ ] **Step 1: Chạy test và đo số lượng**

Run:

```bash
./gradlew clean test --console=plain
grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `TOTAL=437`. **Đây là gate** — nếu `< 437`, engine jqwik không chạy, đi Step 2. Nếu `= 437` → nhảy Step 4.

- [ ] **Step 2 (chỉ khi TOTAL < 437): Ép JUnit về dòng 1.x cho toàn bộ test task**

Cách đơn giản nhất giữ mọi thứ trong một task — ép cả Jupiter và platform về dòng tương thích jqwik:

```gradle
configurations.all {
    resolutionStrategy {
        force 'org.junit.jupiter:junit-jupiter:5.14.4'
        force 'org.junit.jupiter:junit-jupiter-api:5.14.4'
        force 'org.junit.jupiter:junit-jupiter-engine:5.14.4'
        force 'org.junit.jupiter:junit-jupiter-params:5.14.4'
        force 'org.junit.platform:junit-platform-launcher:1.14.4'
        force 'org.junit.platform:junit-platform-engine:1.14.4'
        force 'org.junit.platform:junit-platform-commons:1.14.4'
    }
}
```

Run lại Step 1.

- [ ] **Step 3 (chỉ khi Step 2 làm Jupiter hỏng): tách property test sang task riêng**

```gradle
configurations {
    propertyTestRuntime {
        canBeConsumed = false
        canBeResolved = true
    }
}
dependencies {
    propertyTestRuntime sourceSets.main.output
    propertyTestRuntime sourceSets.test.output
    propertyTestRuntime configurations.testRuntimeClasspath
    propertyTestRuntime 'net.jqwik:jqwik:1.10.1'
}

tasks.register('propertyTest', Test) {
    description = 'jqwik property tests on JUnit Platform 1.x'
    group = 'verification'
    testClassesDirs = sourceSets.test.output.classesDirs
    classpath = configurations.propertyTestRuntime
    useJUnitPlatform { includeEngines 'jqwik' }
    include '**/*PropertyTest.*'
    testLogging { events 'passed', 'failed', 'skipped' }
}

tasks.named('test') {
    exclude '**/*PropertyTest.*'
}
```

Sau đó gate riêng từng task:

```bash
./gradlew clean test propertyTest --console=plain
grep -ho 'tests="[0-9]*"' build/test-results/{test,propertyTest}/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `TOTAL=437`. Nếu `configurations.testRuntimeClasspath` kéo theo Jupiter 6 gây xung đột platform, đổi dòng đó thành `configurations.testRuntimeClasspath.withDependencies { it.removeAll { dep -> dep.group == 'org.junit.jupiter' || dep.group == 'org.junit.platform' } }`.

- [ ] **Step 4: Verify jqwik thực sự chạy (không chỉ tổng đủ)**

Run:

```bash
grep -l "jqwik" build/test-results/test/*.xml 2>/dev/null | head -3
grep -ho 'tests="[0-9]*"' build/test-results/test/*PropertyTest*.xml 2>/dev/null | awk -F'"' '{s+=$2} END {print "PROPERTY="s}'
```

Expected: có file XML của `*PropertyTest`, `PROPERTY` ≥ 160. Nếu `TOTAL=437` nhưng `PROPERTY=0` → engine jqwik vẫn chưa chạy, quay lại Step 2/3.

- [ ] **Step 5: Chạy hai lần liên tiếp để bắt flaky**

Run: `./gradlew test --rerun-tasks --console=plain` (hai lần)
Expected: cả hai `BUILD SUCCESSFUL`, `TOTAL=437`.

- [ ] **Step 6: Commit**

```bash
git add build.gradle
git commit -m "test: keep all 437 tests (incl. 160 jqwik properties) green on Boot 4"
```

---

### Task 4: MySQL image 8.4 LTS

**Files:**
- Modify: `docker-compose.dev.yml:3`
- Modify: `docker-compose.prod.yml:3` và `docker-compose.prod.yml:120` (service backup)
- Modify: `README.md:16` (yêu cầu môi trường)

**Interfaces:**
- Consumes: không có.
- Produces: image name `mysql:8.4` — Task 12 verify lại.

- [ ] **Step 1: Đổi image ở cả 3 chỗ**

```bash
sed -i 's|image: mysql:8.0|image: mysql:8.4|' docker-compose.dev.yml docker-compose.prod.yml
grep -n "image: mysql" docker-compose.dev.yml docker-compose.prod.yml
```

Expected: 3 dòng, đều `mysql:8.4`. Giữ nguyên healthcheck, `--character-set-server=utf8mb4`, memory limits.

- [ ] **Step 2: Validate compose file**

Run: `docker compose -f docker-compose.dev.yml config -q && docker compose -f docker-compose.prod.yml config -q`
Expected: không lỗi (lệnh `config` không cần Docker daemon).

- [ ] **Step 3: Cập nhật README**

`README.md:16`, đổi `- MySQL 8+` thành `- MySQL 8.4 LTS` (giữ nguyên Tomcat dòng này tới Task 11).

- [ ] **Step 4: Ghi chú backup volume (dòng mới trong README, sau mục yêu cầu môi trường)**

```markdown
- Hạ dev từ `mysql:8.0` sang `mysql:8.4` (2026-09): backup dữ liệu trước khi `docker compose up` lần đầu:
  `docker exec petshop-mysql-dev mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" petvaccine > backup-8.4.sql`
```

- [ ] **Step 5: Chạy test để chắc không có gì vỡ**

Run: `./gradlew clean test --console=plain`
Expected: `TOTAL=437`

- [ ] **Step 6: Commit**

```bash
git add docker-compose.dev.yml docker-compose.prod.yml README.md
git commit -m "build: move MySQL images to 8.4 LTS (8.0 EOL since 04/2026)"
```

---

### Task 5: Gộp cấu hình vào application.yml (Review Focus #2)

**Files:**
- Create: `src/test/java/Util/AppConfigPrecedenceTest.java`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/Util/AppConfig.java:12-21` (mảng `PROPERTY_FILES`)
- Delete: `src/main/resources/app.properties`, `db.properties`, `vnpay.properties`, `ship.properties`
- Keep: `src/main/resources/secrets.properties` (gitignored, local secret), mọi `*.example`
- Modify: `README.md` (mục cấu hình), `docs/commerce-deployment.md` nếu nhắc file đã xoá

**Interfaces:**
- Consumes: `Util.AppConfig.get/getOrDefault/getInt/getBoolean/hasValue` (chữ ký giữ nguyên), `Util.AppConfig.setSpringEnvironment(Environment)` — đã được `DataSourceConfig:71` gọi lúc startup.
- Produces: mọi key cũ vẫn tra được qua `AppConfig.get("<key cũ>")` với cùng thứ tự ưu tiên; Task 7-10 không phải đụng tới config.

- [ ] **Step 1: Viết test thứ tự ưu tiên (chưa implement → phải fail/hoặc xác nhận hành vi hiện tại)**

`src/test/java/Util/AppConfigPrecedenceTest.java`:

```java
package Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class AppConfigPrecedenceTest {

    @AfterEach
    void clearBridge() {
        AppConfig.setSpringEnvironment(null);
        System.clearProperty("test.p0.key");
    }

    @Test
    void springEnvironmentWinsOverLegacyFiles() {
        MockEnvironment env = new MockEnvironment().withProperty("db.host", "from-yml");
        AppConfig.setSpringEnvironment(env);
        assertEquals("from-yml", AppConfig.get("db.host"));
    }

    @Test
    void systemPropertyWinsOverSpringEnvironment() {
        System.setProperty("test.p0.key", "from-system");
        MockEnvironment env = new MockEnvironment().withProperty("test.p0.key", "from-yml");
        AppConfig.setSpringEnvironment(env);
        assertEquals("from-system", AppConfig.get("test.p0.key"));
    }

    @Test
    void legacySecretsFileStillReadWhenNoOtherSource() {
        AppConfig.setSpringEnvironment(new MockEnvironment());
        // secrets.properties (gitignored, local) vẫn là nguồn cuối
        assertNull(AppConfig.get("khong.ton.tai.key.p0"));
    }

    @Test
    void envVarFallbackKeyWorks() {
        MockEnvironment env = new MockEnvironment().withProperty("DB_NAME", "petvaccine");
        AppConfig.setSpringEnvironment(env);
        assertEquals("petvaccine", AppConfig.getOrDefault("db.dbname", "petshop", "DB_NAME"));
    }
}
```

Lưu ý: `lookup()` trong `AppConfig:94-100` kiểm tra Spring `Environment` **trước** system property, nhưng `Environment` của Spring tự resolve system property trước env var, nên thứ tự tổng thể vẫn đúng — Step 2 chính là bước chứng minh điều này.

- [ ] **Step 2: Chạy test — 2/4 có thể fail trước khi gộp**

Run: `./gradlew test --tests 'Util.AppConfigPrecedenceTest' --console=plain`
Expected: 4/4 pass (hành vi bridge đã có sẵn). Nếu fail → dừng, sửa `AppConfig.lookup` cho đúng thứ tự trước khi tiếp tục.

- [ ] **Step 3: Chuyển khoá vào `application.yml`**

Trích danh sách khoá trước khi viết yml:

```bash
cd src/main/resources
LC_ALL=C grep -hoE '^[A-Za-z0-9_.-]+=' app.properties db.properties vnpay.properties ship.properties \
  | sed 's/=$//' | LC_ALL=C sort -u > /tmp/opencode/fk.txt
cd -
LC_ALL=C grep -rhoE '(AppConfig|SecretConfig|VnpayConfig|ShippingConfig|AiConfig)\.(get|getOrDefault|getInt|getBoolean)\("[^"]+"' src/main/java \
  | sed 's/.*("//;s/"//' | LC_ALL=C sort -u > /tmp/opencode/ck.txt
wc -l /tmp/opencode/fk.txt /tmp/opencode/ck.txt    # kỳ vọng: 62 và ~86
```

Thêm vào `src/main/resources/application.yml` (giữ **nguyên tên khoá cũ**, kể cả khoá in hoa — vì `AppConfig.get("AI_PROVIDER")` tra đúng khoá đó):

```yaml
# ---- PetShop application config (hợp nhất từ app.properties/db.properties/
# vnpay.properties/ship.properties; secret để env var hoặc secrets.properties) ----
app:
  base-url: http://localhost:8080/PetShop
  context-path: /PetShop
  static:
    cache:
      max-age-seconds: 86400

api:
  provinces:
    base-url: https://provinces.open-api.vn/api/v1

payment:
  bank:
    id: VPB
    display-name: VP Bank
    transfer-prefix: PETSH
    currency: VND
    pending-minutes: 10
    account-number: ""      # PAYMENT_BANK_ACCOUNT_NUMBER
    account-name: ""        # PAYMENT_BANK_ACCOUNT_NAME
    webhook-secret: ""      # PAYMENT_BANK_WEBHOOK_SECRET (fail closed khi trống)
  ghn:
    webhook-secret: ""      # PAYMENT_GHN_WEBHOOK_SECRET (fail closed khi trống)

db:
  host: localhost
  port: 3306
  username: petshop
  password: ""              # PETSHOP_DB_PASSWORD / DB_PASSWORD / MYSQL_PASSWORD
  dbname: petvaccine
  option: useUnicode=true&characterEncoding=utf-8

vnpay:
  tmn-code: ""              # VNPAY_TMN_CODE
  hash-secret: ""           # VNPAY_HASH_SECRET
  pay-url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  return-url: http://localhost:8080/vnpay-return

ratelimit:
  login: 10
  register: 6
  forgot-password: 6
  verify-otp: 6
  reset-password: 6
  checkout: 8
  bank-webhook: 60
  vnpay-ipn: 60
  add-review: 5
  search-autocomplete: 8

# GHN (ship.properties)
TOKEN: ""                   # GHN token — env var TOKEN / GHN_ORDER_TOKEN
GHN_ORDER_TOKEN: ""
SHOP_ID: 200778
FROM_DISTRICT_ID: 3695
FROM_WARD_CODE: "90737"
BASE_URL: https://dev-online-gateway.ghn.vn/shiip/public-api
GHN_ORDER_SHOP_ID: 200778
GHN_ORDER_URL: https://dev-online-gateway.ghn.vn/shiip/public-api

# AI (app.properties — khoá in hoa giữ nguyên)
AI_PROVIDER: deepseek
AI_MODEL: ""
AI_TIMEOUT_SECONDS: 30
AI_MAX_TOOL_STEPS: 6
AI_MAX_PROMPT_CHARS: 12000
AI_FALLBACKS: ""
AI_MEMORY_ENABLED: true
AI_MEMORY_RETENTION_DAYS: 365
AI_MEMORY_BLOCKED_PATTERNS: ""
ANTHROPIC_API_KEY: ""
ANTHROPIC_BASE_URL: https://api.anthropic.com
OPENROUTER_API_KEY: ""
OPENROUTER_BASE_URL: https://openrouter.ai/api/v1
TOKENROUTER_API_KEY: ""
TOKENROUTER_BASE_URL: https://api.tokenrouter.ai/v1
OPENAI_API_KEY: ""
OPENAI_BASE_URL: https://api.openai.com/v1
GOOGLE_API_KEY: ""
GOOGLE_BASE_URL: https://generativelanguage.googleapis.com/v1beta/openai
DEEPSEEK_API_KEY: ""        # DEEPSEEK_API_KEY
DEEPSEEK_BASE_URL: https://api.deepseek.com
DEEPSEEK_MODEL: deepseek-chat
DEEPSEEK_TIMEOUT_SECONDS: 30
MERCHANT_MAX_ITEMS_PER_CHANGE: 25
MERCHANT_MAX_PRICE_DELTA_PCT: 20
MERCHANT_MAX_PROMOTION_DISCOUNT_PCT: 50
MERCHANT_MAX_RESTOCK_QTY: 500
MERCHANT_MAX_CAMPAIGN_BUDGET: 10000
MERCHANT_ENABLE_LISTING_EDITS: true
MERCHANT_ENABLE_INVENTORY: true
MERCHANT_ENABLE_PRICING: true
MERCHANT_ENABLE_CAMPAIGNS: true
MERCHANT_REQUIRE_HOST_APPROVAL: true
MERCHANT_APPROVAL_SURFACE: trang quản trị (Admin > AI Merchant)
```

**Ràng buộc khi chuyển khoá (bắt buộc):**

- Chỉ chuyển **khoá có sẵn trong 4 file** sang yml. **Cấm bịa khoá mới hay đặt giá trị mặc định cho khoá optional** — ví dụ `app.cookies.secure` *không* nằm trong file nào và code mặc định là `request.isSecure()` (`AuthController:589`, `CookieAttributeFilter:21`); nếu thêm `secure: false` vào yml thì cookie hết `Secure` trên HTTPS = lỗ hổng. Tương tự: `mail.smtp.*`, `GOOGLE_CLIENT_ID/SECRET`, `facebook_client_id/secret` do `secrets.properties`/env cung cấp → giữ nguyên chỗ.
- `deploy.tomcat-home` không còn code nào đọc → **không** đưa vào yml, xoá luôn mục Tomcat ở Task 11.
- Khoá in hoa (`AI_*`, `MERCHANT_*`, `GHN_*`, `TOKEN`) giữ nguyên khoá yml dạng đỉnh, không đổi thành `app.ai.*` — vì code tra `AppConfig.get("AI_PROVIDER")`.

**Cross-check bằng lệnh (phải chạy, không được bỏ qua):**

```bash
LC_ALL=C sort -u /tmp/opencode/fk.txt > /tmp/opencode/fk2.txt   # fk.txt = 62 khoá từ 4 file
LC_ALL=C sort -u /tmp/opencode/ck.txt > /tmp/opencode/ck2.txt   # ck.txt = khoá code đọc literal
LC_ALL=C comm -23 /tmp/opencode/fk2.txt /tmp/opencode/ck2.txt   # khoá trong file nhưng code không đọc literal
```

Dòng trả về phải khớp với danh sách "khoá in hoa + `app.context-path` + `deploy.tomcat-home`" (đọc qua biến/AiConfig) — mỗi dòng phải **có mặt trong yml** hoặc rơi vào diện "optional do secrets/env cấp" ở ràng buộc trên. Nếu có khoá lạ → dừng, tra code đọc nó bằng `grep -rn '"<khoá>"' src/main/java` rồi quyết định trước khi xoá file.

- [ ] **Step 4: Rút `AppConfig` về chỉ đọc `secrets.properties`**

`src/main/java/Util/AppConfig.java:12-21`:

```java
private static final String[] PROPERTY_FILES = {
        // Chỉ còn file legacy chứa secret local (gitignored). Các khoá thường
        // đã chuyển vào application.yml và được đọc qua Spring Environment.
        "secrets.properties"
};
```

- [ ] **Step 5: Xoá 4 file cấu hình đã hợp nhất**

```bash
git rm src/main/resources/app.properties src/main/resources/db.properties src/main/resources/vnpay.properties src/main/resources/ship.properties
```

Giữ lại `secrets.properties` (gitignored) và `app.properties.example`/`secrets.properties.example` làm tài liệu.

- [ ] **Step 6: Chạy test + kiểm tra không khoá nào rơi ra ngoài**

Run: `./gradlew clean test --console=plain`
Expected: `TOTAL=437`

Run: `grep -rn "app.properties\|db.properties\|vnpay.properties\|ship.properties" src/main/java README.md docs/commerce-deployment.md | grep -v example`
Expected: **0 dòng** (hoặc chỉ còn chú thích dẫn tới `*.example`).

- [ ] **Step 7: Smoke test app lên được với config mới**

```bash
./gradlew bootWar -x test --console=plain
java -jar build/libs/petshop-boot.war --server.port=8099 > /tmp/opencode/p0-smoke.log 2>&1 &
sleep 45
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8099/actuator/health
grep -iE "APPLICATION FAILED|Migrating|^ERROR" /tmp/opencode/p0-smoke.log | head
kill %1
```

Expected: `200` (hoặc `{"status":"UP"}`), không có dòng `APPLICATION FAILED TO START`. Dòng `Migrating` của `spring-boot-properties-migrator` là bình thường — ghi lại các property nó báo để sửa ở bước này.

- [ ] **Step 8: Commit**

```bash
git add -A src/main/resources src/main/java/Util/AppConfig.java src/test/java/Util/AppConfigPrecedenceTest.java README.md docs/commerce-deployment.md
git commit -m "refactor(config): consolidate 5 properties files into application.yml, keep legacy facade"
```

---

### Task 6: Helper JSON + ghim hành vi trước khi đổi thư viện (Review Focus #3, #4, #5)

**Files:**
- Create: `src/main/java/Util/Json.java`
- Create: `src/test/java/Util/JsonBehaviorTest.java`
- Create: `src/test/java/services/ShippingServiceJsonLenientTest.java`
- Test có sẵn (không sửa, giữ xanh xuyên suốt P0): `src/test/java/com/petshop/web/BankWebhookControllerTest.java`, `GhnWebhookControllerTest.java`, `VnpayIpnControllerTest.java`, `UserAiSupportControllerTest.java`, `McpControllerTest.java`

**Interfaces:**
- Consumes: Jackson 3.1.5 đã có trên classpath từ Task 2 (`tools.jackson.databind.json.JsonMapper`, `tools.jackson.databind.JsonNode`, `tools.jackson.core.JacksonException`).
- Produces: `Util.Json.MAPPER` — `JsonMapper` instance duy nhất, lenient khi đọc. Các Task 7-10 chỉ dùng `Json.MAPPER` (không tạo `new JsonMapper()` chỗ khác).

- [ ] **Step 1: Viết test ghim hành vi (chưa có `Util.Json` → compile fail = đỏ)**

`src/test/java/Util/JsonBehaviorTest.java`:

```java
package Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class JsonBehaviorTest {

    @Test
    void readsLenientJsonWithSingleQuotesAndUnquotedNames() throws Exception {
        JsonNode node = Json.MAPPER.readTree("{name:'xà', count:2}");
        assertEquals("xà", node.path("name").asText());
        assertEquals(2, node.path("count").asInt());
    }

    @Test
    void htmlIsNotEscapedOnWrite() throws Exception {
        ObjectNode out = Json.MAPPER.createObjectNode();
        out.put("html", "<b>xà & \"y\"</b>");
        String json = Json.MAPPER.writeValueAsString(out);
        assertTrue(json.contains("<b>"), "Jackson ghi thẳng HTML, KHÔNG escape như Gson: " + json);
        assertTrue(json.contains("xà"), "Unicode tiếng Việt phải giữ nguyên: " + json);
    }

    @Test
    void bigDecimalAmountsWritePlainNotScientific() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", new BigDecimal("258000"));
        payload.put("fee", new BigDecimal("0.000001"));
        String json = Json.MAPPER.writeValueAsString(payload);
        assertTrue(json.contains("258000"), json);
        assertTrue(json.contains("0.000001"), "Không được ghi dạng 1.0E-6: " + json);
    }

    @Test
    void buildsNestedPayloadLikeGsonObjectModel() throws Exception {
        ObjectNode root = Json.MAPPER.createObjectNode();
        root.put("status", "ok");
        ObjectNode nested = Json.MAPPER.createObjectNode();
        nested.put("id", 7);
        ArrayNode arr = Json.MAPPER.createArrayNode();
        arr.add(1).add(2);
        root.set("data", nested);
        root.set("list", arr);

        String json = Json.MAPPER.writeValueAsString(root);
        assertEquals("{\"status\":\"ok\",\"data\":{\"id\":7},\"list\":[1,2]}", json);
    }
}
```

(import `tools.jackson.databind.node.ObjectNode`, `tools.jackson.databind.node.ArrayNode`.)

- [ ] **Step 2: Viết test lenient của ShippingService (chạy được ngay trên Gson = đỏ/vàng, phải xanh cả sau khi đổi)**

`src/test/java/services/ShippingServiceJsonLenientTest.java`:

```java
package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;   // Task 8 Step 2 sẽ đổi sang JsonNode — assertion giữ nguyên

class ShippingServiceJsonLenientTest {

    private static Object parse(String json) throws Exception {
        Method m = ShippingService.class.getDeclaredMethod("parseJsonLenient", String.class);
        m.setAccessible(true);
        return m.invoke(null, json);
    }

    @Test
    void parsesStrictJson() throws Exception {
        JsonObject node = (JsonObject) parse("{\"ok\":true,\"n\":1}");
        assertEquals("1", node.get("n").getAsString());
    }

    @Test
    void parsesLenientJsonThatStrictRejects() throws Exception {
        // GHN từng trả JSON lỗi; Gson lenient vẫn đọc được
        JsonObject node = (JsonObject) parse("{ok:true,'note':'xà'}");
        assertEquals("xà", node.get("note").getAsString());
    }

    @Test
    void nonJsonStillThrowsWithContext() {
        assertThrows(Exception.class, () -> parse("<html>502</html>"));
    }
}
```

- [ ] **Step 3: Tạo `Util.Json`**

`src/main/java/Util/Json.java`:

```java
package Util;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Mapper JSON dùng chung cho toàn app, thay cho Gson (đã gỡ).
 * Đọc theo kiểu lenient như Gson cũ để không vỡ khi GHN/webhook trả JSON lỗi.
 */
public final class Json {

    public static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .build();

    private Json() {
    }
}
```

- [ ] **Step 4: Chạy test mới**

Run: `./gradlew test --tests 'Util.JsonBehaviorTest' --tests 'services.ShippingServiceJsonLenientTest' --console=plain`
Expected: `JsonBehaviorTest` 4/4 pass; `ShippingServiceJsonLenientTest` 3/3 pass (đang chạy trên Gson — đây là bằng chứng hai thư viện cùng hành vi với các case này).

- [ ] **Step 5: Chạy toàn suite để chắc không phá gì**

Run: `./gradlew clean test --console=plain`
Expected: `TOTAL=444` (437 + 7 test mới)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/Util/Json.java src/test/java/Util/JsonBehaviorTest.java src/test/java/services/ShippingServiceJsonLenientTest.java
git commit -m "test: pin JSON behaviour (lenient read, no HTML escape, plain decimals) before Gson removal"
```

---

### Task 7: Gson → Jackson 3 ở web controllers (18 file)

**Files:**
- Modify (16 file trong `src/main/java/com/petshop/web/`): `AdminAiSupportController`, `AdminMerchantAgentController`, `AdminOrderController`, `AdminUploadController`, `AdminUserController`, `AuthController`, `BankWebhookController`, `CartController`, `CheckoutController`, `GhnWebhookController`, `McpController`, `NotificationController`, `ShopApiController`, `SignatureController`, `UserAiSupportController`, `VnpayIpnController`
- Modify (2 file): `src/main/java/controller/FaceBook/FaceBookLogin.java`, `src/main/java/controller/Google/GoogleLogin.java`

**Interfaces:**
- Consumes: `Util.Json.MAPPER` (Task 6); test characterization có sẵn (`BankWebhookControllerTest`, `GhnWebhookControllerTest`, `VnpayIpnControllerTest`, `UserAiSupportControllerTest`, `McpControllerTest`) — **phải xanh nguyên vẹn**.
- Produces: các controller này không còn `import com.google.gson.*`; Task 10 gỡ dependency.

- [ ] **Step 1: Bảng ánh xạ bắt buộc (áp dụng cho mọi file trong task)**

| Gson | Jackson 3 (`Util.Json.MAPPER`) |
|---|---|
| `import com.google.gson.*` | `import tools.jackson.databind.JsonNode;` + `tools.jackson.databind.node.ObjectNode/ArrayNode` |
| `private final Gson gson = new Gson();` | *(xoá — dùng `Json.MAPPER`)* |
| `JsonParser.parseString(s).getAsJsonObject()` | `(ObjectNode) Json.MAPPER.readTree(s)` (kèm kiểm `isObject()` nếu cần) |
| `new JsonObject()` | `Json.MAPPER.createObjectNode()` |
| `new JsonArray()` | `Json.MAPPER.createArrayNode()` |
| `obj.addProperty(k, v)` | `obj.put(k, v)` |
| `obj.add(k, node)` | `obj.set(k, node)` |
| `obj.get(k).getAsString()` | `obj.path(k).asText()` |
| `obj.get(k).getAsInt()` | `obj.path(k).asInt()` |
| `obj.get(k).getAsJsonObject()` | `(ObjectNode) obj.path(k)` |
| `obj.has(k)` | `obj.has(k)` |
| `obj.get(k).isJsonNull()` | `obj.get(k).isNull()` |
| `arr.add(...)` / `arr.size()` | giữ nguyên |
| `gson.toJson(x)` / `new Gson().toJson(x)` | `Json.MAPPER.writeValueAsString(x)` |
| `gson.fromJson(s, T.class)` | `Json.MAPPER.readValue(s, T.class)` |
| `catch (JsonSyntaxException e)` | `catch (tools.jackson.core.JacksonException e)` |
| `node.fields()` | `node.properties()` |

- [ ] **Step 2: Chạy test characterization trước khi sửa (phải xanh)**

Run: `./gradlew test --tests 'com.petshop.web.*' --console=plain`
Expected: xanh — đây là lưới an toàn; mọi file ở Step 3 phải giữ nguyên kết quả test này.

- [ ] **Step 3: Đổi từng file, theo nhóm dễ vỡ trước**

Thứ tự (test nhạy nhất làm trước): `VnpayIpnController` → `BankWebhookController` → `GhnWebhookController` → `UserAiSupportController` → `McpController` → `AdminMerchantAgentController` → 11 file còn lại → `FaceBookLogin`/`GoogleLogin`.

Ví dụ cụ thể — `VnpayIpnController.java` (phần giữa hai dòng dưới đây là code xử lý giữ nguyên):

```java
// trước
import com.google.gson.Gson;
private final Gson gson = new Gson();
// ... (phần thân method giữ nguyên)
return new Gson().toJson(rsp);

// sau
import Util.Json;
// ... (phần thân method giữ nguyên)
return Json.MAPPER.writeValueAsString(rsp);
```

`BankWebhookController.java:111`:

```java
// trước
JsonObject json = JsonParser.parseString(rawPayload).getAsJsonObject();
// sau
JsonNode parsed = Json.MAPPER.readTree(rawPayload);
if (parsed == null || !parsed.isObject()) {
    throw new IllegalArgumentException("Webhook payload không phải JSON object.");
}
ObjectNode json = (ObjectNode) parsed;
```

(lưu ý các helper `getOptionalString(JsonNode,...)` / `firstRequiredString(JsonNode,...)` trong cùng file phải đổi tham số từ `JsonObject` sang `JsonNode`, và `json.get(k).getAsString()` → `json.path(k).asText()`.)

- [ ] **Step 4: Compile + test sau mỗi nhóm 3 file**

Run: `./gradlew compileJava --console=plain && ./gradlew test --tests 'com.petshop.web.*' --console=plain`
Expected: xanh sau mỗi lần chạy.

- [ ] **Step 5: Verify hết gson ở nhóm web**

Run: `grep -rn "com.google.gson" src/main/java/com/petshop/web src/main/java/controller || echo CLEAN`
Expected: `CLEAN`

- [ ] **Step 6: Chạy toàn suite**

Run: `./gradlew clean test --console=plain` → `TOTAL=444`

- [ ] **Step 7: Commit**

```bash
git add -A src/main/java/com/petshop/web src/main/java/controller
git commit -m "refactor(json): web controllers from Gson to Jackson 3"
```

---

### Task 8: Gson → Jackson 3 ở `services` (ShippingService, DeepSeekService)

**Files:**
- Modify: `src/main/java/services/ShippingService.java` (34 chỗ Gson, gồm `parseJsonLenient`)
- Modify: `src/main/java/services/DeepSeekService.java`
- Test: `src/test/java/services/ShippingServiceJsonLenientTest.java` (Task 6 — **không được sửa assertion, chỉ đổi import**)

**Interfaces:**
- Consumes: `Json.MAPPER` (Task 6), bảng ánh xạ (Task 7 Step 1).
- Produces: `ShippingService.parseJsonLenient(String)` trả `tools.jackson.databind.JsonNode` — nếu có nơi nào khác gọi method này (grep trước), đổi theo.

- [ ] **Step 1: Grep nơi dùng `parseJsonLenient` và kiểu trả về**

Run: `grep -rn "parseJsonLenient" src/main/java`
Expected: chỉ `ShippingService.java`. Nếu có chỗ khác, đưa vào danh sách sửa ở Step 3.

- [ ] **Step 2: Viết đỏ trước khi đổi (đảo test sang Jackson API)**

`src/test/java/services/ShippingServiceJsonLenientTest.java` — thay import và ép kiểu:

```java
import tools.jackson.databind.JsonNode;
// 3 test method giữ nguyên logic, chỉ đổi kiểu trả về của parse() và assertion:
JsonNode node = (JsonNode) parse("{\"ok\":true,\"n\":1}");
assertEquals(1, node.path("n").asInt());
// case lenient:
JsonNode node = (JsonNode) parse("{ok:true,'note':'xà'}");
assertEquals("xà", node.path("note").asText());
// case non-json: assertThrows(Exception.class, () -> parse("<html>502</html>")); giữ nguyên
```

Run: `./gradlew test --tests 'services.ShippingServiceJsonLenientTest' --console=plain`
Expected: **FAIL/compile error** (method vẫn trả `JsonObject` của Gson) → đây là đỏ.

- [ ] **Step 3: Đổi `ShippingService.parseJsonLenient`**

```java
private static JsonNode parseJsonLenient(String json) {
    try {
        return Json.MAPPER.readTree(json);
    } catch (JacksonException e) {
        throw new RuntimeException("Failed to parse JSON response: " + json, e);
    }
}
```

(`Json.MAPPER` đã lenient theo Task 6 nên nhánh fallback riêng không còn cần; nếu test `parsesLenientJsonThatStrictRejects` fail thì thêm feature còn thiếu vào `Json.MAPPER` — **không** nới lỏng test.)

Cùng file: mọi `JsonObject`/`JsonArray` trong body call API GHN đổi sang `JsonNode`/`ArrayNode`, `obj.get(k).getAsString()` → `obj.path(k).asText()`, `gson.toJson(...)` → `Json.MAPPER.writeValueAsString(...)`, xoá field/`import com.google.gson.*`.

- [ ] **Step 4: Đổi `DeepSeekService` theo bảng ánh xạ**

Áp dụng đúng bảng Task 7 Step 1; đặc biệt các chỗ `fromJson(body, Xxx.class)` → `Json.MAPPER.readValue(body, Xxx.class)` (nếu class trong file là inner class có tên khác, giữ nguyên tên class).

- [ ] **Step 5: Chạy test**

Run: `./gradlew test --tests 'services.*' --console=plain`
Expected: xanh, gồm `ShippingServiceJsonLenientTest` 3/3 với assertion kiểu Jackson.

- [ ] **Step 6: Verify + toàn suite + commit**

```bash
grep -rn "com.google.gson" src/main/java/services/ShippingService.java src/main/java/services/DeepSeekService.java || echo CLEAN
./gradlew clean test --console=plain   # TOTAL=444
git add src/main/java/services/ShippingService.java src/main/java/services/DeepSeekService.java src/test/java/services/ShippingServiceJsonLenientTest.java
git commit -m "refactor(json): shipping and deepseek services from Gson to Jackson 3"
```

---

### Task 9: Gson → Jackson 3 ở `services/ai` (10 file)

**Files:**
- Modify (10 file): `services/ai/OpenAiCompatibleProvider.java` (39 chỗ), `AnthropicProvider.java` (28), `CommerceAgent.java` (13), `CommerceTools.java` (29), `common/Cards.java` (15), `common/MemoryService.java`, `merchant/PetShopMerchantBackend.java` (33), `merchant/MerchantTools.java` (33), `merchant/MerchantChangeDAO.java` (14), `merchant/MerchantAgent.java`
- Test có sẵn (phải xanh): `src/test/java/services/ai/AiProviderAbstractionTest.java`, `CommerceFullPortTest.java`, `AgentAdversarialTest.java`

**Interfaces:**
- Consumes: `Json.MAPPER` (Task 6), bảng ánh xạ (Task 7 Step 1).
- Produces: không còn `com.google.gson` trong `src/main/java/services/ai`; các method parse JSON trả `JsonNode`.

- [ ] **Step 1: Chạy lưới an toàn hiện có**

Run: `./gradlew test --tests 'services.ai.*' --console=plain`
Expected: xanh — 3 file test trên là bảo chứng hành vi agent trước khi đổi.

- [ ] **Step 2: Đổi `OpenAiCompatibleProvider` làm mẫu (file nặng nhất, 39 chỗ)**

`parseResponse` (dòng 243-277) đổi thành:

```java
private ChatResponse parseResponse(String body, String requestId, long latency) throws AiException {
    try {
        JsonNode res = Json.MAPPER.readTree(body);
        // Giữ hành vi cũ: body thiếu "choices"/"message" phải ném AiException
        // (Gson ném IllegalStateException khigetAsJsonObject/get(0) trượt →
        // catch(Exception) gói thành BAD_RESPONSE). Jackson path() trả missing
        // node thay vì ném, nên phải kiểm tra tường minh.
        if (!res.isObject()
                || !res.path("choices").isArray()
                || res.path("choices").isEmpty()
                || !res.path("choices").path(0).isObject()
                || !res.path("choices").path(0).path("message").isObject()) {
            throw new AiException(AiException.Kind.BAD_RESPONSE, name,
                    "Unparsable response from '" + name + "'", null);
        }
        JsonNode msg = res.path("choices").path(0).path("message");
        String content = msg.has("content") && !msg.path("content").isNull()
                ? msg.path("content").asText() : "";
        List<ToolCall> calls = new ArrayList<>();
        if (msg.path("tool_calls").isArray()) {
            for (JsonNode t : msg.path("tool_calls")) {
                JsonNode fn = t.path("function");
                calls.add(new ToolCall(
                        t.has("id") ? t.path("id").asText() : UUID.randomUUID().toString(),
                        fn.path("name").asText(),
                        fn.has("arguments") && !fn.path("arguments").isNull()
                                ? fn.path("arguments").asText() : "{}"));
            }
        }
        Integer promptTokens = null, completionTokens = null;
        if (res.path("usage").isObject()) {
            JsonNode u = res.path("usage");
            if (u.has("prompt_tokens")) promptTokens = u.path("prompt_tokens").asInt();
            if (u.has("completion_tokens")) completionTokens = u.path("completion_tokens").asInt();
        }
        String respModel = res.has("model") ? res.path("model").asText() : model;
        return new ChatResponse(content, calls, respModel, name, requestId, latency,
                promptTokens, completionTokens);
    } catch (JacksonException e) {
        throw new AiException(AiException.Kind.BAD_RESPONSE, name,
                "Unparsable response from '" + name + "'", e);
    }
}
```

(import `tools.jackson.core.JacksonException`, `tools.jackson.databind.JsonNode`, `Util.Json`; bỏ mọi `import com.google.gson.*`.)

Cùng file: `buildPayload`/`toWireMessage`/`mergeStreamedToolCalls` đổi `JsonObject`→`ObjectNode`, `JsonArray`→`ArrayNode`, `gson.toJson`→`Json.MAPPER.writeValueAsString`, `addProperty`→`put`, `add`→`set`.

- [ ] **Step 3: Thêm test ghim hành vi body AI hỏng**

`src/test/java/services/ai/OpenAiCompatibleProviderParseTest.java`:

```java
package services.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class OpenAiCompatibleProviderParseTest {

    private static ChatResponse parse(String body) throws Exception {
        OpenAiCompatibleProvider p = new OpenAiCompatibleProvider(
                "test", "https://example.test/v1", "k", "m", 5);
        Method m = OpenAiCompatibleProvider.class
                .getDeclaredMethod("parseResponse", String.class, String.class, long.class);
        m.setAccessible(true);
        return (ChatResponse) m.invoke(p, body, "req-1", 1L);
    }

    @Test
    void fullBodyParsesContentUsageAndModel() throws Exception {
        ChatResponse r = parse("{\"model\":\"m1\",\"choices\":[{\"message\":{\"content\":\"xin\"}}],"
                + "\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":4}}");
        assertEquals("xin", r.getContent());
        assertEquals("m1", r.getModel());
        assertEquals(3, r.getPromptTokens());
        assertEquals(4, r.getCompletionTokens());
    }

    @Test
    void malformedBodyThrowsAiException() {
        assertThrows(Exception.class, () -> parse("<html>502</html>"));
    }

    @Test
    void missingChoicesThrowsAiException() {
        assertThrows(Exception.class, () -> parse("{\"model\":\"m1\"}"));
    }
}
```

Run: `./gradlew test --tests 'services.ai.OpenAiCompatibleProviderParseTest' --console=plain`
Expected: 3/3 pass trên Gson (case `missingChoices` ném vì `getAsJsonArray("choices")` trượt) — chứng minh guard ở Step 2 giữ đúng hành vi sau khi đổi.

- [ ] **Step 4: Chạy test sau khi xong file mẫu**

Run: `./gradlew test --tests 'services.ai.*' --console=plain`
Expected: xanh.

- [ ] **Step 5: Đổi 9 file còn lại theo bảng ánh xạ, chia 3 nhịp**

Nhịp A: `AnthropicProvider`, `CommerceTools`, `CommerceAgent`.
Nhịp B: `merchant/PetShopMerchantBackend`, `merchant/MerchantTools`, `merchant/MerchantChangeDAO`, `merchant/MerchantAgent`.
Nhịp C: `common/Cards`, `common/MemoryService`.

Sau mỗi nhịp: `./gradlew compileJava --console=plain && ./gradlew test --tests 'services.ai.*' --console=plain` → xanh.

- [ ] **Step 6: Verify + toàn suite + commit**

```bash
grep -rn "com.google.gson" src/main/java/services/ai || echo CLEAN
./gradlew clean test --console=plain   # TOTAL=447 (444 + 3 test AI mới)
git add src/main/java/services/ai src/test/java/services/ai/OpenAiCompatibleProviderParseTest.java
git commit -m "refactor(json): AI providers and agent tools from Gson to Jackson 3"
```

---

### Task 10: Gỡ dependency Gson + đổi 2 test file + gate

**Files:**
- Modify: `build.gradle:47` (xoá `implementation 'com.google.code.gson:gson'`)
- Modify: `src/test/java/com/petshop/web/AdminOrderControllerTest.java`, `AdminMerchantAgentControllerTest.java` (2 file còn dùng `com.google.gson`)

**Interfaces:**
- Consumes: Task 7-9 đã xoá hết Gson ở main.
- Produces: classpath không còn Gson; Task 11/12 verify bằng `dependencies`.

- [ ] **Step 1: Đổi 2 test file sang Jackson**

`AdminOrderControllerTest.java` (`import com.google.gson.JsonObject` → ):

```java
import tools.jackson.databind.node.ObjectNode;
import Util.Json;
// các assertion trong test giữ nguyên logic, chỉ đổi cách dựng object:
ObjectNode obj = Json.MAPPER.createObjectNode();     // thay new JsonObject()
obj.put("id", 1);                                    // thay addProperty("id", 1)
```

Tương tự `AdminMerchantAgentControllerTest.java`: `JsonArray` → `tools.jackson.databind.node.ArrayNode` + `Json.MAPPER.createArrayNode()`.

Run: `./gradlew test --tests 'com.petshop.web.AdminOrderControllerTest' --tests 'com.petshop.web.AdminMerchantAgentControllerTest' --console=plain`
Expected: xanh.

- [ ] **Step 2: Gỡ dependency**

`build.gradle`, xoá dòng:

```gradle
    implementation 'com.google.code.gson:gson'
```

- [ ] **Step 3: Verify classpath không còn Gson**

Run: `./gradlew dependencies --configuration runtimeClasspath | grep -i gson || echo NO_GSON`
Expected: `NO_GSON`

Run: `grep -rn "com.google.gson" src/ || echo NO_GSON_IMPORTS`
Expected: `NO_GSON_IMPORTS`

- [ ] **Step 4: Chạy toàn suite**

Run: `./gradlew clean test --console=plain`
Expected: `TOTAL=447`

- [ ] **Step 5: Commit**

```bash
git add build.gradle src/test/java/com/petshop/web/AdminOrderControllerTest.java src/test/java/com/petshop/web/AdminMerchantAgentControllerTest.java
git commit -m "build: drop Gson dependency, JSON now Jackson 3 only"
```

---

### Task 11: Bỏ WAR legacy, gỡ properties-migrator, cập nhật tài liệu

**Files:**
- Modify: `build.gradle` (block `war`, `providedRuntime`/`providedCompile` liên quan Tomcat ngoài, xoá `runtimeOnly spring-boot-properties-migrator`)
- Modify: `Start.bat` (chạy `bootWar` + `java -jar` thay vì Tomcat 10)
- Modify: `README.md:16,110-125,151`, `docs/commerce-deployment.md`, `docs/launch-checklist.md`
- Verify: `Dockerfile` (giữ nguyên `gradle bootWar` + `java -jar`)

**Interfaces:**
- Consumes: Tasks 2-10.
- Produces: artifact duy nhất `petshop-boot.war` chạy bằng `java -jar`; Task 12 nghiệm thu trên artifact này.

- [ ] **Step 1: Gỡ properties-migrator và kiểm tra log startup sạch**

`build.gradle` — xoá dòng `runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'`.

Run:

```bash
./gradlew bootWar -x test --console=plain
java -jar build/libs/petshop-boot.war --server.port=8098 > /tmp/opencode/p0-migrator.log 2>&1 &
sleep 40
grep -ciE "Migrating|properties-migrator" /tmp/opencode/p0-migrator.log || true
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8098/actuator/health
kill %1
```

Expected: `0` (không còn property nào cần migrate) + `200`. Nếu còn dòng `Migrating` → sửa property tương ứng trong `application.yml` rồi chạy lại trước khi tiếp tục.

- [ ] **Step 2: Bỏ task `war` legacy**

`build.gradle` — thay block hiện tại:

```gradle
tasks.named('war') {
    enabled = true
    archiveFileName = 'ROOT.war'
}
```

bằng:

```gradle
// WAR thường (ROOT.war cho Tomcat ngoài) đã bỏ — chỉ còn bootWar (JSP cần WAR).
tasks.named('war') {
    enabled = false
}
```

Xoá `providedCompile 'jakarta.servlet:jakarta.servlet-api:6.1.0'` và `providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'` **chỉ sau khi Step 3 xác minh app vẫn boot** (Tomcat embedded phải nằm trong artifact).

- [ ] **Step 3: Smoke test artifact sau khi bỏ provided-scope**

```bash
./gradlew bootWar -x test --console=plain
unzip -l build/libs/petshop-boot.war | grep -c "tomcat-embed-core"
java -jar build/libs/petshop-boot.war --server.port=8097 > /tmp/opencode/p0-boot.log 2>&1 &
sleep 40
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8097/actuator/health
kill %1
```

Expected: `tomcat-embed-core` count ≥ 1 và health `200`. Nếu fail → khôi phục 2 dòng `provided*` ở Step 2, commit bản khôi phục, dừng và báo lại.

- [ ] **Step 4: Đổi `Start.bat` sang luồng `java -jar`**

Thay nội dung `Start.bat`:

```bat
@echo off
setlocal
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
if not defined PETSHOP_URL set "PETSHOP_URL=http://localhost:8080/home"
set "WAR_FILE=%PROJECT_ROOT%\build\libs\petshop-boot.war"

echo Step 1: Building bootWar...
cd /d "%PROJECT_ROOT%"
call gradlew.bat bootWar -x test
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build that bai!
    pause
    exit /b 1
)

echo Step 2: Starting PetShop ^(java -jar, khong can Tomcat ngoai^)...
start "PetShop" cmd /c "java -jar %WAR_FILE%"

echo.
echo   URL: %PETSHOP_URL%
timeout /t 20 /nobreak >nul
if /I "%PETSHOP_OPEN_BROWSER%"=="true" start "" "%PETSHOP_URL%"
endlocal
```

- [ ] **Step 5: Cập nhật README + docs**

- `README.md:16`: `- Tomcat 10.x` → xoá dòng (yêu cầu môi trường chỉ còn JDK 21, Gradle Wrapper, MySQL 8.4 LTS).
- `README.md:110-125` (mục deploy bằng Tomcat `shutdown.bat`, `PETSHOP_TOMCAT_HOME`, `gradlew war`): thay bằng:

```markdown
### Chạy ứng dụng

```bat
gradlew bootWar
java -jar build\libs\petshop-boot.war
```

Ứng dụng chạy embedded Tomcat (không cần cài Tomcat ngoài). Docker: `docker compose -f docker-compose.dev.yml up --build`.
```

- `README.md:151`: `- Cài JDK, MySQL, Tomcat 10` → `- Cài JDK 21, MySQL 8.4` (hoặc Docker).
- `docs/commerce-deployment.md`, `docs/launch-checklist.md`: thay mọi chỗ nhắc `ROOT.war`, `gradlew war`, `PETSHOP_TOMCAT_HOME`, Tomcat 10 bằng `petshop-boot.war`/`gradlew bootWar`/`java -jar`.

- [ ] **Step 6: Chạy toàn suite lần cuối của task**

Run: `./gradlew clean test --console=plain`
Expected: `TOTAL=447`

- [ ] **Step 7: Commit**

```bash
git add build.gradle Start.bat README.md docs/commerce-deployment.md docs/launch-checklist.md
git commit -m "build: drop legacy ROOT.war/Tomcat 10 path, bootWar only, remove properties-migrator"
```

---

### Task 12: Nghiệm thu P0

**Files:**
- Không sửa code — chỉ verify và (nếu thiếu) bổ sung tài liệu.

**Interfaces:**
- Consumes: toàn bộ Task 1-11.
- Produces: bằng chứng nghiệm thu theo đúng 9 tiêu chí của spec.

- [ ] **Step 1: Build sạch**

Run: `./gradlew clean test --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Đếm test (không được thấp hơn baseline 437)**

```bash
grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
grep -ho 'tests="[0-9]*"' build/test-results/propertyTest/*.xml 2>/dev/null | awk -F'"' '{s+=$2} END {print "PROPERTY="s}'
```

Expected: `TOTAL ≥ 447` (437 baseline + 7 test ở Task 6 + 3 test ở Task 9), và `PROPERTY` (nếu tách task) ≥ 160.

- [ ] **Step 3: Verify dependency & cấu hình**

```bash
./gradlew dependencies --configuration runtimeClasspath | grep -E "spring-boot-dependencies|gson|jackson-databind" | head -5
grep -rn "com.google.gson" src/ || echo NO_GSON
ls src/main/resources/*.properties | grep -vE "example|secrets" || echo NO_CONFIG_PROPERTIES   # secrets.properties là gitignored, local-only → không tính
grep -n "image: mysql" docker-compose.dev.yml docker-compose.prod.yml
grep -n "4.1.1" build.gradle | head -3
```

Expected: BOM `4.1.1`, có `tools.jackson.core:jackson-databind`, `NO_GSON`, `NO_CONFIG_PROPERTIES`, 3 dòng `mysql:8.4`, plugin `4.1.1`.

- [ ] **Step 4: Smoke test end-to-end (đăng nhập → giỏ hàng → checkout COD → admin)**

```bash
./gradlew bootWar -x test --console=plain
java -jar build/libs/petshop-boot.war --server.port=8096 > /tmp/opencode/p0-accept.log 2>&1 &
sleep 40
curl -s -o /dev/null -w "health=%{http_code}\n" http://localhost:8096/actuator/health
curl -s -o /dev/null -w "home=%{http_code}\n" http://localhost:8096/
curl -s -o /dev/null -w "shop=%{http_code}\n" http://localhost:8096/shop
kill %1
```

Expected: `health=200`, `home=200`, `shop=200`; log không có `APPLICATION FAILED TO START`. Sau đó chạy tay luồng mua trên app đang chạy (đăng nhập, thêm giỏ, checkout COD 1 đơn, xem đơn ở `/admin`).

- [ ] **Step 5: Đối chiếu checklist spec**

- [ ] `./gradlew clean test` ≥447 pass, không hạ số test
- [ ] App khởi động, `/actuator/health` = UP
- [ ] Luồng đăng nhập → giỏ hàng → checkout COD → admin chạy tay được
- [ ] Không còn `import com.google.gson`
- [ ] Không còn `.properties` cấu hình ngoài `*.example`
- [ ] Không còn task `war`/ROOT.war; `bootWar` sinh `petshop-boot.war`
- [ ] Cả 2 compose file dùng `mysql:8.4`
- [ ] README ghi Spring Boot 4.1.1, MySQL 8.4 LTS, không còn yêu cầu Tomcat 10
- [ ] Mỗi task 1 commit riêng trên `refactor/p0-baseline`

- [ ] **Step 6: Ghi kết quả vào spec**

Bổ sung mục `## 8. Kết quả nghiệm thu (2026-09-xx)` vào cuối `docs/superpowers/specs/2026-09-25-p0-baseline-upgrade-design.md`, ghi: số test trước/sau, commit range, các mục checklist đã tick.

```bash
git add docs/superpowers/specs/2026-09-25-p0-baseline-upgrade-design.md
git commit -m "docs: record P0 acceptance results"
```
