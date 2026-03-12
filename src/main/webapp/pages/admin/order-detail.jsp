<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Chi tiết đơn hàng - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .order-info-card { background: white; border-radius: 12px; padding: 25px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 25px; }
        .info-label { color: #64748b; font-size: 0.85rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
        .info-value { color: #1e293b; font-size: 1rem; font-weight: 500; margin-bottom: 15px; }
        .status-badge { padding: 6px 15px; border-radius: 20px; font-weight: 700; font-size: 0.85rem; }
        .product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; }
        .total-row { font-size: 1.25rem; font-weight: 800; color: #10314d; }
    </style>
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="orders"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bx-detail'></i> Chi tiết đơn hàng #${order.id}</h1>
            <div>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary btn-sm">
                    <i class='bx bx-left-arrow-alt'></i> Quay lại
                </a>
            </div>
        </div>

        <div class="row">
            <div class="col-lg-4">
                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold">Thông tin khách hàng</h5>
                    <div class="info-label">Người nhận</div>
                    <div class="info-value text-primary fw-bold">${order.fullname}</div>
                    
                    <div class="info-label">Số điện thoại</div>
                    <div class="info-value">${order.phone}</div>
                    
                    <div class="info-label">Địa chỉ</div>
                    <div class="info-value">${order.address}</div>
                    
                    <div class="info-label">Ngày đặt</div>
                    <div class="info-value"><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></div>
                    
                    <div class="info-label">Trạng thái hiện tại</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${order.status == 'Pending'}"><span class="badge bg-warning text-dark">Chờ xử lý</span></c:when>
                            <c:when test="${order.status == 'Confirmed'}"><span class="badge bg-info text-white">Đã xác nhận</span></c:when>
                            <c:when test="${order.status == 'Shipping'}"><span class="badge bg-primary text-white">Đang giao</span></c:when>
                            <c:when test="${order.status == 'Completed'}"><span class="badge bg-success text-white">Hoàn thành</span></c:when>
                            <c:when test="${order.status == 'Cancelled'}"><span class="badge bg-danger text-white">Đã hủy</span></c:when>
                        </c:choose>
                    </div>

                    <c:if test="${not empty order.note}">
                        <div class="info-label">Ghi chú</div>
                        <div class="info-value text-danger italic">${order.note}</div>
                    </c:if>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold">Danh sách sản phẩm</h5>
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead>
                                <tr class="bg-light">
                                    <th>Ảnh</th>
                                    <th>Sản phẩm</th>
                                    <th class="text-center">Số lượng</th>
                                    <th class="text-end">Đơn giá</th>
                                    <th class="text-end">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${order.items}">
                                    <tr>
                                        <td>
                                            <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${item.product.image}" 
                                                 class="product-img"
                                                 onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'">
                                        </td>
                                        <td class="align-middle fw-bold">${item.product.name}</td>
                                        <td class="align-middle text-center">${item.quantity}</td>
                                        <td class="align-middle text-end">
                                            <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                        </td>
                                        <td class="align-middle text-end">
                                            <fmt:formatNumber value="${item.price * item.quantity}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="4" class="text-end fw-bold py-3">TỔNG CỘNG:</td>
                                    <td class="text-end total-row py-3">
                                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold">Cập nhật trạng thái</h5>
                    <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="row g-3">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <div class="col-md-8">
                            <select name="status" class="form-select">
                                <option value="Pending" ${order.status == 'Pending' ? 'selected' : ''}>Chờ xử lý</option>
                                <option value="Confirmed" ${order.status == 'Confirmed' ? 'selected' : ''}>Xác nhận đơn hàng</option>
                                <option value="Shipping" ${order.status == 'Shipping' ? 'selected' : ''}>Đang giao hàng</option>
                                <option value="Completed" ${order.status == 'Completed' ? 'selected' : ''}>Đã hoàn thành</option>
                                <option value="Cancelled" ${order.status == 'Cancelled' ? 'selected' : ''}>Hủy đơn hàng</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <button type="submit" class="btn btn-primary w-100">Cập nhật</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="/components/scripts.jsp" />
</body>
</html>
