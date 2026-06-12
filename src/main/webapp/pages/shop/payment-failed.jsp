<%--
  Created by IntelliJ IDEA.
  User: HUU DAT
  Date: 6/12/2026
  Time: 3:51 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thanh toán thất bại | PetShop</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<jsp:include page="/components/navbar.jsp"/>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-7">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-body text-center p-5">
                    <div class="mb-4">
                        <div class="d-inline-flex align-items-center justify-content-center rounded-circle bg-danger text-white"
                             style="width:80px;height:80px;font-size:42px;">
                            ✕
                        </div>
                    </div>

                    <h2 class="fw-bold text-danger mb-3">Thanh toán thất bại</h2>

                    <p class="text-muted fs-5 mb-2">
                        ${fn:escapeXml(paymentMessage)}
                    </p>

                    <p class="text-muted mb-4">
                        Mã đơn hàng:
                        <strong>#${orderId}</strong>
                    </p>

                    <div class="alert alert-warning text-start">
                        Đơn hàng của bạn đã được ghi nhận nhưng chưa thanh toán thành công.
                        Bạn có thể kiểm tra lại đơn hàng hoặc mua sắm tiếp.
                    </div>

                    <div class="d-flex justify-content-center gap-3 mt-4">
                        <a href="${pageContext.request.contextPath}/my-orders"
                           class="btn btn-primary px-4">
                            Xem đơn hàng
                        </a>

                        <a href="${pageContext.request.contextPath}/shop"
                           class="btn btn-outline-secondary px-4">
                            Quay lại cửa hàng
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/components/footer.jsp"/>

</body>
</html>