# P1 — Dồn package legacy vào `com.petshop.*` (design spec)

Ngày: 2026-09-27 · Nhánh thực thi dự kiến: `refactor/p1-packages` · Phase roadmap: P0 ✅ → **P1** → P2 (Spring Data JPA) → P3 (Thymeleaf) → P4 (Security)

## 1. Hiểu chung (đã chốt với user)

- **Mục đích:** xoá 6 package top-level legacy (`DAO`, `Model`, `controller`, `Util`, `Context`, `Constant` — 101 file main) để toàn bộ code nằm dưới `com.petshop.*`, chuẩn bị cho P2 thay DAO bằng Spring Data JPA trên layout sạch.
- **Giả định tách bạch:** những gì user nói (phạm vi 6 package đúng spec P0; test mirror theo; pure move) vs tôi đề xuất (approach A 6 commit; mapping `web.oauth`/`web.filter`; gates).
- **Success:** 6 commit trên `main`, gate §5 xanh, zero behaviour change.

## 2. Phạm vi

**Trong scope (101 file main + 23 file test move + 35 file test sửa import tại chỗ):**

| Package cũ | File | Package mới |
|---|---|---|
| `Constant` | 1 (`IConstant`) | `com.petshop.constant` |
| `Context` | 2 (`DBContext`, `LegacySchemaMigrator`) | `com.petshop.context` |
| `Util` | 21 | `com.petshop.util` |
| `Model` | 39 (+`FbAccount`, `GgAccount`) | `com.petshop.model` (giữ subpackage) |
| `DAO` | 29 | `com.petshop.dao` (giữ tên `*DAO`/`*Dao` lẫn lộn — P2 chuẩn hoá) |
| `controller.FaceBook`, `controller.Google` | 2 (`FaceBookLogin`, `GoogleLogin`) | `com.petshop.web.oauth` |
| `controller.filter` | 7 filter | `com.petshop.web.filter` |

**Ngoài scope:** `services` (52 file), `db` (migration), `com.petshop.*` hiện tại. Test move mirror đúng layout main (`DAO`→`com/petshop/dao`, `Model`→`com/petshop/model`, `Util`→`com/petshop/util`, `controller/filter`→`com/petshop/web/filter` — khớp `src/test/java/com/petshop/web` hiện tại). Test ở `audit`/`services`/`com` (35 file) chỉ sửa import tại chỗ, không move.

## 3. Cơ chế move + rules (đã duyệt §2)

1. `git mv` giữ history (file chỉ đổi 1 dòng `package` → rename detect ~100%).
2. Sửa import cơ học theo mapping §2; class cùng package mới thì drop import thừa.
3. **Rule package-private gãy:** member package-private bị khác package sau move → nới lên `public`, ghi vào report commit. Không đổi gì khác.
4. **Không clash:** đã check — 9 file `controller/*` không trùng tên với `com.petshop.web`. Nếu lúc làm phát hiện clash: escalate, không đoán.
5. **Cấm:** sửa logic, đổi tên class/method/field, sửa annotation/string, drive-by cleanup.
6. Thứ tự commit leaf-first: `constant` → `context` → `util` → `model` → `dao` → `controller`. Mỗi commit = package move + test mirror của nó + sửa import tại chỗ ở mọi consumer (main + test) của package đó — commit nào cũng tự compile được.

## 4. Ràng buộc kỹ thuật (đã xác minh 2026-09-27)

- Không có string-based class reference (`Class.forName`/`loadClass` = 0 hit), không `@ComponentScan` tường minh — `@SpringBootApplication` ở `com.petshop` scan mặc định `com.petshop.*`; filter đăng ký tường minh ở `WebRegistrationConfig` (chỉ đổi import).
- Không có top-level class package-private trong 6 package (`grep "^class \|^interface "` = 0) — rủi ro package-private chỉ còn ở member level, lộ ở compile.
- Baseline: Spring Boot 4.1.1, suite **451 test / PROPERTY 160 / 1 skip Docker** (sau P0, commit `f30bd96`).

## 5. Gates (đã duyệt §3)

**Mỗi commit:** `compileJava`+`compileTestJava` xanh; `./gradlew clean test` → TOTAL=451, 0 fail/error, 1 skip, PROPERTY=160; diff chỉ rename + import.

**Cuối P1:** `ls src/main/java/` chỉ còn `com services db`; `grep -rn "^package \(DAO\|Model\|controller\|Util\|Context\|Constant\)\." src/` rỗng; `grep -rn "import \(DAO\|Model\|controller\|Util\|Context\|Constant\)\." src/` rỗng; test mirror đúng mapping; `bootWar` build + smoke health UP một lần cuối.

## 6. Non-goals & follow-up

- Không refactor `DBContext` tĩnh, không chuẩn hoá tên DAO, không gộp class — tất cả là việc P2.
- Không đụng `services`, `db/migration`.
- Follow-up đã biết (không làm trong P1): javadoc `PetShopApplication` còn ghi "deploy plain WAR to Tomcat 10.1" (sai từ P0); `CheckoutConcurrencyTest` pin `mysql:8.0` (giữ theo test-freeze P0).

## 7. Rủi ro

| Rủi ro | Giảm thiểu |
|---|---|
| Member package-private gãy compile sau tách package | Rule nới `public` tối thiểu + ghi log (§3.3); lộ ngay ở commit nhỏ (approach A) |
| Import sót (string/annotation) | Đã xác minh không tồn tại (§4); gate grep cuối P1 |
| Test mirror sai mapping | Mỗi commit bundle main+test cùng package, gate 451 giữ nguyên số case |
| Clash tên lúc move | Đã check trước (§3.4); escalate nếu phát sinh |
