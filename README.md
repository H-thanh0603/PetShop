PetShop là đồ án web e-commerce bán sản phẩm cho thú cưng, tập trung vào storefront, giỏ hàng, checkout, đơn hàng và trang quản trị.

## Phạm vi hiện tại

- Bán sản phẩm thú cưng
- Quản lý giỏ hàng, checkout, đơn hàng
- Quản trị sản phẩm, người dùng, báo cáo
- Đăng nhập thường và social login
- Thanh toán `COD`, `VNPAY` demo, `Chuyển khoản ngân hàng` theo luồng chờ đối soát

## Yêu cầu môi trường

- JDK 21
- Gradle Wrapper đi kèm project
- Spring Boot 4.1.1 (Gradle plugin + BOM `spring-boot-dependencies`)
- MySQL 8.4 LTS
- MySQL bắt buộc chạy local cho test suite từ P2 (DB `petshop_test`, user `petshop`, password qua `PETSHOP_DB_PASSWORD`); thiếu DB suite fail rõ, không skip.
- Hạ dev từ `mysql:8.0` sang `mysql:8.4` (2026-09): backup dữ liệu trước khi `docker compose up` lần đầu:
  `docker exec petshop-mysql-dev mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" petvaccine > backup-8.4.sql`

## Chuẩn bị cấu hình

### 1. Cấu hình database

Ứng dụng ưu tiên đọc mật khẩu DB theo thứ tự:

1. system property `petshop.db.password`
2. environment variable `PETSHOP_DB_PASSWORD`
3. environment variable `MYSQL_PASSWORD`
4. `db.password` trong `src/main/resources/application.yml`

Ví dụ trên Windows:

```bat
set PETSHOP_DB_PASSWORD=your_mysql_password
```

### 2. Cấu hình ứng dụng chung

Toàn bộ cấu hình thường nằm trong `src/main/resources/application.yml`
(hợp nhất từ các file cấu hình cũ). File mẫu `app.properties.example` chỉ còn
giữ làm tài liệu.

Các khóa quan trọng nên điền:

- `app.base-url`
- `app.context-path`
- `api.provinces.base-url`
- `payment.bank.id`
- `payment.bank.account-number`
- `payment.bank.account-name`
- `payment.bank.display-name`
- `payment.bank.transfer-prefix`
- `payment.bank.currency`
- `payment.bank.webhook-secret`

Thứ tự ưu tiên config hiện tại là:

1. System property
2. Environment variable
3. `application.yml`
4. `secrets.properties` (gitignored, local)

### 3. Cấu hình social login

Nếu cần demo Google/Facebook login, copy file mẫu:

```bat
copy src\main\resources\secrets.properties.example src\main\resources\secrets.properties
```

Điền các khóa:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `facebook_client_id`
- `facebook_client_secret`
- `payment.bank.webhook-secret`

Hoặc dùng environment variables tương ứng.

Redirect URI local mặc định:

- `http://localhost:8080/PetShop/LoginByGoogleServlet`
- `http://localhost:8080/PetShop/LoginByFacebookServlet`

## Chuẩn bị database

Thư mục `sql/` đã được tách lại để dễ setup trên máy khác.

Cách nhanh nhất:

1. Tạo database rỗng cho PetShop
2. Chạy file `sql/SETUP_ALL.sql`
3. Nếu cần tài khoản demo, chạy thêm `sql/demo_accounts.sql`

Nếu bạn đã có schema cũ, vẫn cần đảm bảo migration `sql/14_payment_transactions.sql` đã được áp dụng hoặc để ứng dụng tự tạo bảng `payment_transactions` khi khởi động.

## Chạy local bằng Start.bat

`Start.bat` build `petshop-boot.war` rồi chạy bằng `java -jar` (embedded Tomcat, không cần cài Tomcat ngoài):

- lấy `PROJECT_ROOT` từ chính thư mục chứa script
- hỗ trợ `PETSHOP_URL` (mặc định `http://localhost:8080/home`)
- hỗ trợ `PETSHOP_OPEN_BROWSER=false`

Ví dụ:

```bat
set PETSHOP_DB_PASSWORD=your_mysql_password
Start.bat
```

### Chạy ứng dụng

```bat
gradlew bootWar
java -jar build\libs\petshop-boot.war
```

Ứng dụng chạy embedded Tomcat (không cần cài Tomcat ngoài). Docker: `docker compose -f docker-compose.dev.yml up --build`.

## Luồng thanh toán hiện tại

### COD

- tạo đơn hàng ngay
- trạng thái thanh toán chưa thanh toán

### MoMo

- vẫn đang là chế độ demo trên UI/backend
- đã đi qua payment transaction chung để sau này thay bằng callback thật dễ hơn

### Chuyển khoản ngân hàng

- tạo đơn hàng thành công
- tạo `payment_transactions`
- sinh mã chuyển khoản riêng cho từng đơn
- đơn không bị đánh dấu đã thanh toán ngay
- admin phải xác nhận đối soát ở trang quản trị

## Gợi ý demo trên máy khác

1. Cài JDK 21, MySQL 8.4 (hoặc Docker)
2. Import `sql/SETUP_ALL.sql`
3. Cấu hình `src/main/resources/application.yml` (xem mục "Chuẩn bị cấu hình")
4. Set `PETSHOP_DB_PASSWORD`
5. Chạy `Start.bat`
6. Mở `http://localhost:8080/home`

## Ghi chú cho admin

Trang `Admin > Đơn hàng` hiện đã có:

- badge trạng thái đối soát thanh toán
- cảnh báo số đơn đang chờ đối soát
- thao tác duyệt thanh toán chuyển khoản trực tiếp từ danh sách hoặc trang chi tiết đơn
## Operations Handbook

Tài liệu vận hành/audit thực chiến cho đồ án nằm tại [docs/petshop-operating-handbook.md](docs/petshop-operating-handbook.md). File này tổng hợp:

- cách hệ thống phản ứng khi số user tăng lên
- tối ưu hơn 1.000 sản phẩm
- chống spam, lock account, session timeout
- timeout thanh toán chuyển khoản
- mô hình kho theo lô, cận hạn, tồn lâu, đề xuất nhập hàng
- backup, audit log và các góc khuất vận hành khác
