# PetShop Operating Handbook

## Executive Summary

Tài liệu này là handbook vận hành thực chiến cho đồ án `PetShop`, nhưng vẫn viết theo cách dễ dùng khi bảo vệ. Mục tiêu không phải biến dự án thành microservices ngay, mà là trả lời rõ:

- Nếu số tài khoản tăng lên khoảng `5.000 user`, hệ thống nghẽn ở đâu trước.
- Nếu catalog vượt `1.000 sản phẩm`, làm sao list/search vẫn nhanh.
- Nếu người dùng spam login, spam checkout, spam review thì chặn như thế nào.
- Nếu user bỏ dở trong `30 phút`, session cần xử lý ra sao.
- Thanh toán chuyển khoản nên chờ bao lâu, hết hạn thì đơn đi về đâu.
- Hàng nhập `1 ngày`, `1 tuần`, `1 tháng`, `4 tháng` trước được theo dõi bằng cách nào.
- Hàng cận date, quá date, tồn lâu và nhập thêm phải quyết định trên dữ liệu gì.

Hiện trạng của dự án sau đợt nâng cấp này:

- Session timeout giữ ở `30 phút`.
- Login lockout giữ ở `5 lần sai / 15 phút`.
- Rate limit đã tách theo endpoint quan trọng: `login`, `register`, `forgot-password`, `checkout`, `add-review`, `search-autocomplete`.
- Thanh toán chuyển khoản có `payment transaction`, `expires_at`, trạng thái `PENDING_VERIFICATION` và `EXPIRED`.
- Storefront đã chuyển các filter/sort/pagination nặng từ app layer sang SQL.
- Nền schema cho `inventory_batches`, `stock_movements`, `suppliers`, `stock_imports` đã có để mở rộng kho theo lô.

## P0: Việc Phải Có Trước Khi Demo Hoặc Cho User Thật

### 1. Session và timeout

**Bài toán**

Nếu user mở web rồi bỏ đó, session không thể giữ vô thời hạn vì tăng rủi ro chiếm quyền phiên và giữ tài nguyên server không cần thiết.

**Chính sách**

- `session-timeout = 30 phút`
- Nếu quá 30 phút không tương tác: session hết hạn.
- Remember-me chỉ dùng để tạo lại session mới, không kéo dài session cũ mãi mãi.
- Với hành động nhạy cảm như checkout, đổi mật khẩu, thao tác admin: nếu session vừa được recreate từ remember-me thì nên buộc xác nhận lại trong roadmap tiếp theo.

**Khuyến nghị**

- `P0`: giữ timeout 30 phút như hiện tại.
- `P1`: thêm cờ `recent-authenticated-at` cho hành động nhạy cảm.
- `P2`: nếu multi-node, cân nhắc đưa session store ra ngoài app node.

### 2. Spam, brute-force, abuse

**Bài toán**

Web nhỏ thường chết trước vì abuse chứ chưa chắc vì nhiều user thật. Những điểm dễ bị spam nhất là:

- `/login`
- `/register`
- `/forgot-password`
- `/checkout`
- `/add-review`
- `/api/search-autocomplete`

**Hiện trạng**

Đã có rate-limit theo endpoint và lock account sau `5` lần đăng nhập sai trong `15 phút`.

**Khuyến nghị**

- `P0`: log rõ request bị rate-limit vào `security_events`.
- `P0`: quên mật khẩu không được lộ email có tồn tại hay không.
- `P1`: tách limit theo `IP` và theo `account`.
- `P1`: chỉ bật CAPTCHA sau hành vi đáng ngờ, không bật ngay cho mọi user.
- `P2`: nếu lên nhiều node, chuyển bộ đếm rate-limit từ memory sang Redis.

### 3. Thanh toán chuyển khoản và đơn treo

**Bài toán**

Nếu không có quy tắc timeout, đơn chuyển khoản sẽ treo vô thời hạn, làm lệch tồn kho và làm admin khó xử lý.

