<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng - PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <style>
        body { background-color: #f8fafc; font-family: 'Segoe UI', sans-serif; }
        .detail-container { max-width: 900px; margin: 40px auto; padding: 20px; }
        .card { border: none; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); overflow: hidden; }
        .card-header { background: #1e293b; color: white; padding: 20px; border: none; }
        .product-item { border-bottom: 1px solid #f1f5f9; padding: 15px 0; }
        .product-item:last-child { border-bottom: none; }
        .product-img { width: 70px; height: 70px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; }
        .info-section { background: #fff; padding: 25px; border-radius: 12px; margin-bottom: 25px; }
        .label { color: #64748b; font-size: 0.8rem; font-weight: 700; text-transform: uppercase; margin-bottom: 5px; }
        .value { color: #1e293b; font-weight: 500; margin-bottom: 15px; }
        .status-badge { font-weight: 700; font-size: 0.85rem; padding: 5px 12px; border-radius: 20px; }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <div class="container detail-container">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="mb-0 fw-bold">Chi tiết đơn hàng #${order.id}</h3>
            <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-outline-secondary btn-sm">
                <i class='bx bx-arrow-back'></i> Quay lại
            </a>
        </div>

        <div class="row">
            <div class="col-lg-8">
                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between">
                        <span>Sản phẩm</span>
                        <span class="status-badge 
                            ${order.status == 'Pending' ? 'bg-warning text-dark' : ''}
                            ${order.status == 'Confirmed' ? 'bg-info text-white' : ''}
                            ${order.status == 'Shipping' ? 'bg-primary text-white' : ''}
                            ${order.status == 'Completed' ? 'bg-success text-white' : ''}
                            ${order.status == 'Cancelled' ? 'bg-danger text-white' : ''}">
                            ${order.status}
                        </span>
                    </div>
                    <div class="card-body">
                        <c:forEach var="item" items="${order.items}">
                            <div class="product-item d-flex align-items-center">
                                <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${item.product.image}" 
                                     class="product-img me-3" 
                                     onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'">
                                <div class="flex-grow-1">
                                    <h6 class="mb-0 fw-bold">${item.product.name}</h6>
                                    <small class="text-muted">Số lượng: ${item.quantity}</small>
                                </div>
                                <div class="text-end">
                                    <p class="mb-0 fw-bold"><fmt:formatNumber value="${item.price * item.quantity}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></p>
                                    <small class="text-muted"><fmt:formatNumber value="${item.price}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></small>
                                </div>
                            </div>
                        </c:forEach>
                        
                        <div class="mt-4 pt-3 border-top">
                            <div class="d-flex justify-content-between align-items-center">
                                <h5 class="mb-0 fw-bold">Tổng thanh toán</h5>
                                <h4 class="mb-0 fw-bold text-primary">${order.formattedTotalAmount}</h4>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="info-section shadow-sm">
                    <div class="label">Người nhận</div>
                    <div class="value">${order.fullname}</div>
                    
                    <div class="label">Số điện thoại</div>
                    <div class="value">${order.phone}</div>
                    
                    <div class="label">Địa chỉ giao hàng</div>
                    <div class="value">${order.address}</div>
                    
                    <div class="label">Ngày đặt hàng</div>
                    <div class="value"><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></div>
                    
                    <c:if test="${not empty order.note}">
                        <div class="label">Ghi chú từ khách</div>
                        <div class="value text-muted small">${order.note}</div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
