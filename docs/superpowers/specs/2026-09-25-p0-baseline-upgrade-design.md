# P0 — Hạ tầng & phiên bản (Baseline Upgrade)

- Ngày: 2026-09-25
- Trạng thái: đã được đối tác duyệt (design approved)
- Thuộc chuỗi hiện đại hoá PetShop: **P0 → P1 → P2 → P3 → P4** (spec riêng cho từng phase)

## 1. Bối cảnh

PetShop là đồ án e-commerce thú cưng, **vừa nộp đồ án vừa triển khai thật**. Hai ràng buộc phải giữ xuyên suốt:

1. **Demo luôn phải chạy được** — không được vỡ demo trước hội đồng.
2. **Version và bảo mật phải còn được hỗ trợ** — không chấp nhận dependency đã EOL.

Hiện trạng (đo ngày 2026-09-25):

- Spring Boot **3.5.16** — OSS support hết hạn **30/06/2026** (không còn patch bảo mật).
- Docker `mysql:8.0` — EOL **04/2026** (Sustaining Support). Local dev đã là MySQL 8.4.11.
- 5 file cấu hình rời: `app.properties` (101 dòng), `db.properties`, `secrets.properties`, `vnpay.properties`, `ship.properties` — song song với 3 file `application*.yml`.
- **Gson** (30 file, ~179 chỗ) song song với **Jackson** (đã có sẵn qua starter, chưa dùng chỗ nào).
- Đóng gói kép: task `war` (ROOT.war cho Tomcat 10 ngoài, legacy) + `bootWar` (petshop-boot.war).
- Baseline kiểm thử: **78 file test, 437 test case (278 `@Test` JUnit + 160 `@Property` jqwik), 0 fail**. Không có `@SpringBootTest`, `@MockBean`, `@WebMvcTest`, `TestRestTemplate` → di chuyển test sang Boot 4 gần như bằng 0.
- Không dùng `RestTemplateBuilder`, `WebClient.Builder`, `@ConfigurationProperties`, Jackson customizer. Không còn `javax.*` (0 chỗ).

## 2. Mục tiêu phase P0

Nền tảng để 4 phase sau (dồn package, JPA, Thymeleaf, Spring Security) xây dựng lên trên một build xanh và một bộ version còn được hỗ trợ.

### Trong phạm vi

| # | Hạng mục |
|---|---|
| 1 | Spring Boot **3.5.16 → 4.1.1** (OSS đến 31/07/2027), kể cả đổi tên starter |
| 2 | Xoá cảnh báo deprecated trên 3.5.16 trước khi nâng (bước 0) |
| 3 | MySQL image `8.0` → **`8.4` LTS** ở `docker-compose.dev.yml` và `docker-compose.prod.yml` |
| 4 | Gộp 5 file `.properties` vào `application.yml` + env var, **giữ nguyên facade tĩnh** và thứ tự ưu tiên đọc |
| 5 | Thay **Gson → Jackson 3** (`tools.jackson`) ở toàn bộ 30 file main |
| 6 | Bỏ đóng gói WAR legacy (`war`/`ROOT.war`, `providedRuntime tomcat`, `providedCompile servlet-api`), giữ `bootWar` |
| 7 | Nâng jqwik/test dependency cho tương thích JUnit platform của Boot 4 |
| 8 | Cập nhật README / Start.bat / tài liệu môi trường theo version mới |

### Ngoài phạm vi (thuộc phase sau)

- Spring Data JPA (P2), Thymeleaf (P3), Spring Security (P4), dồn package legacy vào `com.petshop.*` (P1).
- Bất kỳ thay đổi hành vi nào với người dùng: giao diện, luồng mua, API, schema DB.

## 3. Các quyết định thiết kế

| Quyết định | Lựa chọn | Lý do / phương án thay thế đã bỏ |
|---|---|---|
| Hướng nâng Boot | **A: nhảy thẳng 3.5.16 → 4.1.1** trên 1 nhánh | B (4.0.8 rồi 4.1.1) bị bỏ vì 4.0 hết OSS 31/12/2026, làm 2 lần công. C (không nâng) bị bỏ vì 3.5 đã EOL. |
| Trình tự JSON | Làm **sau** khi lên Boot 4 | Boot 4 dùng Jackson 3 (`tools.jackson`); làm Gson→Jackson trước là phải migrate 2 lần. |
| Gộp config | **Giữ facade tĩnh**, chỉ đổi backing store | ~100+ chỗ gọi `SecretConfig.x()`, `AppConfig.y()`, `DBProperties.z()` không phải sửa; giảm rủi ro sai precedence. |
| WAR | Bỏ task `war`/ROOT.war, **giữ `bootWar`** | JSP chỉ chạy được ở WAR với embedded Tomcat — phải giữ tới P3 (chuyển Thymeleaf xong mới bỏ hẳn). |
| Mỗi bước | 1 commit riêng, test xanh mới sang bước sau | Rollback được từng bước khi demo cần gấp. |