**Chính sách**

- Chuyển khoản tạo `order + payment_transaction`.
- Trạng thái đầu là `PENDING_VERIFICATION`.
- Thời gian chờ đề xuất: `2 giờ`.
- Hết `2 giờ` mà chưa đối soát:
  - transaction -> `EXPIRED`
  - đơn vẫn còn để admin kiểm tra lịch sử
  - UI phải hiển thị rõ là `Quá hạn thanh toán`

**Khuyến nghị**

- `P0`: giữ `2 giờ` làm mặc định cho đồ án.
- `P1`: thêm job nhắc admin khi số đơn pending/expired tăng bất thường.
- `P2`: khi có API ngân hàng thật, chỉ thay verification adapter, không thay checkout core.

## P1: Hiệu Năng Storefront Và Database

### 4. 5.000 user nghĩa là gì

**Giải thích để bảo vệ**

`5.000 user` không đồng nghĩa `5.000 concurrent users`. Với đồ án này nên hiểu là:

- khoảng `5.000` tài khoản đăng ký
- vài chục đến vài trăm request đồng thời ở peak nhỏ

Điểm nghẽn đầu tiên thường là:

- query lọc/sort sản phẩm không tối ưu
- ảnh sản phẩm nặng
- autocomplete gọi quá nhiều
- connection pool bị chiếm bởi query chậm

**Mục tiêu thực tế**

- Trang shop phổ biến: `< 2 giây`
- Autocomplete/search đơn giản: `< 500ms`
- Checkout ổn định ở tải thấp đến trung bình

### 5. Hơn 1.000 sản phẩm thì load nhanh thế nào

**Nguyên tắc**

Không được dùng `getAllProducts()` rồi lọc/sort trong Java cho trang listing lớn.

**Cách làm đúng**

- filter ở SQL
- sort ở SQL
- pagination ở SQL
- chỉ lấy field cần dùng
- lazy-load ảnh
- cache asset tĩnh bằng `Cache-Control`

**Hiện trạng**

Đã chuyển shop filter/sort/pagination sang DAO SQL-backed và thêm cache header cho `/assets/*`.

**Khuyến nghị**

- `P1`: theo dõi query plan các case search phổ biến.
- `P1`: thêm index ghép cho `products`, `orders`, `users`, `payment_transactions`.
- `P2`: nếu search phức tạp hơn nữa, cân nhắc full-text search.
- `P3`: CDN hoặc object storage cho ảnh nếu lưu lượng ảnh tăng.

### 6. Database, index, connection pool

**Các index quan trọng**

- `products(is_active, category, pet_type_id, price, discount, product_id)`
- `orders(status, createdAt, user_id)`
- `order_items(order_id, product_id)`
- `payment_transactions(verification_status, status, created_at)`
- `payment_transactions(transfer_reference)`
- `users(email, username, locked_until)`

**Hikari pool**

- `20` connection là ổn cho một app node nhỏ.
- Không tăng pool mù quáng nếu query còn chậm.
- Trước khi tăng pool phải đo:
  - số query chậm
  - thời gian checkout
  - số request chờ connection

## P1/P2: Kho Theo Lô, Hạn Dùng, Hàng Tồn Lâu

### 7. Vì sao phải có batch

Nếu chỉ lưu `quantity` tổng ở `products`, bạn không trả lời được:

- sản phẩm này nhập lô nào trước
- lô nào gần hết hạn
- hàng nào nằm từ 4 tháng trước
- bán nhầm lô mới trước lô cũ hay không

Vì vậy cần các bảng:

- `suppliers`
- `stock_imports`
- `inventory_batches`
- `stock_movements`

### 8. Theo dõi hàng nhập từ 1 ngày, 1 tuần, 1 tháng, 4 tháng trước

**Cách tính**

Dựa trên `inventory_batches.received_at` và `remaining_quantity`.

Các bucket gợi ý:

