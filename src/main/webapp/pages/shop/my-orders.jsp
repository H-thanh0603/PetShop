<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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
            --surface: rgba(255, 255, 255, 0.92);
            --border: #e2e8f0;
            --text: #0f172a;
            --muted: #64748b;
            --primary: #2563eb;
            --success: #16a34a;
            --warning: #d97706;
            --danger: #dc2626;
            --indigo: #4f46e5;
            --shadow-sm: 0 8px 24px rgba(15, 23, 42, 0.06);
            --shadow-md: 0 14px 40px rgba(15, 23, 42, 0.10);
        }

        body {
            background:
                    radial-gradient(circle at top left, #dbeafe 0%, transparent 30%),
                    radial-gradient(circle at top right, #ede9fe 0%, transparent 28%),
                    linear-gradient(180deg, #f8fbff 0%, #f8fafc 100%);
            color: var(--text);
            font-family: 'Segoe UI', sans-serif;
        }

        .orders-page { min-height: 100vh; padding: 48px 0 72px; }
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
        .hero-box::before, .hero-box::after {
            content: "";
            position: absolute;
            border-radius: 999px;
            background: rgba(255,255,255,0.12);
        }
        .hero-box::before { width: 220px; height: 220px; top: -80px; right: -40px; }
        .hero-box::after { width: 140px; height: 140px; bottom: -40px; left: -30px; }
        .hero-title { font-size: 2rem; font-weight: 800; margin-bottom: 8px; position: relative; z-index: 2; }
        .hero-subtitle { color: rgba(255,255,255,0.85); margin-bottom: 0; position: relative; z-index: 2; }
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
        }
        .hero-stat-label { font-size: 0.8rem; color: rgba(255,255,255,0.8); }
        .hero-stat-value { font-size: 1.2rem; font-weight: 800; margin-top: 4px; }

        /* Chuyển đổi trạng thái kiểu Tabs của File 2 nhưng giữ màu sắc đồng bộ */
        .nav-tabs-custom {
            background: var(--surface);
            padding: 6px 12px;
            border-radius: 16px;
            box-shadow: var(--shadow-sm);
            border: 1px solid var(--border);
        }
        .nav-tabs-custom .nav-link {
            color: var(--muted);
            border: none;
            border-bottom: 3px solid transparent;
            padding: 12px 16px;
            font-weight: 600;
            font-size: 0.95rem;
            transition: all 0.2s;
        }
        .nav-tabs-custom .nav-link:hover {
            color: var(--primary);
        }
        .nav-tabs-custom .nav-link.active {
            color: var(--primary);
            border-bottom-color: var(--primary);
            background: transparent;
        }

        .toolbar {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
        }
        .toolbar form { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; width: 100%; justify-content: space-between; }
        .toolbar-right { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; width: 100%; justify-content: flex-end; }
        .search-box {
            min-width: 320px;
            background: rgba(255,255,255,0.9);
            border: 1px solid var(--border);
            border-radius: 999px;
            padding: 10px 18px;
            display: flex;
            align-items: center;
            gap: 10px;
            box-shadow: var(--shadow-sm);
        }
        .search-box input { border: none; outline: none; box-shadow: none; width: 100%; background: transparent; }
        .order-stack { display: grid; gap: 18px; }
        .order-shell {
            background: var(--surface);
            border: 1px solid rgba(226,232,240,0.9);
            border-radius: 24px;
            box-shadow: var(--shadow-sm);
            overflow: hidden;
            transition: all 0.28s ease;
        }
        .order-shell:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
        .order-topbar {
            padding: 18px 22px 14px;
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: flex-start;
            border-bottom: 1px solid #eef2f7;
            flex-wrap: wrap;
        }
        .order-code { font-size: 1.15rem; font-weight: 800; color: var(--text); display: flex; align-items: center; gap: 8px; }
        .order-meta { display: flex; flex-wrap: wrap; gap: 14px; color: var(--muted); font-size: 0.9rem; }
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
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-confirmed { background: #dbeafe; color: #1d4ed8; }
        .status-shipping { background: #e0e7ff; color: #4338ca; }
        .status-delivered { background: #cffafe; color: #0891b2; }
        .status-completed { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        .status-awaiting-payment { background: #ffedd5; color: #c2410c; }
        .status-paid { background: #ccfbf1; color: #0f766e; }

        .payment-badge { display: inline-flex; align-items: center; gap: 6px; margin-top: 8px; border-radius: 999px; padding: 6px 10px; font-size: 0.78rem; font-weight: 700; }
        .payment-pending { background: #fff7ed; color: #c2410c; }
        .payment-verified { background: #dcfce7; color: #166534; }
        .payment-failed { background: #fee2e2; color: #991b1b; }
        .payment-neutral { background: #e0f2fe; color: #075985; }
        .payment-unpaid { background: #f1f5f9; color: #475569; }
        .payment-expired { background: #fef2f2; color: #b91c1c; }

        .order-main { padding: 20px 22px 18px; }
        .summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
        .summary-card { background: #f8fafc; border: 1px solid #e9eef5; border-radius: 18px; padding: 16px; }
        .summary-label { font-size: 0.82rem; color: var(--muted); margin-bottom: 8px; display: flex; align-items: center; gap: 6px; }
        .summary-value { font-weight: 700; color: var(--text); line-height: 1.45; }
        .summary-value.primary { color: var(--danger); font-size: 1.1rem; font-weight: 800; }

        .order-actions { margin-top: 18px; display: flex; justify-content: space-between; gap: 12px; align-items: center; flex-wrap: wrap; }
        .mini-hint { color: var(--muted); font-size: 0.9rem; }
        .btn-glass { border-radius: 14px; padding: 10px 16px; font-weight: 700; border: 1px solid #dbeafe; background: linear-gradient(180deg, #ffffff, #eff6ff); color: var(--primary); }
        .btn-soft-dark { border-radius: 14px; padding: 10px 16px; font-weight: 700; }

        .order-detail { border-top: 1px dashed #dbe3ef; background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%); }
        .order-detail-inner { padding: 22px; }
        .detail-grid { display: grid; grid-template-columns: 1.2fr 1fr; gap: 18px; }
        .detail-panel { background: #fff; border: 1px solid #edf2f7; border-radius: 20px; padding: 18px; height: 100%; }
        .detail-title { font-size: 1rem; font-weight: 800; margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }

        .progress-track { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-top: 8px; flex-wrap: wrap; }
        .progress-step { flex: 1; min-width: 110px; text-align: center; }
        .progress-step .dot { width: 36px; height: 36px; border-radius: 50%; margin: 0 auto 8px; display: grid; place-items: center; background: #e2e8f0; color: #64748b; font-weight: 700; }
        .progress-step.active .dot, .progress-step.done .dot { background: #dbeafe; color: var(--primary); }
        .progress-step.done .dot { background: #dcfce7; color: var(--success); }
        .progress-step .label { font-size: 0.84rem; color: var(--muted); font-weight: 600; }
        .progress-step.active .label, .progress-step.done .label { color: var(--text); }

        .info-list { display: grid; gap: 12px; }
        .info-item { background: #f8fafc; border: 1px solid #eef2f7; border-radius: 16px; padding: 14px; }
        .info-item .label { font-size: 0.78rem; color: var(--muted); text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 6px; }
        .info-item .value { font-weight: 700; color: var(--text); line-height: 1.5; }

        .product-list { display: grid; gap: 12px; margin-top: 14px; }
        .product-row { display: grid; grid-template-columns: 56px 1fr auto; gap: 12px; align-items: center; padding: 12px; border: 1px solid #eef2f7; border-radius: 16px; background: #fafcff; }
        .product-thumb { width: 56px; height: 56px; border-radius: 14px; overflow: hidden; background: linear-gradient(135deg, #dbeafe, #ede9fe); display: grid; place-items: center; }
        .product-thumb img { width: 100%; height: 100%; object-fit: cover; }
        .product-name { font-weight: 700; color: var(--text); margin-bottom: 4px; }
        .product-meta { font-size: 0.86rem; color: var(--muted); }
        .product-price { font-weight: 800; color: var(--text); }

        .empty-state { background: rgba(255,255,255,0.92); border: 1px solid var(--border); border-radius: 28px; padding: 56px 24px; box-shadow: var(--shadow-sm); }
        .empty-illustration { width: 108px; height: 108px; border-radius: 50%; background: linear-gradient(135deg, #dbeafe, #ede9fe); display: grid; place-items: center; font-size: 2.4rem; color: var(--primary); margin: 0 auto 18px; }

        @media (max-width: 992px) { .summary-grid, .detail-grid, .hero-stats { grid-template-columns: 1fr; } }
        @media (max-width: 768px) {
            .orders-page { padding-top: 28px; }
            .hero-title { font-size: 1.55rem; }
            .product-row { grid-template-columns: 48px 1fr; }
            .product-price { grid-column: 2; }
            .search-box { min-width: 100%; }
        }
    </style>
    <link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/components/navbar.jsp" />
<jsp:include page="/components/toast.jsp" />

<div class="container orders-page">
    <div class="hero-box">
        <div class="row align-items-center g-3">
            <div class="col-lg-7">
                <h1 class="hero-title"><i class='bx bx-package me-2'></i>Đơn hàng của tôi</h1>
                <p class="hero-subtitle">Theo dõi trạng thái đơn hàng, xem sản phẩm thực tế và quản lý lịch sử mua sắm của bạn.</p>
            </div>
            <div class="col-lg-5">
                <div class="hero-stats">
                    <div class="hero-stat">
                        <div class="hero-stat-label">Tổng đơn</div>
                        <div class="hero-stat-value">${totalOrders}</div>
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

    <c:if test="${not empty repurchaseSuggestions}">
        <div class="order-stack mb-4">
            <c:forEach var="s" items="${repurchaseSuggestions}">
                <div class="order-shell">
                    <div class="order-main">
                        <div class="order-actions mt-0">
                            <div>
                                <div class="order-code"><i class='bx bx-refresh'></i> Gợi ý mua lại</div>
                                <div class="mini-hint">${fn:escapeXml(s.message)}</div>
                            </div>
                            <form action="${pageContext.request.contextPath}/my-orders" method="post">
                                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                <input type="hidden" name="action" value="reorder">
                                <input type="hidden" name="orderId" value="${s.orderId}">
                                <button type="submit" class="btn btn-primary rounded-4">
                                    <i class='bx bx-cart-add'></i> Mua lại
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:set var="statusAct" value="${not empty selectedStatus ? selectedStatus : (not empty currentStatus ? currentStatus : 'all')}" />
    <div class="card mb-4 border-0 shadow-sm rounded-3">
        <div class="card-body p-0">
            <ul class="nav nav-tabs nav-tabs-custom justify-content-start border-bottom-0">
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'all' || statusAct == 'All' ? 'active' : ''}" href="?status=all&keyword=${fn:escapeXml(keyword)}">Tất cả</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Awaiting Payment' ? 'active' : ''}" href="?status=Awaiting Payment&keyword=${fn:escapeXml(keyword)}">Chờ thanh toán</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Pending' ? 'active' : ''}" href="?status=Pending&keyword=${fn:escapeXml(keyword)}">Chờ xử lý</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Paid' ? 'active' : ''}" href="?status=Paid&keyword=${fn:escapeXml(keyword)}">Đã thanh toán</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Shipping' ? 'active' : ''}" href="?status=Shipping&keyword=${fn:escapeXml(keyword)}">Đang giao</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Delivered' ? 'active' : ''}" href="?status=Delivered&keyword=${fn:escapeXml(keyword)}">Đã giao</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Completed' ? 'active' : ''}" href="?status=Completed&keyword=${fn:escapeXml(keyword)}">Hoàn thành</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${statusAct == 'Cancelled' ? 'active' : ''}" href="?status=Cancelled&keyword=${fn:escapeXml(keyword)}">Đã hủy</a>
                </li>
            </ul>
        </div>
    </div>

    <div class="toolbar">
        <form action="${pageContext.request.contextPath}/my-orders" method="get">
            <input type="hidden" name="status" value="${fn:escapeXml(statusAct)}">
            <div class="toolbar-right">
                <div class="search-box">
                    <i class='bx bx-search-alt-2 text-secondary'></i>
                    <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tìm theo mã đơn, người nhận, số điện thoại...">
                </div>
                <button class="btn btn-primary rounded-pill px-4" type="submit">Tìm kiếm</button>
            </div>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="empty-state text-center">
                <div class="empty-illustration"><i class='bx bx-cart'></i></div>
                <h4 class="fw-bold mb-2">Không tìm thấy đơn hàng phù hợp</h4>
                <p class="text-muted mb-4">Thử thay đổi bộ lọc hoặc tiếp tục mua sắm để tạo đơn hàng mới.</p>
                <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary btn-lg px-4 rounded-4">Mua sắm ngay</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="order-stack">
                <c:forEach var="o" items="${orders}" varStatus="loop">
                    <div class="order-shell">
                        <div class="order-topbar">
                            <div>
                                <div class="order-code"><i class='bx bx-receipt'></i> Đơn hàng #${o.id}</div>
                                <div class="order-meta">
                                    <span><i class='bx bx-calendar'></i> <fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/></span>
                                    <span><i class='bx bx-user'></i> ${fn:escapeXml(o.fullname)}</span>
                                    <span><i class='bx bx-package'></i> ${o.itemCount} sản phẩm</span>
                                </div>
                            </div>
                            <span class="order-status-badge ${o.statusCssClass}">
                                <i class='bx bx-radio-circle-marked'></i> ${o.statusLabel}
                            </span>
                        </div>

                        <div class="order-main">
                            <div class="summary-grid">
                                <div class="summary-card">
                                    <div class="summary-label"><i class='bx bx-map'></i> Địa chỉ nhận hàng</div>
                                    <div class="summary-value">${fn:escapeXml(o.address)}</div>
                                </div>
                                <div class="summary-card">
                                    <div class="summary-label"><i class='bx bx-wallet'></i> Tổng thanh toán</div>
                                    <div class="summary-value primary">${o.formattedTotalAmount}</div>
                                </div>
                                <div class="summary-card">
                                    <div class="summary-label"><i class='bx bx-credit-card'></i> Thanh toán</div>
                                    <div class="summary-value">
                                        ${fn:escapeXml(o.paymentMethodLabel)}<br><span class="text-muted small">${fn:escapeXml(o.paymentFlowLabel)}</span>
                                        <br><span class="payment-badge ${o.paymentVerificationCssClass}">${fn:escapeXml(o.paymentVerificationLabel)}</span>
                                    </div>
                                </div>
                                <div class="summary-card">
                                    <div class="summary-label"><i class='bx bx-package'></i> Trạng thái đơn</div>
                                    <div class="summary-value">${fn:escapeXml(o.statusDescription)}</div>
                                </div>
                            </div>

                            <div class="order-actions">
                                <div class="mini-hint"><i class='bx bx-info-circle'></i> Mở chi tiết để xem sản phẩm trong đơn và tiến trình giao hàng.</div>
                                <div class="d-flex gap-2 flex-wrap">
                                    <button class="btn btn-glass" type="button" data-bs-toggle="collapse" data-bs-target="#detail-${loop.index}">
                                        <i class='bx bx-chevron-down'></i> Chi tiết
                                    </button>
                                    <a href="${pageContext.request.contextPath}/my-orders?action=view&id=${o.id}" class="btn btn-outline-dark btn-soft-dark">
                                        <i class='bx bx-link-external'></i> Trang riêng
                                    </a>
                                    <form action="${pageContext.request.contextPath}/my-orders" method="post">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                        <input type="hidden" name="action" value="reorder">
                                        <input type="hidden" name="orderId" value="${o.id}">
                                        <button type="submit" class="btn btn-glass">
                                            <i class='bx bx-cart-add'></i> Mua lại
                                        </button>
                                    </form>
                                    <c:if test="${o.status == 'Delivered' || o.status == 'Completed'}">
                                        <form action="${pageContext.request.contextPath}/my-orders" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                            <input type="hidden" name="action" value="confirmReceipt">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <button type="submit" class="btn btn-success text-white" ${o.status == 'Completed' ? 'disabled' : ''}>
                                                <i class='bx bx-check-double'></i> ${o.status == 'Completed' ? 'Đã nhận hàng' : 'Xác nhận nhận hàng'}
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${o.repayable}">
                                        <form action="${pageContext.request.contextPath}/my-orders" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                            <input type="hidden" name="action" value="repay">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <button type="submit" class="btn btn-warning">
                                                <i class='bx bx-credit-card'></i> Thanh toán lại
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${o.cancelableByUser}">
                                        <form action="${pageContext.request.contextPath}/my-orders" method="post" onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn hàng này?');">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                            <input type="hidden" name="action" value="cancel">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <button type="submit" class="btn btn-outline-danger btn-soft-dark">
                                                <i class='bx bx-x-circle'></i> Hủy đơn
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </div>

                        <div class="collapse order-detail" id="detail-${loop.index}">
                            <div class="order-detail-inner">
                                <div class="detail-grid">
                                    <div class="detail-panel">
                                        <div class="detail-title"><i class='bx bx-git-branch'></i> Tiến trình đơn hàng</div>
                                        <div class="progress-track">
                                            <div class="progress-step done">
                                                <div class="dot"><i class='bx bx-receipt'></i></div>
                                                <div class="label">Đặt hàng</div>
                                            </div>
                                            <div class="progress-step ${o.status == 'Pending' ? 'active' : (o.status == 'Confirmed' || o.status == 'Shipping' || o.status == 'Completed' ? 'done' : '')}">
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
                                            <c:forEach items="${o.items}" var="item">
                                                <div class="product-row">
                                                    <div class="product-thumb">
                                                        <img loading="lazy" src="${fn:startsWith(item.product.image, 'http') ? fn:escapeXml(item.product.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(item.product.image)}"
                                                             onerror="this.src='https://placehold.co/120x120/e2e8f0/1e293b?text=PetShop'">
                                                    </div>
                                                    <div>
                                                        <div class="product-name">${fn:escapeXml(item.product.name)}</div>
                                                        <div class="product-meta">SL: ${item.quantity} · Đơn giá:
                                                            <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                        </div>
                                                    </div>
                                                    <div class="product-price">
                                                        <fmt:formatNumber value="${item.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <div class="detail-panel">
                                        <div class="detail-title"><i class='bx bx-detail'></i> Thông tin chi tiết</div>
                                        <div class="info-list">
                                            <div class="info-item">
                                                <div class="label">Người nhận</div>
                                                <div class="value">${fn:escapeXml(o.fullname)}</div>
                                            </div>
                                            <div class="info-item">
                                                <div class="label">Số điện thoại</div>
                                                <div class="value">${fn:escapeXml(o.phone)}</div>
                                            </div>
                                            <div class="info-item">
                                                <div class="label">Địa chỉ giao hàng</div>
                                                <div class="value">${fn:escapeXml(o.address)}</div>
                                            </div>
                                            <div class="info-item">
                                                <div class="label">Thanh toán</div>
                                                <div class="value">
                                                    ${fn:escapeXml(o.paymentMethodLabel)} · ${fn:escapeXml(o.paymentFlowLabel)}
                                                    <br><span class="payment-badge ${o.paymentVerificationCssClass}">${fn:escapeXml(o.paymentVerificationLabel)}</span>
                                                    <c:if test="${not empty o.paymentReference}">
                                                        <br><span class="text-muted small">Mã chuyển khoản: ${fn:escapeXml(o.paymentReference)}</span>
                                                    </c:if>
                                                    <c:if test="${not empty o.paymentVerificationMessage}">
                                                        <br><span class="text-muted small">${fn:escapeXml(o.paymentVerificationMessage)}</span>
                                                    </c:if>
                                                </div>
                                            </div>
                                            <div class="info-item">
                                                <div class="label">Ghi chú</div>
                                                <div class="value">${empty o.note ? 'Không có ghi chú từ khách hàng.' : fn:escapeXml(o.note)}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${totalPages > 1}">
                <nav class="d-flex justify-content-center mt-4">
                    <ul class="pagination pagination-md shadow-sm rounded-3">
                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                            <a class="page-link px-3" href="?status=${statusAct}&page=${currentPage - 1}&keyword=${fn:escapeXml(keyword)}">
                                <i class="bx bx-chevron-left"></i> Trước
                            </a>
                        </li>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <li class="page-item ${currentPage == i ? 'active' : ''}">
                                <a class="page-link px-3" href="?status=${statusAct}&page=${i}&keyword=${fn:escapeXml(keyword)}">${i}</a>
                            </li>
                        </c:forEach>

                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                            <a class="page-link px-3" href="?status=${statusAct}&page=${currentPage + 1}&keyword=${fn:escapeXml(keyword)}">
                                Sau <i class="bx bx-chevron-right"></i>
                            </a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/components/footer.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>