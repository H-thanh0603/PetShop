<%--
  Created by IntelliJ IDEA.
  User: HUU DAT
  Date: 6/12/2026
  Time: 11:20 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt hàng thành công</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        :root {
            --primary: #1d9e75;
            --primary-light: #e1f5ee;
            --primary-dark: #0f6e56;
            --text-main: #1a1a1a;
            --text-sub: #5f5e5a;
            --text-hint: #888780;
            --border: #e4e3dc;
            --bg-page: #f7f6f2;
            --bg-card: #ffffff;
            --bg-surface: #f1efe8;
            --amber: #ba7517;
            --amber-light: #faeeda;
            --danger: #a32d2d;
            --danger-light: #fcebeb;
            --radius-md: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }

        body {
            font-family: 'Inter', sans-serif;
            background: var(--bg-page);
            color: var(--text-main);
            min-height: 100vh;
            padding: 2rem 1rem 4rem;
            font-size: 15px;
            line-height: 1.6;
        }

        .page-wrap {
            max-width: 680px;
            margin: 0 auto;
        }

        /* ── Header ── */
        .success-header {
            text-align: center;
            padding: 2.5rem 1rem 2rem;
        }

        .icon-ring {
            width: 64px;
            height: 64px;
            border-radius: 50%;
            margin: 0 auto 1.25rem;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .icon-ring.green {
            background: var(--primary-light);
        }
        .icon-ring.amber {
            background: var(--amber-light);
        }
        .icon-ring svg {
            width: 30px;
            height: 30px;
        }

        .success-header h1 {
            font-size: 22px;
            font-weight: 600;
            color: var(--text-main);
            margin-bottom: 6px;
        }
        .success-header p {
            font-size: 14px;
            color: var(--text-sub);
        }
        .order-id-badge {
            display: inline-block;
            margin-top: 10px;
            background: var(--bg-surface);
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 4px 14px;
            font-size: 13px;
            font-weight: 500;
            color: var(--text-sub);
            letter-spacing: 0.02em;
        }

        /* ── Bank transfer alert ── */
        .bank-alert {
            background: var(--amber-light);
            border: 1px solid #f5c47588;
            border-radius: var(--radius-lg);
            padding: 1rem 1.25rem;
            margin-bottom: 1.25rem;
            display: flex;
            gap: 12px;
            align-items: flex-start;
        }
        .bank-alert svg { flex-shrink: 0; margin-top: 2px; }
        .bank-alert-text { font-size: 14px; }
        .bank-alert-text strong { font-weight: 600; color: var(--amber); display: block; margin-bottom: 3px; }
        .bank-alert-text span { color: var(--text-sub); }

        /* ── Timer countdown ── */
        .countdown-bar {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 1rem 1.25rem;
            margin-bottom: 1.25rem;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .countdown-digits {
            font-size: 22px;
            font-weight: 600;
            color: var(--amber);
            min-width: 64px;
            font-variant-numeric: tabular-nums;
        }
        .countdown-label { font-size: 13px; color: var(--text-sub); }
        .countdown-expired { color: var(--danger); }

        /* ── QR + Bank info ── */
        .bank-transfer-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 1.5rem;
            margin-bottom: 1.25rem;
        }
        .bank-card-title {
            font-size: 13px;
            font-weight: 500;
            color: var(--text-hint);
            text-transform: uppercase;
            letter-spacing: 0.06em;
            margin-bottom: 1rem;
        }
        .bank-layout {
            display: flex;
            gap: 1.5rem;
            align-items: flex-start;
        }
        .qr-wrapper {
            flex-shrink: 0;
            width: 130px;
            height: 130px;
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            overflow: hidden;
            background: #fff;
        }
        .qr-wrapper img {
            width: 100%;
            height: 100%;
            object-fit: contain;
            display: block;
        }
        .bank-details { flex: 1; min-width: 0; }
        .bank-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 7px 0;
            border-bottom: 1px solid var(--border);
            gap: 8px;
        }
        .bank-row:last-child { border-bottom: none; }
        .bank-row-label { font-size: 13px; color: var(--text-hint); flex-shrink: 0; }
        .bank-row-value {
            font-size: 14px;
            font-weight: 500;
            color: var(--text-main);
            text-align: right;
            word-break: break-all;
        }
        .copy-btn {
            background: none;
            border: 1px solid var(--border);
            border-radius: 6px;
            padding: 3px 8px;
            font-size: 12px;
            cursor: pointer;
            color: var(--text-sub);
            white-space: nowrap;
            flex-shrink: 0;
            transition: background 0.15s;
        }
        .copy-btn:hover { background: var(--bg-surface); }
        .copy-btn.copied { color: var(--primary); border-color: var(--primary); }

        .transfer-ref-row .bank-row-value {
            color: var(--primary-dark);
            font-weight: 600;
        }

        /* ── Section card ── */
        .section-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 1.25rem 1.5rem;
            margin-bottom: 1.25rem;
        }
        .section-card-title {
            font-size: 13px;
            font-weight: 500;
            color: var(--text-hint);
            text-transform: uppercase;
            letter-spacing: 0.06em;
            margin-bottom: 0.875rem;
        }

        /* ── Order items ── */
        .order-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 0;
            border-bottom: 1px solid var(--border);
        }
        .order-item:last-child { border-bottom: none; padding-bottom: 0; }
        .order-item:first-child { padding-top: 0; }
        .item-img {
            width: 52px;
            height: 52px;
            border-radius: var(--radius-md);
            object-fit: cover;
            border: 1px solid var(--border);
            flex-shrink: 0;
            background: var(--bg-surface);
        }
        .item-info { flex: 1; min-width: 0; }
        .item-name {
            font-size: 14px;
            font-weight: 500;
            color: var(--text-main);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .item-meta { font-size: 13px; color: var(--text-hint); margin-top: 2px; }
        .item-price {
            font-size: 14px;
            font-weight: 600;
            color: var(--text-main);
            white-space: nowrap;
        }

        /* ── Info rows (address / customer) ── */
        .info-row {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            padding: 7px 0;
            border-bottom: 1px solid var(--border);
        }
        .info-row:last-child { border-bottom: none; }
        .info-row-icon { color: var(--text-hint); margin-top: 2px; flex-shrink: 0; }
        .info-row-label { font-size: 13px; color: var(--text-hint); min-width: 90px; flex-shrink: 0; }
        .info-row-value { font-size: 14px; color: var(--text-main); font-weight: 500; }

        /* ── Price summary ── */
        .price-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            padding: 5px 0;
            color: var(--text-sub);
        }
        .price-row.total {
            border-top: 1px solid var(--border);
            margin-top: 6px;
            padding-top: 12px;
            font-size: 17px;
            font-weight: 600;
            color: var(--text-main);
        }
        .price-row.discount .price-val { color: var(--primary-dark); }
        .shipping-free { color: var(--primary); font-weight: 500; }

        /* ── Payment method badge ── */
        .payment-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            font-weight: 500;
            border-radius: 20px;
            padding: 4px 12px;
        }
        .payment-badge.cod {
            background: #e1f5ee;
            color: var(--primary-dark);
        }
        .payment-badge.bank {
            background: var(--amber-light);
            color: var(--amber);
        }
        .payment-badge svg { width: 14px; height: 14px; }

        /* ── CTA buttons ── */
        .cta-group {
            display: flex;
            gap: 10px;
            margin-top: 2rem;
        }
        .btn {
            flex: 1;
            padding: 12px 16px;
            border-radius: var(--radius-md);
            font-size: 15px;
            font-weight: 500;
            cursor: pointer;
            text-align: center;
            text-decoration: none;
            border: 1px solid transparent;
            transition: opacity 0.15s, background 0.15s;
            display: inline-block;
        }
        .btn-primary {
            background: var(--primary);
            color: #fff;
        }
        .btn-primary:hover { opacity: 0.88; }
        .btn-outline {
            background: var(--bg-card);
            color: var(--text-main);
            border-color: var(--border);
        }
        .btn-outline:hover { background: var(--bg-surface); }

        /* ── Status steps (bank transfer) ── */
        .steps-track {
            display: flex;
            align-items: flex-start;
            gap: 0;
            margin-top: 0.5rem;
        }
        .step {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 6px;
            position: relative;
        }
        .step:not(:last-child)::after {
            content: '';
            position: absolute;
            top: 12px;
            left: 50%;
            width: 100%;
            height: 2px;
            background: var(--border);
        }
        .step.done::after { background: var(--primary); }
        .step-dot {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            border: 2px solid var(--border);
            background: var(--bg-card);
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            z-index: 1;
        }
        .step.done .step-dot {
            background: var(--primary);
            border-color: var(--primary);
        }
        .step.active .step-dot {
            border-color: var(--amber);
            background: var(--amber-light);
        }
        .step-dot svg { width: 12px; height: 12px; }
        .step-label { font-size: 11px; color: var(--text-hint); text-align: center; line-height: 1.3; }
        .step.done .step-label { color: var(--primary-dark); }
        .step.active .step-label { color: var(--amber); font-weight: 500; }

        @media (max-width: 520px) {
            .bank-layout { flex-direction: column; }
            .qr-wrapper { width: 100%; height: auto; aspect-ratio: 1; }
            .cta-group { flex-direction: column; }
        }
    </style>