- `<= 1 ngày`: hàng mới nhập
- `2-7 ngày`: hàng tuần này
- `8-30 ngày`: hàng tháng này
- `> 120 ngày`: hàng tồn quá lâu

**Tác dụng**

- biết hàng quay vòng chậm
- biết hàng mới nhập chưa bán
- biết mặt hàng bị “chôn vốn”

### 9. Hàng cận date và quá date

**Quy tắc nên dùng**

- `<= 30 ngày`: cảnh báo vàng
- `<= 7 ngày`: cảnh báo cam
- `<= 1 ngày`: cảnh báo đỏ
- `quá hạn`: khóa bán

**Xuất kho**

- hàng có hạn dùng: `FEFO`
- hàng không nhạy hạn: `FIFO`

**Hành động vận hành**

- gắn nhãn nội bộ “cận date”
- cân nhắc giảm giá xả hàng
- không cho checkout từ batch quá hạn
- tạo alert hằng ngày cho admin

### 10. Làm sao biết cần nhập hàng thế nào là ổn

**Công thức đơn giản đủ để bảo vệ**

`reorder_point = average_daily_sales × lead_time + safety_stock`

Trong đó:

- `average_daily_sales`: trung bình số lượng bán/ngày, ví dụ 30 ngày gần nhất
- `lead_time`: số ngày từ lúc đặt nhà cung cấp đến lúc hàng về
- `safety_stock`: lượng đệm chống biến động

**Ví dụ**

- bán trung bình `4` gói/ngày
- lead time `5` ngày
- safety stock `8`
- reorder point = `4 × 5 + 8 = 28`

Nếu tồn kho hiện tại dưới `28`, hệ thống nên đề xuất nhập thêm.

**Khuyến nghị**

- `P1`: dùng công thức đơn giản như trên.
- `P2`: tách ngưỡng theo nhóm hàng bán nhanh/chậm.
- `P3`: kết hợp mùa vụ, khuyến mãi, lịch nhập nhà cung cấp.

## P2: Quan Sát Hệ Thống, Backup, Audit

### 11. Cần log gì

**Log tối thiểu**

- login fail
- account locked
- request bị rate-limit
- checkout fail
- payment pending backlog
- payment expired
- stock low
- batch near expiry
- admin update payment verification
- admin chỉnh tay kho

`security_events` là bước đầu để gom các sự kiện bảo mật/abuse.

### 12. Dashboard tối thiểu cho admin vận hành

- số đơn chờ đối soát
- số đơn quá hạn thanh toán
- số user đang bị lock
- số request bị rate-limit trong ngày
- số sản phẩm gần hết hàng
- số batch cận hạn / quá hạn

### 13. Backup và recovery

**Backup thật phải gồm**

- database
- ảnh upload
- file cấu hình ngoài source

**Nguyên tắc**

- backup DB mỗi ngày
- giữ nhiều bản gần nhất
- test restore định kỳ

Nếu không thử restore thì backup chỉ là cảm giác an toàn giả.

## P2/P3: Những Góc Khuất Dễ Quên

### 14. Tồn kho âm hoặc lệch tồn

Nguyên nhân thường gặp:

- race condition khi nhiều checkout cùng lúc
- admin chỉnh tay mà không ghi log
- rollback order nhưng không hoàn tồn đúng

**Khuyến nghị**

- mọi thay đổi kho phải đi qua `stock_movements`
- batch allocation phải có transaction DB
- định kỳ đối chiếu `products.quantity` với tổng `inventory_batches.remaining_quantity`

### 15. Coupon abuse

Rủi ro:

- tạo nhiều tài khoản
- spam retry checkout
- share mã giảm giá ngoài ý muốn

**Khuyến nghị**

- rate-limit endpoint áp mã
- lưu usage theo user, theo IP, theo coupon
- khóa coupon nếu thấy pattern bất thường

### 16. Admin thao tác sai

Các thao tác cần audit:

