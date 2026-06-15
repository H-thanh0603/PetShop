<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Chi tiết đơn hàng - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .order-info-card { background: white; border-radius: 16px; padding: 25px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 25px; }
        .info-label { color: #64748b; font-size: 0.85rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
        .info-value { color: #1e293b; font-size: 1rem; font-weight: 500; margin-bottom: 15px; }
        .product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; }
        .total-row { font-size: 1.25rem; font-weight: 800; color: #10314d; }
        .timeline-step { display: flex; gap: 12px; margin-bottom: 18px; }
        .timeline-dot { width: 40px; height: 40px; border-radius: 50%; display: grid; place-items: center; background: #e2e8f0; color: #64748b; flex-shrink: 0; }
        .timeline-step.active .timeline-dot { background: #dbeafe; color: #2563eb; }
        .timeline-step.done .timeline-dot { background: #dcfce7; color: #16a34a; }
        .payment-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 700; margin-top: 8px; }
        .payment-pending { background: #fff7ed; color: #c2410c; }
        .payment-verified { background: #dcfce7; color: #166534; }
        .payment-failed { background: #fee2e2; color: #991b1b; }
        .payment-neutral { background: #e0f2fe; color: #075985; }
        .payment-unpaid { background: #f1f5f9; color: #475569; }
        .payment-expired { background: #fef2f2; color: #b91c1c; }
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
                    <div class="info-value text-primary fw-bold">${fn:escapeXml(order.fullname)}</div>

                    <div class="info-label">Số điện thoại</div>
                    <div class="info-value">${fn:escapeXml(order.phone)}</div>

                    <div class="info-label">Địa chỉ</div>
                    <div class="info-value">${fn:escapeXml(order.address)}</div>

                    <div class="info-label">Ngày đặt</div>
                    <div class="info-value"><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></div>

                    <div class="info-label">Thanh toán</div>
                    <div class="info-value">
                        ${fn:escapeXml(order.paymentMethodLabel)} · ${fn:escapeXml(order.paymentFlowLabel)}
                        <br><span class="payment-badge ${order.paymentVerificationCssClass}">${fn:escapeXml(order.paymentVerificationLabel)}</span>
                        <c:if test="${not empty order.paymentReference}">
                            <br><span class="text-muted small">Mã chuyển khoản: ${fn:escapeXml(order.paymentReference)}</span>
                        </c:if>
                        <c:if test="${not empty order.paymentVerificationMessage}">
                            <br><span class="text-muted small">${fn:escapeXml(order.paymentVerificationMessage)}</span>
                        </c:if>
                        <c:if test="${not empty order.paymentVerifiedAt}">
                            <br><span class="text-muted small">Thời gian xác nhận: <fmt:formatDate value="${order.paymentVerifiedAt}" pattern="dd/MM/yyyy HH:mm:ss"/></span>
                        </c:if>
                    </div>

                    <div class="info-label">Trạng thái hiện tại</div>
                    <div class="info-value">
                        <span class="badge ${order.statusCssClass}">${fn:escapeXml(order.statusLabel)}</span>
                    </div>

                    <div class="info-label">Ghi chú</div>
                    <div class="info-value">${empty order.note ? 'Không có ghi chú.' : fn:escapeXml(order.note)}</div>
                </div>

                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold">Tiến trình đơn hàng</h5>
                    <div class="timeline-step done">
                        <div class="timeline-dot"><i class='bx bx-receipt'></i></div>
                        <div><strong>Đặt hàng</strong><div class="text-muted small">Khách đã tạo đơn thành công.</div></div>
                    </div>
                    <div class="timeline-step ${order.status == 'Pending' ? 'active' : (order.status == 'Confirmed' || order.status == 'Shipping' || order.status == 'Completed' ? 'done' : '')}">
                        <div class="timeline-dot"><i class='bx bx-check-shield'></i></div>
                        <div><strong>Xác nhận</strong><div class="text-muted small">Admin xác nhận và chuẩn bị đơn.</div></div>
                    </div>
                    <div class="timeline-step ${order.status == 'Shipping' || order.status == 'Completed' ? 'done' : ''}">
                        <div class="timeline-dot"><i class='bx bx-car'></i></div>
                        <div><strong>Vận chuyển</strong><div class="text-muted small">Đơn hàng đang trên đường giao.</div></div>
                    </div>
                    <div class="timeline-step ${order.status == 'Completed' ? 'done' : ''}">
                        <div class="timeline-dot"><i class='bx bx-home-heart'></i></div>
                        <div><strong>Hoàn tất</strong><div class="text-muted small">Khách đã nhận hàng thành công.</div></div>
                    </div>
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
                                            <img loading="lazy" src="${fn:startsWith(item.product.image, 'http') ? fn:escapeXml(item.product.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(item.product.image)}"
                                                 class="product-img"
                                                 onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'">
                                        </td>
                                        <td class="align-middle fw-bold">${fn:escapeXml(item.product.name)}</td>
                                        <td class="align-middle text-center">${item.quantity}</td>
                                        <td class="align-middle text-end">
                                            <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                        </td>
                                        <td class="align-middle text-end">
                                            <fmt:formatNumber value="${item.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="4" class="text-end fw-bold py-3">TỔNG CỘNG:</td>
                                    <td class="text-end total-row py-3">
                                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold">Cập nhật trạng thái</h5>
                    <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="row g-3">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <input type="hidden" name="returnTo" value="detail">
                        <div class="col-md-8">
                            <select name="status" class="form-select">
                                <option value="Awaiting Payment" ${order.status == 'Awaiting Payment' ? 'selected' : ''}>Chờ thanh toán</option>
                                <option value="Paid" ${order.status == 'Paid' ? 'selected' : ''}>Đã thanh toán</option>
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

                <c:if test="${order.bankTransferPayment}">
                    <div class="order-info-card">
                        <h5 class="mb-4 fw-bold">Duyệt thanh toán chuyển khoản</h5>
                        <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="row g-3">
                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                            <input type="hidden" name="action" value="updatePaymentVerification">
                            <input type="hidden" name="orderId" value="${order.id}">
                            <input type="hidden" name="returnTo" value="detail">
                            <div class="col-md-5">
                                <label class="form-label">Trạng thái đối soát</label>
                                <select name="verificationStatus" class="form-select">
                                    <option value="PENDING" ${order.paymentVerificationStatus == 'PENDING' ? 'selected' : ''}>Tiếp tục chờ đối soát</option>
                                    <option value="VERIFIED" ${order.paymentVerificationStatus == 'VERIFIED' ? 'selected' : ''}>Đã nhận tiền</option>
                                    <option value="FAILED" ${order.paymentVerificationStatus == 'FAILED' ? 'selected' : ''}>Đối soát chưa khớp</option>
                                    <option value="EXPIRED" ${order.paymentVerificationStatus == 'EXPIRED' ? 'selected' : ''}>Quá hạn thanh toán</option>
                                </select>
                            </div>
                            <div class="col-md-7">
                                <label class="form-label">Ghi chú</label>
                                <input type="text" name="verificationMessage" class="form-control"
                                       value="${fn:escapeXml(order.paymentVerificationMessage)}"
                                       placeholder="Nhập ghi chú để admin khác dễ theo dõi">
                            </div>
                            <div class="col-12">
                                <button type="submit" class="btn btn-primary">Lưu trạng thái thanh toán</button>
                            </div>
                        </form>
                    </div>
                </c:if>

                <%-- Status History --%>
                <c:if test="${not empty statusHistory}">
                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold"><i class='bx bx-history'></i> Lịch sử thay đổi trạng thái</h5>
                    <div class="table-responsive">
                        <table class="table table-sm table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Trạng thái cũ</th>
                                    <th>Trạng thái mới</th>
                                    <th>Người thực hiện</th>
                                    <th>Thời gian</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="h" items="${statusHistory}">
                                    <tr>
                                        <td><span class="badge bg-secondary">${h.oldStatus}</span></td>
                                        <td><span class="badge bg-primary">${h.newStatus}</span></td>
                                        <td>${fn:escapeXml(h.changedByName)}</td>
                                        <td><fmt:formatDate value="${h.changedAt}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                </c:if>

                <c:if test="${not empty orderLogs}">
                <div class="order-info-card">
                    <h5 class="mb-4 fw-bold"><i class='bx bx-list-check'></i> Nhật ký hoạt động</h5>
                    <div class="table-responsive">
                        <table class="table table-sm table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Thời gian</th>
                                    <th>Actor</th>
                                    <th>Hành động</th>
                                    <th>Trạng thái</th>
                                    <th>Ghi chú</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="l" items="${orderLogs}">
                                    <tr>
                                        <td><fmt:formatDate value="${l.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                                        <td>${fn:escapeXml(l.actorType)}<c:if test="${not empty l.actorId}"> #${l.actorId}</c:if></td>
                                        <td>${fn:escapeXml(l.action)}</td>
                                        <td>
                                            <span class="badge bg-secondary">${fn:escapeXml(l.oldStatus)}</span>
                                            <i class='bx bx-right-arrow-alt'></i>
                                            <span class="badge bg-primary">${fn:escapeXml(l.newStatus)}</span>
                                        </td>
                                        <td>${fn:escapeXml(l.note)}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                </c:if>
            </div>
        </div>
    </main>

    <jsp:include page="/components/scripts.jsp" />
</body>
</html>
