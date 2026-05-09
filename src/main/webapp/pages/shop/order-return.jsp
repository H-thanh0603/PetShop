<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Yêu Cầu Đổi Trả - PetShop</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/components/navbar.jsp"/>

<div class="container mt-5">
    <div class="card mx-auto shadow" style="max-width: 600px;">
        <div class="card-header bg-warning text-dark">
            <h4 class="mb-0"><i class='bx bx-transfer'></i> Yêu Cầu Đổi Trả / Hoàn Tiền</h4>
        </div>
        <div class="card-body">
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <p class="text-muted">Đơn hàng số: <strong>#${orderId}</strong></p>
            <p class="text-muted">Số tiền hoàn trả tối đa:
                <strong class="text-danger">${totalPrice} VNĐ</strong>
            </p>

            <form action="${pageContext.request.contextPath}/order-return" method="post">
                <input type="hidden" name="orderId" value="${orderId}">
                <input type="hidden" name="totalPrice" value="${totalPrice}">

                <div class="mb-3">
                    <label for="reason" class="form-label fw-bold">
                        Lý do đổi trả <span class="text-danger">*</span>
                    </label>
                    <textarea class="form-control" id="reason" name="reason" rows="4"
                              placeholder="Vui lòng ghi rõ lý do (Ví dụ: Sản phẩm lỗi, không giống mô tả...)"
                              required></textarea>
                </div>

                <div class="d-flex justify-content-between">
                    <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-secondary">
                        <i class='bx bx-arrow-back'></i> Quay lại
                    </a>
                    <button type="submit" class="btn btn-warning">
                        <i class='bx bx-send'></i> Gửi Yêu Cầu
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>