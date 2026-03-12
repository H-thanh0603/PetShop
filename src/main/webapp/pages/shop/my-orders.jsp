<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đơn hàng của tôi - PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <style>
        body { background-color: #f1f5f9; font-family: 'Segoe UI', sans-serif; }
        .page-title { color: #1e293b; font-weight: 800; text-transform: uppercase; margin-bottom: 30px; }
        .order-card { background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-bottom: 20px; border: none; transition: 0.3s; }
        .order-card:hover { transform: translateY(-3px); box-shadow: 0 10px 15px rgba(0,0,0,0.1); }
        .order-header { padding: 15px 20px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
        .order-id { font-weight: 700; color: #3b82f6; }
        .order-date { font-size: 0.85rem; color: #64748b; }
        .order-body { padding: 20px; }
        .order-status { font-weight: 600; font-size: 0.85rem; padding: 5px 12px; border-radius: 20px; }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-confirmed { background: #dbeafe; color: #1e40af; }
        .status-shipping { background: #e0e7ff; color: #3730a3; }
        .status-completed { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        .order-total { font-weight: 800; color: #1e293b; font-size: 1.1rem; }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <div class="container my-5" style="min-height: 500px;">
        <h2 class="page-title text-center"><i class='bx bx-package'></i> Đơn hàng của tôi</h2>

        <c:choose>
            <c:when test="${empty orders}">
                <div class="text-center py-5 bg-white rounded shadow-sm">
                    <img src="https://cdn-icons-png.flaticon.com/512/11329/11329060.png" width="120" style="opacity: 0.5;">
                    <h5 class="mt-4 text-muted">Bạn chưa có đơn hàng nào</h5>
                    <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary px-4 mt-3">Mua sắm ngay</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row justify-content-center">
                    <div class="col-lg-10">
                        <c:forEach var="o" items="${orders}">
                            <div class="order-card">
                                <div class="order-header">
                                    <div>
                                        <span class="order-id">#${o.id}</span>
                                        <span class="order-date ms-3"><i class='bx bx-calendar'></i> <fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/></span>
                                    </div>
                                    <span class="order-status 
                                        ${o.status == 'Pending' ? 'status-pending' : ''}
                                        ${o.status == 'Confirmed' ? 'status-confirmed' : ''}
                                        ${o.status == 'Shipping' ? 'status-shipping' : ''}
                                        ${o.status == 'Completed' ? 'status-completed' : ''}
                                        ${o.status == 'Cancelled' ? 'status-cancelled' : ''}">
                                        <c:choose>
                                            <c:when test="${o.status == 'Pending'}">Chờ xử lý</c:when>
                                            <c:when test="${o.status == 'Confirmed'}">Đã xác nhận</c:when>
                                            <c:when test="${o.status == 'Shipping'}">Đang giao</c:when>
                                            <c:when test="${o.status == 'Completed'}">Hoàn thành</c:when>
                                            <c:when test="${o.status == 'Cancelled'}">Đã hủy</c:when>
                                            <c:otherwise>${o.status}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="order-body d-flex justify-content-between align-items-center">
                                    <div>
                                        <p class="mb-1 text-muted small">Người nhận: <strong>${o.fullname}</strong></p>
                                        <p class="mb-0 text-muted small">Địa chỉ: ${o.address}</p>
                                    </div>
                                    <div class="text-end">
                                        <p class="mb-1 text-muted small">Tổng tiền:</p>
                                        <p class="order-total mb-2">${o.formattedTotalAmount}</p>
                                        <a href="${pageContext.request.contextPath}/my-orders?action=view&id=${o.id}" class="btn btn-sm btn-outline-primary px-3">Chi tiết</a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