</head>
<body>
<div class="page-wrap">

    <%-- ────────────────────────────────────────────
         Header: icon + tiêu đề
    ─────────────────────────────────────────────── --%>
    <div class="success-header">
        <c:choose>
            <c:when test="${pendingVerification}">
                <div class="icon-ring amber">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#ba7517" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="12" y1="8" x2="12" y2="12"/>
                        <line x1="12" y1="16" x2="12.01" y2="16"/>
                    </svg>
                </div>
                <h1>Đặt hàng thành công — chờ thanh toán</h1>
                <p>Đơn hàng đã được tạo. Vui lòng chuyển khoản đúng nội dung để xác nhận.</p>
            </c:when>
            <c:otherwise>
                <div class="icon-ring green">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#1d9e75" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6L9 17l-5-5"/>
                    </svg>
                </div>
                <h1>Đặt hàng thành công!</h1>
                <p>Cảm ơn bạn đã mua sắm. Chúng tôi sẽ xử lý đơn hàng ngay.</p>
            </c:otherwise>
        </c:choose>
        <span class="order-id-badge">Mã đơn hàng #${orderId}</span>
    </div>

    <%-- ────────────────────────────────────────────
         TRẠNG THÁI ĐƠN HÀNG (timeline)
    ─────────────────────────────────────────────── --%>
    <div class="section-card">
        <p class="section-card-title">Trạng thái đơn hàng</p>
        <div class="steps-track">
            <div class="step done">
                <div class="step-dot">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
                </div>
                <span class="step-label">Đặt hàng<br>thành công</span>
            </div>
            <c:choose>
                <c:when test="${pendingVerification}">
                    <div class="step active">
                        <div class="step-dot">
                            <svg viewBox="0 0 24 24" fill="none" stroke="#ba7517" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/></svg>
                        </div>
                        <span class="step-label">Chờ xác nhận<br>thanh toán</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="step done">
                        <div class="step-dot">
                            <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
                        </div>
                        <span class="step-label">Đã xác nhận<br>thanh toán</span>
                    </div>
                </c:otherwise>
            </c:choose>
            <div class="step">
                <div class="step-dot"></div>
                <span class="step-label">Đang chuẩn<br>bị hàng</span>
            </div>
            <div class="step">
                <div class="step-dot"></div>
                <span class="step-label">Đang giao<br>hàng</span>
            </div>
            <div class="step">
                <div class="step-dot"></div>
                <span class="step-label">Đã giao<br>hàng</span>
            </div>
        </div>
    </div>

    <%-- ────────────────────────────────────────────
         BANK TRANSFER: cảnh báo + đếm ngược + QR
    ─────────────────────────────────────────────── --%>
    <c:if test="${pendingVerification}">
        <div class="bank-alert">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ba7517" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <div class="bank-alert-text">
                <strong>Quan trọng: chuyển khoản đúng nội dung</strong>
                <span>Nội dung chuyển khoản phải khớp chính xác mã tham chiếu bên dưới để hệ thống tự động xác nhận. Không chuyển sai nội dung hoặc số tiền.</span>
            </div>
        </div>

        <div class="countdown-bar">
            <div>
                <div class="countdown-digits" id="countdown-display">--:--</div>
                <div class="countdown-label">thời gian còn lại để hoàn tất chuyển khoản</div>
            </div>
            <div style="margin-left:auto; text-align:right;">
                <div style="font-size:13px;color:var(--text-hint);">Hết hạn lúc</div>
                <div style="font-size:14px;font-weight:500;" id="expire-time-display">${paymentExpiresAt}</div>
            </div>
        </div>

        <div class="bank-transfer-card">
            <p class="bank-card-title">Thông tin chuyển khoản</p>
            <div class="bank-layout">
                <div class="qr-wrapper">
                        <%-- VietQR dynamic QR --%>
                    <img
                            src="https://img.vietqr.io/image/${bankId}-${bankAccountNumber}-compact2.png?amount=${finalTotal}&addInfo=${transferReference}&accountName=${bankAccountName}"
                            alt="QR chuyển khoản"
                            onerror="this.style.display='none'; document.getElementById('qr-fallback').style.display='flex';"
                    />
                    <div id="qr-fallback" style="display:none;width:100%;height:100%;align-items:center;justify-content:center;font-size:12px;color:var(--text-hint);text-align:center;padding:8px;">
                        Không tải được QR
                    </div>
                </div>
                <div class="bank-details">
                    <div class="bank-row">
                        <span class="bank-row-label">Ngân hàng</span>
                        <span class="bank-row-value">${bankDisplayName}</span>
                    </div>
                    <div class="bank-row">
                        <span class="bank-row-label">Số tài khoản</span>
                        <span class="bank-row-value" id="bank-acc">${bankAccountNumber}</span>
                        <button class="copy-btn" onclick="copyText('bank-acc', this)">Sao chép</button>
                    </div>
                    <div class="bank-row">
                        <span class="bank-row-label">Tên tài khoản</span>
                        <span class="bank-row-value">${bankAccountName}</span>
                    </div>
                    <div class="bank-row">
                        <span class="bank-row-label">Số tiền</span>
                        <span class="bank-row-value" id="transfer-amount">
                            <fmt:formatNumber value="${finalTotal}" pattern="#,###"/> đ
                        </span>
                        <button class="copy-btn" onclick="copyText('transfer-amount', this)">Sao chép</button>
                    </div>
                    <div class="bank-row transfer-ref-row">
                        <span class="bank-row-label">Nội dung CK</span>
                        <span class="bank-row-value" id="transfer-ref">${transferReference}</span>
                        <button class="copy-btn" onclick="copyText('transfer-ref', this)">Sao chép</button>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <%-- ────────────────────────────────────────────
         SẢN PHẨM ĐÃ ĐẶT
    ─────────────────────────────────────────────── --%>
    <c:if test="${not empty orderItems}">
        <div class="section-card">
            <p class="section-card-title">Sản phẩm đã đặt (${fn:length(orderItems)} sản phẩm)</p>
            <c:forEach var="item" items="${orderItems}">
                <div class="order-item">
                    <img class="item-img"
                         src="${not empty item.product.image ? item.product.image : pageContext.request.contextPath.concat('/assets/img/no-image.png')}"
                         alt="${item.product.name}" />
                    <div class="item-info">
                        <div class="item-name">${item.product.name}</div>
                        <div class="item-meta">x${item.quantity}</div>
                    </div>
                    <div class="item-price">
                        <fmt:formatNumber value="${item.totalPrice}" pattern="#,###"/> đ
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <%-- ────────────────────────────────────────────
         TỔNG KẾT GIÁ
    ─────────────────────────────────────────────── --%>
    <div class="section-card">
        <p class="section-card-title">Chi tiết thanh toán</p>

        <div class="price-row">
            <span>Tạm tính</span>
            <span class="price-val">
                <fmt:formatNumber value="${totalAmount}" pattern="#,###"/> đ
            </span>
        </div>

        <div class="price-row">
            <span>Phí vận chuyển</span>
            <c:choose>
                <c:when test="${shippingFee == 0}">
                    <span class="price-val shipping-free">Miễn phí</span>
                </c:when>
                <c:otherwise>
                    <span class="price-val">
                        <fmt:formatNumber value="${shippingFee}" pattern="#,###"/> đ
                    </span>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${discount > 0}">
            <div class="price-row discount">
                <span>Giảm giá (mã coupon)</span>
                <span class="price-val">
                    <fmt:formatNumber value="${discount}" pattern="#,###"/> đ
                </span>
            </div>
        </c:if>

        <div class="price-row total">
            <span>Tổng cộng</span>
            <span class="price-val">
                <fmt:formatNumber value="${finalTotal}" pattern="#,###"/> đ
            </span>
        </div>

        <div style="margin-top:12px; display:flex; justify-content:space-between; align-items:center;">
            <span style="font-size:13px;color:var(--text-hint);">Phương thức thanh toán</span>
            <c:choose>
                <c:when test="${paymentMethod eq 'VNPAY'}">
                    <span class="payment-badge bank">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg>
                        Chuyển khoản qua Vnpay
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="payment-badge cod">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/><path d="M12 6v6l4 2"/></svg>
                        Thanh toán khi nhận hàng
                    </span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- ────────────────────────────────────────────
         THÔNG TIN NHẬN HÀNG
    ─────────────────────────────────────────────── --%>
    <div class="section-card">
        <p class="section-card-title">Thông tin nhận hàng</p>

        <div class="info-row">
            <span class="info-row-label">Người nhận</span>
            <span class="info-row-value">${user.fullname}</span>
        </div>
        <div class="info-row">
            <span class="info-row-label">Số điện thoại</span>
            <span class="info-row-value">${user.phone}</span>
        </div>
        <c:if test="${not empty user.email}">
            <div class="info-row">
                <span class="info-row-label">Email</span>
                <span class="info-row-value">${user.email}</span>
            </div>
        </c:if>
        <c:if test="${not empty shippingAddress}">
            <div class="info-row">
                <span class="info-row-label">Địa chỉ</span>
                <span class="info-row-value">${shippingAddress}</span>
            </div>
        </c:if>
        <c:if test="${not empty orderNote}">
            <div class="info-row">
                <span class="info-row-label">Ghi chú</span>
                <span class="info-row-value">${orderNote}</span>
            </div>
        </c:if>
    </div>

    <%-- ────────────────────────────────────────────
         CTA
    ─────────────────────────────────────────────── --%>
    <div class="cta-group">
        <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-outline">Xem đơn hàng của tôi</a>
        <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary">Tiếp tục mua sắm</a>
    </div>

