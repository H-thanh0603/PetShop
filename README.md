PetShop là đồ án web e-commerce bán sản phẩm cho thú cưng, tập trung vào storefront, giỏ hàng, checkout, đơn hàng và trang quản trị.

## Phạm vi hiện tại

- Bán sản phẩm thú cưng
- Quản lý giỏ hàng, checkout, đơn hàng
- Quản trị sản phẩm, người dùng, báo cáo
- Đăng nhập thường và social login
- Thanh toán `COD`, `MoMo` demo, `Chuyển khoản ngân hàng` theo luồng chờ đối soát

## Yêu cầu môi trường

- JDK 21
- Gradle Wrapper đi kèm project
- MySQL 8+
- Tomcat 10.x

## Chuẩn bị cấu hình

### 1. Cấu hình database

Ứng dụng ưu tiên đọc mật khẩu DB theo thứ tự:

1. system property `petshop.db.password`
2. environment variable `PETSHOP_DB_PASSWORD`
3. environment variable `MYSQL_PASSWORD`
4. `src/main/resources/db.properties`

Ví dụ trên Windows:

```bat
set PETSHOP_DB_PASSWORD=your_mysql_password
```

### 2. Cấu hình ứng dụng chung

Copy file mẫu:

```bat
copy src\main\resources\app.properties.example src\main\resources\app.properties
```

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
- `payment.bank.verification-mode`
- `payment.momo.mode`

Thứ tự ưu tiên config hiện tại là:

1. System property
2. Environment variable
3. `app.properties`
4. File legacy như `db.properties`, `secrets.properties`, `ship.properties`

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

`Start.bat` đã được sửa để ít phụ thuộc máy cá nhân hơn:

- lấy `PROJECT_ROOT` từ chính thư mục chứa script
- hỗ trợ `PETSHOP_TOMCAT_HOME`
- hỗ trợ `PETSHOP_BASE_URL`
- hỗ trợ `PETSHOP_CONTEXT_PATH`
- hỗ trợ `PETSHOP_SKIP_BUILD=true`
- hỗ trợ `PETSHOP_OPEN_BROWSER=false`
- dùng `shutdown.bat` của Tomcat thay vì `taskkill java.exe`

Ví dụ:

```bat
set PETSHOP_TOMCAT_HOME=E:\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49
set PETSHOP_CONTEXT_PATH=/PetShop
set PETSHOP_BASE_URL=http://localhost:8080/PetShop/home
set PETSHOP_DB_PASSWORD=your_mysql_password
Start.bat
```

Nếu chỉ muốn deploy lại WAR mà không build lại:

```bat
set PETSHOP_SKIP_BUILD=true
Start.bat
```

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

1. Cài JDK, MySQL, Tomcat 10
2. Import `sql/SETUP_ALL.sql`
3. Tạo `app.properties` từ file mẫu
4. Set `PETSHOP_DB_PASSWORD` và `PETSHOP_TOMCAT_HOME`
5. Chạy `Start.bat`
6. Mở `http://localhost:8080/PetShop/home`

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
