# P2 Spring Data JPA Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace 28 JDBC DAOs with Spring Data JPA repositories wave by wave, deleting `DBContext` last, with zero behaviour change.

**Architecture:** Five strangler waves (A infra+2 leaves → B leaves → C mid → D tx-heavy → E reporting+teardown); each DAO migrates in its own commit (entity+repository+call-site+test replacement) and the suite stays green throughout; mock-SQL tests are replaced 1:1+ by MySQL repository tests.

**Tech Stack:** Java 21, Spring Boot 4.1.1, spring-boot-starter-data-jpa (Hibernate 7), Flyway (schema owner), MySQL 8.4 (`petshop_test` for tests), Gradle 8.14.

**Spec:** `docs/superpowers/specs/2026-09-27-p2-spring-data-jpa-design.md`

## Global Constraints

- Baseline suite: TOTAL=451, PROPERTY=160, 0 fail/error, 1 Docker skip; TOTAL không hạ ở bất kỳ commit nào (thay test 1:1+, được tăng).
- `spring.jpa.hibernate.ddl-auto=validate` — cấm `update`/`create-drop`; Flyway sở hữu schema duy nhất.
- `@Column(name=...)` tường minh mọi entity field; entities = Model + annotation; ctor cũ giữ nguyên + ctor rỗng `protected`; field presentation → `@Transient`; không association trừ khi task chỉ định (`@ManyToOne` LAZY + JOIN FETCH).
- Không `@Transactional` mặc định trên repository call; `@Transactional` (REQUIRED) chỉ ở service method chứa block manual-tx cũ; modifying repo query mang `@Transactional` của chính nó (tương đương auto-commit 1 statement).
- Giữ contract `boolean`-false-on-error: catch `DataAccessException` (không catch `Exception` tràn lan, không để checked `SQLException` lọt ra).
- Overload nhận `Connection conn` bị xoá — caller dựa vào ambient transaction; service method tương ứng phải có `@Transactional` (Wave D kiểm tra từng cái).
- Service refactor chỉ trong method bị migration chạm tới + suite xanh bao phủ + ghi report; cấm drive-by.
- 1 DAO = 1 commit, message `refactor(jpa): <XxxDAO> to repository`; xanh mới sang DAO tiếp.
- Không commit secret; password DB chỉ qua env (`PETSHOP_DB_PASSWORD`/`DB_PASSWORD`), không bao giờ in ra log.
- Nhánh thực thi: `refactor/p2-jpa` (tạo từ `main`).
- Từ Task 1, suite bắt buộc cần MySQL local + DB `petshop_test` (fail rõ nếu thiếu, cấm skip-gracefully).

## Review Focus

- Mất `FOR UPDATE` (PESSIMISTIC_WRITE) ở `getProductByIdForUpdate`, `getCouponByIdForUpdate`, `getLatestByOrderIdForUpdate`, `findPendingByTransferReferenceInContentForUpdate` → oversell/double-spend race mà test đơn luồng không bắt được. Người dùng trông đợi lock giữ nguyên. Test ghim: mỗi task sở hữu `@Lock` method thêm sequence-invariant test (reserve→finalize→release giữ nguyên số liệu, Tasks 5/7/8) + reviewer xác minh `@Lock` hiện diện.
- Xoá overload `Connection` nhưng service method quên `@Transactional` → partial commit khi lỗi giữa chừng. Người dùng trông đợi all-or-nothing như block manual-tx cũ. Test ghim: mỗi Wave-D tx boundary một rollback test — service method throw `RuntimeException` giữa chừng, assert không còn dòng nào persist (Tasks 7/8/9, step style của task).
- Contract `boolean false` bị hẹp: cũ swallow mọi `Exception`, mới chỉ catch `DataAccessException` → lỗi lập trình (NPE) giờ propagate (đúng) nhưng lỗi DB phải vẫn `false`. Test ghim: mỗi wave một test assert `false` khi vi phạm constraint (VD save trùng unique) — Tasks 2/3/4/5/7/8/9 mỗi task một case.
- N+1 ở admin lists (`getOrdersPage`, `getAllOrders` join users cũ). Người dùng trông đợi 1 query. Test ghim: Task 9 repo test assert `customerFullname` populated từ `@Query` JOIN FETCH (đúng shape, không lazy-load sau session).
- `validate` fail trên schema thiếu cột (drift giữa entity và migration). Người dùng trông đợi fail-fast ở test boot chứ không phải prod. Test ghim: tự động — mọi `@DataJpaTest` boot đều validate; Task 10 boot full app smoke.

---

### Task 1: Wave A infra + `RememberTokenDAO` + `SecurityEventDAO`

**Files:**
- Modify: `build.gradle` (add starter-data-jpa), `src/main/resources/application.yml` (JPA keys), `README.md` (MySQL test requirement — append to environment section)
- Create: `src/test/java/com/petshop/config/JpaTestConfig.java`, `src/main/java/com/petshop/model/RememberToken.java`, `src/main/java/com/petshop/model/SecurityEvent.java`, `src/main/java/com/petshop/repository/RememberTokenRepository.java`, `src/main/java/com/petshop/repository/SecurityEventRepository.java`, `src/test/java/com/petshop/repository/RememberTokenRepositoryTest.java`, `src/test/java/com/petshop/repository/SecurityEventRepositoryTest.java`
- Modify (call-site swap — find exact sites first): consumers of `new RememberTokenDAO(`, `new SecurityEventDAO(`
- Delete: `src/main/java/com/petshop/dao/RememberTokenDAO.java`, `src/main/java/com/petshop/dao/SecurityEventDAO.java`
- Test: the two new `*RepositoryTest` files + full suite

**Interfaces:**
- Consumes: `DBProperties` (host/port/user/password/dbname for test config), Flyway `classpath:db/migration` locations, `PasswordUtil.hashPassword/verifyPassword` (unchanged)
- Produces: `JpaTestConfig` (DataSource+Flyway+entity-scan for ALL later repo tests), `com.petshop.repository.*` package, the per-DAO migration recipe every later task repeats

- [ ] **Step 1: Verify MySQL connectivity + create `petshop_test`**

