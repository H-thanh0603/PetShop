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