- đổi giá sản phẩm
- đổi trạng thái đơn
- duyệt thanh toán
- nhập kho
- chỉnh batch
- hủy đơn sau khi đã ghi nhận thanh toán

### 17. Ảnh sản phẩm nặng

Đây thường là bottleneck sớm hơn cả DB.

**Khuyến nghị**

- resize chuẩn khi upload
- không dùng ảnh gốc quá lớn ở listing
- bật cache asset
- nếu web lớn hơn nữa thì đẩy ảnh sang object storage/CDN

### 18. Pháp lý cơ bản

Tối thiểu nên có:

- trang `Privacy Policy`
- trang `Terms of Service`
- giải thích dữ liệu nào được lưu: email, số điện thoại, địa chỉ
- chính sách retention dữ liệu cơ bản
- tránh log plaintext của dữ liệu nhạy cảm

## Roadmap Ưu Tiên

### P0

- Giữ session timeout `30 phút`
- Login lock `5 lần / 15 phút`
- Quên mật khẩu không lộ email tồn tại
- Rate-limit các endpoint nhạy cảm
- Thanh toán chuyển khoản có `2 giờ` timeout và trạng thái `EXPIRED`

### P1

- Tối ưu storefront hoàn toàn bằng SQL filter/sort/pagination
- Index lại bảng lớn
- Hoàn thiện admin view cho pending/expired payments
- Bắt đầu dùng `inventory_batches` và `stock_movements` cho nhập/xuất

### P2

- Dashboard vận hành
- Alert cận date, low stock, pending payment backlog
- Backup/restore runbook
- Reorder recommendation theo sales velocity

### P3

- Redis cho rate-limit/session khi multi-node
- Search tốt hơn full-text
- CDN/object storage cho ảnh
- Ngưỡng nhập hàng nâng cao theo mùa vụ

## Câu Trả Lời Nhanh Khi Bảo Vệ

### Nếu 5.000 user thì sao?

5.000 user đăng ký chưa đáng sợ bằng concurrent traffic. Với kiến trúc hiện tại, nghẽn sớm nhất là query lọc sản phẩm, ảnh nặng và abuse endpoint. Vì vậy ưu tiên là SQL pagination/filter, index đúng, cache asset và rate-limit.

### Làm sao để hơn 1.000 sản phẩm vẫn nhanh?

Không lấy toàn bộ sản phẩm rồi lọc ở Java. Phải lọc, sort và phân trang ở SQL; chỉ load dữ liệu cần hiển thị; ảnh phải lazy-load và có cache header.

### Nếu user spam thì sao?

Đã có rate-limit theo endpoint và lock account sau 5 lần đăng nhập sai trong 15 phút. Roadmap tiếp theo là log security events, CAPTCHA theo ngữ cảnh, và Redis nếu triển khai nhiều node.

### Nếu user không dùng trong 30 phút thì sao?

Session hết hạn sau 30 phút idle. Đây là cân bằng giữa an toàn và trải nghiệm. Remember-me chỉ giúp đăng nhập lại, không kéo dài session hoạt động vô hạn.

### Thời gian chờ thanh toán là bao nhiêu?

Mặc định `2 giờ` cho chuyển khoản. Sau thời gian này giao dịch chuyển `EXPIRED` để tránh đơn treo vô hạn và giúp admin xử lý rõ ràng.

### Theo dõi hàng nhập từ 4 tháng trước, 1 tuần trước thế nào?

Không thể làm đúng nếu chỉ có `quantity` tổng. Phải theo dõi theo `inventory_batches.received_at` và nhóm tồn kho theo tuổi hàng.

### Hàng gần hết hạn xử lý sao?

Theo batch, dùng FEFO, cảnh báo theo mốc 30/7/1 ngày và khóa bán batch quá hạn.

### Làm sao biết khi nào cần nhập hàng?

Dùng `reorder_point = average_daily_sales × lead_time + safety_stock`, sau đó so sánh với tồn kho hiện tại để đề xuất nhập.