## 4. Kế hoạch thực thi

Mỗi bước là một commit riêng trên nhánh `refactor/p0-baseline`. **Cổng chuyển bước: `./gradlew clean test` phải xanh (≥437 test).**

### Bước 0 — Dọn deprecated trên 3.5.16
- Bật `-Xlint:deprecation` trong `tasks.withType(JavaCompile)` (tạm thời, hoặc giữ vĩnh viễn).
- Sửa mọi cảnh báo deprecated của Spring Boot 3.x, vì Boot 4 xoá toàn bộ API deprecated trong 3.x.
- Chạy `./gradlew clean test`.

### Bước 1 — Nâng Spring Boot 4.1.1
- `build.gradle`: plugin `org.springframework.boot` `3.5.16` → `4.1.1`, BOM `spring-boot-dependencies:4.1.1`.
- Đổi tên starter:
  - `spring-boot-starter-web` → `spring-boot-starter-webmvc`
  - `org.flywaydb:flyway-core` + `flyway-mysql` → `spring-boot-starter-flyway` (Flyway giờ có starter riêng; không khai báo flyway version tay)
  - Jackson: **không khai báo thêm** — `webmvc` starter đã kéo Jackson 3; chỉ thêm `spring-boot-starter-jackson` nếu compile báo thiếu
- Thêm `org.springframework.boot:spring-boot-properties-migrator` (scope runtime) để bắt property đổi tên khi start; **gỡ bỏ ở bước cuối cùng**.
- Thứ tự: **compile trước, đừng chạy app**; đọc từng lỗi, sửa import (các class `org.springframework.boot.web.*` hay đổi package).
- Dự phòng nếu lỗi import quá nhiều: tạm dùng `spring-boot-starter-classic` + `spring-boot-starter-test-classic` làm cầu, chuyển dần sang starter chuẩn rồi gỡ classic.
- Chạy `./gradlew clean test`.

### Bước 2 — Test dependency
- Đối chiếu jqwik (đang pin `1.10.1`) với JUnit platform của Boot 4; nâng version jqwik nếu cần.
- Mockito/BouncyCastle/underscore do BOM quản lý — không pin tay.
- Xác nhận đủ 278 `@Test` + 160 `@Property` chạy.

### Bước 3 — MySQL 8.4
- `docker-compose.dev.yml` và `docker-compose.prod.yml`: `image: mysql:8.0` → `mysql:8.4`.
- Healthcheck giữ nguyên (script `healthcheck.sh` có ở 8.4); giữ `--character-set-server=utf8mb4`.
- Ghi chú backup volume trước khi hạ tầng dev đổi image: `mysqldump` hoặc `docker run … mysql-dump`.
- README: yêu cầu môi trường `MySQL 8+` → `MySQL 8.4 LTS`.

### Bước 4 — Gộp cấu hình
- Đưa toàn bộ khoá trong `app.properties`, `db.properties`, `vnpay.properties`, `ship.properties`, `secrets.properties` vào **`application.yml`** (giá trị không nhạy cảm) hoặc **env var** (secret); `application-dev.yml`/`application-prod.yml` chỉ giữ phần khác biệt giữa 2 môi trường. Bản `*.properties.example` giữ lại làm tài liệu mẫu.
- **Giữ nguyên tên biến môi trường** mà docker-compose đang set (`DB_PASSWORD`, `PETSHOP_DB_PASSWORD`, `MYSQL_PASSWORD`, `VNPAY_*`, `GOOGLE_CLIENT_ID`…) — compose không được sửa theo.
- Facade (`SecretConfig`, `AppConfig`, `DBProperties`, `VnpayConfig`, `ShippingConfig`, `AiConfig`, `SocialAuthUtil`) đọc từ Spring `Environment` thay vì `Properties.load`; một bean gán `Environment` vào holder tĩnh lúc startup (`@PostConstruct`).
- Thứ tự ưu tiên **không đổi**: system property → env var → file → legacy.
- Verify: app khởi động được, `/actuator/health` UP, không còn secret nào rơi vào log.

