<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Thống kê & Báo cáo - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        * { font-family: 'Nunito', sans-serif; }
        
        .stats-overview {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            margin-bottom: 24px;
        }
        .stat-box {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .stat-box .icon {
            width: 50px;
            height: 50px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }
        .stat-box .icon.blue { background: #dbeafe; color: #2563eb; }
        .stat-box .icon.green { background: #dcfce7; color: #16a34a; }
        .stat-box .icon.purple { background: #f3e8ff; color: #7c3aed; }
        .stat-box .icon.orange { background: #ffedd5; color: #ea580c; }
        .stat-box h3 { margin: 0; font-size: 1.5rem; font-weight: 700; color: #1e293b; }
        .stat-box p { margin: 0; color: #64748b; font-size: 0.85rem; }
        
        .chart-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 24px;
            margin-bottom: 24px;
        }
        .chart-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            overflow: hidden;
        }
        .chart-card.full-width {
            grid-column: span 2;
        }
        .chart-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .chart-header h5 {
            margin: 0;
            font-weight: 700;
            color: #1e293b;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .chart-header h5 i { color: #64748b; }
        .chart-body {
            padding: 20px;
            position: relative;
            min-height: 300px;
        }
        
        .year-filter {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .year-filter select {
            padding: 6px 12px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp"><jsp:param name="currentPage" value="statistics"/></jsp:include>
    
    <main class="admin-main">
        <div class="page-header-admin">
            <div>
                <h1 class="page-title"><i class='bx bxs-bar-chart-alt-2'></i> Thống kê & Báo cáo</h1>
                <p class="page-subtitle">Phân tích dữ liệu kinh doanh PetShop</p>
            </div>
            <div class="d-flex align-items-center gap-3">
                <div class="year-filter">
                    <label>Năm:</label>
                    <select onchange="window.location.href='?year='+this.value">
                        <c:forEach var="y" begin="${currentYear - 2}" end="${currentYear}">
                            <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                        </c:forEach>
                    </select>
                </div>
                <jsp:include page="/components/admin-header-dropdown.jsp" />
            </div>
        </div>

        <!-- Overview Stats -->
        <div class="stats-overview">
            <div class="stat-box">
                <div class="icon blue"><i class='bx bxs-user'></i></div>
                <div><h3>${overview.totalUsers}</h3><p>Khách hàng</p></div>
            </div>
            <div class="stat-box">
                <div class="icon green"><i class='bx bxs-package'></i></div>
                <div><h3>${overview.totalProducts}</h3><p>Sản phẩm</p></div>
            </div>
            <div class="stat-box">
                <div class="icon orange"><i class='bx bxs-alarm-exclamation'></i></div>
                <div><h3>${overview.pendingOrders}</h3><p>Đơn hàng chờ xử lý</p></div>
            </div>
        </div>

        <!-- Charts Row 1 -->
        <div class="chart-grid">
            <!-- Biểu đồ cột: Doanh thu theo tháng -->
            <div class="chart-card">
                <div class="chart-header">
                    <h5><i class='bx bx-money'></i> Doanh thu theo tháng (VNĐ)</h5>
                </div>
                <div class="chart-body">
                    <canvas id="revenueChart"></canvas>
                </div>
            </div>

            <!-- Biểu đồ tròn: Trạng thái đơn hàng -->
            <div class="chart-card">
                <div class="chart-header">
                    <h5><i class='bx bx-pie-chart-alt-2'></i> Trạng thái đơn hàng</h5>
                </div>
                <div class="chart-body">
                    <canvas id="statusChart"></canvas>
                </div>
            </div>
        </div>

        <!-- Charts Row 2 -->
        <div class="chart-grid">
            <!-- Biểu đồ cột ngang: Sản phẩm bán chạy -->
            <div class="chart-card">
                <div class="chart-header">
                    <h5><i class='bx bx-trophy'></i> Top 5 sản phẩm bán chạy</h5>
                </div>
                <div class="chart-body">
                    <canvas id="topProductsChart"></canvas>
                </div>
            </div>

            <!-- Biểu đồ đường: Đơn hàng theo tháng -->
            <div class="chart-card">
                <div class="chart-header">
                    <h5><i class='bx bx-line-chart'></i> Đơn hàng theo tháng</h5>
                </div>
                <div class="chart-body">
                    <canvas id="ordersChart"></canvas>
                </div>
            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        var monthLabels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
        
        // 1. Biểu đồ doanh thu
        var revenueData = ${revenueByMonthJson};
        var revenueCounts = new Array(12).fill(0);
        revenueData.forEach(function(item) { revenueCounts[item.month - 1] = item.revenue; });
        
        new Chart(document.getElementById('revenueChart'), {
            type: 'bar',
            data: {
                labels: monthLabels,
                datasets: [{
                    label: 'Doanh thu',
                    data: revenueCounts,
                    backgroundColor: 'rgba(37, 99, 235, 0.8)',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true } }
            }
        });
        
        // 2. Biểu đồ trạng thái đơn hàng
        var statusData = ${orderStatusJson};
        new Chart(document.getElementById('statusChart'), {
            type: 'doughnut',
            data: {
                labels: statusData.map(function(item) { return item.label; }),
                datasets: [{
                    data: statusData.map(function(item) { return item.count; }),
                    backgroundColor: ['#f59e0b', '#3b82f6', '#10b981', '#ef4444', '#64748b']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'right' } }
            }
        });
        
        // 3. Biểu đồ Top sản phẩm
        var topProductsData = ${topProductsJson};
        new Chart(document.getElementById('topProductsChart'), {
            type: 'bar',
            data: {
                labels: topProductsData.map(function(item) { return item.label; }),
                datasets: [{
                    label: 'Số lượng bán',
                    data: topProductsData.map(function(item) { return item.count; }),
                    backgroundColor: 'rgba(124, 58, 237, 0.8)',
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } }
            }
        });
        
        // 4. Biểu đồ đơn hàng theo tháng
        var ordersData = ${ordersByMonthJson};
        var pendingOrders = new Array(12).fill(0);
        var completedOrders = new Array(12).fill(0);
        ordersData.forEach(function(item) {
            pendingOrders[item.month - 1] = item.pending;
            completedOrders[item.month - 1] = item.completed;
        });
        
        new Chart(document.getElementById('ordersChart'), {
            type: 'line',
            data: {
                labels: monthLabels,
                datasets: [
                    {
                        label: 'Chờ xử lý',
                        data: pendingOrders,
                        borderColor: '#f59e0b',
                        tension: 0.3,
                        fill: false
                    },
                    {
                        label: 'Hoàn thành',
                        data: completedOrders,
                        borderColor: '#10b981',
                        tension: 0.3,
                        fill: false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'top' } },
                scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
            }
        });
    </script>
</body>
</html>