```bash
mysql -u petshop -p"$PETSHOP_DB_PASSWORD" -e "SELECT VERSION();" && mysql -u petshop -p"$PETSHOP_DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS petshop_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Expected: version `8.4.x`, `Query OK`. Password stays in the env var — never `echo` it. If auth fails, try `DB_PASSWORD` env instead and record which worked in the report; if neither works → BLOCKED (environment, do not change code for it).

- [ ] **Step 2: Add dependency + JPA keys**

In `build.gradle`, next to the other `org.springframework.boot` starters, add exactly (BOM-managed, no version):

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

In `src/main/resources/application.yml`, under `spring:` add exactly:

```yaml
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    show-sql: false
```

Run: `./gradlew compileJava 2>&1 | tail -2` — Expected: `BUILD SUCCESSFUL` (no code uses JPA yet; `validate` has nothing to check until entities exist).

- [ ] **Step 3: Create `JpaTestConfig`**

Create `src/test/java/com/petshop/config/JpaTestConfig.java` with exactly this content:

```java
package com.petshop.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test-only JPA infrastructure: points at the `petshop_test` database
 * (created in Task 1 Step 1), migrates it with the SAME Flyway locations
 * as production, and scans entities. Used via
 * `@Import(JpaTestConfig.class)` on every `@DataJpaTest`.
 */
