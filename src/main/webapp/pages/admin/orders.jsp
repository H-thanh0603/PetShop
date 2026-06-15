<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Đơn hàng - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .status-badge { padding: 6px 12px; border-radius: 20px; font-size: 0.85rem; font-weight: 600; }
        .status-awaiting-payment { background: #fff7ed; color: #c2410c; }
        .status-paid { background: #ccfbf1; color: #0f766e; }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-confirmed { background: #e0f2fe; color: #075985; }
        .status-shipping { background: #f3e8ff; color: #6b21a8; }
        .status-completed { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        .order-id { font-family: monospace; font-weight: bold; color: #3b82f6; }
        .customer-info p { margin-bottom: 2px; font-size: 0.9rem; }
        .total-amount { font-weight: 700; color: #1e293b; }
        .toolbar-wrap { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 20px; }
        .filter-chip {
            display: inline-flex; align-items: center; gap: 6px; padding: 8px 14px; border-radius: 999px;
            border: 1px solid #dbe3ef; background: #fff; color: #475569; text-decoration: none; font-weight: 600;
        }
        .filter-chip.active { background: #eff6ff; color: #2563eb; border-color: #bfdbfe; }
        .search-form { display: flex; gap: 10px; flex-wrap: wrap; }
        .search-form input { min-width: 280px; }
        .table-subtitle { color: #64748b; font-size: 0.9rem; }
        .payment-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 700; margin-top: 6px; }
        .payment-pending { background: #fff7ed; color: #c2410c; }
        .payment-verified { background: #dcfce7; color: #166534; }
        .payment-failed { background: #fee2e2; color: #991b1b; }
        .payment-neutral { background: #e0f2fe; color: #075985; }
        .payment-unpaid { background: #f1f5f9; color: #475569; }
        .payment-expired { background: #fef2f2; color: #b91c1c; }
        .review-alert { margin: 0 1.5rem 1rem; padding: 14px 16px; border-radius: 14px; background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%); color: #9a3412; border: 1px solid #fdba74; }
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
            <div class="table-header d-block">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <div>
                        <span class="table-title">Danh sách đơn hàng</span>
                        <div class="table-subtitle">Trang ${currentPage}/${totalPages} · Tổng ${totalOrders} đơn hàng</div>
                    </div>
                </div>
            </div>

            <div class="toolbar-wrap px-4 pt-3">
                <div class="d-flex flex-wrap gap-2">
                    <a class="filter-chip ${empty selectedStatus || selectedStatus == 'all' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=all&keyword=${fn:escapeXml(keyword)}">Tất cả</a>
                    <a class="filter-chip ${selectedStatus == 'Awaiting Payment' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Awaiting Payment&keyword=${fn:escapeXml(keyword)}">Chờ thanh toán</a>
                    <a class="filter-chip ${selectedStatus == 'Paid' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Paid&keyword=${fn:escapeXml(keyword)}">Đã thanh toán</a>
                    <a class="filter-chip ${selectedStatus == 'Pending' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Pending&keyword=${fn:escapeXml(keyword)}">Chờ xử lý</a>
                    <a class="filter-chip ${selectedStatus == 'Confirmed' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Confirmed&keyword=${fn:escapeXml(keyword)}">Đã xác nhận</a>
                    <a class="filter-chip ${selectedStatus == 'Shipping' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Shipping&keyword=${fn:escapeXml(keyword)}">Đang giao</a>
                    <a class="filter-chip ${selectedStatus == 'Completed' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Completed&keyword=${fn:escapeXml(keyword)}">Hoàn thành</a>
                    <a class="filter-chip ${selectedStatus == 'Cancelled' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?status=Cancelled&keyword=${fn:escapeXml(keyword)}">Đã hủy</a>
                </div>
                <form action="${pageContext.request.contextPath}/admin/orders" method="get" class="search-form">
                    <input type="hidden" name="status" value="${empty selectedStatus ? 'all' : selectedStatus}">
                    <input type="text" class="form-control" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tìm theo mã đơn, tên khách, số điện thoại...">
                    <button class="btn btn-primary" type="submit"><i class='bx bx-search'></i> Tìm</button>
                </form>
            </div>

            <c:if test="${pendingPaymentReviewCount > 0}">
                <div class="review-alert">
                    <strong>${pendingPaymentReviewCount}</strong> đơn hàng đang chờ admin đối soát thanh toán chuyển khoản.
                </div>
            </c:if>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 80px;">Mã ĐH</th>
                        <th>Khách hàng</th>
                        <th style="width: 110px;">Sản phẩm</th>
                        <th>Thanh toán</th>
                        <th style="width: 130px;">Tổng tiền</th>
                        <th style="width: 150px;">Ngày đặt</th>
                        <th style="width: 140px;">Trạng thái</th>
                        <th style="width: 100px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty orders}">
                        <tr>
                            <td colspan="8" class="text-center py-5">Không có đơn hàng phù hợp.</td>
                        </tr>
                    </c:if>
                    <c:forEach items="${orders}" var="o">
                        <tr>
                            <td><span class="order-id">#${o.id}</span></td>
                            <td class="customer-info">
                                <p><strong>${fn:escapeXml(o.fullname)}</strong></p>
                                <p class="text-muted"><i class='bx bx-phone'></i> ${fn:escapeXml(o.phone)}</p>
                                <p class="text-muted small">${fn:escapeXml(o.address)}</p>
                            </td>
                            <td><span class="badge bg-light text-dark">${o.itemCount} SP</span></td>
                            <td>
                                <div>${fn:escapeXml(o.paymentMethodLabel)}</div>
                                <small class="text-muted">${fn:escapeXml(o.paymentFlowLabel)}</small>
                                <div>
                                    <span class="payment-badge ${o.paymentVerificationCssClass}">
                                        ${fn:escapeXml(o.paymentVerificationLabel)}
                                    </span>
                                </div>
                                <c:if test="${not empty o.paymentReference}">
                                    <br><small class="text-muted">Ref: ${fn:escapeXml(o.paymentReference)}</small>
                                </c:if>
                                <c:if test="${not empty o.paymentVerificationMessage}">
                                    <br><small class="text-muted">${fn:escapeXml(o.paymentVerificationMessage)}</small>
                                </c:if>
                            </td>
                            <td><span class="total-amount">${fn:escapeXml(o.formattedTotalAmount)}</span></td>
                            <td><fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <span class="status-badge ${o.statusCssClass}">${fn:escapeXml(o.statusLabel)}</span>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <a href="${pageContext.request.contextPath}/admin/orders?action=view&id=${o.id}" class="action-btn edit" title="Xem chi tiết">
                                        <i class='bx bx-show'></i>
                                    </a>
                                    <c:if test="${o.awaitingPaymentReview}">
                                        <button class="action-btn edit" onclick="openPaymentModal(${o.id}, '${fn:escapeXml(o.paymentVerificationStatus)}', '${fn:escapeXml(o.paymentReference)}')" title="Đối soát thanh toán">
                                            <i class='bx bx-check-circle'></i>
                                        </button>
                                    </c:if>
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

    <div class="modal-overlay" id="updateStatusModal">
        <div class="modal-box" style="max-width: 400px;">
            <div class="modal-header">
                <h3 class="modal-title">Cập nhật đơn hàng</h3>
                <button class="modal-close" onclick="closeModal()"><i class='bx bx-x'></i></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/orders" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <input type="hidden" name="action" value="updateStatus">
                <input type="hidden" name="orderId" id="modalOrderId">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Trạng thái mới</label>
                        <select name="status" id="modalStatus" class="form-select">
                            <option value="Awaiting Payment">Chờ thanh toán</option>
                            <option value="Paid">Đã thanh toán</option>
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

    <div class="modal-overlay" id="paymentReviewModal">
        <div class="modal-box" style="max-width: 460px;">
            <div class="modal-header">
                <h3 class="modal-title">Duyệt thanh toán chuyển khoản</h3>
                <button class="modal-close" onclick="closePaymentModal()"><i class='bx bx-x'></i></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/orders" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <input type="hidden" name="action" value="updatePaymentVerification">
                <input type="hidden" name="orderId" id="paymentModalOrderId">
                <div class="modal-body">
                    <div class="mb-3 text-muted small">
                        Mã chuyển khoản: <strong id="paymentModalReference">Chưa có</strong>
                    </div>
                    <div class="form-group mb-3">
                        <label class="form-label">Trạng thái đối soát</label>
                        <select name="verificationStatus" id="paymentModalStatus" class="form-select">
                            <option value="PENDING">Tiếp tục chờ đối soát</option>
                            <option value="VERIFIED">Đã nhận tiền</option>
                            <option value="FAILED">Đối soát chưa khớp</option>
                            <option value="EXPIRED">Quá hạn thanh toán</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Ghi chú</label>
                        <textarea name="verificationMessage" class="form-control" rows="3" placeholder="Ví dụ: Đã khớp nội dung chuyển khoản / cần kiểm tra lại sao kê..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closePaymentModal()">Đóng</button>
                    <button type="submit" class="btn btn-primary">Lưu đối soát</button>
                </div>
            </form>
        </div>
    </div>

    <%-- Pagination controls --%>
    <c:if test="${totalPages > 1}">
    <div class="d-flex justify-content-center align-items-center gap-2 py-4">
        <c:if test="${currentPage > 1}">
            <a href="${pageContext.request.contextPath}/admin/orders?page=${currentPage-1}&size=${pageSize}&status=${selectedStatus}&keyword=${fn:escapeXml(keyword)}"
               class="btn btn-sm btn-outline-secondary"><i class='bx bx-chevron-left'></i> Trước</a>
        </c:if>
        <c:forEach begin="1" end="${totalPages}" var="p">
            <c:choose>
                <c:when test="${p == currentPage}">
                    <span class="btn btn-sm btn-primary">${p}</span>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/admin/orders?page=${p}&size=${pageSize}&status=${selectedStatus}&keyword=${fn:escapeXml(keyword)}"
                       class="btn btn-sm btn-outline-secondary">${p}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:if test="${currentPage < totalPages}">
            <a href="${pageContext.request.contextPath}/admin/orders?page=${currentPage+1}&size=${pageSize}&status=${selectedStatus}&keyword=${fn:escapeXml(keyword)}"
               class="btn btn-sm btn-outline-secondary">Sau <i class='bx bx-chevron-right'></i></a>
        </c:if>
    </div>
    </c:if>

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
        function openPaymentModal(id, status, reference) {
            document.getElementById('paymentModalOrderId').value = id;
            document.getElementById('paymentModalStatus').value = status || 'PENDING';
            document.getElementById('paymentModalReference').textContent = reference || 'Chưa có';
            document.getElementById('paymentReviewModal').classList.add('show');
        }
        function closePaymentModal() {
            document.getElementById('paymentReviewModal').classList.remove('show');
        }
    </script>
</body>
</html>
