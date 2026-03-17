<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Quản lý Người dùng - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        * { font-family: 'Nunito', sans-serif; }
        
        /* User Avatar */
        .user-avatar {
            width: 42px; height: 42px;
            border-radius: 50%;
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: white;
            display: flex; align-items: center; justify-content: center;
            font-weight: 700; font-size: 1rem;
            flex-shrink: 0;
        }
        .user-avatar.admin { background: linear-gradient(135deg, #f59e0b, #d97706); }
        .user-avatar.locked { background: linear-gradient(135deg, #ef4444, #dc2626); }
        
        /* Status Badge */
        .status-dot {
            width: 8px; height: 8px;
            border-radius: 50%;
            display: inline-block;
            margin-right: 6px;
        }
        .status-dot.active { background: #10b981; }
        .status-dot.locked { background: #ef4444; }
        
        /* Table Row Clickable */
        .table-admin tbody tr {
            cursor: pointer;
            transition: all 0.2s;
        }
        .table-admin tbody tr:hover {
            background: #f0f9ff !important;
        }
        .table-admin tbody tr.selected {
            background: #dbeafe !important;
            border-left: 3px solid #3b82f6;
        }
        
        /* Stats Mini */
        .stat-mini {
            display: flex; align-items: center; gap: 4px;
            font-size: 0.8rem; color: #64748b;
        }
        .stat-mini i { font-size: 0.9rem; }
        
        /* Modal Tabs */
        .modal-tabs {
            display: flex; border-bottom: 2px solid #e2e8f0;
            margin-bottom: 20px;
        }
        .modal-tab {
            padding: 12px 20px;
            border: none; background: none;
            font-weight: 600; color: #64748b;
            cursor: pointer;
            border-bottom: 2px solid transparent;
            margin-bottom: -2px;
            transition: all 0.2s;
        }
        .modal-tab:hover { color: #3b82f6; }
        .modal-tab.active {
            color: #3b82f6;
            border-bottom-color: #3b82f6;
        }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        
        /* User Detail Header */
        .user-detail-header {
            display: flex; align-items: center; gap: 16px;
            padding: 20px; background: linear-gradient(135deg, #f8fafc, #f1f5f9);
            border-radius: 12px; margin-bottom: 20px;
        }
        .user-detail-avatar {
            width: 72px; height: 72px;
            border-radius: 50%;
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: white;
            display: flex; align-items: center; justify-content: center;
            font-weight: 700; font-size: 1.8rem;
        }
        .user-detail-avatar.admin { background: linear-gradient(135deg, #f59e0b, #d97706); }
        
        /* Info Grid */
        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 16px;
        }
        .info-item {
            padding: 12px;
            background: #f8fafc;
            border-radius: 8px;
        }
        .info-label {
            font-size: 0.8rem;
            color: #64748b;
            margin-bottom: 4px;
            display: flex; align-items: center; gap: 6px;
        }
        .info-value {
            font-weight: 600;
            color: #0f172a;
        }
        
        /* Order Card */
        .mini-card {
            padding: 12px;
            background: #f8fafc;
            border-radius: 8px;
            margin-bottom: 10px;
            display: flex; align-items: center; gap: 12px;
        }
        .mini-card-icon {
            width: 40px; height: 40px;
            border-radius: 10px;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.2rem;
        }
        .mini-card-icon.order { background: #dbeafe; color: #3b82f6; }
        
        /* Action Dropdown */
        .action-dropdown {
            position: relative;
        }
        .action-dropdown-menu {
            position: absolute;
            right: 0; top: 100%;
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.12);
            min-width: 180px;
            z-index: 100;
            display: none;
        }
        .action-dropdown.open .action-dropdown-menu { display: block; }
        .action-dropdown-menu a, .action-dropdown-menu button {
            display: flex; align-items: center; gap: 10px;
            padding: 10px 16px;
            color: #334155;
            text-decoration: none;
            font-size: 0.9rem;
            border: none; background: none;
            width: 100%; text-align: left;
            cursor: pointer;
        }
        .action-dropdown-menu a:hover, .action-dropdown-menu button:hover {
            background: #f1f5f9;
        }
        .action-dropdown-menu .danger { color: #dc2626; }
        .action-dropdown-menu .danger:hover { background: #fef2f2; }
    </style>
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp"><jsp:param name="currentPage" value="users"/></jsp:include>
    <jsp:include page="/components/admin-toast.jsp" />
    
    <main class="admin-main">
        <!-- Header -->
        <div class="page-header-admin">
            <div>
                <h1 class="page-title"><i class='bx bxs-group'></i> Quản lý Người dùng</h1>
                <p class="page-subtitle">Xem và quản lý tài khoản người dùng</p>
            </div>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <!-- Stats -->
        <div class="row g-4 mb-4">
            <div class="col-md-3">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-primary"><i class='bx bxs-group'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${totalUsers}</div>
                        <div class="stat-label-admin">Tổng người dùng</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-success"><i class='bx bxs-user'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${totalRegularUsers}</div>
                        <div class="stat-label-admin">Khách hàng</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-warning"><i class='bx bxs-crown'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${totalAdmins}</div>
                        <div class="stat-label-admin">Quản trị viên</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card-admin">
                    <div class="stat-icon-admin bg-info"><i class='bx bxs-user-plus'></i></div>
                    <div class="stat-info">
                        <div class="stat-number-admin">${newUsersThisWeek}</div>
                        <div class="stat-label-admin">Mới trong tuần</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Search & Filter -->
        <div class="card-admin mb-4">
            <div class="card-body-admin py-3 px-4">
                <form method="GET" class="d-flex gap-3 align-items-center flex-wrap">
                    <div class="flex-grow-1" style="min-width: 250px;">
                        <div class="position-relative">
                            <i class='bx bx-search position-absolute' style="left:12px;top:50%;transform:translateY(-50%);color:#94a3b8;"></i>
                            <input type="text" name="keyword" class="form-control ps-5" 
                                   placeholder="Tìm theo tên, email, SĐT..." value="${keyword}">
                        </div>
                    </div>
                    <select name="role" class="form-select" style="width:180px;">
                        <option value="">Tất cả vai trò</option>
                        <option value="user" ${selectedRole == 'user' ? 'selected' : ''}>Khách hàng</option>
                        <option value="admin" ${selectedRole == 'admin' ? 'selected' : ''}>Quản trị viên</option>
                    </select>
                    <button type="submit" class="btn btn-primary">
                        <i class='bx bx-search'></i> Tìm kiếm
                    </button>
                    <c:if test="${not empty keyword || not empty selectedRole}">
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline-secondary">
                            <i class='bx bx-x'></i> Xóa lọc
                        </a>
                    </c:if>
                    <button type="button" class="btn btn-success ms-auto" data-bs-toggle="modal" data-bs-target="#addUserModal">
                        <i class='bx bx-plus'></i> Thêm người dùng
                    </button>
                </form>
            </div>
        </div>

        <!-- Users Table -->
        <div class="card-admin">
            <div class="card-header-admin">
                <h5><i class='bx bx-list-ul'></i> Danh sách người dùng (${users.size()})</h5>
            </div>
            <div class="card-body-admin">
                <div class="table-responsive">
                    <table class="table-admin" id="usersTable">
                        <thead>
                            <tr>
                                <th style="width:50px;">ID</th>
                                <th>Người dùng</th>
                                <th>Email / SĐT</th>
                                <th style="width:100px;">Vai trò</th>
                                <th style="width:120px;">Ngày tạo</th>
                                <th style="width:100px;">Thống kê</th>
                                <th style="width:120px;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr style="cursor:pointer;" 
                                    onclick="viewUserDetail(this)"
                                    data-id="${user.id}"
                                    data-username="${user.username}"
                                    data-fullname="${user.fullname}"
                                    data-email="${user.email}"
                                    data-phone="${user.phone}"
                                    data-address="${user.address}"
                                    data-role="${user.role}"
                                    data-status="${user.status != null ? user.status : 'active'}"
                                    data-ordercount="${user.orderCount}"
                                    data-totalspent="<fmt:formatNumber value='${user.totalSpent}' type='currency' currencySymbol='đ' maxFractionDigits='0'/>"
                                    data-createdat="<fmt:formatDate value='${user.createdAt}' pattern='dd/MM/yyyy HH:mm'/>">
                                    <td><strong>#${user.id}</strong></td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="user-avatar ${user.role == 'admin' ? 'admin' : ''} ${user.status == 'locked' ? 'locked' : ''}">
                                                ${not empty user.fullname ? user.fullname.substring(0,1).toUpperCase() : 'U'}
                                            </div>
                                            <div>
                                                <div class="fw-bold">${not empty user.fullname ? user.fullname : 'Unknown'}</div>
                                                <small class="text-muted">@${user.username}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div>${user.email}</div>
                                        <small class="text-muted">${not empty user.phone ? user.phone : '-'}</small>
                                    </td>
                                    <td>
                                        <span class="badge-admin ${user.role == 'admin' ? 'badge-warning' : 'badge-primary'}">
                                            <i class='bx ${user.role == "admin" ? "bxs-crown" : "bxs-user"}'></i>
                                            ${user.role == 'admin' ? 'Admin' : 'User'}
                                        </span>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy"/>
                                    </td>
                                    <td>
                                        <div class="stat-mini"><i class='bx bx-shopping-bag'></i> ${user.orderCount} đơn hàng</div>
                                        <div class="stat-mini"><i class='bx bx-wallet'></i> <fmt:formatNumber value="${user.totalSpent}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></div>
                                    </td>
                                    <td onclick="event.stopPropagation();">
                                        <div class="action-buttons">
                                            <button class="btn-action-admin" onclick="viewUserDetailById(${user.id})" title="Xem chi tiết">
                                                <i class='bx bx-show'></i>
                                            </button>
                                            <c:if test="${user.role != 'admin'}">
                                                <button class="btn-action-admin" onclick="changeRole(${user.id}, '${user.username}', 'admin')" title="Nâng Admin" style="color:#f59e0b;">
                                                    <i class='bx bx-up-arrow-alt'></i>
                                                </button>
                                            </c:if>
                                            <c:if test="${user.role == 'admin' && user.id != sessionScope.user.id}">
                                                <button class="btn-action-admin" onclick="changeRole(${user.id}, '${user.username}', 'user')" title="Hạ User" style="color:#3b82f6;">
                                                    <i class='bx bx-down-arrow-alt'></i>
                                                </button>
                                            </c:if>
                                            <c:if test="${user.id != sessionScope.user.id}">
                                                <button class="btn-action-admin btn-delete" onclick="confirmDelete(${user.id}, '${user.username}')">
                                                    <i class='bx bx-trash'></i>
                                                </button>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty users}">
                                <tr><td colspan="7" class="text-center py-5">
                                    <i class='bx bx-user-x' style="font-size:3rem;color:#cbd5e1;"></i>
                                    <p class="text-muted mt-2 mb-0">Không tìm thấy người dùng nào</p>
                                </td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <!-- View User Detail Modal -->
    <div class="modal fade" id="viewUserModal" tabindex="-1">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class='bx bx-user-circle'></i> Chi tiết người dùng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <!-- User Header -->
                    <div class="user-detail-header">
                        <div class="user-detail-avatar" id="detailAvatar">A</div>
                        <div class="flex-grow-1">
                            <h4 class="mb-1" id="detailFullname">-</h4>
                            <div class="text-muted mb-2">@<span id="detailUsername">-</span></div>
                            <div class="d-flex gap-2">
                                <span class="badge-admin badge-primary" id="detailRoleBadge">User</span>
                                <span class="badge-admin badge-success" id="detailStatusBadge">Hoạt động</span>
                            </div>
                        </div>
                        <div class="text-end">
                            <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal()">
                                <i class='bx bx-edit'></i> Sửa
                            </button>
                            <button class="btn btn-sm btn-outline-secondary" onclick="openMoreActions()">
                                <i class='bx bx-dots-vertical-rounded'></i>
                            </button>
                        </div>
                    </div>
                    
                    <!-- Tabs -->
                    <div class="modal-tabs">
                        <button class="modal-tab active" onclick="switchTab('info')">
                            <i class='bx bx-info-circle'></i> Thông tin
                        </button>
                        <button class="modal-tab" onclick="switchTab('orders')">
                            <i class='bx bx-shopping-bag'></i> Đơn hàng (<span id="orderCount">0</span>)
                        </button>
                        <div class="modal-tab-placeholder"></div>
                    </div>
                    
                    <!-- Tab: Info -->
                    <div class="tab-content active" id="tab-info">
                        <div class="info-grid">
                            <div class="info-item">
                                <div class="info-label"><i class='bx bx-id-card'></i> ID</div>
                                <div class="info-value" id="detailId">-</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label"><i class='bx bx-envelope'></i> Email</div>
                                <div class="info-value" id="detailEmail">-</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label"><i class='bx bx-phone'></i> Số điện thoại</div>
                                <div class="info-value" id="detailPhone">-</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label"><i class='bx bx-calendar'></i> Ngày đăng ký</div>
                                <div class="info-value" id="detailCreatedAt">-</div>
                            </div>
                            <div class="info-item" style="grid-column: span 2;">
                                <div class="info-label"><i class='bx bx-map'></i> Địa chỉ</div>
                                <div class="info-value" id="detailAddress">-</div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Tab: Orders -->
                    <div class="tab-content" id="tab-orders">
                        <div id="ordersList">
                            <p class="text-muted text-center py-4">Chưa có đơn hàng nào</p>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Add User Modal -->
    <div class="modal fade" id="addUserModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class='bx bx-user-plus'></i> Thêm người dùng mới</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="action" value="add">
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Username <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" name="username" required placeholder="vd: nguyenvana">
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Mật khẩu <span class="text-danger">*</span></label>
                                <input type="password" class="form-control" name="password" required placeholder="Nhập mật khẩu">
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Họ và tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="fullname" required placeholder="Nguyễn Văn A">
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Email</label>
                                <input type="email" class="form-control" name="email" placeholder="email@example.com">
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Số điện thoại</label>
                                <input type="text" class="form-control" name="phone" placeholder="0901234567">
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Vai trò</label>
                            <select class="form-select" name="role">
                                <option value="user">Khách hàng (User)</option>
                                <option value="admin">Quản trị viên (Admin)</option>
                            </select>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-success"><i class='bx bx-plus'></i> Thêm người dùng</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Edit User Modal -->
    <div class="modal fade" id="editUserModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class='bx bx-edit'></i> Chỉnh sửa thông tin</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="userId" id="editUserId">
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label fw-bold">Họ và tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="fullname" id="editFullname" required>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Email</label>
                                <input type="email" class="form-control" name="email" id="editEmail">
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Số điện thoại</label>
                                <input type="text" class="form-control" name="phone" id="editPhone">
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Địa chỉ</label>
                            <textarea class="form-control" name="address" id="editAddress" rows="2"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary"><i class='bx bx-save'></i> Lưu thay đổi</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- More Actions Modal -->
    <div class="modal fade" id="moreActionsModal" tabindex="-1">
        <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Thao tác khác</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body p-0">
                    <div class="list-group list-group-flush">
                        <button class="list-group-item list-group-item-action" onclick="toggleUserStatus()">
                            <i class='bx bx-lock-alt me-2' id="toggleStatusIcon"></i>
                            <span id="toggleStatusText">Khóa tài khoản</span>
                        </button>
                        <button class="list-group-item list-group-item-action" onclick="openResetPasswordModal()">
                            <i class='bx bx-key me-2'></i> Reset mật khẩu
                        </button>
                        <button class="list-group-item list-group-item-action text-danger" onclick="confirmDeleteFromDetail()">
                            <i class='bx bx-trash me-2'></i> Xóa người dùng
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Reset Password Modal -->
    <div class="modal fade" id="resetPasswordModal" tabindex="-1">
        <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class='bx bx-key'></i> Reset mật khẩu</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="action" value="resetPassword">
                    <input type="hidden" name="userId" id="resetPasswordUserId">
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label fw-bold">Mật khẩu mới</label>
                            <input type="password" class="form-control" name="newPassword" required minlength="6">
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-warning">Reset mật khẩu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Change Role Modal -->
    <div class="modal fade" id="roleModal" tabindex="-1">
        <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content text-center">
                <div class="modal-body py-4">
                    <i class='bx bx-user-check text-primary' style="font-size:4rem;"></i>
                    <h5 class="mt-3">Thay đổi vai trò?</h5>
                    <p class="text-muted">Bạn có chắc muốn thay đổi vai trò của <strong id="roleUsername"></strong>?</p>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                        <input type="hidden" name="action" value="updateRole">
                        <input type="hidden" name="userId" id="roleUserId">
                        <input type="hidden" name="role" id="newRole">
                        <div class="d-flex gap-2 justify-content-center">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-primary">Xác nhận</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Delete Modal -->
    <div class="modal fade" id="deleteModal" tabindex="-1">
        <div class="modal-dialog modal-sm modal-dialog-centered">
            <div class="modal-content text-center">
                <div class="modal-body py-4">
                    <i class='bx bx-error-circle text-danger' style="font-size:4rem;"></i>
                    <h5 class="mt-3">Xác nhận xóa?</h5>
                    <p class="text-muted">Bạn có chắc muốn xóa người dùng <strong id="deleteName"></strong>?<br>
                    <small class="text-danger">Hành động này không thể hoàn tác!</small></p>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="userId" id="deleteId">
                        <div class="d-flex gap-2 justify-content-center">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-danger">Xóa</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        var currentUser = null;
        
        document.addEventListener('DOMContentLoaded', function() {
            // Clean up any stale modal backdrops
            document.querySelectorAll('.modal-backdrop').forEach(function(el) { el.remove(); });
            document.body.classList.remove('modal-open');
            document.body.style.overflow = '';
            document.body.style.paddingRight = '';
        });
        
        <c:if test="${not empty sessionScope.message}">
            showAdminToast('${sessionScope.message}', '${sessionScope.messageType}');
        </c:if>
        <% session.removeAttribute("message"); session.removeAttribute("messageType"); %>
        
        // View user detail when clicking on table row
        function viewUserDetail(row) {
            currentUser = {
                id: row.dataset.id,
                username: row.dataset.username || '',
                fullname: row.dataset.fullname || 'Unknown',
                email: row.dataset.email || '',
                phone: row.dataset.phone || '',
                address: row.dataset.address || '',
                role: row.dataset.role || 'user',
                status: row.dataset.status || 'active',
                orderCount: row.dataset.ordercount || 0,
                totalSpent: row.dataset.totalspent || '0đ',
                createdAt: row.dataset.createdat || ''
            };
            showUserModal(currentUser);
        }
        
        // View user detail by ID (from action button)
        function viewUserDetailById(userId) {
            var row = document.querySelector('tr[data-id="' + userId + '"]');
            if (row) {
                viewUserDetail(row);
            }
        }
        
        // Show user detail modal
        function showUserModal(user) {
            if (!user) return;
            
            var firstChar = user.fullname ? user.fullname.charAt(0).toUpperCase() : 'U';
            document.getElementById('detailAvatar').textContent = firstChar;
            document.getElementById('detailAvatar').className = 'user-detail-avatar' + (user.role === 'admin' ? ' admin' : '');
            document.getElementById('detailFullname').textContent = user.fullname || 'Unknown';
            document.getElementById('detailUsername').textContent = user.username || '-';
            
            var roleBadge = document.getElementById('detailRoleBadge');
            roleBadge.className = 'badge-admin ' + (user.role === 'admin' ? 'badge-warning' : 'badge-primary');
            roleBadge.innerHTML = '<i class="bx ' + (user.role === 'admin' ? 'bxs-crown' : 'bxs-user') + '"></i> ' + (user.role === 'admin' ? 'Admin' : 'User');
            
            var statusBadge = document.getElementById('detailStatusBadge');
            statusBadge.className = 'badge-admin ' + (user.status === 'locked' ? 'badge-danger' : 'badge-success');
            statusBadge.textContent = user.status === 'locked' ? 'Đã khóa' : 'Hoạt động';
            
            document.getElementById('detailId').textContent = '#' + user.id;
            document.getElementById('detailEmail').textContent = user.email || 'Chưa cập nhật';
            document.getElementById('detailPhone').textContent = user.phone || 'Chưa cập nhật';
            document.getElementById('detailAddress').textContent = user.address || 'Chưa cập nhật';
            document.getElementById('detailCreatedAt').textContent = user.createdAt || '-';
            
            document.getElementById('orderCount').textContent = user.orderCount;
            
            // Update toggle status button
            var toggleIcon = document.getElementById('toggleStatusIcon');
            var toggleText = document.getElementById('toggleStatusText');
            if (user.status === 'locked') {
                toggleIcon.className = 'bx bx-lock-open-alt me-2';
                toggleText.textContent = 'Mở khóa tài khoản';
            } else {
                toggleIcon.className = 'bx bx-lock-alt me-2';
                toggleText.textContent = 'Khóa tài khoản';
            }
            
            resetTabs();
            var modal = new bootstrap.Modal(document.getElementById('viewUserModal'));
            modal.show();
        }
        
        // Reset tabs to default state
        function resetTabs() {
            var tabs = document.querySelectorAll('.modal-tab');
            var contents = document.querySelectorAll('.tab-content');
            tabs.forEach(function(tab, i) {
                tab.classList.toggle('active', i === 0);
            });
            contents.forEach(function(content, i) {
                content.classList.toggle('active', i === 0);
            });
        }
        
        // Switch between tabs
        function switchTab(tabName) {
            var tabs = document.querySelectorAll('.modal-tab');
            var contents = document.querySelectorAll('.tab-content');
            tabs.forEach(function(tab) { tab.classList.remove('active'); });
            contents.forEach(function(content) { content.classList.remove('active'); });
            
            if (event && event.target) {
                event.target.classList.add('active');
            }
            var targetTab = document.getElementById('tab-' + tabName);
            if (targetTab) {
                targetTab.classList.add('active');
            }
            
            // Load data when switching to orders tab
            if (tabName === 'orders' && currentUser) {
                loadUserOrders(currentUser.id);
            }
        }
        
        // Load user's orders via AJAX
        function loadUserOrders(userId) {
            var container = document.getElementById('ordersList');
            container.innerHTML = '<p class="text-center py-3"><i class="bx bx-loader-alt bx-spin"></i> Đang tải...</p>';
            
            fetch('${pageContext.request.contextPath}/admin/users/api?action=getOrders&userId=' + userId)
                .then(function(response) { return response.json(); })
                .then(function(orders) {
                    if (orders.length === 0) {
                        container.innerHTML = '<p class="text-muted text-center py-4"><i class="bx bx-shopping-bag" style="font-size:2rem;"></i><br>Chưa có đơn hàng nào</p>';
                        return;
                    }
                    
                    var html = '';
                    orders.forEach(function(order) {
                        var statusClass = 'badge-secondary';
                        if (order.status === 'Pending') statusClass = 'badge-warning';
                        else if (order.status === 'Confirmed') statusClass = 'badge-info';
                        else if (order.status === 'Shipping') statusClass = 'badge-primary';
                        else if (order.status === 'Completed') statusClass = 'badge-success';
                        else if (order.status === 'Cancelled') statusClass = 'badge-danger';
                        
                        html += '<div class="mini-card">' +
                            '<div class="mini-card-icon order"><i class="bx bx-package"></i></div>' +
                            '<div class="flex-grow-1">' +
                                '<div class="fw-bold">Đơn hàng #' + order.id + '</div>' +
                                '<small class="text-muted">' + order.createdAt + '</small>' +
                            '</div>' +
                            '<div class="text-end">' +
                                '<div class="fw-bold text-primary">' + order.formattedTotalAmount + '</div>' +
                                '<span class="badge-admin ' + statusClass + '">' + order.status + '</span>' +
                            '</div>' +
                        '</div>';
                    });
                    container.innerHTML = html;
                })
                .catch(function(err) {
                    container.innerHTML = '<p class="text-danger text-center py-4">Lỗi tải dữ liệu</p>';
                    console.error(err);
                });
        }
        
        // Helper to escape HTML
        function escapeHtml(str) {
            if (!str) return '';
            var div = document.createElement('div');
            div.textContent = str;
            return div.innerHTML;
        }
        
        // Open edit modal from detail view
        function openEditModal() {
            if (!currentUser) return;
            
            document.getElementById('editUserId').value = currentUser.id;
            document.getElementById('editFullname').value = currentUser.fullname || '';
            document.getElementById('editEmail').value = currentUser.email || '';
            document.getElementById('editPhone').value = currentUser.phone || '';
            document.getElementById('editAddress').value = currentUser.address || '';
            
            var viewModal = bootstrap.Modal.getInstance(document.getElementById('viewUserModal'));
            if (viewModal) viewModal.hide();
            
            setTimeout(function() {
                new bootstrap.Modal(document.getElementById('editUserModal')).show();
            }, 300);
        }
        
        // Open more actions modal
        function openMoreActions() {
            var viewModal = bootstrap.Modal.getInstance(document.getElementById('viewUserModal'));
            if (viewModal) viewModal.hide();
            
            setTimeout(function() {
                new bootstrap.Modal(document.getElementById('moreActionsModal')).show();
            }, 300);
        }
        
        // Toggle user status (lock/unlock)
        function toggleUserStatus() {
            if (!currentUser) return;
            
            var newStatus = currentUser.status === 'locked' ? 'active' : 'locked';
            var form = document.createElement('form');
            form.method = 'POST';
            form.action = '${pageContext.request.contextPath}/admin/users';
            form.innerHTML = '<input type="hidden" name="action" value="toggleStatus">' +
                '<input type="hidden" name="userId" value="' + currentUser.id + '">' +
                '<input type="hidden" name="status" value="' + newStatus + '">';
            document.body.appendChild(form);
            form.submit();
        }
        
        // Open reset password modal
        function openResetPasswordModal() {
            if (!currentUser) return;
            
            document.getElementById('resetPasswordUserId').value = currentUser.id;
            var moreModal = bootstrap.Modal.getInstance(document.getElementById('moreActionsModal'));
            if (moreModal) moreModal.hide();
            
            setTimeout(function() {
                new bootstrap.Modal(document.getElementById('resetPasswordModal')).show();
            }, 300);
        }
        
        // Confirm delete from detail view
        function confirmDeleteFromDetail() {
            if (!currentUser) return;
            
            document.getElementById('deleteId').value = currentUser.id;
            document.getElementById('deleteName').textContent = currentUser.username || currentUser.fullname;
            
            var moreModal = bootstrap.Modal.getInstance(document.getElementById('moreActionsModal'));
            if (moreModal) moreModal.hide();
            
            setTimeout(function() {
                new bootstrap.Modal(document.getElementById('deleteModal')).show();
            }, 300);
        }
        
        // Change user role (from action button)
        function changeRole(userId, username, role) {
            document.getElementById('roleUserId').value = userId;
            document.getElementById('roleUsername').textContent = username;
            document.getElementById('newRole').value = role;
            new bootstrap.Modal(document.getElementById('roleModal')).show();
        }
        
        // Confirm delete user (from action button)
        function confirmDelete(userId, username) {
            document.getElementById('deleteId').value = userId;
            document.getElementById('deleteName').textContent = username;
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
</body>
</html>
