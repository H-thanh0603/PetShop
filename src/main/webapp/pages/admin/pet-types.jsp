<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Loại Thú Cưng - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="pet-types"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bxs-dog'></i> Quản lý Loại Thú Cưng</h1>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <div class="table-section">
            <div class="table-header">
                <span class="table-title"><i class='bx bx-list-ul'></i> Danh sách loại thú cưng</span>
                <button class="btn-add" onclick="openAddModal()">
                    <i class='bx bx-plus'></i> Thêm loại mới
                </button>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Code</th>
                        <th>Tên</th>
                        <th>Icon (Boxicons)</th>
                        <th>Thứ tự</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty petTypes}">
                        <tr><td colspan="7"><div class="empty-state"><i class='bx bxs-dog'></i><p>Chưa có loại thú cưng nào</p></div></td></tr>
                    </c:if>
                    <c:forEach items="${petTypes}" var="pt" varStatus="s">
                        <tr data-id="${pt.id}" data-name="${pt.name}" data-icon="${pt.icon}"
                            data-order="${pt.displayOrder}" data-active="${pt.active}">
                            <td><strong>${s.index + 1}</strong></td>
                            <td><code style="background:#f1f5f9;padding:3px 8px;border-radius:6px;">${pt.code}</code></td>
                            <td><strong>${pt.name}</strong></td>
                            <td>
                                <i class='${pt.icon}' style="font-size:1.4rem;margin-right:6px;"></i>
                                <small style="color:#64748b;">${pt.icon}</small>
                            </td>
                            <td>${pt.displayOrder}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${pt.active}">
                                        <span style="background:#dcfce7;color:#16a34a;padding:4px 12px;border-radius:20px;font-size:0.8rem;font-weight:600;">Hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="background:#fee2e2;color:#dc2626;padding:4px 12px;border-radius:20px;font-size:0.8rem;font-weight:600;">Ẩn</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <button class="action-btn edit" onclick="openEditModal(this.closest('tr'))" title="Sửa"><i class='bx bx-edit-alt'></i></button>
                                    <form method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="toggle">
                                        <input type="hidden" name="id" value="${pt.id}">
                                        <input type="hidden" name="isActive" value="${!pt.active}">
                                        <button type="submit" class="action-btn ${pt.active ? 'delete' : 'edit'}" title="${pt.active ? 'Ẩn' : 'Hiện'}">
                                            <i class='bx ${pt.active ? "bx-hide" : "bx-show"}'></i>
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </main>

    <!-- Add Modal -->
    <div class="modal-overlay" id="addModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Thêm loại thú cưng mới</h3>
                <button class="modal-close" onclick="document.getElementById('addModal').classList.remove('show')"><i class='bx bx-x'></i></button>
            </div>
            <form method="post">
                <input type="hidden" name="action" value="add">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Mã code <span class="required">*</span></label>
                        <input type="text" class="form-input" name="code" placeholder="VD: dog, cat, fish..." required>
                        <div class="input-hint">Chữ thường, không dấu, không khoảng trắng</div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Tên hiển thị <span class="required">*</span></label>
                        <input type="text" class="form-input" name="name" placeholder="VD: Chó, Mèo, Cá..." required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Icon (Boxicons class)</label>
                        <input type="text" class="form-input" name="icon" placeholder="VD: bx bxs-dog" value="bx bxs-dog">
                        <div class="input-hint">Xem icon tại <a href="https://boxicons.com" target="_blank">boxicons.com</a></div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Thứ tự hiển thị</label>
                        <input type="number" class="form-input" name="displayOrder" value="0" min="0">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Trạng thái</label>
                        <select class="form-input" name="isActive">
                            <option value="true">Hoạt động</option>
                            <option value="false">Ẩn</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="document.getElementById('addModal').classList.remove('show')">Hủy</button>
                    <button type="submit" class="btn btn-primary"><i class='bx bx-save'></i> Lưu</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Modal -->
    <div class="modal-overlay" id="editModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Chỉnh sửa loại thú cưng</h3>
                <button class="modal-close" onclick="document.getElementById('editModal').classList.remove('show')"><i class='bx bx-x'></i></button>
            </div>
            <form method="post">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="id" id="editId">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Tên hiển thị <span class="required">*</span></label>
                        <input type="text" class="form-input" name="name" id="editName" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Icon (Boxicons class)</label>
                        <input type="text" class="form-input" name="icon" id="editIcon">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Thứ tự hiển thị</label>
                        <input type="number" class="form-input" name="displayOrder" id="editOrder" min="0">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Trạng thái</label>
                        <select class="form-input" name="isActive" id="editActive">
                            <option value="true">Hoạt động</option>
                            <option value="false">Ẩn</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="document.getElementById('editModal').classList.remove('show')">Hủy</button>
                    <button type="submit" class="btn btn-primary"><i class='bx bx-save'></i> Lưu</button>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="/components/scripts.jsp" />
    <jsp:include page="/components/admin-toast.jsp" />
    <script>
        function openAddModal() { document.getElementById('addModal').classList.add('show'); }
        function openEditModal(row) {
            document.getElementById('editId').value = row.dataset.id;
            document.getElementById('editName').value = row.dataset.name;
            document.getElementById('editIcon').value = row.dataset.icon;
            document.getElementById('editOrder').value = row.dataset.order;
            document.getElementById('editActive').value = row.dataset.active;
            document.getElementById('editModal').classList.add('show');
        }
        document.querySelectorAll('.modal-overlay').forEach(o => {
            o.addEventListener('click', e => { if (e.target === o) o.classList.remove('show'); });
        });
    </script>
</body>
</html>
