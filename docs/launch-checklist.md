# Checklist trước khi mở bán (go-live)

> Làm theo thứ tự. Các mục [USER] chỉ bạn làm được; [DEV] có thể nhờ hỗ trợ kỹ thuật.

## 1. Rotate toàn bộ key đã lộ trong git history [USER] — BẮT BUỘC

Các key sau từng được commit (commit `64ec36d` đã gỡ khỏi code nhưng **vẫn nằm trong git history**):

| Key | Rotate ở đâu |
|---|---|
| VNPAY TMN code + hash secret | Dashboard merchant VNPAY (sandbox + production) |
| GHN token (2 token) | Portal GHN → Cài đặt API |
| DeepSeek API key | platform.deepseek.com |
| Số tài khoản nhận tiền | tự quyết định có đổi TK hay không |

Sau khi rotate, điền giá trị MỚI vào `.env` trên server (từ `.env.example`).

## 2. Purge git history [USER quyết định, có thể nhờ DEV chạy lệnh]

```bash
# Backup repo trước!
git clone --mirror git@github.com:H-Thanh0603/PetShop.git petshop-mirror
cd petshop-mirror
pip install git-filter-repo
git filter-repo --invert-paths \
  --path src/main/resources/vnpay.properties \
  --path src/main/resources/ship.properties \
  --path src/main/resources/app.properties \
  --path src/main/resources/db.properties \
  --invert-paths --sensitive-data-removal
# Lưu ý: cách trên xoá cả file; muốn chỉ xoá nội dung key trong history,
# dùng --replace-text với danh sách key cũ.
git push --force
```

Sau khi force-push: mọi clone cũ đều phải re-clone. Nếu repo đang public, coi như key đã lộ 100% — **bước 1 (rotate) là bắt buộc, bước 2 là phòng thủ**.

## 3. Cấu hình webhook secrets [USER + DEV]

- **GHN**: trong portal GHN, đăng ký webhook URL kèm secret:
  `https://<domain>/api/ghn/webhook?secret=<giá trị PAYMENT_GHN_WEBHOOK_SECRET trong .env>`
- **Bank webhook** (nếu dùng SePay/cassopay): cấu hình header `X-Bank-Webhook-Secret` trùng `PAYMENT_BANK_WEBHOOK_SECRET` trong `.env`. Secret để trống = endpoint chặn mọi request.
- **VNPAY IPN**: trong cấu hình tích hợp VNPAY, đặt IPN URL = `https://<domain>/api/payment/vnpay-ipn` (method GET/POST đều hỗ trợ).

## 4. Triển khai server [DEV]

```bash
# Trên server (Ubuntu, đã cài Docker + Docker Compose plugin)
git clone <repo> && cd PetShop
cp .env.example .env && nano .env          # điền secret thật
docker compose -f docker-compose.prod.yml up -d --build

# Phát hành chứng chỉ TLS lần đầu (domain phải đã trỏ A record về server)
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot -w /var/www/certbot -d melipet.shop \
  --email <email-ban> --agree-tos --no-eff-email
docker compose -f docker-compose.prod.yml restart nginx
```

Sửa `deploy/nginx/default.conf` nếu dùng domain khác `melipet.shop`.

## 5. Smoke test sau deploy [DEV]

Chạy lần lượt, tất cả phải đạt:

```bash
curl -s https://<domain>/actuator/health                 # {"status":"UP"}
curl -s -o /dev/null -w "%{http_code}\n" https://<domain>/            # 302
curl -s -o /dev/null -w "%{http_code}\n" https://<domain>/home        # 200
curl -s -o /dev/null -w "%{http_code}\n" https://<domain>/shop        # 200
curl -s -o /dev/null -w "%{http_code}\n" https://<domain>/admin/login # 200
curl -s https://<domain>/api/ghn/webhook                              # 401
curl -s -o /dev/null -w "%{http_code}\n" -X POST https://<domain>/login   # 403 (CSRF)
```

Luồng tay trên trình duyệt:
1. Đăng ký + đăng nhập tài khoản khách.
2. Thêm giỏ → checkout COD → kiểm tra đơn hiện trong "Đơn hàng của tôi".
3. Checkout VNPAY sandbox → thanh toán → đơn chuyển "Paid". Test thêm: thanh toán xong **đóng tab trước khi quay về** → trong vòng vài phút VNPAY IPN phải tự xác nhận đơn.
4. Checkout chuyển khoản → thấy mã tham chiếu + QR → (giả lập) webhook ngân hàng → đơn chuyển xác nhận.
5. Upload ảnh sản phẩm trong admin → ảnh phải hiển thị VÀ **sống sót qua lần redeploy** (`docker compose ... up -d --build` lại).
6. Đặt đơn với tồn kho = 1, mở 2 tab đặt cùng lúc → chỉ 1 đơn thành công (test chống oversell).

## 6. Monitoring & backup [DEV]

- Grafana: `ssh -L 3000:localhost:3000 <server>` → `http://localhost:3000` (user `admin`, password trong `.env`). Xem nhanh: HTTP error rate, HikariCP pool, JVM memory.
- Backup tự động mỗi 02:00 vào `./backups/` trên server. **Kiểm tra khôi phục ít nhất 1 lần trước khi mở bán:**
  ```bash
  gunzip -c backups/petvaccine_XXXX.sql.gz | docker exec -i petshop-mysql mysql -u petshop -p"$DB_PASSWORD" petvaccine_restore_test
  ```
- Backup thư mục ảnh: `docker run --rm -v petshop_upload-data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/uploads_$(date +%F).tar.gz -C /data .`

## 7. Việc hành chính [USER]

- Hoá đơn/VAT nếu cần xuất cho khách.
- Chính sách xử lý dữ liệu cá nhân (Nghị định 13/2023/NĐ-CP): trang `/privacy-policy` đã có khung — rà lại nội dung cho đúng thực tế.
- Đổi mật khẩu tài khoản admin mặc định/demo, xoá tài khoản demo (`demo_accounts.sql`) khỏi DB production.
- Cập nhật thông tin liên hệ thật trong knowledge base AI (trang admin AI Support) — hiện còn số hotline demo `0900 000 000`.

## 8. Đã biết cố ý chưa làm (khi nào cần thì làm)

- Multi-tenant / chuỗi cửa hàng — schema hiện single-shop.
- Redis / nhiều instance — rate-limit, OTP, lockout, scheduler đang in-memory, **chỉ được chạy đúng 1 instance app**.
- Thymeleaf / JPA / Spring Security — hiện giữ JSP + DAO SQL thuần + filter tự viết.
