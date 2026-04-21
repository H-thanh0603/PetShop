<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Báo cáo vận hành - Admin</title>
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .summary-grid, .report-grid { display:grid; gap:20px; }
        .summary-grid { grid-template-columns: repeat(4, 1fr); margin-bottom:24px; }
        .report-grid { grid-template-columns: repeat(2, 1fr); }
        .summary-card, .panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; box-shadow:0 4px 12px rgba(15,23,42,.04); }
        .summary-card { padding:20px; }
        .summary-card h3 { margin:0; font-size:1.7rem; font-weight:800; }
        .summary-card p { margin:6px 0 0; color:#64748b; }
        .panel-header { padding:16px 20px; border-bottom:1px solid #e2e8f0; display:flex; justify-content:space-between; align-items:center; }
        .panel-header h5 { margin:0; font-weight:700; display:flex; gap:8px; align-items:center; }
        .panel-body { padding:18px 20px; }
        .simple-table { width:100%; border-collapse:collapse; }
        .simple-table th, .simple-table td { padding:12px 10px; border-bottom:1px solid #f1f5f9; vertical-align:top; }
        .simple-table th { color:#64748b; text-transform:uppercase; font-size:.75rem; }
        .muted { color:#64748b; font-size:.87rem; }
        .pill { display:inline-flex; padding:4px 10px; border-radius:999px; font-size:.74rem; font-weight:700; }
        .green { background:#dcfce7; color:#15803d; }
        .orange { background:#ffedd5; color:#c2410c; }
        .red { background:#fee2e2; color:#b91c1c; }
        .blue { background:#dbeafe; color:#1d4ed8; }
        .full { grid-column: 1 / -1; }
        .empty-state { text-align:center; color:#94a3b8; padding:28px 16px; }
        @media (max-width: 1100px) {
            .summary-grid, .report-grid { grid-template-columns: 1fr 1fr; }
        }
        @media (max-width: 768px) {
            .summary-grid, .report-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<jsp:include page="/components/admin-sidebar.jsp"><jsp:param name="currentPage" value="reports"/></jsp:include>

<main class="admin-main">
    <div class="page-header-admin">
        <div>
            <h1 class="page-title"><i class='bx bx-spreadsheet'></i> Báo cáo vận hành</h1>
            <p class="page-subtitle">Tổng hợp dữ liệu kinh doanh, khách hàng, coupon và chất lượng sản phẩm</p>
        </div>
        <div class="d-flex align-items-center gap-3">
            <form method="GET" class="d-flex align-items-center gap-2">
                <label class="fw-semibold">Năm:</label>
                <select name="year" class="form-select" style="width:120px;" onchange="this.form.submit()">
                    <c:forEach begin="${currentYear - 3}" end="${currentYear}" var="y">
                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                    </c:forEach>
                </select>
            </form>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>
    </div>

    <div class="summary-grid">
        <div class="summary-card"><h3><fmt:formatNumber value="${totalRevenue}" type="number" maxFractionDigits="0"/>đ</h3><p>Tổng doanh thu toàn hệ thống</p></div>
        <div class="summary-card"><h3><fmt:formatNumber value="${currentMonthRevenue}" type="number" maxFractionDigits="0"/>đ</h3><p>Doanh thu tháng hiện tại</p></div>
        <div class="summary-card"><h3>${completedOrders}</h3><p>Đơn hàng đã hoàn tất</p></div>
        <div class="summary-card"><h3>${overview.lowRatingReviews}</h3><p>Review 1-2 sao cần theo dõi</p></div>
    </div>

    <div class="report-grid">
        <section class="panel">
            <div class="panel-header"><h5><i class='bx bx-trophy'></i> Top sản phẩm bán chạy</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty topProducts}"><div class="empty-state">Chưa có dữ liệu.</div></c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Sản phẩm</th><th>Đã bán</th><th>Doanh thu</th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${topProducts}">
                                <tr>
                                    <td>${item.product}</td>
                                    <td>${item.count}</td>
                                    <td><fmt:formatNumber value="${item.revenue}" type="number" maxFractionDigits="0"/>đ</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header"><h5><i class='bx bx-group'></i> Khách hàng giá trị cao</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty topCustomers}"><div class="empty-state">Chưa có dữ liệu.</div></c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Khách hàng</th><th>Số đơn</th><th>Tổng chi</th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${topCustomers}">
                                <tr>
                                    <td>${item.fullname}<div class="muted">${item.email}</div></td>
                                    <td>${item.totalOrders}</td>
                                    <td><fmt:formatNumber value="${item.totalSpent}" type="number" maxFractionDigits="0"/>đ</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header"><h5><i class='bx bx-purchase-tag'></i> Mức sử dụng coupon</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty couponUsage}"><div class="empty-state">Chưa có coupon.</div></c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Mã</th><th>Loại</th><th>Đã dùng</th><th>Trạng thái</th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${couponUsage}">
                                <tr>
                                    <td>${item.code}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.discountType == 'percent'}">${item.discountPercent}%</c:when>
                                            <c:otherwise><fmt:formatNumber value="${item.discountValue}" type="number" maxFractionDigits="0"/>đ</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${item.used}/${item.quantity}</td>
                                    <td>
                                        <span class="pill ${item.active ? 'green' : 'red'}">${item.active ? 'Đang hoạt động' : 'Đã tắt'}</span>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header"><h5><i class='bx bx-pie-chart-alt-2'></i> Phân bổ trạng thái đơn hàng</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty orderStatus}"><div class="empty-state">Chưa có đơn hàng.</div></c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Trạng thái</th><th>Số lượng</th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${orderStatus}">
                                <tr>
                                    <td>${item.status}</td>
                                    <td>${item.count}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header"><h5><i class='bx bx-error-circle'></i> Sản phẩm cần nhập thêm</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty lowStockProducts}"><div class="empty-state">Không có cảnh báo tồn kho.</div></c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Sản phẩm</th><th>Danh mục</th><th>Tồn kho</th><th>Review</th></tr></thead>
                            <tbody>
                            <c:forEach var="product" items="${lowStockProducts}">
                                <tr>
                                    <td>${product.name}</td>
                                    <td>${empty product.category ? 'Chưa phân loại' : product.category}</td>
                                    <td><span class="pill orange">${product.stock}</span></td>
                                    <td>${product.formattedAverageRating} ★</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header"><h5><i class='bx bx-star'></i> Review tiêu cực gần đây</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty lowRatingReviews}"><div class="empty-state">Chưa có review thấp sao.</div></c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Khách hàng</th><th>Sản phẩm</th><th>Điểm</th><th>Nội dung</th></tr></thead>
                            <tbody>
                            <c:forEach var="review" items="${lowRatingReviews}">
                                <tr>
                                    <td>${review.userName}</td>
                                    <td>${review.productName}</td>
                                    <td><span class="pill red">${review.rating}/5</span></td>
                                    <td>${review.comment}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel full">
            <div class="panel-header"><h5><i class='bx bx-line-chart'></i> Doanh thu theo tháng (${selectedYear})</h5></div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty revenueByMonth}">
                        <div class="empty-state">Năm ${selectedYear} chưa có doanh thu.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Tháng</th><th>Doanh thu</th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${revenueByMonth}">
                                <tr>
                                    <td>Tháng ${item.month}</td>
                                    <td><fmt:formatNumber value="${item.revenue}" type="number" maxFractionDigits="0"/>đ</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>
</body>
</html>
