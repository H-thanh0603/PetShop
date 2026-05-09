<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Yêu Cầu Đổi Trả - Hoàn Tiền</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card mx-auto" style="max-width: 600px;">
        <div class="card-header bg-danger text-white">
            <h4 class="mb-0">Tạo Yêu Cầu Hoàn Tiền / Đổi Trả</h4>
        </div>
        <div class="card-body">
            <p class="text-muted">Đơn hàng số: <strong>#${orderId}</strong></p>
            <p class="text-muted">Số tiền hoàn trả tối đa: <strong class="text-danger">${totalPrice} VNĐ</strong></p>

            <form action="order-return" method="post">
                <input type="hidden" name="orderId" value="${orderId}">
                <input type="hidden" name="totalPrice" value="${totalPrice}">

                <div class="mb-3">
                    <label for="reason" class="form-label font-weight-bold">Lý do đổi trả sản phẩm <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="reason" name="reason" rows="4"
                              placeholder="Vui lòng ghi rõ lý do (Ví dụ: Sản phẩm lỗi, không giống mô tả, giao sai kích thước cho thú cưng...)" required></textarea>
                </div>

                <div class="d-flex justify-content-between">
                    <a href="order-history" class="btn btn-secondary">Quay lại</a>
                    <button type="submit" class="btn btn-danger">Gửi Yêu Cầu</button>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>