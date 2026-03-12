<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Gửi Thông báo - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        * { font-family: 'Nunito', sans-serif; }
        
        .notification-stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-bottom: 24px;
        }
        .stat-box {
            background: white;
            border-radius: 16px;
            padding: 24px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .stat-box .icon {
            width: 60px;
            height: 60px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.8rem;
        }
        .stat-box .icon.warning { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); color: white; }
        .stat-box .icon.success { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; }
        .stat-box .icon.info { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: white; }
        .stat-box .info h3 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1e293b; }
        .stat-box .info p { margin: 4px 0 0 0; color: #64748b; font-size: 0.9rem; }
        
        .filter-card {
            background: white;
            border-radius: 16px;
            padding: 20px 24px;
            margin-bottom: 24px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
        }
        .filter-left {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .filter-select {
            padding: 10px 16px;
            border: 2px solid #e2e8f0;
            border-radius: 10px;
            font-size: 0.95rem;
            font-weight: 600;
            min-width: 200px;
            transition: all 0.2s;
        }
        .filter-select:focus {
            border-color: #0d9488;
            outline: none;
        }
        
        .btn-send-all {
            background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%);
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 10px;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s;
        }
        .btn-send-all:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(13, 148, 136, 0.4);
            color: white;
        }
        .btn-send-all:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        
        .reminder-table {
            background: white;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }
        .table-header {
            background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%);
            color: white;
            padding: 18px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .table-header h5 {
            margin: 0;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .data-table {
            width: 100%;
            border-collapse: collapse;
        }
        .data-table th {
            background: #f8fafc;
            padding: 14px 20px;
            text-align: left;
            font-size: 0.8rem;
            font-weight: 700;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 2px solid #e2e8f0;
        }
        .data-table td {
            padding: 16px 20px;
            border-bottom: 1px solid #f1f5f9;
            vertical-align: middle;
        }
        .data-table tr:hover {
            background: #f8fafc;
        }
        
        .customer-info .name {
            font-weight: 700;
            color: #1e293b;
            font-size: 0.95rem;
        }
        .customer-info .email {
            color: #64748b;
            font-size: 0.85rem;
            margin-top: 2px;
        }
        
        .pet-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            background: #f0fdfa;
            color: #0d9488;
            border-radius: 20px;
            font-weight: 600;
            font-size: 0.85rem;
        }
        
        .vaccine-name {
            font-weight: 600;
            color: #334155;
        }
        
        .due-date {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 14px;
            border-radius: 8px;
            font-weight: 700;
            font-size: 0.9rem;
        }
        .due-date.urgent {
            background: #fef2f2;
            color: #dc2626;
        }
        .due-date.soon {
            background: #fffbeb;
            color: #d97706;
        }
        .due-date.normal {
            background: #f0fdf4;
            color: #16a34a;
        }
        
        .btn-send {
            background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%);
            color: white;
            border: none;
            padding: 10px 18px;
            border-radius: 8px;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: all 0.2s;
            font-size: 0.9rem;
        }
        .btn-send:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
            color: white;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }
        .empty-state i {
            font-size: 5rem;
            color: #10b981;
            margin-bottom: 16px;
        }
        .empty-state h4 {
            color: #1e293b;
            margin-bottom: 8px;
        }
        .empty-state p {
            color: #64748b;
        }
        
        /* Preview Modal */
        .email-preview {
            background: #f8fafc;
            border-radius: 12px;
            padding: 20px;
            max-height: 400px;
            overflow-y: auto;
        }
        .email-preview iframe {
            width: 100%;
            height: 350px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
        }
    </style>
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp"><jsp:param name="currentPage" value="notifications"/></jsp:include>
    <jsp:include page="/components/admin-toast.jsp" />
    
    <main class="admin-main">
        <div class="page-header-admin">
            <div>
                <h1 class="page-title"><i class='bx bxs-bell-ring'></i> Gửi Thông báo</h1>
                <p class="page-subtitle">Nhắc lịch tiêm chủng cho khách hàng qua email</p>
            </div>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <!-- Stats -->
        <div class="notification-stats">
            <div class="stat-box">
                <div class="icon warning"><i class='bx bx-bell'></i></div>
                <div class="info">
                    <h3>${totalReminders}</h3>
                    <p>Cần nhắc nhở</p>
                </div>
            </div>
            <div class="stat-box">
                <div class="icon success"><i class='bx bx-calendar-check'></i></div>
                <div class="info">
                    <h3>${selectedDays}</h3>
                    <p>Ngày tới</p>
                </div>
            </div>
            <div class="stat-box">
                <div class="icon info"><i class='bx bx-envelope'></i></div>
                <div class="info">
                    <h3><i class='bx bx-check-circle' style="color: #10b981;"></i></h3>
                    <p>Sẵn sàng gửi</p>
                </div>
            </div>
        </div>

        <!-- Filter & Actions -->
        <div class="filter-card">
            <div class="filter-left">
                <label class="fw-bold" style="color: #475569;"><i class='bx bx-filter-alt'></i> Lọc theo:</label>
                <form method="GET" id="filterForm">
                    <select name="days" class="filter-select" onchange="this.form.submit()">
                        <option value="3" ${selectedDays == 3 ? 'selected' : ''}>🔴 Trong 3 ngày tới (Khẩn cấp)</option>
                        <option value="7" ${selectedDays == 7 ? 'selected' : ''}>🟡 Trong 7 ngày tới</option>
                        <option value="14" ${selectedDays == 14 ? 'selected' : ''}>🟢 Trong 14 ngày tới</option>
                        <option value="30" ${selectedDays == 30 ? 'selected' : ''}>📅 Trong 30 ngày tới</option>
                    </select>
                </form>
            </div>
            <div class="filter-right">
                <form method="POST" action="${pageContext.request.contextPath}/admin/notifications" 
                      onsubmit="return confirm('Bạn có chắc muốn gửi email nhắc nhở đến tất cả ${totalReminders} khách hàng?');">
                    <input type="hidden" name="action" value="sendAll">
                    <input type="hidden" name="days" value="${selectedDays}">
                    <button type="submit" class="btn-send-all" ${empty reminders ? 'disabled' : ''}>
                        <i class='bx bx-send'></i> Gửi tất cả (${totalReminders})
                    </button>
                </form>
            </div>
        </div>

        <!-- Reminders Table -->
        <div class="reminder-table">
            <div class="table-header">
                <h5><i class='bx bx-list-ul'></i> Danh sách cần nhắc nhở tiêm chủng</h5>
                <span class="badge bg-white text-dark">${totalReminders} khách hàng</span>
            </div>
            
            <c:choose>
                <c:when test="${empty reminders}">
                    <div class="empty-state">
                        <i class='bx bx-check-circle'></i>
                        <h4>Tuyệt vời! Không có lịch tiêm nào cần nhắc nhở</h4>
                        <p>Tất cả khách hàng đã được thông báo hoặc chưa đến hạn tiêm trong ${selectedDays} ngày tới</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Khách hàng</th>
                                <th>Thú cưng</th>
                                <th>Vaccine</th>
                                <th>Ngày tiêm tiếp theo</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="reminder" items="${reminders}">
                                <tr>
                                    <td>
                                        <div class="customer-info">
                                            <div class="name">${reminder.fullname}</div>
                                            <div class="email"><i class='bx bx-envelope'></i> ${reminder.email}</div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="pet-badge">
                                            <i class='bx bxs-dog'></i> ${reminder.petName}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="vaccine-name">
                                            <i class='bx bx-injection' style="color: #0d9488;"></i> ${reminder.vaccineName}
                                        </span>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${reminder.nextDueDate}" pattern="dd/MM/yyyy" var="formattedDate"/>
                                        <c:set var="today" value="<%=new java.util.Date()%>"/>
                                        <c:set var="dueDate" value="${reminder.nextDueDate}"/>
                                        <c:set var="diffDays" value="${(dueDate.time - today.time) / (1000 * 60 * 60 * 24)}"/>
                                        
                                        <c:choose>
                                            <c:when test="${diffDays <= 3}">
                                                <span class="due-date urgent">
                                                    <i class='bx bx-error-circle'></i> ${formattedDate}
                                                </span>
                                            </c:when>
                                            <c:when test="${diffDays <= 7}">
                                                <span class="due-date soon">
                                                    <i class='bx bx-time-five'></i> ${formattedDate}
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="due-date normal">
                                                    <i class='bx bx-calendar'></i> ${formattedDate}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <form method="POST" action="${pageContext.request.contextPath}/admin/notifications" style="display:inline;">
                                            <input type="hidden" name="action" value="sendReminder">
                                            <input type="hidden" name="email" value="${reminder.email}">
                                            <input type="hidden" name="customerName" value="${reminder.fullname}">
                                            <input type="hidden" name="petName" value="${reminder.petName}">
                                            <input type="hidden" name="vaccineName" value="${reminder.vaccineName}">
                                            <input type="hidden" name="dueDate" value="${formattedDate}">
                                            <button type="submit" class="btn-send" title="Gửi email nhắc nhở">
                                                <i class='bx bx-send'></i> Gửi
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
        
        <!-- Info Card -->
        <div class="card-admin mt-4">
            <div class="card-body-admin">
                <h6 class="fw-bold mb-3"><i class='bx bx-info-circle text-primary'></i> Hướng dẫn</h6>
                <ul class="mb-0" style="color: #64748b;">
                    <li>🔴 <strong>Màu đỏ:</strong> Lịch tiêm trong 3 ngày tới - Cần gửi nhắc nhở ngay</li>
                    <li>🟡 <strong>Màu vàng:</strong> Lịch tiêm trong 7 ngày tới - Nên gửi nhắc nhở sớm</li>
                    <li>🟢 <strong>Màu xanh:</strong> Lịch tiêm còn nhiều ngày - Có thể gửi nhắc nhở trước</li>
                    <li>📧 Email sẽ được gửi đến địa chỉ email đã đăng ký của khách hàng</li>
                </ul>
            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        <c:if test="${not empty sessionScope.message}">
            showAdminToast('${sessionScope.message}', '${sessionScope.messageType}');
        </c:if>
        <% session.removeAttribute("message"); session.removeAttribute("messageType"); %>
    </script>
</body>
</html>
