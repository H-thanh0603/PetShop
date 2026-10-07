# P2 Spring Data JPA — Final Report (2026-10-07)

Kết thúc Wave E. Trạng thái: **DONE với 2 mục ghi nhận có chủ đích** (không phải failure).

## Final gates

| Gate | Kết quả |
|---|---|
| Suite | TOTAL=539, 0 fail, 0 error, 1 Docker skip (Testcontainers, daemon không chạy trên máy build) — ≥ 451 ✓ |
| `new XxxDAO(` trong src/main | Chỉ còn ProductDAO/PromotionDAO nội bộ (xem BLOCKED 1) |
| bootWar | BUILD SUCCESSFUL |
| Smoke boot (:8096) | `/actuator/health` → `{"status":"UP"}` |
| Suite cần MySQL local | `PETSHOP_DB_PASSWORD` (env) — `petshop_test` migrate bằng cùng Flyway locations |

## Task 9 — OrderDAO hub (44 methods)

- `OrderRepository` + `OrderRepositoryCustom/Impl`; JDBC `OrderDAO` đã xoá.
- **Write methods chạy trong `TransactionTemplate` thay vì `@Transactional`**: contract
  boolean-false-on-error đòi hỏi catch `DataAccessException` giữa chừng phải rollback và trả
  `false`. Với boundary declarative, proxy của repository bên trong (ví dụ `insertHistory`)
  đã kịp đánh dấu transaction chung rollback-only → boundary commit ném
  `UnexpectedRollbackException` thay vì `false` (chứng minh:
  `OrderTxAtomicityIT.failedStatusUpdateLeavesStatusUnchanged`). Template rollback ngay khi
  exception thoát ra — port trung thành của `conn.rollback()` thủ công trong DAO cũ. Business
  failure ném `TxFailedException extends DataAccessException` để service bắt giữ được contract.
- **Wiring fix bắt buộc để full-context boot**: `@SpringBootApplication(scanBasePackages =
  {"com.petshop", "services"})` + stereotype cho 9 service — controllers constructor-inject
  các service ngoài component scan (đây là lý do `OrderTxAtomicityIT` chưa từng xanh trước đó).

## Task 10 — ReportDAO (15 methods)

- `ReportRepository` (native @Query) + nested projection interfaces; getter names giữ nguyên
  tên Map key cũ nên JSP EL và JSON chart tự xây render không đổi.
- `StoredNotificationView.getCreatedAt` là `java.util.Date` (Hibernate 7 trả `LocalDateTime`
  cho DATETIME; JSTL `fmt:formatDate` cần `Date` — Spring Data có converter này, không có cho
  `Timestamp`).
- Recent reviews tái dùng `ReviewRepository.fillNames`; low-stock chuyển thành
  `ProductRepository.findLowStockProducts` (bỏ LEFT JOIN reviews vô dụng — aggregate không đọc).

## BLOCKED 1 — Teardown `DBContext` (giữ lại có chủ đích)

Plan điều kiện: grep `DBContext|DBProperties` phải = 0. Thực tế còn ~90 refs ngoài 2 file đó:

- `ProductDAO` (46) / `PromotionDAO` (9): các method JDBC chưa tới lượt swap — còn consumer
  thật: `ProductPricingService`, `InventoryService`, `PetShopMerchantBackend`
  (`new ProductDAO()`), `PromotionDAO` tự gọi `new ProductDAO()`.
- AI-memory JDBC: `MerchantChangeDAO`, `DbMemoryStore`, `AuditLog` — đã là Spring beans nhưng
  vẫn đọc/ghi qua `DBContext.getConnection()`.
- 3 controller (`AdminReadController` categories-JDBC, `ShopApiController`, `GhnWebhookController`).
- `LegacySchemaMigrator` (Flyway V1) + `DataSourceConfig`.

→ Theo luật plan ("a DAO was missed; do not delete"): **DBContext giữ lại làm facade** cho tới
khi một wave P3 migrate nốt 5 điểm còn lại. Không có hành vi mới nào phụ thuộc nó ngoài các
điểm trên.

## BLOCKED 2 (accepted) — `DBProperties` giữ lại làm facade

`DataSourceConfig` đọc config nguồn chân lý qua `DBProperties` — đúng kịch bản plan dự liệu
("keep facade if DataSourceConfig uses it"). `dbContextBinder` bean giữ nguyên theo đó.

## Việc tiếp theo (P3 đề xuất)

1. Migrate `ProductPricingService`/`InventoryService`/`PetShopMerchantBackend` khỏi
   `ProductDAO`/`PromotionDAO` JDBC → rồi xoá 2 DAO + `DBContext`.
2. AI-memory JDBC (3 file) sang JPA repositories.
3. Categories-JDBC trong `AdminReadController` → `ProductRepository` queries.
4. Bật Docker daemon ở máy build để Testcontainers concurrency test chạy thay vì skip.
