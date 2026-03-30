<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đơn hàng của tôi - PetShop</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">

    <style>
        :root {
            --bg: #f8fafc;
            --surface: rgba(255, 255, 255, 0.9);
            --surface-solid: #ffffff;
            --border: #e2e8f0;
            --text: #0f172a;
            --muted: #64748b;
            --primary: #2563eb;
            --primary-soft: #dbeafe;
            --success: #16a34a;
            --success-soft: #dcfce7;
            --warning: #d97706;
            --warning-soft: #fef3c7;
            --danger: #dc2626;
            --danger-soft: #fee2e2;
            --indigo: #4f46e5;
            --indigo-soft: #e0e7ff;
            --shadow-sm: 0 8px 24px rgba(15, 23, 42, 0.06);
            --shadow-md: 0 14px 40px rgba(15, 23, 42, 0.10);
            --radius-xl: 24px;
            --radius-lg: 18px;
            --radius-md: 14px;
        }

        body {
            background:
                    radial-gradient(circle at top left, #dbeafe 0%, transparent 30%),
                    radial-gradient(circle at top right, #ede9fe 0%, transparent 28%),
                    linear-gradient(180deg, #f8fbff 0%, #f8fafc 100%);
            color: var(--text);
            font-family: 'Segoe UI', sans-serif;
        }

        .orders-page {
            min-height: 100vh;
            padding: 48px 0 72px;
        }

        .hero-box {
            background: linear-gradient(135deg, rgba(37,99,235,0.96), rgba(79,70,229,0.95));
            border-radius: 28px;
            padding: 28px;
            color: white;
            box-shadow: var(--shadow-md);
            position: relative;
            overflow: hidden;
            margin-bottom: 28px;
        }

        .hero-box::before,
        .hero-box::after {
            content: "";
            position: absolute;
            border-radius: 999px;
            background: rgba(255,255,255,0.12);
        }

        .hero-box::before {
            width: 220px;
            height: 220px;
            top: -80px;
            right: -40px;
        }

        .hero-box::after {
            width: 140px;
            height: 140px;
            bottom: -40px;
            left: -30px;
        }

        .hero-title {
            font-size: 2rem;
            font-weight: 800;
            margin-bottom: 8px;
            position: relative;
            z-index: 2;
        }

        .hero-subtitle {
            color: rgba(255,255,255,0.85);
            margin-bottom: 0;
            position: relative;
            z-index: 2;
        }

        .hero-stats {
            position: relative;
            z-index: 2;
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 12px;
            margin-top: 20px;
        }

        .hero-stat {
            background: rgba(255,255,255,0.14);
            border: 1px solid rgba(255,255,255,0.18);
            border-radius: 18px;
            padding: 14px 16px;
            backdrop-filter: blur(10px);
        }

        .hero-stat-label {
            font-size: 0.8rem;
            color: rgba(255,255,255,0.8);
        }

        .hero-stat-value {
            font-size: 1.2rem;
            font-weight: 800;
            margin-top: 4px;
        }

        .toolbar {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
        }

        .toolbar-left,
        .toolbar-right {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            align-items: center;
        }

        .chip-filter {
            background: rgba(255,255,255,0.85);
            border: 1px solid var(--border);
            color: var(--muted);
            border-radius: 999px;
            padding: 10px 16px;
            font-size: 0.92rem;
            font-weight: 600;
            transition: all 0.25s ease;
        }

        .chip-filter:hover,
        .chip-filter.active {
            background: #eff6ff;
            color: var(--primary);
            border-color: #bfdbfe;
            box-shadow: 0 6px 18px rgba(37,99,235,0.12);
        }

        .search-box {
            min-width: 260px;
            background: rgba(255,255,255,0.9);
            border: 1px solid var(--border);
            border-radius: 999px;
            padding: 10px 16px;
            display: flex;
            align-items: center;
            gap: 10px;
            box-shadow: var(--shadow-sm);
        }

        .search-box input {
            border: none;
            outline: none;
            box-shadow: none;
            width: 100%;
            background: transparent;
        }

        .order-stack {
            display: grid;
            gap: 18px;
        }

        .order-shell {
            background: var(--surface);
            border: 1px solid rgba(226,232,240,0.9);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-sm);
            overflow: hidden;
            transition: all 0.28s ease;
            backdrop-filter: blur(10px);
        }

        .order-shell:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-md);
        }

        .order-topbar {
            padding: 18px 22px 14px;
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: flex-start;
            border-bottom: 1px solid #eef2f7;
            flex-wrap: wrap;
        }

        .order-code-wrap {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .order-code {
            font-size: 1.15rem;
            font-weight: 800;
            color: var(--text);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .order-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 14px;
            color: var(--muted);
            font-size: 0.9rem;
        }

        .order-status-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.87rem;
            font-weight: 700;
            padding: 10px 14px;
            border-radius: 999px;
            white-space: nowrap;
        }

        .status-pending {
            background: var(--warning-soft);
            color: #92400e;
        }

        .status-confirmed {
            background: var(--primary-soft);
            color: #1d4ed8;
        }

        .status-shipping {
            background: var(--indigo-soft);
            color: #4338ca;
        }

        .status-completed {
            background: var(--success-soft);
            color: #166534;
        }

        .status-cancelled {
            background: var(--danger-soft);
            color: #991b1b;
        }

        .order-main {
            padding: 20px 22px 18px;
        }

        .summary-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 14px;
        }

        .summary-card {
            background: #f8fafc;
            border: 1px solid #e9eef5;
            border-radius: 18px;
            padding: 16px;
        }

        .summary-label {
            font-size: 0.82rem;
            color: var(--muted);
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .summary-value {
            font-weight: 700;
            color: var(--text);
            line-height: 1.45;
        }

        .summary-value.primary {
            color: var(--primary);
            font-size: 1.1rem;
            font-weight: 800;
        }

        .order-actions {
            margin-top: 18px;
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
        }

        .mini-hint {
            color: var(--muted);
            font-size: 0.9rem;
        }

        .btn-glass {
            border-radius: 14px;
            padding: 10px 16px;
            font-weight: 700;
            border: 1px solid #dbeafe;
            background: linear-gradient(180deg, #ffffff, #eff6ff);
            color: var(--primary);
            transition: all 0.25s ease;
        }

        .btn-glass:hover {
            transform: translateY(-1px);
            box-shadow: 0 10px 22px rgba(37,99,235,0.12);
            color: #1d4ed8;
        }

        .btn-soft-dark {
            border-radius: 14px;
            padding: 10px 16px;
            font-weight: 700;
        }

        .order-detail {
            border-top: 1px dashed #dbe3ef;
            background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
        }

        .order-detail-inner {
            padding: 22px;
        }

        .detail-grid {
            display: grid;
            grid-template-columns: 1.2fr 1fr;
            gap: 18px;
        }

        .detail-panel {
            background: #fff;
            border: 1px solid #edf2f7;
            border-radius: 20px;
            padding: 18px;
            height: 100%;
        }

        .detail-title {
            font-size: 1rem;
            font-weight: 800;
            margin-bottom: 14px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .progress-track {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            margin-top: 8px;
            flex-wrap: wrap;
        }

        .progress-step {
            flex: 1;
            min-width: 110px;
            text-align: center;
            position: relative;
        }

        .progress-step .dot {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            margin: 0 auto 8px;
            display: grid;
            place-items: center;
            background: #e2e8f0;
            color: #64748b;
            font-weight: 700;
        }

        .progress-step.active .dot,
        .progress-step.done .dot {
            background: #dbeafe;
            color: var(--primary);
        }

        .progress-step.done .dot {
            background: #dcfce7;
            color: var(--success);
        }

        .progress-step .label {
            font-size: 0.84rem;
            color: var(--muted);
            font-weight: 600;
        }

        .progress-step.active .label,
        .progress-step.done .label {
            color: var(--text);
        }

        .info-list {
            display: grid;
            gap: 12px;
        }

        .info-item {
            background: #f8fafc;
            border: 1px solid #eef2f7;
            border-radius: 16px;
            padding: 14px;
        }

        .info-item .label {
            font-size: 0.78rem;
            color: var(--muted);
            text-transform: uppercase;
            letter-spacing: 0.04em;
            margin-bottom: 6px;
        }

        .info-item .value {
            font-weight: 700;
            color: var(--text);
            line-height: 1.5;
        }

        .product-list {
            display: grid;
            gap: 12px;
            margin-top: 14px;
        }

        .product-row {
            display: grid;
            grid-template-columns: 56px 1fr auto;
            gap: 12px;
            align-items: center;
            padding: 12px;
            border: 1px solid #eef2f7;
            border-radius: 16px;
            background: #fafcff;
        }

        .product-thumb {
            width: 56px;
            height: 56px;
            border-radius: 14px;
            background: linear-gradient(135deg, #dbeafe, #ede9fe);
            display: grid;
            place-items: center;
            font-size: 1.2rem;
            color: var(--primary);
        }

        .product-name {
            font-weight: 700;
            color: var(--text);
            margin-bottom: 4px;
        }

        .product-meta {
            font-size: 0.86rem;
            color: var(--muted);
        }

        .product-price {
            font-weight: 800;
            color: var(--text);
        }

        .empty-state {
            background: rgba(255,255,255,0.92);
            border: 1px solid var(--border);
            border-radius: 28px;
            padding: 56px 24px;
            box-shadow: var(--shadow-sm);
        }

        .empty-illustration {
            width: 108px;
            height: 108px;
            border-radius: 50%;
            background: linear-gradient(135deg, #dbeafe, #ede9fe);
            display: grid;
            place-items: center;
            font-size: 2.4rem;
            color: var(--primary);
            margin: 0 auto 18px;
        }

        @media (max-width: 992px) {
            .summary-grid,
            .detail-grid,
            .hero-stats {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 768px) {
            .orders-page {
                padding-top: 28px;
            }

            .hero-title {
                font-size: 1.55rem;
            }

            .order-topbar,
            .order-main,
            .order-detail-inner {
                padding-left: 16px;
                padding-right: 16px;
            }

            .product-row {
                grid-template-columns: 48px 1fr;
            }

            .product-price {
                grid-column: 2;
            }
        }
    </style>
</head>
<body>
<jsp:include page="/components/navbar.jsp" />

<div class="container orders-page">
    <div class="hero-box">
        <div class="row align-items-center g-3">
            <div class="col-lg-7">
                <h1 class="hero-title">
                    <i class='bx bx-package me-2'></i>Đơn hàng của tôi
                </h1>
                <p class="hero-subtitle">
                    Theo dõi trạng thái đơn hàng, xem nhanh thông tin giao nhận và mở chi tiết trực tiếp ngay trên danh sách.
                </p>
            </div>
            <div class="col-lg-5">
                <div class="hero-stats">
                    <div class="hero-stat">
                        <div class="hero-stat-label">Tổng đơn</div>
                        <div class="hero-stat-value">${orders != null ? orders.size() : 0}</div>
                    </div>
                    <div class="hero-stat">
                        <div class="hero-stat-label">Đang xử lý</div>
                        <div class="hero-stat-value">${countPending}</div>
                    </div>
                    <div class="hero-stat">
                        <div class="hero-stat-label">Hoàn thành</div>
                        <div class="hero-stat-value">${countCompleted}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="toolbar">
        <div class="toolbar-left">
            <button class="chip-filter active">Tất cả</button>
            <button class="chip-filter">Chờ xử lý</button>
            <button class="chip-filter">Đang giao</button>
            <button class="chip-filter">Hoàn thành</button>
        </div>
        <div class="toolbar-right">
            <div class="search-box">
                <i class='bx bx-search-alt-2 text-secondary'></i>
                <input type="text" placeholder="Tìm theo mã đơn, người nhận...">
            </div>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="empty-state text-center">
                <div class="empty-illustration">
                    <i class='bx bx-cart'></i>
                </div>
                <h4 class="fw-bold mb-2">Bạn chưa có đơn hàng nào</h4>
                <p class="text-muted mb-4">
                    Hãy bắt đầu mua sắm để đơn hàng đầu tiên của bạn xuất hiện tại đây.
                </p>
                <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary btn-lg px-4 rounded-4">
                    Mua sắm ngay
                </a>
            </div>
        </c:when>

        <c:otherwise>
            <div class="order-stack">

                <c:forEach var="o" items="${orders}" varStatus="loop">
                    <div class="order-shell">
                        <div class="order-topbar">
                            <div class="order-code-wrap">
                                <div class="order-code">
                                    <i class='bx bx-receipt'></i>
                                    Đơn hàng #${o.id}
                                </div>
                                <div class="order-meta">
                                        <span>
                                            <i class='bx bx-calendar'></i>
                                            <fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </span>
                                    <span>
                                            <i class='bx bx-user'></i>
                                            ${o.fullname}
                                        </span>
                                </div>
                            </div>

                            <span class="order-status-badge
                                    ${o.status == 'Pending' ? 'status-pending' : ''}
                                    ${o.status == 'Confirmed' ? 'status-confirmed' : ''}
                                    ${o.status == 'Shipping' ? 'status-shipping' : ''}
                                    ${o.status == 'Completed' ? 'status-completed' : ''}
                                    ${o.status == 'Cancelled' ? 'status-cancelled' : ''}">
                                    <i class='bx bx-radio-circle-marked'></i>
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

                        <div class="order-main">
                            <div class="summary-grid">
                                <div class="summary-card">
                                    <div class="summary-label">
                                        <i class='bx bx-map'></i> Địa chỉ nhận hàng
                                    </div>
                                    <div class="summary-value">
                                            ${o.address}
                                    </div>
                                </div>

                                <div class="summary-card">
                                    <div class="summary-label">
                                        <i class='bx bx-wallet'></i> Tổng thanh toán
                                    </div>
                                    <div class="summary-value primary">
                                            ${o.formattedTotalAmount}
                                    </div>
                                </div>

                                <div class="summary-card">
                                    <div class="summary-label">
                                        <i class='bx bx-package'></i> Trạng thái đơn
                                    </div>
                                    <div class="summary-value">
                                        <c:choose>
                                            <c:when test="${o.status == 'Pending'}">Đơn hàng đang chờ hệ thống xác nhận.</c:when>
                                            <c:when test="${o.status == 'Confirmed'}">Đơn hàng đã được xác nhận và chuẩn bị giao.</c:when>
                                            <c:when test="${o.status == 'Shipping'}">Đơn hàng đang trên đường giao đến bạn.</c:when>
                                            <c:when test="${o.status == 'Completed'}">Đơn hàng đã giao thành công.</c:when>
                                            <c:when test="${o.status == 'Cancelled'}">Đơn hàng đã bị hủy.</c:when>
                                            <c:otherwise>${o.status}</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>

                            <div class="order-actions">
                                <div class="mini-hint">
                                    <i class='bx bx-info-circle'></i>
                                    Nhấn “Chi tiết” để xem tiến trình, thông tin giao hàng và sản phẩm.
                                </div>

                                <div class="d-flex gap-2 flex-wrap">
                                    <button
                                            class="btn btn-glass"
                                            type="button"
                                            data-bs-toggle="collapse"
                                            data-bs-target="#detail-${loop.index}"
                                            aria-expanded="false"
                                            aria-controls="detail-${loop.index}">
                                        <i class='bx bx-chevron-down'></i> Chi tiết
                                    </button>

                                    <a href="${pageContext.request.contextPath}/my-orders?action=view&id=${o.id}"
                                       class="btn btn-outline-dark btn-soft-dark">
                                        <i class='bx bx-link-external'></i> Trang riêng
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="collapse order-detail" id="detail-${loop.index}">
                            <div class="order-detail-inner">
                                <div class="detail-grid">
                                    <div class="detail-panel">
                                        <div class="detail-title">
                                            <i class='bx bx-git-branch'></i> Tiến trình đơn hàng
                                        </div>

                                        <div class="progress-track">
                                            <div class="progress-step ${o.status == 'Pending' || o.status == 'Confirmed' || o.status == 'Shipping' || o.status == 'Completed' ? 'done' : ''}">
                                                <div class="dot"><i class='bx bx-receipt'></i></div>
                                                <div class="label">Đặt hàng</div>
                                            </div>

                                            <div class="progress-step ${o.status == 'Confirmed' || o.status == 'Shipping' || o.status == 'Completed' ? 'done' : (o.status == 'Pending' ? 'active' : '')}">
                                                <div class="dot"><i class='bx bx-check-shield'></i></div>
                                                <div class="label">Xác nhận</div>
                                            </div>

                                            <div class="progress-step ${o.status == 'Shipping' || o.status == 'Completed' ? 'done' : ''}">
                                                <div class="dot"><i class='bx bx-car'></i></div>
                                                <div class="label">Vận chuyển</div>
                                            </div>

                                            <div class="progress-step ${o.status == 'Completed' ? 'done' : ''}">
                                                <div class="dot"><i class='bx bx-home-heart'></i></div>
                                                <div class="label">Hoàn tất</div>
                                            </div>
                                        </div>

                                        <div class="product-list">
                                            <!-- Demo UI: thay bằng orderItems thực nếu backend có -->
                                            <div class="product-row">
                                                <div class="product-thumb">
                                                    <i class='bx bx-bone'></i>
                                                </div>
                                                <div>
                                                    <div class="product-name">Thức ăn hạt cao cấp cho thú cưng</div>
                                                    <div class="product-meta">Phân loại: Gói 1kg · SL: 1</div>
                                                </div>
                                                <div class="product-price">${o.formattedTotalAmount}</div>
                                            </div>

                                            <div class="product-row">
                                                <div class="product-thumb">
                                                    <i class='bx bx-donate-heart'></i>
                                                </div>
                                                <div>
                                                    <div class="product-name">Phụ kiện chăm sóc thú cưng</div>
                                                    <div class="product-meta">Combo cơ bản · SL: 1</div>
                                                </div>
                                                <div class="product-price">Đã gồm</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="detail-panel">
                                        <div class="detail-title">
                                            <i class='bx bx-detail'></i> Thông tin chi tiết
                                        </div>

                                        <div class="info-list">
                                            <div class="info-item">
                                                <div class="label">Người nhận</div>
                                                <div class="value">${o.fullname}</div>
                                            </div>

                                            <div class="info-item">
                                                <div class="label">Địa chỉ giao hàng</div>
                                                <div class="value">${o.address}</div>
                                            </div>

                                            <div class="info-item">
                                                <div class="label">Trạng thái hiện tại</div>
                                                <div class="value">
                                                    <c:choose>
                                                        <c:when test="${o.status == 'Pending'}">Đơn đang chờ xử lý từ cửa hàng.</c:when>
                                                        <c:when test="${o.status == 'Confirmed'}">Đơn đã được xác nhận, đang chuẩn bị hàng.</c:when>
                                                        <c:when test="${o.status == 'Shipping'}">Shipper đang giao đơn đến địa chỉ của bạn.</c:when>
                                                        <c:when test="${o.status == 'Completed'}">Đơn hàng đã giao thành công và hoàn tất.</c:when>
                                                        <c:when test="${o.status == 'Cancelled'}">Đơn hàng đã bị hủy, vui lòng kiểm tra lại nếu cần.</c:when>
                                                        <c:otherwise>${o.status}</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>

                                            <div class="info-item">
                                                <div class="label">Thanh toán</div>
                                                <div class="value">${o.formattedTotalAmount}</div>
                                            </div>

                                            <div class="info-item">
                                                <div class="label">Ghi chú</div>
                                                <div class="value">Vui lòng kiểm tra điện thoại khi đơn chuyển sang trạng thái giao hàng.</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>

            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/components/footer.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>