# P2 — Spring Data JPA thay tầng DAO (design spec)

Ngày: 2026-09-27 · Nhánh thực thi dự kiến: `refactor/p2-jpa` · Phase roadmap: P0 ✅ → P1 ✅ → **P2** → P3 (Thymeleaf) → P4 (Security)

## 1. Hiểu chung (đã chốt với user)

- **Mục đích:** thay 28 DAO JDBC tay (~7.4k dòng SQL, 247 chỗ gọi `DBContext`, 7 DAO quản transaction tay, 0 `@Transactional`) bằng Spring Data JPA: entities + repositories + `@Transactional` đúng biên cũ.
- **Cách làm:** strangler theo 5 waves ngang (§5), mỗi DAO 1 commit xanh, `DBContext` chết cuối.
- **Quyết định user:** strangler (Q1) · annotate Model làm entity (Q2) · được refactor service trong method chạm migration (Q3) · migrate tất cả kể cả reporting (Q4) · test trên MySQL local (Q5) · waves ngang (A).
- **Success:** 0 DAO JDBC (trừ Flyway/migration), 0 `DBContext`, suite ≥451 xanh trên MySQL local, demo + E2E giữ nguyên.

## 2. Hiện trạng (xác minh 2026-09-27)

- 28 DAO + `DBProperties`; `DBContext` static 2 chế độ (Spring DataSource push lúc boot, fallback HikariCP ngoài Spring).
- DAO là instance (`new ProductDAO()` ×15 call-site nhiều nhất), method mở connection riêng auto-commit trừ 7 DAO có block `setAutoCommit(false)` (103 dòng commit/rollback): InventoryBatch, Wishlist, Order (4 block), PaymentTransaction (2), Address, Promotion, SalesSummary.
- 0 `jakarta.persistence`, 0 `spring-boot-starter-data-jpa`, 0 Hibernate, 0 `@Transactional` trong repo.
- Test DAO hiện tại mock `Connection` + assert chuỗi SQL (VD `ProductDAOStockReservationTest`) — sẽ bị thay bằng repo test MySQL.
- `OrderDAO` là hub: 16 `new XxxDAO` nội bộ (ProductDAO×8, OrderLogDAO×7, OrderStatusHistory×3, …) — migrate muộn.
- Schema: Flyway sở hữu (22 file `db/legacy` + Java migrations); cột có cả camelCase thật (`createdAt`) lẫn snake_case.

## 3. Infra + entity rules (đã duyệt §1)

- `org.springframework.boot:spring-boot-starter-data-jpa` (BOM 4.1.1, Hibernate 7).
- `spring.jpa.hibernate.ddl-auto=validate` — sai mapping fail ngay lúc boot test.
- `@Column(name=...)` tường minh mọi field (không đoán naming strategy).
- Entities = Model + annotation; giữ ctor cũ + thêm ctor rỗng `protected`; field presentation từ JOIN → `@Transient`; không vẽ association (FK giữ dạng id), trừ khi cần.
- JOIN trong SQL cũ → `@Query` (JPQL; native nơi JPQL bất khả — reporting) giữ đúng shape cũ, chống N+1.
- **Bỏ fallback schema-cũ** (mapper `try/catch-ignored` cột optional, VD `reserveStock` legacy `stock` decrement): Flyway migrate trước khi code chạy (single instance) nên columns luôn tồn tại. Hệ quả chấp nhận: code mới không chạy trên schema cũ.
- Test DB `petshop_test` trên MySQL local, Flyway migrate khi test boot, repo test `@Transactional` rollback. Từ Wave A suite bắt buộc cần MySQL (cấm skip-gracefully; cập nhật README/dev docs ở Wave A).

## 4. Transaction + service rules (đã duyệt §3)

- Repository call không bọc `@Transactional` mặc định (giữ semantics 1-call-1-tx như auto-commit hiện tại); chỉ service method chứa block manual-tx cũ mới `@Transactional` (REQUIRED).
- Chân dung 1 migration: DAO method → repository (`derived query` nếu đủ else `@Query`); `new XxxDAO()` → inject repository; service method có manual-tx → `@Transactional`; mock-SQL test → repo test MySQL.
- Service refactor chỉ trong method bị migration chạm tới, có suite xanh bao phủ trước-sau, ghi report; cấm drive-by.
- 1 DAO = 1 commit (entity+repo+call-site+test thay thế), xanh mới tiếp; xoá file DAO cũ, không adapter chết.

