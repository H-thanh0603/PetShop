PetShop là đồ án web **e-commerce bán sản phẩm cho thú cưng**.

## Phạm vi hiện tại

- Bán sản phẩm thú cưng
- Quản lý giỏ hàng, checkout, đơn hàng
- Quản trị sản phẩm, người dùng, thống kê
- Đăng nhập thường và social login

Các luồng dịch vụ/đặt hẹn/cộng đồng cũ không còn là phần chức năng storefront chính.

## Cấu hình DB

Để chạy ứng dụng mà không cần ghi mật khẩu vào code, hãy:

1. Mở `Start.bat`
2. Nhập mật khẩu MySQL khi được hỏi, hoặc đặt trước biến môi trường:

```bat
set PETSHOP_DB_PASSWORD=your_mysql_password
```

Ứng dụng cũng hỗ trợ biến `MYSQL_PASSWORD` và system property `petshop.db.password`.

## Cấu hình social login

Đăng nhập Google/Facebook chỉ hoạt động khi bạn cung cấp OAuth credentials.

### Cách 1: tạo `secrets.properties`

Copy file mẫu:

```bat
copy src\main\resources\secrets.properties.example src\main\resources\secrets.properties
```

Sau đó điền:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `facebook_client_id`
- `facebook_client_secret`

### Cách 2: dùng environment variables / system properties

Ứng dụng cũng đọc trực tiếp từ:

```bat
set GOOGLE_CLIENT_ID=...
set GOOGLE_CLIENT_SECRET=...
set facebook_client_id=...
set facebook_client_secret=...
```

### Redirect URI cần khai báo trong Google/Facebook console

Nếu chạy local với context path hiện tại:

- `http://localhost:8080/PetShop/LoginByGoogleServlet`
- `http://localhost:8080/PetShop/LoginByFacebookServlet`

Redirect URI trong provider console phải khớp tuyệt đối với URL thật đang chạy.