### Bước 5 — Gson → Jackson 3
- 30 file main, ~179 chỗ. API khác hẳn (Gson object model → `JsonNode`), nên **lập test trước** cho các đường dễ vỡ:
  - `VnpayIpnController` (webhook VNPAY, so chữ ký + parse payload)
  - `GhnWebhookController` (webhook vận chuyển)
  - Parser JSON của AI provider (`OpenAiCompatibleProvider`, `AnthropicProvider`, `CommerceTools`)
- Sau khi có test đỏ/chứng minh hành vi hiện tại: thay `Gson`/`JsonParser`/`JsonObject` bằng `tools.jackson.*`, xoá `com.google.code.gson:gson` khỏi `build.gradle`.
- Cấm `com.google.gson` quay lại: grep kiểm tra ở bước verify.

### Bước 6 — Bỏ WAR legacy & cập nhật tài liệu
- `build.gradle`: tắt/bỏ task `war` (ROOT.war), bỏ `providedRuntime 'spring-boot-starter-tomcat'` và `providedCompile 'jakarta.servlet:jakarta.servlet-api'` — chúng phục vụ đường deploy Tomcat 10 ngoài.
- Giữ `bootWar` → `petshop-boot.war` (JSP cần WAR).
- Cập nhật `README.md`, `Start.bat` (bỏ bước chạy Tomcat 10 riêng), `docs/commerce-deployment.md` nếu có nhắc Tomcat 10/WAR thường.
- Đảm bảo `Dockerfile` vẫn build (`gradle bootWar`) và entrypoint không đổi.

## 5. Rủi ro & giảm thiểu

| Rủi ro | Ảnh hưởng | Giảm thiểu |
|---|---|---|
| Boot 4 đổi package class nội bộ | Build hỏng | Compile-first; classic starter bridge; properties-migrator |
| JSON đổi hành vi khi sang Jackson 3 | Webhook thanh toán/AI sai | Test trước ở bước 5 cho 3 nhóm code dễ vỡ |
| Gộp config sai precedence | App không đọc được secret → start fail | Giữ facade; smoke test health + login sau mỗi lần gộp |
| jqwik không khớp JUnit platform mới | 160 test `@Property` hỏng | Nâng jqwik ở bước 2; nếu kẹt, cô lập bước riêng |
| Docker daemon tắt trong môi trường dev | 1 test Testcontainers không chạy | Chấp nhận (đang skip); smoke test dùng MySQL local 8.4.11 |
| Hạ tầng dev đổi image MySQL 8.4 | Lỗi upgrade schema volume cũ | Backup volume trước; 8.0→8.4 upgrade in-place được hỗ trợ |

## 6. Tiêu chí hoàn thành

- [ ] `./gradlew clean test` xanh với **≥437 test case** (không hạ số test).
- [ ] App khởi động được, `/actuator/health` trả `UP`.
- [ ] Chạy tay được luồng: đăng nhập → thêm giỏ hàng → checkout 1 đơn (COD) → xem đơn ở trang admin.
- [ ] Không còn import `com.google.gson` trong `src/main/java`.
- [ ] Không còn file `.properties` cấu hình nào ngoài `*.example`.
- [ ] Không còn task `war`/ROOT.war; `bootWar` vẫn sinh `petshop-boot.war`.
- [ ] `docker-compose.dev.yml` và `docker-compose.prod.yml` dùng `mysql:8.4`.
- [ ] README ghi rõ: Spring Boot 4.1.1, MySQL 8.4 LTS, JDK 21, không còn yêu cầu Tomcat 10 ngoài.
- [ ] Mỗi bước một commit riêng trên `refactor/p0-baseline`.

## 7. Bối cảnh các phase tiếp theo (ngoài spec này)

| Phase | Nội dung | Phụ thuộc |
|---|---|---|
| P1 | Dồn `DAO/Model/controller/Util/Context/Constant` vào `com.petshop.*` | P0 |
| P2 | Spring Data JPA thay 30 DAO, bỏ `DBContext` tĩnh | P1 |
| P3 | 57 JSP → Thymeleaf, bỏ JSP/JSTL/jasper | P2 |
| P4 | Spring Security thay filter tự chế, OAuth2 Google/Facebook | P2 + P3 |

Mỗi phase sẽ có spec và kế hoạch riêng, gate là `./gradlew clean test` xanh và demo không vỡ.