@TestConfiguration
@EntityScan("com.petshop.model")
public class JpaTestConfig {

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        String pw = System.getenv("PETSHOP_DB_PASSWORD");
        if (pw == null) {
            pw = System.getenv("DB_PASSWORD");
        }
        if (pw == null || pw.isBlank()) {
            throw new IllegalStateException(
                    "Repository tests need PETSHOP_DB_PASSWORD (or DB_PASSWORD) for the local petshop MySQL user");
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/petshop_test"
                + "?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        config.setUsername("petshop");
        config.setPassword(pw);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setPoolName("PetShopTestPool");
        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }
}
```

- [ ] **Step 4: Create entities `RememberToken` + `SecurityEvent`**

Create `src/main/java/com/petshop/model/RememberToken.java` (table DDL: `remember_tokens(id INT AUTO_INCREMENT PK, user_id INT NOT NULL, token_hash VARCHAR(255) NOT NULL, expires_at TIMESTAMP NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)`):

```java
package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "remember_tokens")
public class RememberToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RememberToken() {
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

Create `src/main/java/com/petshop/model/SecurityEvent.java` (table DDL: `security_events(id INT AUTO_INCREMENT PK, event_type VARCHAR(100) NOT NULL, principal VARCHAR(255) NULL, ip_address VARCHAR(64) NULL, details TEXT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)`):

```java
package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "principal", length = 255)
    private String principal;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SecurityEvent() {
    }

    // getters/setters for eventType, principal, ipAddress, details; getter for id/createdAt
}
```

- [ ] **Step 5: Create repositories**

Create `src/main/java/com/petshop/repository/RememberTokenRepository.java`:

```java
package com.petshop.repository;

import com.petshop.model.RememberToken;
import com.petshop.util.PasswordUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RememberTokenRepository extends JpaRepository<RememberToken, Integer> {

    List<RememberToken> findByExpiresAtAfter(LocalDateTime now);

    List<RememberToken> findByUserId(Integer userId);

    @Transactional
    default boolean saveToken(int userId, String plainToken) {
        try {
            RememberToken token = new RememberToken();
            token.setUserId(userId);
            token.setTokenHash(PasswordUtil.hashPassword(plainToken));
            token.setExpiresAt(LocalDateTime.now().plusDays(7));
            save(token);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(RememberTokenRepository.class)
                    .error("Error saving remember token for user id={}", userId, e);
            return false;
        }
    }

    default int findMatchingToken(String plainToken, int[] outUserId) {
        for (RememberToken token : findByExpiresAtAfter(LocalDateTime.now())) {
            if (PasswordUtil.verifyPassword(plainToken, token.getTokenHash())) {
                outUserId[0] = token.getUserId();
                return token.getId();
            }
        }
        return -1;
    }

    @Modifying
    @Transactional
    @Query("DELETE FROM RememberToken t WHERE t.userId = :userId")
    void deleteAllTokensForUser(@Param("userId") int userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RememberToken t WHERE t.expiresAt <= :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
```

Create `src/main/java/com/petshop/repository/SecurityEventRepository.java`:

```java
package com.petshop.repository;

import com.petshop.model.SecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Integer> {

    @Transactional
    default void log(String eventType, String principal, String ipAddress, String details) {
        try {
            SecurityEvent event = new SecurityEvent();
            event.setEventType(eventType);
            event.setPrincipal(principal);
            event.setIpAddress(ipAddress);
            event.setDetails(details);
            save(event);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(SecurityEventRepository.class)
                    .error("Failed to write security event {}", eventType, e);
        }
    }
}
```

- [ ] **Step 6: Find call sites**

```bash
grep -rn "new RememberTokenDAO(\|new SecurityEventDAO(" src/main src/test --include='*.java'
```

Expected: a short list (RememberTokenDAO is referenced by `AuthControllerTest`; record every hit — each must be swapped in Step 7).

- [ ] **Step 7: Swap call sites to injection**

For each production hit: replace field `private final RememberTokenDAO x = new RememberTokenDAO();` (or inline `new RememberTokenDAO()`) with constructor-injected `RememberTokenRepository` (same for SecurityEvent). Example transformation:

```java
// before
private final RememberTokenDAO rememberTokens = new RememberTokenDAO();
...
rememberTokens.saveToken(userId, token);
// after
private final RememberTokenRepository rememberTokens;
public AuthController(..., RememberTokenRepository rememberTokens) { ...; this.rememberTokens = rememberTokens; }
...
rememberTokens.saveToken(userId, token);
```

Keep every method name and argument order identical; only the type and construction change.

- [ ] **Step 8: Write repository tests (replacing mock-SQL coverage)**

Create `src/test/java/com/petshop/repository/RememberTokenRepositoryTest.java` with exactly this content (pattern for all later repo tests):

```java
package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RememberTokenRepositoryTest {

    @Autowired
    private RememberTokenRepository repository;

    @Test
    void saveTokenThenFindMatchingTokenReturnsIdAndUserId() {
        assertTrue(repository.saveToken(1, "plain-secret-1"));
        int[] outUserId = new int[1];
        int id = repository.findMatchingToken("plain-secret-1", outUserId);
        assertTrue(id > 0);
        assertEquals(1, outUserId[0]);
    }

    @Test
    void findMatchingTokenReturnsMinusOneForWrongSecret() {
        repository.saveToken(1, "right-secret");
        assertEquals(-1, repository.findMatchingToken("wrong-secret", new int[1]));
    }

    @Test
    void deleteExpiredTokensKeepsFreshTokens() {
        repository.saveToken(1, "fresh");
        repository.saveToken(1, "stale");
        assertEquals(2, repository.findAll().size());
        repository.deleteExpiredTokens(LocalDateTime.now().plusDays(8));
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void deleteAllTokensForUserRemovesOnlyThatUser() {
        repository.saveToken(1, "u1-token");
        repository.saveToken(1, "u2-token");
        repository.deleteAllTokensForUser(1);
        assertEquals(0, repository.findByUserId(1).size());
        assertEquals(1, repository.findByUserId(2).size());
    }
}
```

Create `src/test/java/com/petshop/repository/SecurityEventRepositoryTest.java`:

```java
package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityEventRepositoryTest {

    @Autowired
    private SecurityEventRepository repository;

    @Test
    void logPersistsEvent() {
        repository.log("LOGIN_FAIL", "user@example.com", "127.0.0.1", "bad password");
        assertEquals(1, repository.findAll().size());
    }
}
```

- [ ] **Step 9: Delete the old DAOs**

```bash
git rm src/main/java/com/petshop/dao/RememberTokenDAO.java src/main/java/com/petshop/dao/SecurityEventDAO.java && grep -rn "RememberTokenDAO\|SecurityEventDAO" src/ | wc -l
```

Expected: `0`

- [ ] **Step 10: Compile**

```bash
./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 11: Full suite**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}' && grep -ho 'failures="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "FAILURES="s}'
```

Expected: `BUILD SUCCESSFUL`, `TOTAL=456` (451 + 5 new: 4 RememberToken + 1 SecurityEvent), `FAILURES=0`. General rule for all later tasks: TOTAL must equal previous TOTAL + (new tests added); never lower.

- [ ] **Step 12: Commit + README note**

Append to `README.md` environment section (find the MySQL bullet first, add after it):

```markdown
- MySQL bắt buộc chạy local cho test suite từ P2 (DB `petshop_test`, user `petshop`, password qua `PETSHOP_DB_PASSWORD`); thiếu DB suite fail rõ, không skip.
```

```bash
git add -A && git commit -m "refactor(jpa): Wave A infra + RememberToken/SecurityEvent repositories"
```

---

### Task 2: Wave B1 leaves (7 DAOs)

**Files:**
- Modify (annotate entities): `src/main/java/com/petshop/model/AiChatMessage.java`, `AiChatSession.java`, `AiSupportSetting.java`, `BankWebhookEvent.java`, `Certificate.java`, `Coupon.java`, `CustomerSupportKnowledge.java`
- Create (repositories): `src/main/java/com/petshop/repository/AiChatMessageRepository.java`, `AiChatSessionRepository.java`, `AiSupportSettingRepository.java`, `BankWebhookEventRepository.java`, `CertificateRepository.java`, `CouponRepository.java`, `CustomerSupportKnowledgeRepository.java`
- Create (repo tests): `src/test/java/com/petshop/repository/` — one `*RepositoryTest` per DAO above (pattern: Task 1 Step 8)
- Modify (call-site swaps + test rewrites): consumers found by `grep -rn "new <X>DAO(" src/`; tests referencing these DAOs: `UserAiSupportControllerTest`, `AdminAiSupportControllerTest` (AiChat/AiSupportSetting/CustomerSupportKnowledge), `BankWebhookReconciliationServiceTest` (BankWebhookEvent), `SignatureControllerTest` (Certificate), `CheckoutControllerTest`/`CheckoutServiceInventoryBatchTest`/`CheckoutConcurrencyTest` (Coupon)
- Delete: the 7 `src/main/java/com/petshop/dao/<X>.java` files (one `git rm` per DAO, at its own sub-step)

**Interfaces:**
- Consumes: `JpaTestConfig` (Task 1), entity-annotation recipe (Task 1 Step 4), repository/test patterns (Task 1 Steps 5/8)
- Produces: 7 repositories + entities for Waves C-E consumers

**Method inventory (migrate every method; `Connection`-param overloads collapse to the ambient-tx single method):**
- `AiChatMessageDAO`: `create`, `getMessagesBySessionId`, `getRecentMessagesBySessionId`, `markMessagesAsRead`, `getUnreadCountBySessionId`, `countBySession`, `countUserMessagesToday`
- `AiChatSessionDAO`: `create`, `getById`, `getLatestOpenSessionByUserId`, `getSessionsForAdmin`, `getSessionsByUserId`, `getWaitingAdminSessions`, `updateStatus`
- `AiSupportSettingDAO`: `getSetting` (2-arg with default → default method wrapping `findByKey`), `updateSetting`, `getAllSettings`
- `BankWebhookEventDAO`: `findByProviderTransactionId(Connection, String)` → `findByProviderTransactionId(String)` (drop conn)
- `CertificateDAO`: `findByOrderId`
- `CouponDao`: `getValidCouponByCode(String)` + conn overload (collapse to one), `increaseUsedIfAvailable(Connection, int)` → `@Modifying @Query` conditional update returning boolean (rows==1), `getCouponByIdForUpdate(Connection, int)` → `@Lock(PESSIMISTIC_WRITE) findById`
- `CustomerSupportKnowledgeDAO`: `getAllActive`, `getAll`, `getById`, `create`, `update`, `delete`

- [ ] **Step 1: Annotate the 7 entities**

For each of the 7 Model classes: add `@Entity`, `@Table(name=<exact table>)`, `@Id` + `@GeneratedValue(IDENTITY)` on the PK, explicit `@Column(name=...)` on every persistent field, `@Transient` on presentation/computed fields, protected no-arg ctor (keep existing ctors). Table names: read from each DAO's SQL (`grep -oE "FROM [a-z_]+|INTO [a-z_]+|UPDATE [a-z_]+" <DAO>.java | sort -u`) — the table name in the entity MUST match the DAO's SQL exactly.

- [ ] **Step 2: Create the 7 repositories**

Derived queries where the DAO method is a simple lookup (`findByOrderId`, `findByProviderTransactionId`, `findByKey`); `@Query` where the DAO SQL has JOIN/conditions; `@Modifying` for updates/deletes; `@Lock(PESSIMISTIC_WRITE)` for the `ForUpdate` method. Boolean-false-on-error methods become default methods catching `DataAccessException` (Task 1 Step 5 pattern).

- [ ] **Step 3: Per-DAO loop (repeat for each of the 7 DAOs)**

```bash
grep -rn "new CouponDao(" src/main src/test --include='*.java'
```

Swap every production call site to constructor-injected repository (Task 1 Step 7 pattern); rewrite affected tests (mock DAO → mock/stub repository; `mockConstruction` → `Mockito.mock(Repo.class)` + constructor-passed mock); write the DAO's `*RepositoryTest` (Task 1 Step 8 pattern, real MySQL data, `@Transactional` rollback); `git rm` the DAO file; verify `grep -rn "CouponDao" src/ | wc -l` = `0`; run `./gradlew compileJava compileTestJava` (must be green before the next DAO); commit: `git add -A && git commit -m "refactor(jpa): CouponDao to repository"` (one commit per DAO, subject pattern with the DAO name).

- [ ] **Step 4: Constraint-violation pin (Review Focus)**

Add to one B1 repo test (pick `CouponRepositoryTest`): saving a duplicate unique-key row returns `false` (not throw). Exact shape: call the boolean-returning save twice with the same unique value, assert second call is `false`.

- [ ] **Step 5: Full suite + wave gate**

```bash
./gradlew clean test 2>&1 | tail -2 && grep -ho 'tests="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}' && grep -ho 'failures="[0-9]*"' build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "FAILURES="s}'
```

Expected: `BUILD SUCCESSFUL`, TOTAL = previous TOTAL + (new tests added in this task), `FAILURES=0`. No separate commit (commits happened per-DAO in Step 3).

---

### Task 3: Wave B2 leaves (8 DAOs)

**Files:**
- Modify (annotate entities): `src/main/java/com/petshop/model/Notification.java` — CHECK FIRST: no `Notification.java` in model list (model files: 37 listed, no Notification). If missing, CREATE it from the DAO SQL + table DDL (same as Task 1 Step 4 pattern for RememberToken). Entities for: `Notification` (new if absent), `OrderLog`, `OrderSign`, `OrderSignature`, `OrderStatusHistory`, `PetType`, `Review`, plus `AdminActionLog` — CHECK: no `AdminActionLog.java` in model list either; CREATE from DAO SQL + DDL if absent.
- Create (repositories): `NotificationRepository`, `OrderLogRepository`, `OrderSignRepository`, `OrderSignatureRepository`, `OrderStatusHistoryRepository`, `PetTypeRepository`, `ReviewRepository`, `AdminActionLogRepository` in `src/main/java/com/petshop/repository/`
- Create (repo tests): 8 `*RepositoryTest` files (Task 1 Step 8 pattern)
- Modify (call-site swaps + test rewrites): consumers per `grep -rn "new <X>DAO("`; tests: `NotificationControllerTest`/`AdminOrderControllerTest`/`AdminAiSupportControllerTest` (Notification), `BankWebhookReconciliationServiceTest` (OrderLog), `SignatureControllerTest`/`AccountControllerTest` (OrderSign/Signature), `AdminReadControllerTest`/`AdminProductControllerTest`/`ShopControllerTest` (PetType), `CatalogControllerTest`/`ShopApiControllerTest`/`AdminReviewControllerTest`/`WishlistToggleTest` (Review — incl. `mockConstruction(ReviewDAO.class)` at `ShopApiControllerTest.java:136` → `Mockito.mock(ReviewRepository.class)`), admin tests + `AdminAuditLoggingPropertyTest` (AdminActionLog)
- Delete: the 8 DAO files (one `git rm` per DAO)

**Interfaces:**
- Consumes: Task 1-2 patterns; `OrderLog`/`Review`/etc. entities feed Wave D (OrderDAO embeds OrderLog writes)
- Produces: 8 repositories for Wave D-E consumers

**Method inventory:**
- `NotificationDAO`: `getNotificationsByUserId` (returns `List<Map<String,Object>>` → interface projection `NotificationView` with getters matching SELECT aliases, or entity list if the DAO selects full rows — read the SQL first), `getUnreadCountByUserId`, `create`, `markAllAsRead` (@Modifying)
- `OrderLogDAO`: `getByOrderId`
- `OrderSignDAO`: `findByOrderId`, `findByUserId`, `findPendingByUserId`
- `OrderSignatureDAO`: `findByOrderId`, `findByUserId`
- `OrderStatusHistoryDAO`: `getHistoryByOrderId`
- `PetTypeDAO`: `getActivePetTypes`, `getAllPetTypes`, `getPetTypeByCode`, `getPetTypeById`, `addPetType`, `updatePetType`, `togglePetTypeStatus`
- `ReviewDAO`: `getReviewsByProductId`, `hasUserReviewedProduct`, `getAllReviews`, `getReviewsByMaxRating`, `deleteReview`, `hasUserPurchasedProduct`, `addReview`, `countReviewsByUserInLastHour`, `hasDuplicateRecentComment`, `updateReviewStatus`, `replyReview`
- `AdminActionLogDAO`: `log` (+ its real overloads — list via `grep -hEo 'public [^(]+\([^)]*\)'` before migrating)

- [ ] **Step 1: Annotate/create the 8 entities** (Task 2 Step 1 procedure; CREATE `Notification`/`AdminActionLog` models first if absent, DDL from `grep -rn -i "CREATE TABLE.*notification\|CREATE TABLE.*admin_action" src/main/resources/db/legacy/*.sql`)

- [ ] **Step 2: Create the 8 repositories** (Task 2 Step 2 procedure; `Map`-shaped selects → interface projections with exact alias getters)

- [ ] **Step 3: Per-DAO loop (repeat for each of the 8 DAOs)**

Same 8 sub-steps as Task 2 Step 3 (find call sites → swap to injection → rewrite tests → write repo test → `git rm` → grep-zero → compile green → commit `refactor(jpa): <X> to repository`).

- [ ] **Step 4: Constraint-violation pin (Review Focus)**

Add to `ReviewRepositoryTest`: `addReview` twice violating a constraint (or delete-then-add duplicate where unique) returns `false`.

- [ ] **Step 5: Full suite + wave gate + bootWar**

Suite gate exactly as Task 2 Step 5. Then (Wave B ends here):

```bash
./gradlew bootWar -x test 2>&1 | tail -2 && ls -la build/libs/petshop-boot.war
```

Expected: `BUILD SUCCESSFUL`, war exists. No separate commit.

---

### Task 4: Wave C `UserDAO` (48 methods)

**Files:**
- Modify (annotate entity): `src/main/java/com/petshop/model/User.java`
- Create: `src/main/java/com/petshop/repository/UserRepository.java`, `src/test/java/com/petshop/repository/UserRepositoryTest.java`
- Modify (call-site swaps + test rewrites): consumers per `grep -rn "new UserDAO(" src/`; tests: `AccountControllerTest`, `AuthControllerTest`, `AdminUserControllerTest`, `CheckoutControllerTest`, `FindingStructuralCompletenessPropertyTest`, `CheckoutServiceInventoryBatchTest`, `CheckoutConcurrencyTest` (mock/stub swap, no logic change)
- Delete: `src/main/java/com/petshop/dao/UserDAO.java`

**Interfaces:**
- Consumes: Task 1-3 patterns; `User` entity feeds Wave D (Order→User join) and Wave E
- Produces: `UserRepository` for Waves D-E

**Method inventory (all 48 — migrate every one; `Connection`-param overload collapses):**
`login`, `loginByEmail`, `loginByEmailOrUsername`, `checkUsernameExists`, `register`, `countUsers`, `getUserById`, `markDiscountAsUsed(Connection,int)` → ambient-tx single method, `getEmailByUserId`, `getUserByEmail`, `checkEmailExists`, `checkPhoneExists`, `updatePassword`, `saveResetToken`, `getUserByResetToken`, `clearResetToken`, `getAllUsers`, `getUsersByRole`, `updateUserRole`, `deleteUser`, `deactivateUser`, `countUsersByRole`, `getUserFullById`, `updateUser`, `getAllUsersWithStats`, `searchUsers`, `countNewUsersThisWeek`, `updateUserStatus`, `resetUserPassword`, `addUser`, `HaveEmail`, `insertUser`, `updateProfile`, `updateProfileAndEmail`, `isEmailTakenByAnotherUser`, `isPhoneTakenByAnotherUser`, `migratePasswordsToBCrypt` (one-shot helper — find callers via `grep -rn "migratePasswordsToBCrypt" src/`, preserve as a `@Transactional` service-layer method with identical semantics, covered by its existing test if any else a new repo test), `getFailedLoginAttempts`, `getLockedUntil`, `incrementFailedAttempts`, `lockAccount`, `resetFailedAttempts`, `isAccountLocked`, `saveVerificationToken`, `getUserByVerificationToken`, `getUserByExpiredVerificationToken`, `markEmailVerified`, `isEmailVerified`.

Password/BCrypt logic (`PasswordUtil`) stays in the repository default methods exactly as the DAO called it — no re-hashing, no algorithm change.

- [ ] **Step 1: Annotate `User` entity** (Task 2 Step 1 procedure; table/columns from the DAO SQL)

- [ ] **Step 2: Create `UserRepository`** (Task 2 Step 2 procedure; login/lockout counters as derived + `@Modifying` updates; `migratePasswordsToBCrypt` preserved per inventory note)

- [ ] **Step 3: Swap call sites + rewrite tests + write `UserRepositoryTest`** (Task 2 Step 3 procedure; repo test covers login success/fail, lockout counter round-trip, verification-token flow on real MySQL)

- [ ] **Step 4: `git rm` + grep-zero + compile + suite** (Task 2 Step 3 tail; commit `refactor(jpa): UserDAO to repository`)

- [ ] **Step 5: Constraint-violation pin (Review Focus)**

`UserRepositoryTest`: `register` duplicate username returns `false` (unique violation swallowed per contract).

---

### Task 5: Wave C `ProductDAO` (47 methods + `reserveStock` fallback)

**Files:**
- Modify (annotate entity): `src/main/java/com/petshop/model/Product.java`
- Create: `src/main/java/com/petshop/repository/ProductRepository.java`, `src/test/java/com/petshop/repository/ProductRepositoryTest.java`
- Modify (call-site swaps + test rewrites): consumers per `grep -rn "new ProductDAO(" src/`; tests: `CatalogControllerTest`, `ShopApiControllerTest`, `AdminProductControllerTest`, `AdminPromotionControllerTest`, `CheckoutControllerTest`, `AdminInventoryControllerTest`, `ShopControllerTest`, `WishlistToggleTest`, `FindingStructuralCompletenessPropertyTest`, `PaginationIndexPropertyTest`, `DeepSeekServiceTest`, `CheckoutServiceInventoryBatchTest`, `CheckoutConcurrencyTest`; DELETE `ProductDAOStockReservationTest.java` (mock-SQL, superseded) only after its assertions are re-covered below
- Delete: `src/main/java/com/petshop/dao/ProductDAO.java`, `src/test/java/com/petshop/dao/ProductDAOStockReservationTest.java`

**Interfaces:**
- Consumes: Task 1-4 patterns
- Produces: `ProductRepository` for Waves D-E (OrderDAO embeds Product reads)

**Method inventory (migrate all; conn overloads collapse; `ForUpdate` gets `@Lock`):**
Reads: `getAllProducts`, `getProductsByPetType`, `getProductsByPetTypeFallback`, `getCategoriesByPetType`, `getAllCategories`, `getAllBrands`, `getPopularCategories`, `getProductsByCategory`, `searchProducts`, `searchProductsLimit`, `searchProductsForAdvice`, `getFilteredProductsPage` + `countFilteredProducts` (criteria object → `@Query` with same WHERE, same pagination), `getDiscountedProductsList/Page/Count`, `getAllProductsPage`, `getTotalProductsCount`, `getPopularProductsPage/Count`, `getProductById` (+conn overload collapse), `getTotalProducts`, `getDiscountedProducts`, `getRelatedProducts`, `getProductsByPage`, `getStock`, `getLowStockProducts`, `getOutOfStockProducts`. Writes: `addProduct` (all overloads → one `save`-based method each, keep overload names), `addProductAndReturnId` (both overloads → `save` + return generated id), `updateProduct` (all overloads), `deleteProduct`, `softDeleteProduct`, `decreaseStock` (+conn collapse), `increaseStock` (+conn collapse), `updateStock`. Locking: `getProductByIdForUpdate(Connection,int)` → `@Lock(PESSIMISTIC_WRITE) Product findForUpdateById(int id)` (drop conn). Stock reservation: `reserveStock` → `@Modifying @Query` (`reserved_quantity` path) + legacy `stock` path ONLY if the column-absent fallback must be kept — it must NOT (spec §3: rely on Flyway; `reserved_quantity` exists via `V4__ProductsReservedQuantity`); `releaseReservedStock`, `finalizeReservedStock` → `@Modifying @Query` equivalents.

- [ ] **Step 1: Annotate `Product` entity** (Task 2 Step 1 procedure)

- [ ] **Step 2: Create `ProductRepository`** with every method above; `@Lock(PESSIMISTIC_WRITE)` on the ForUpdate method (Review Focus — reviewer will verify presence)

- [ ] **Step 3: Swap call sites + rewrite tests** (Task 2 Step 3 procedure)

- [ ] **Step 4: `ProductRepositoryTest` incl. supersede coverage + sequence invariant (Review Focus)**

Repo test must re-cover BOTH deleted mock tests with real data: reserve-then-`reserved_quantity`-increased assertion, and the legacy-fallback test is DROPPED (fallback deleted per spec §3 — record the deletion + reason in the report). Plus sequence invariant: reserve → finalize → release keeps `stock`/`reserved_quantity` consistent (single-thread pin for the lock-protected path).

- [ ] **Step 5: `git rm` (DAO + old mock test) + grep-zero + compile + suite** (commit `refactor(jpa): ProductDAO to repository`)

- [ ] **Step 6: Constraint-violation pin (Review Focus)**

`ProductRepositoryTest`: `addProduct` violating a NOT NULL/unique constraint returns `false`.

---

### Task 6: Wave C `CartDAO`

**Files:**
- Modify (annotate entity): `src/main/java/com/petshop/model/CartItem.java` (+ cart table entity — CHECK: `CartDAO` methods (`saveCartItem`, `addToCart`, `getCartByUserId` returning `Map<Integer,CartItem>`) imply a cart/cart_items table; if the row Model differs from `CartItem`, create/annotate accordingly from DAO SQL + DDL)
- Create: `src/main/java/com/petshop/repository/CartRepository.java`, `src/test/java/com/petshop/repository/CartRepositoryTest.java`
- Modify (call-site swaps + test rewrites): consumers per `grep -rn "new CartDAO(" src/`; tests: `AuthControllerTest` (incl. `mockConstruction(CartDAO.class)` at `:91` → `Mockito.mock(CartRepository.class)` + constructor-passed mock), `CartControllerTest`, `CheckoutControllerTest`, `WishlistToggleTest`, `ReorderServiceTest`, `CheckoutServiceInventoryBatchTest`, `CheckoutConcurrencyTest`
- Delete: `src/main/java/com/petshop/dao/CartDAO.java`

**Interfaces:**
- Consumes: Task 1-5 patterns
- Produces: `CartRepository` for Wave D (checkout flow)

**Method inventory:** `saveCartItem`, `addToCart`, `removeFromCart`, `updateCartQuantity`, `clearCart` (+conn overload collapse), `getCartByUserId` (returns `Map<Integer,CartItem>` → default method assembling the map from `findByUserId` list, same keying as the DAO), `getTotalQuantity`, `syncCartFromSession`.

- [ ] **Step 1: Annotate/create cart entities** (Task 2 Step 1 procedure)

- [ ] **Step 2: Create `CartRepository`** (derived + default-method map assembly preserving DAO keying)

- [ ] **Step 3: Swap call sites + rewrite tests (incl. mockConstruction at AuthControllerTest:91)** (Task 2 Step 3 procedure)

- [ ] **Step 4: `CartRepositoryTest`** (add/remove/quantity/clear/sync round-trips on real MySQL)

- [ ] **Step 5: `git rm` + grep-zero + compile + suite + bootWar (Wave C ends here)**

```bash
./gradlew bootWar -x test 2>&1 | tail -2 && ls -la build/libs/petshop-boot.war
```

Expected: `BUILD SUCCESSFUL`, war exists. Commit `refactor(jpa): CartDAO to repository` (code commit happens with the DAO deletion as in prior tasks; this step only adds the bootWar gate, no separate commit).

---

### Task 7: Wave D1 (`AddressDao`, `WishlistDAO`, `PromotionDAO`, `SalesSummaryDAO` + tx)

**Files:**
- Modify (annotate entities): `src/main/java/com/petshop/model/Address.java`, `Product.java` (wishlist returns `List<Product>` — no new entity), `Promotion.java`, `PromotionCandidate.java` (DTO/projection — read DAO SQL first; if it maps a join, make it an interface projection, not an entity), plus sales-summary row shape (read `SalesSummaryDAO` SQL; likely aggregate → projection)
- Create: `AddressRepository`, `WishlistRepository`, `PromotionRepository`, `SalesSummaryRepository` + 4 `*RepositoryTest` files
- Modify (call-site swaps + `@Transactional` + bounded service refactor): consumers per grep; tests: `AccountControllerTest`/`CheckoutControllerTest` (Address), `CatalogControllerTest`/`ShopControllerTest`/`WishlistToggleTest` (Wishlist), `PageControllerTest`/`AdminPromotionControllerTest`/`ProductPricingServiceTest` (Promotion); `SalesSummaryDAO` has no test refs → new test only
- Delete: the 4 DAO files (one commit per DAO)

**Interfaces:**
- Consumes: Tasks 1-6 patterns; FIRST task to add service-layer `@Transactional`
- Produces: tx-boundary pattern + rollback-test pattern for Tasks 8-9

**Method inventory + tx mapping (exact):**
- `AddressDao` (manual-tx inside DAO — find the method via `grep -n "setAutoCommit" AddressDao.java`; every method: `getAddressesByUserId`, `setDefaultAddress`, `hasAnyAddress`, `getAddressById`, `deleteAddress`, `isDefaultAddress`, `setNewestAddressAsDefault`, `getDefaultAddressByUserId`): the manual-tx method's CALLER service method gets `@Transactional`; conn params (if any) deleted.
- `WishlistDAO` (manual-tx: `toggleWishlist` region per `setAutoCommit` grep — verify): `getWishlistProductsByUserId`, `getWishlistProductIdsByUserId`, `isInWishlist`, `addToWishlist`, `removeFromWishlist`, `toggleWishlist`, `toggleWishlistAndReturnState`.
- `PromotionDAO` (manual-tx: flash-sale reserve/release): `findActivePromotionCandidates` (+conn collapse), `reserveFlashSaleQuantity(Connection,...)` → `@Modifying @Query` conditional decrement (rows==1 → true) called from a `@Transactional` service method, `releaseFlashSaleQuantity` likewise, `getFlashSaleProducts`, `getAllPromotions`, `getPromotionById`, `savePromotion`, `updatePromotionStatus`, `deletePromotion`, `canDeletePromotion`.
- `SalesSummaryDAO`: `refreshRecent`, `rebuildAll` (aggregate writes — read SQL; `@Modifying @Query` (native if needed) inside `@Transactional` service methods).

- [ ] **Step 1: Annotate entities** (Task 2 Step 1 procedure; `PromotionCandidate`/sales shapes as projections, not entities)

- [ ] **Step 2: Create the 4 repositories** (`@Lock` where a ForUpdate-style method exists; `@Modifying` writes carry own `@Transactional`)

- [ ] **Step 3: Per-DAO loop (repeat ×4)**

Same 8 sub-steps as Task 2 Step 3, PLUS: for each deleted manual-tx block, add `@Transactional` to the exact service method that called it (find via the DAO method's callers), and delete any `Connection` params along the call chain. Service refactor confined to those methods; every deviation logged in the commit body.

- [ ] **Step 4: Rollback test per tx boundary (Review Focus)**

For EACH service method made `@Transactional` in this task: add a test that throws `RuntimeException` mid-method and asserts zero partial persists (e.g., flash-sale reserve failure leaves quantity untouched; address default-switch failure keeps old default). Style:

```java
@Test
void failedReserveLeavesQuantityUntouched() {
    int before = repository.currentQuantity(...);
    assertThrows(RuntimeException.class, () -> service.reserveWithForcedFailure(...));
    assertEquals(before, repository.currentQuantity(...));
}
```

(Forced failure via invalid input that passes validation but fails mid-tx, or a test subclass hook — implementer chooses the least invasive trigger and documents it.)

- [ ] **Step 5: Constraint-violation pin + suite** (one `false`-on-duplicate test; TOTAL gate as Task 2 Step 5; one commit per DAO)

---

### Task 8: Wave D2 (`InventoryBatchDAO`, `PaymentTransactionDAO` + tx)

**Files:**
- Modify (annotate entities): `src/main/java/com/petshop/model/InventoryBatch.java`, `InventoryAgingSnapshot.java` (view/snapshot — read SQL; projection if not a table), `ProductAdminInventoryView.java` (view → projection), `ReorderRecommendation.java` (computed → projection/service assembly, NOT an entity), `PaymentTransaction.java`
- Create: `InventoryBatchRepository`, `PaymentTransactionRepository` + 2 `*RepositoryTest` files
- Modify (call-site swaps + `@Transactional` + bounded refactor): consumers per grep; tests: `CheckoutControllerTest`/`AdminInventoryControllerTest`/`CheckoutServiceInventoryBatchTest` (InventoryBatch), `VnpayIpnControllerTest`/`ShopApiControllerTest`/`CheckoutControllerTest`/`PaymentTransactionDAOTest` (mock-SQL — DELETE only after re-cover, see Step 4)/`BankWebhookReconciliationServiceTest`/`CheckoutServiceInventoryBatchTest`/`CheckoutConcurrencyTest` (PaymentTransaction)
- Delete: the 2 DAO files + `src/test/java/com/petshop/dao/PaymentTransactionDAOTest.java` (after re-cover)

**Interfaces:**
- Consumes: Tasks 1-7 patterns + tx/rollback patterns
- Produces: repositories for Task 9 (OrderDAO embeds both)

**Method inventory + tx mapping:**
- `InventoryBatchDAO` (manual-tx ×2 — locate via setAutoCommit grep): `recordImportBatch`, `findAllocatableBatchesForProduct`, `hasTrackedBatchesForProduct(Connection)` (collapse), `getNearExpiryBatches`, `getInventoryAgingSnapshots`, `getProductAdminInventoryViews`, `getReorderRecommendations`.
- `PaymentTransactionDAO` (manual-tx ×2): `save(Connection)` (collapse to ambient), `getLatestByOrderId` (+conn collapse), `getLatestByOrderIdForUpdate(Connection)` → `@Lock(PESSIMISTIC_WRITE)`, `findPendingByTransferReferenceInContentForUpdate(Connection)` → `@Lock(PESSIMISTIC_WRITE)`, `expirePendingTransactions`, `attachLatestToOrders(Connection,List<Order>)` → move to service-layer default flow (attach via repository call per order inside the caller's transaction — same N, no N+1 regression vs old loop), `applyTransaction(Order,PaymentTransaction)` → service method (unchanged logic, new home — record in commit body).

- [ ] **Step 1: Annotate entities / define projections** (Task 2 Step 1 procedure; view/computed shapes as projections)

- [ ] **Step 2: Create the 2 repositories** (`@Lock` on both ForUpdate methods — Review Focus)

- [ ] **Step 3: Per-DAO loop (×2)** (Task 7 Step 3 procedure + `@Transactional` on manual-tx callers + `applyTransaction` relocation logged)

- [ ] **Step 4: Supersede `PaymentTransactionDAOTest` + sequence invariant + rollback tests (Review Focus)**

Re-cover every mock assertion with real-MySQL equivalents (expiry sweep, latest-by-order, ForUpdate read); sequence invariant for reserve/expire flow; rollback test per new `@Transactional` boundary (Task 7 Step 4 style). THEN `git rm` the old mock test.

- [ ] **Step 5: Constraint-violation pin + suite** (TOTAL gate; one commit per DAO)

---

### Task 9: Wave D3 `OrderDAO` hub (44 methods + tx)

**Files:**
- Modify (annotate entities): `src/main/java/com/petshop/model/Order.java`, `OrderItem.java`, `OrderLog.java`, `OrderStatus.java`, `OrderStatusHistory.java` (+ `CustomerRepurchaseSuggestion` — computed suggestion → projection/service assembly, NOT an entity; `PaymentTransaction`/`Product` already done)
- Modify (association, the allowed exception): add LAZY `@ManyToOne User` to `Order` ONLY (needed for the users-JOIN list methods below) — no other associations
- Create: `src/main/java/com/petshop/repository/OrderRepository.java`, `src/test/java/com/petshop/repository/OrderRepositoryTest.java`
- Modify (call-site swaps + `@Transactional` + bounded refactor): 14 main consumers per `grep -rn "new OrderDAO(" src/main`; tests (17 files — largest surface): `VnpayIpnControllerTest`, `SignatureControllerTest`, `AdminOrderControllerTest`, `AccountControllerTest`, `ShopApiControllerTest`, `AdminUserControllerTest`, `CheckoutControllerTest`, `GhnWebhookControllerTest`, `MyOrdersControllerTest`, `OrderDAOStatusTransitionTest` + `OrderDAOLegacySchemaFallbackTest` (mock-SQL — DELETE after re-cover, fallback itself DROPPED per spec §3), `FindingStructuralCompletenessPropertyTest`, `PaginationIndexPropertyTest`, `ReorderServiceTest`, `BankWebhookReconciliationServiceTest`, `CheckoutServiceInventoryBatchTest`, `CheckoutConcurrencyTest`
- Delete: `src/main/java/com/petshop/dao/OrderDAO.java` + the 2 mock tests above

**Interfaces:**
- Consumes: ALL prior repositories (`ProductRepository`, `PaymentTransactionRepository`, `OrderLogRepository`, `UserRepository`...) — OrderDAO's 16 internal `new XxxDAO` become injected repositories in services
- Produces: final order aggregate for Task 10

**Method inventory (44 — groups):** creates: `saveOrder` (+conn collapse), `saveOrderItem` (+conn collapse); reads: `getAllOrders`, `getOrdersPage` + `countOrders` (JOIN users → `@Query` JOIN FETCH + fill `@Transient customerFullname/customerPhone` from the fetched association in a default method), `getOrderById` (same JOIN FETCH treatment), `getOrderItems` (+conn collapse), `getOrdersByUserId`, `getStatusHistory`, `getOrderLogs`, `getRepurchaseSuggestions` (projection), `getTodayRevenue`, `countPendingOrders(ByUserId)`, `countOrdersAwaitingPaymentVerification`, `countCompletedOrdersByUserId`, `getTotalSpentByUserId`, `countOrdersByUserId`, `getOrdersPendingGhnPush`, `getOrdersForGhnSync`; writes: `updateStatus` (both overloads — keep both signatures, one delegates), `updatePaymentVerification`, `updatePaymentStatus(conn)` (collapse), `markAwaitingPaymentOrderPaid(conn)` (collapse), `releaseReservedStockForOrder(conn)` + `finalizeReservedStockForOrder(conn)` (collapse to ambient-tx repo calls), `cancelOrderByUser`, `updateOrderPaymentStatus`, `markOnlinePaymentAwaiting/Paid/PaidAndFinalize`, `updateGhnStatus`, `autoCompleteDeliveredOrders`, `markOrderAsPaid`, `updateOrderStatusRaw`; guards: `isWithinCancellationWindow` (pure read — keep logic verbatim in default method).

- [ ] **Step 1: Annotate `Order`/`OrderItem`/`OrderLog`/`OrderStatus`/`OrderStatusHistory` + LAZY User association on `Order`** (Task 2 Step 1 procedure + association exception)

- [ ] **Step 2: Create `OrderRepository`** with the exact `@Query("SELECT o FROM Order o JOIN FETCH o.user ...")` list methods + derived rest + `@Modifying` writes (own `@Transactional`)

- [ ] **Step 3: Swap 14 main consumers + rewrite 17 test files** (Task 2 Step 3 procedure at largest scale; internal `new XxxDAO` inside OrderDAO methods become service-level injected repositories — the services calling OrderDAO get the extra collaborators)

- [ ] **Step 4: `OrderRepositoryTest` incl. supersede + shape pin (Review Focus)**

Re-cover both deleted mock tests with real data (status transitions incl. `updateStatusRaw` paths; the legacy-fallback test is DROPPED — fallback deleted per spec §3, record reason). Shape pin: `getOrdersPage` returns `customerFullname` populated from the JOIN FETCH (no lazy-load-after-session). Sequence invariant for reserve→finalize→release across Order+Product.

- [ ] **Step 5: Rollback tests for checkout+vnpay boundaries (Review Focus)**

One rollback test per service method made `@Transactional` in this task (Task 7 Step 4 style) — checkout create-order fail mid-way persists nothing.

- [ ] **Step 6: `git rm` (DAO + 2 mock tests) + grep-zero + compile + suite + bootWar (Wave D ends here)**

BootWar gate as Task 6 Step 5. Commit `refactor(jpa): OrderDAO to repository` (code commit with deletion as prior tasks; bootWar gate adds no separate commit).

- [ ] **Step 7: Constraint-violation pin (Review Focus)**

`OrderRepositoryTest`: duplicate unique (or NOT NULL violation) on order create path returns `false`.

---

### Task 10: Wave E `ReportDAO` + teardown + final gates

**Files:**
- Modify (annotate entities if missing for report shapes — prefer projections): read `ReportDAO` SQL first; `Review`/`Product`/`Order` entities already exist
- Create: `src/main/java/com/petshop/repository/ReportRepository.java` (+ projection interfaces `RevenueByMonthView`, `TopSellingProductView`, `TopCustomerView`, `CouponUsageView`, `StoredNotificationView`, `OrdersByStatusView` — exact getter names = SELECT aliases in the DAO SQL), `src/test/java/com/petshop/repository/ReportRepositoryTest.java`
- Modify (call-site swaps): `AdminReadControllerTest` is the only test ref; main consumers per `grep -rn "new ReportDAO(" src/main`
- Delete: `src/main/java/com/petshop/dao/ReportDAO.java`, `src/main/java/com/petshop/context/DBContext.java`, `src/main/java/com/petshop/dao/DBProperties.java` (ONLY if `DataSourceConfig` no longer references it — verify via `grep -rn "DBProperties" src/main`; if still referenced, KEEP the facade and record why), `dbContextBinder` bean in `DataSourceConfig.java` (only together with DBContext deletion)
- Test: `ReportRepositoryTest` + full suite + smoke + E2E

**Interfaces:**
- Consumes: all prior repositories/entities
- Produces: P2 DONE certificate

**Method inventory (`ReportDAO`, 15 — aggregations stay aggregations):** `getOverviewStats`, `getRevenueByMonth`, `getOrdersByStatus`, `getTopSellingProducts`, `getOrdersByMonthWithStatus`, `getTotalRevenue`, `getCurrentMonthRevenue`, `getCompletedOrdersCount`, `getRecentOrders`, `getLowStockProducts`, `getRecentLowRatingReviews`, `getRecentReviews`, `getTopCustomers`, `getCouponUsage`, `getStoredNotifications`. Each becomes `@Query` (JPQL; `nativeQuery = true` where the DAO SQL uses MySQL-specific constructs — GROUP_CONCAT, DATE_FORMAT, etc. — with projection interfaces binding the exact aliases). `List<Map<String,Object>>` shapes → projection interfaces (NOT Maps — Spring Data native `Map` returns are unreliable); single-row maps → single projection object.

- [ ] **Step 1: Create projections + `ReportRepository`** (one `@Query` per method, SQL lifted from the DAO verbatim then minimally adapted to entity/projection names; `@Column` aliases must match projection getters exactly)

- [ ] **Step 2: Swap call sites + `ReportRepositoryTest`** (row-count/value assertions against Flyway-seeded `petshop_test` data — seed minimal rows in-test via repositories, not SQL dumps)

- [ ] **Step 3: `git rm ReportDAO` + grep-zero + compile + suite** (commit `refactor(jpa): ReportDAO to repository`)

- [ ] **Step 4: Teardown `DBContext`**

```bash
grep -rn "DBContext\|DBProperties" src/main src/test --include='*.java' | grep -v "com/petshop/context/DBContext.java\|com/petshop/dao/DBProperties.java"
```

Expected: `0` (every consumer migrated). If hits remain → BLOCKED (a DAO was missed; do not delete). Then:

```bash
git rm src/main/java/com/petshop/context/DBContext.java && ./gradlew compileJava compileTestJava 2>&1 | tail -2
```

Expected: `BUILD SUCCESSFUL`. Then DBProperties decision per Files note (keep facade if `DataSourceConfig` uses it; else `git rm` + remove `dbContextBinder` bean). Commit: `git add -A && git commit -m "refactor(jpa): remove DBContext static accessor (all DAOs migrated)"` (fold DBProperties removal into the same commit if applicable).

- [ ] **Step 5: Final gates**

```bash
grep -rn "DBContext" src/ | wc -l; grep -rnE "new [A-Za-z]*DAO[A-Za-z]*\(" src/main --include='*.java' | wc -l; ls src/main/java/com/petshop/dao/ 2>&1; grep -rhoE "tests=\"[0-9]*\"" build/test-results/test/*.xml | awk -F'"' '{s+=$2} END {print "TOTAL="s}'
```

Expected: `0`, `0`, (`DBProperties.java` only — if kept — else `No such file`), TOTAL ≥ 451. Then full suite + bootWar + smoke + E2E exactly as P0/P1 acceptance (login→cart→COD order→admin green, health UP on :8096, 8080 occupied):

```bash
./gradlew clean test 2>&1 | tail -2 && ./gradlew bootWar -x test 2>&1 | tail -2
```

Expected both `BUILD SUCCESSFUL`. Boot + smoke + E2E per Task 7-style procedure (record outputs in report; no commit — acceptance only).