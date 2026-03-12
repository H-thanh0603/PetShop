<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Báo cáo Thống kê - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        * { font-family: 'Nunito', sans-serif; }
    </style>
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .chart-container { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); margin-bottom: 24px; }
        .chart-title { font-size: 1.1rem; font-weight: 700; color: #263238; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }
        .chart-title i { color: #00bfa5; }
    </style>
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp"><jsp:param name="currentPage" value="reports"/></jsp:include>
    
    <main class="admin-main">
        <div class="page-header-admin">
            <div>
                <h1 class="page-title"><i class='bx bxs-bar-chart-alt-2'></i> Báo cáo Thống kê</h1>
                <p class="page-subtitle">Tổng quan hoạt động và doanh thu</p>
            </div>
            <div class="d-flex gap-3 align-items-center">
                <form method="GET" class="d-flex gap-2 align-items-center">
                    <label class="fw-bold">Năm:</label>
                    <select name="year" class="form-select" style="width:120px;" onchange="this.form.submit()">
                        <c:forEach begin="${currentYear - 5}" end="${currentYear}" var="y">
                            <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                        </c:forEach>
                    </select>
                </form>
                <jsp:include page="/components/admin-header-dropdown.jsp" />
            </div>
        </div>

        <!-- Overview Stats -->
        <div class="row g-4 mb-4">
            <div class="col-md-2">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-primary"><i class='bx bxs-group'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${overview.totalUsers}</div>
                        <div class="stat-label-admin">Người dùng</div>
                    </div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-success"><i class='bx bxs-calendar-check'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${overview.totalAppointments}</div>
                        <div class="stat-label-admin">Lịch hẹn</div>
                    </div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-info"><i class='bx bx-check-circle'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${overview.completedAppointments}</div>
                        <div class="stat-label-admin">Hoàn thành</div>
                    </div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-warning"><i class='bx bxs-dog'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${overview.totalPets}</div>
                        <div class="stat-label-admin">Thú cưng</div>
                    </div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-danger"><i class='bx bx-injection'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${overview.totalVaccinations}</div>
                        <div class="stat-label-admin">Mũi tiêm</div>
                    </div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin" style="background:#8b5cf6;"><i class='bx bxs-user-badge'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${overview.totalDoctors}</div>
                        <div class="stat-label-admin">Bác sĩ</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Charts Row 1 -->
        <div class="row g-4 mb-4">
            <div class="col-lg-8">
                <div class="chart-container">
                    <h5 class="chart-title"><i class='bx bx-line-chart'></i> Lịch hẹn theo tháng (${selectedYear})</h5>
                    <canvas id="appointmentsChart" height="100"></canvas>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="chart-container">
                    <h5 class="chart-title"><i class='bx bx-pie-chart-alt-2'></i> Theo dịch vụ</h5>
                    <canvas id="servicesChart" height="200"></canvas>
                </div>
            </div>
        </div>

        <!-- Charts Row 2 -->
        <div class="row g-4">
            <div class="col-lg-6">
                <div class="chart-container">
                    <h5 class="chart-title"><i class='bx bx-bar-chart'></i> Tiêm chủng theo tháng (${selectedYear})</h5>
                    <canvas id="vaccinationsChart" height="150"></canvas>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="chart-container">
                    <h5 class="chart-title"><i class='bx bx-injection'></i> Vaccine phổ biến</h5>
                    <canvas id="vaccinesChart" height="150"></canvas>
                </div>
            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Data from server
        const appointmentsByMonth = ${appointmentsByMonthJson};
        const vaccinationsByMonth = ${vaccinationsByMonthJson};
        const appointmentsByService = ${appointmentsByServiceJson};
        const popularVaccines = ${popularVaccinesJson};
        
        // Appointments Chart
        const months = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
        const appointmentsData = new Array(12).fill(0);
        const completedData = new Array(12).fill(0);
        appointmentsByMonth.forEach(item => {
            appointmentsData[item.month - 1] = item.count;
            completedData[item.month - 1] = item.completed;
        });
        
        new Chart(document.getElementById('appointmentsChart'), {
            type: 'line',
            data: {
                labels: months,
                datasets: [{
                    label: 'Tổng lịch hẹn',
                    data: appointmentsData,
                    borderColor: '#00bfa5',
                    backgroundColor: 'rgba(0, 191, 165, 0.1)',
                    fill: true,
                    tension: 0.4
                }, {
                    label: 'Hoàn thành',
                    data: completedData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
        });
        
        // Services Pie Chart
        new Chart(document.getElementById('servicesChart'), {
            type: 'doughnut',
            data: {
                labels: appointmentsByService.map(i => i.service),
                datasets: [{
                    data: appointmentsByService.map(i => i.count),
                    backgroundColor: ['#00bfa5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#84cc16']
                }]
            },
            options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
        });
        
        // Vaccinations Chart
        const vaccinationsData = new Array(12).fill(0);
        vaccinationsByMonth.forEach(item => {
            vaccinationsData[item.month - 1] = item.count;
        });
        
        new Chart(document.getElementById('vaccinationsChart'), {
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: 'Số mũi tiêm',
                    data: vaccinationsData,
                    backgroundColor: '#10b981',
                    borderRadius: 8
                }]
            },
            options: { responsive: true, plugins: { legend: { display: false } } }
        });
        
        // Popular Vaccines Chart
        new Chart(document.getElementById('vaccinesChart'), {
            type: 'bar',
            data: {
                labels: popularVaccines.map(i => i.vaccine),
                datasets: [{
                    label: 'Số lượt tiêm',
                    data: popularVaccines.map(i => i.count),
                    backgroundColor: '#8b5cf6',
                    borderRadius: 8
                }]
            },
            options: { 
                responsive: true, 
                indexAxis: 'y',
                plugins: { legend: { display: false } } 
            }
        });
    </script>
</body>
</html>