</div>

<!-- Modal chữ ký số -->
<div class="modal fade" id="signatureModal" tabindex="-1" aria-labelledby="signatureModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius:16px;padding:10px; color: #1a1a1a;">
            <div class="modal-header" style="border-bottom:1px solid #eee;">
                <h5 class="modal-title" id="signatureModalLabel" style="font-weight:700;">Tải công cụ ký điện tử</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3 text-start">
                    <label class="form-label" style="font-weight:600; display: block; margin-bottom: 8px;">Order Hash</label>
                    <div style="display: flex; gap: 8px;">
                        <input type="text" id="modalOrderHash" class="form-control" readonly value="${orderHash}" style="flex: 1; font-family:monospace;font-size:0.85rem;">
                        <button class="btn btn-outline-primary" type="button" id="btnCopyHash" onclick="navigator.clipboard.writeText(document.getElementById('modalOrderHash').value);this.innerText='Đã copy!';setTimeout(()=>this.innerText='Copy',2000);">Copy</button>
                    </div>
                </div>
                <p style="font-size:0.9rem;color:#555;margin-bottom:16px; text-align: left;">
                    Mở tool, nhập hash + private key, bấm <strong>Tạo chữ ký</strong>, rồi quay lại trang cá nhân để upload.
                </p>
                <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                    <a id="btnDownloadKey" href="data:application/octet-stream;base64,${privateKeyBase64}" download="private_key_${orderId}.pem" class="btn btn-outline-danger" style="flex: 1; min-width: 160px; padding: 12px; border-radius: 8px;">
                        🔑 Tải Private Key
                    </a>
                    <a id="btnDownloadTool" href="${toolUrl}" class="btn btn-primary" style="flex: 1; min-width: 160px; padding: 12px; border-radius: 8px; background: linear-gradient(45deg,#4a6cf7,#6f8cff); border: none;">
                        ⬇ Tải Crypto Tool
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    <%-- Tự động hiện modal chữ ký nếu có --%>
    document.addEventListener("DOMContentLoaded", function() {
        const showModal = "${showSignatureModal}";
        if (showModal === "true") {
            const modalEl = document.getElementById('signatureModal');
            if (modalEl && typeof bootstrap !== 'undefined') {
                const modalInstance = bootstrap.Modal.getOrCreateInstance(modalEl);
                modalInstance.show();
            }
        }
    });

    <%-- Copy to clipboard --%>
    function copyText(elementId, btn) {
        const el = document.getElementById(elementId);
        if (!el) return;
        const text = el.innerText.trim();
        navigator.clipboard.writeText(text).then(() => {
            btn.textContent = 'Đã sao chép';
            btn.classList.add('copied');
            setTimeout(() => {
                btn.textContent = 'Sao chép';
                btn.classList.remove('copied');
            }, 2000);
        });
    }

    <%-- Countdown timer for bank transfer --%>
    (function () {
        const ttl = parseInt('${paymentTtlSeconds}', 10);
        const expiresAtRaw = '${paymentExpiresAt}';
        if (!ttl || !expiresAtRaw) return;

        const display = document.getElementById('countdown-display');
        const expDisplay = document.getElementById('expire-time-display');
        if (!display) return;

        let expiresAt;
        try {
            expiresAt = new Date(expiresAtRaw.replace(' ', 'T'));
        } catch (e) {
            expiresAt = new Date(Date.now() + ttl * 1000);
        }

        if (expDisplay) {
            expDisplay.textContent = expiresAt.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
        }

        function tick() {
            const remaining = Math.floor((expiresAt - Date.now()) / 1000);
            if (remaining <= 0) {
                display.textContent = '00:00';
                display.classList.add('countdown-expired');
                clearInterval(timer);
                return;
            }
            const m = Math.floor(remaining / 60).toString().padStart(2, '0');
            const s = (remaining % 60).toString().padStart(2, '0');
            display.textContent = m + ':' + s;
        }

        tick();
        const timer = setInterval(tick, 1000);
    })();
</script>
</body>
</html>

