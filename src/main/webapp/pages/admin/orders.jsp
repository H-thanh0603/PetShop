<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Đơn hàng - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .status-badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 600;
        }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-confirmed { background: #e0f2fe; color: #075985; }
        .status-shipping { background: #f3e8ff; color: #6b21a8; }
        .status-completed { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        
        .order-id { font-family: monospace; font-weight: bold; color: #3b82f6; }
        .customer-info p { margin-bottom: 2px; font-size: 0.9rem; }
        .total-amount { font-weight: 700; color: #1e293b; }
    </style>
</head>
<body>

    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="orders"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bx-cart'></i> Quản lý Đơn hàng</h1>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <div class="table-section mt-4">
            <div class="table-header">
                <span class="table-title">Danh sách đơn hàng mới nhất</span>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 80px;">Mã ĐH</th>
                        <th>Khách hàng</th>
                        <th>Địa chỉ giao hàng</th>
                        <th style="width: 130px;">Tổng tiền</th>
                        <th style="width: 150px;">Ngày đặt</th>
                        <th style="width: 140px;">Trạng thái</th>
                        <th style="width: 100px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty orders}">
                        <tr>
                            <td colspan="7" class="text-center py-5">Chưa có đơn hàng nào.</td>
                        </tr>
                    </c:if>
                    <c:forEach items="${orders}" var="o">
                        <tr>
                            <td><span class="order-id">#${o.id}</span></td>
                            <td class="customer-info">
                                <p><strong>${o.fullname}</strong></p>
                                <p class="text-muted"><i class='bx bx-phone'></i> ${o.phone}</p>
                            </td>
                            <td>
                                <p class="small mb-0">${o.address}</p>
                                <c:if test="${not empty o.note}">
                                    <small class="text-danger italic">Ghi chú: ${o.note}</small>
                                </c:if>
                            </td>
                            <td><span class="total-amount">${o.formattedTotalAmount}</span></td>
                            <td><fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.status == 'Pending'}"><span class="status-badge status-pending">Chờ xử lý</span></c:when>
                                    <c:when test="${o.status == 'Confirmed'}"><span class="status-badge status-confirmed">Đã xác nhận</span></c:when>
                                    <c:when test="${o.status == 'Shipping'}"><span class="status-badge status-shipping">Đang giao</span></c:when>
                                    <c:when test="${o.status == 'Completed'}"><span class="status-badge status-completed">Hoàn thành</span></c:when>
                                    <c:when test="${o.status == 'Cancelled'}"><span class="status-badge status-cancelled">Đã hủy</span></c:when>
                                </c:choose>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <a href="${pageContext.request.contextPath}/admin/orders?action=view&id=${o.id}" class="action-btn edit" title="Xem chi tiết">
                                        <i class='bx bx-show'></i>
                                    </a>
                                    <button class="action-btn edit" onclick="openUpdateModal(${o.id}, '${o.status}')" title="Cập nhật trạng thái">
                                        <i class='bx bx-refresh'></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </main>

    <!-- Update Status Modal -->
    <div class="modal-overlay" id="updateStatusModal">
        <div class="modal-box" style="max-width: 400px;">
            <div class="modal-header">
                <h3 class="modal-title">Cập nhật đơn hàng</h3>
                <button class="modal-close" onclick="closeModal()"><i class='bx bx-x'></i></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/orders" method="post">
                <input type="hidden" name="action" value="updateStatus">
                <input type="hidden" name="orderId" id="modalOrderId">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Trạng thái mới</label>
                        <select name="status" id="modalStatus" class="form-select">
                            <option value="Pending">Chờ xử lý</option>
                            <option value="Confirmed">Xác nhận đơn hàng</option>
                            <option value="Shipping">Đang giao hàng</option>
                            <option value="Completed">Đã hoàn thành</option>
                            <option value="Cancelled">Hủy đơn hàng</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeModal()">Đóng</button>
                    <button type="submit" class="btn btn-primary">Cập nhật</button>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="/components/scripts.jsp" />
    <jsp:include page="/components/admin-toast.jsp" />
    <script>
        function openUpdateModal(id, status) {
            document.getElementById('modalOrderId').value = id;
            document.getElementById('modalStatus').value = status;
            document.getElementById('updateStatusModal').classList.add('show');
        }
        function closeModal() {
            document.getElementById('updateStatusModal').classList.remove('show');
        }
    </script>
</body>
</html>