## 5. Wave list (đã duyệt §2)

- **A — infra + mẫu:** starter-data-jpa, JPA config, test DB + Flyway test, README env; migrate `RememberTokenDAO`, `SecurityEventDAO` (1 consumer mỗi con, không tx).
- **B — lá/CRUD (~15):** `AiChatMessageDAO`, `AiChatSessionDAO`, `AiSupportSettingDAO`, `BankWebhookEventDAO`, `CertificateDAO`, `CouponDao`, `CustomerSupportKnowledgeDAO`, `NotificationDAO`, `OrderLogDAO`, `OrderSignDAO`, `OrderSignatureDAO`, `OrderStatusHistoryDAO`, `PetTypeDAO`, `ReviewDAO`, `AdminActionLogDAO` (plan có thể chẻ B1/B2).
- **C — mid:** `UserDAO`, `ProductDAO`, `CartDAO` (kèm thay mock-test `reserveStock`).
- **D — tx-heavy (7 DAO):** `OrderDAO` cuối cùng, `PaymentTransactionDAO`, `InventoryBatchDAO`, `WishlistDAO`, `AddressDao`, `PromotionDAO`, `SalesSummaryDAO` + `@Transactional` services.
- **E — reporting + teardown:** `ReportDAO` + xoá `DBContext`/fallback/`shutdown` + `DBProperties` (nếu `DataSourceConfig` hết dùng — plan xác minh, fallback giữ facade) + gates cuối.
- Thứ tự trong wave: ít consumer + không bị DAO khác `new` trước.

## 6. Test strategy + gates (đã duyệt §4)

- Thay mock-test bằng repo test MySQL 1:1+ (data thật qua Flyway baseline, rollback mỗi test); TOTAL không hạ so với baseline wave (451).
- Test `mockConstruction(DAO.*)` ở service/web tests → viết lại mock repository; việc của từng wave.
- Gate mỗi DAO commit: compile + suite xanh + `grep "new <DaoVừaXoá>("` = 0.
- Gate mỗi wave: `DBContext.getConnection` count giảm (plan track), bootWar build được.
- Gate cuối: `grep DBContext src/main` = 0; `src/main/java/com/petshop/dao/` chỉ còn `DBProperties` nếu giữ; `grep "new .*DAO(" src/main` = 0; suite ≥451 xanh; bootWar smoke + E2E login→cart→COD; `validate` pass.

## 7. Non-goals & follow-up

- Không đổi schema (Flyway sở hữu; không migration mới trừ khi entity phát hiện drift — khi đó là bug mapping, không phải đổi schema).
- Không đổi hành vi API/demo; không đổi prompt/model AI; không đụng Thymeleaf/Security (P3/P4).
- Follow-up đã biết: stale-session sau đổi package entity (chấp nhận, restart deploy); test-suite bắt buộc MySQL từ Wave A (môi trường CI sau này phải có MySQL).

## 8. Rủi ro

| Rủi ro | Giảm thiểu |
|---|---|
| Mapping sai cột (camelCase/snake_case lẫn lộn) | `@Column` tường minh + `validate` fail-fast ở test boot |
| N+1 thay JOIN cũ (VD admin order list) | Rule `@Query` giữ shape, review soi derived-query trên quan hệ list |
| Tx semantics đổi (manual-tx → declarative) | `@Transactional` chỉ đúng method cũ, suite (gồm concurrency test) bao phủ |
| Mock-test bị xoá làm mất coverage SQL-shape | Thay 1:1+ bằng repo test assert data thật, mạnh hơn assert chuỗi |
| Suite phụ thuộc MySQL local | Ghi README Wave A, fail rõ khi thiếu DB, CI bổ sung service MySQL |
| Service refactor lan man (Q3 mở) | Bound method-chạm-migration + suite trước-sau + ghi report; reviewer soi |
