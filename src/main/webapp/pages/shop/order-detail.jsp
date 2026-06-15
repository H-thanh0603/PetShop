<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng - PetShop</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <style>
        body { background-color: #f8fafc; font-family: 'Segoe UI', sans-serif; }
        .detail-container { max-width: 1100px; margin: 40px auto; padding: 20px; }
        .card { border: none; border-radius: 18px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); overflow: hidden; }
        .card-header { background: #1e293b; color: white; padding: 20px; border: none; }
        .product-item { border-bottom: 1px solid #f1f5f9; padding: 15px 0; }
        .product-item:last-child { border-bottom: none; }
        .product-img { width: 70px; height: 70px; object-fit: cover; border-radius: 12px; border: 1px solid #e2e8f0; }
        .info-section { background: #fff; padding: 25px; border-radius: 18px; margin-bottom: 25px; }
        .label { color: #64748b; font-size: 0.8rem; font-weight: 700; text-transform: uppercase; margin-bottom: 5px; }
        .value { color: #1e293b; font-weight: 500; margin-bottom: 15px; }
        .status-badge { font-weight: 700; font-size: 0.85rem; padding: 5px 12px; border-radius: 20px; }
        .payment-badge { display: inline-flex; align-items: center; gap: 6px; padding: 5px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 700; margin-top: 8px; }
        .payment-pending { background: #fff7ed; color: #c2410c; }
        .payment-verified { background: #dcfce7; color: #166534; }
        .payment-failed { background: #fee2e2; color: #991b1b; }
        .payment-neutral { background: #e0f2fe; color: #075985; }
        .payment-unpaid { background: #f1f5f9; color: #475569; }
        .payment-expired { background: #fef2f2; color: #b91c1c; }
        .timeline-step { display: flex; gap: 12px; margin-bottom: 18px; }
        .timeline-dot { width: 40px; height: 40px; border-radius: 50%; display: grid; place-items: center; background: #e2e8f0; color: #64748b; flex-shrink: 0; }
        .timeline-step.active .timeline-dot { background: #dbeafe; color: #2563eb; }
        .timeline-step.done .timeline-dot { background: #dcfce7; color: #16a34a; }
    </style>
    <link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />
    <jsp:include page="/components/toast.jsp" />

    <div class="container detail-container">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
            <div>
                <h3 class="mb-1 fw-bold">Chi tiết đơn hàng #${order.id}</h3>
                <div class="text-muted">
                    <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/> · ${order.statusLabel}
                </div>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <c:if test="${order.repayable}">
                    <form action="${pageContext.request.contextPath}/my-orders" method="post">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="action" value="repay">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <button type="submit" class="btn btn-warning">
                            <i class='bx bx-credit-card'></i> Thanh toán lại
                        </button>
                    </form>
                </c:if>
                <c:if test="${order.cancelableByUser}">
                    <form action="${pageContext.request.contextPath}/my-orders" method="post" onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn hàng này?');">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <button type="submit" class="btn btn-outline-danger">
                            <i class='bx bx-x-circle'></i> Hủy đơn
                        </button>
                    </form>
                </c:if>
                <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-outline-secondary">
                    <i class='bx bx-arrow-back'></i> Quay lại
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <span>Sản phẩm trong đơn</span>
                        <span class="status-badge ${order.statusCssClass}">${fn:escapeXml(order.statusLabel)}</span>
                    </div>
                    <div class="card-body">
                        <c:forEach var="item" items="${order.items}">
                            <div class="product-item d-flex align-items-center gap-3">
                                <img loading="lazy" src="${fn:startsWith(item.product.image, 'http') ? fn:escapeXml(item.product.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(item.product.image)}"
                                     class="product-img"
                                     onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'">
                                <div class="flex-grow-1">
                                    <h6 class="mb-0 fw-bold">${fn:escapeXml(item.product.name)}</h6>
                                    <small class="text-muted">Số lượng: ${item.quantity}</small>
                                </div>
                                <div class="text-end">
                                    <p class="mb-0 fw-bold"><fmt:formatNumber value="${item.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></p>
                                    <small class="text-muted"><fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/> / sản phẩm</small>
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
                    <h5 class="fw-bold mb-4">Thông tin giao hàng</h5>
                    <div class="label">Người nhận</div>
                    <div class="value">${fn:escapeXml(order.fullname)}</div>

                    <div class="label">Số điện thoại</div>
                    <div class="value">${fn:escapeXml(order.phone)}</div>

                    <div class="label">Địa chỉ giao hàng</div>
                    <div class="value">${fn:escapeXml(order.address)}</div>

                    <div class="label">Thanh toán</div>
                    <div class="value">
                        ${fn:escapeXml(order.paymentMethodLabel)} · ${fn:escapeXml(order.paymentFlowLabel)}
                        <br><span class="payment-badge ${order.paymentVerificationCssClass}">${fn:escapeXml(order.paymentVerificationLabel)}</span>
                        <c:if test="${not empty order.paymentReference}">
                            <br><span class="text-muted small">Mã chuyển khoản: ${fn:escapeXml(order.paymentReference)}</span>
                        </c:if>
                        <c:if test="${not empty order.paymentVerificationMessage}">
                            <br><span class="text-muted small">${fn:escapeXml(order.paymentVerificationMessage)}</span>
                        </c:if>
                        <c:if test="${not empty order.paymentVerifiedAt}">
                            <br><span class="text-muted small">Xác nhận lúc: <fmt:formatDate value="${order.paymentVerifiedAt}" pattern="dd/MM/yyyy HH:mm:ss"/></span>
                        </c:if>
                    </div>

                    <div class="label">Ghi chú từ khách</div>
                    <div class="value">${empty order.note ? 'Không có ghi chú.' : fn:escapeXml(order.note)}</div>
                </div>

                <div class="info-section shadow-sm">
                    <h5 class="fw-bold mb-4">Tiến trình đơn hàng</h5>

                    <div class="timeline-step done">
                        <div class="timeline-dot"><i class='bx bx-receipt'></i></div>
                        <div><div class="fw-bold">Đặt hàng</div><div class="text-muted small">Đơn hàng đã được ghi nhận.</div></div>
                    </div>
                    <div class="timeline-step ${order.status == 'Pending' ? 'active' : (order.status == 'Confirmed' || order.status == 'Shipping' || order.status == 'Completed' ? 'done' : '')}">
                        <div class="timeline-dot"><i class='bx bx-check-shield'></i></div>
                        <div><div class="fw-bold">Xác nhận</div><div class="text-muted small">Cửa hàng kiểm tra và chuẩn bị sản phẩm.</div></div>
                    </div>
                    <div class="timeline-step ${order.status == 'Shipping' || order.status == 'Completed' ? 'done' : ''}">
                        <div class="timeline-dot"><i class='bx bx-car'></i></div>
                        <div><div class="fw-bold">Vận chuyển</div><div class="text-muted small">Đơn hàng đang trên đường giao tới bạn.</div></div>
                    </div>
                    <div class="timeline-step ${order.status == 'Completed' ? 'done' : ''}">
                        <div class="timeline-dot"><i class='bx bx-home-heart'></i></div>
                        <div><div class="fw-bold">Hoàn tất</div><div class="text-muted small">Bạn đã nhận hàng thành công.</div></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
