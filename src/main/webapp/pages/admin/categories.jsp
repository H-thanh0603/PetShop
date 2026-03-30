<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Danh Mục - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
</head>
<body>
    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="categories"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bx-category'></i> Quản lý Danh Mục Sản Phẩm</h1>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:12px;padding:16px 20px;margin-bottom:24px;color:#1e40af;font-size:0.9rem;">
            <i class='bx bx-info-circle'></i>
            Danh mục được tự động tạo từ trường <strong>category</strong> của sản phẩm. Bạn có thể đổi tên hoặc gán loại thú cưng cho từng danh mục.
        </div>

        <div class="table-section">
            <div class="table-header">
                <span class="table-title"><i class='bx bx-list-ul'></i> Danh sách danh mục (<strong>${categories.size()}</strong>)</span>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Tên danh mục</th>
                        <th>Số sản phẩm</th>
                        <th>Loại thú cưng</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty categories}">
                        <tr><td colspan="5"><div class="empty-state"><i class='bx bx-category'></i><p>Chưa có danh mục nào</p></div></td></tr>
                    </c:if>
                    <c:forEach items="${categories}" var="cat" varStatus="s">
                        <tr>
                            <td><strong>${s.index + 1}</strong></td>
                            <td><strong>${cat[0]}</strong></td>
                            <td><span style="background:#dbeafe;color:#1d4ed8;padding:3px 10px;border-radius:20px;font-size:0.85rem;font-weight:600;">${cat[1]} sản phẩm</span></td>
                            <td>
                                <span style="background:#f0fdf4;color:#16a34a;padding:3px 10px;border-radius:20px;font-size:0.85rem;">${cat[2]}</span>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <button class="action-btn edit" onclick="openRenameModal('${cat[0]}')" title="Đổi tên"><i class='bx bx-edit-alt'></i></button>
                                    <button class="action-btn" style="background:#f0fdf4;color:#16a34a;" onclick="openAssignModal('${cat[0]}')" title="Gán loại thú cưng"><i class='bx bxs-dog'></i></button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </main>

    <!-- Rename Modal -->
    <div class="modal-overlay" id="renameModal">
        <div class="modal-box" style="max-width:450px;">
            <div class="modal-header">
                <h3 class="modal-title">Đổi tên danh mục</h3>
                <button class="modal-close" onclick="document.getElementById('renameModal').classList.remove('show')"><i class='bx bx-x'></i></button>
            </div>
            <form method="post">
                <input type="hidden" name="action" value="rename">
                <input type="hidden" name="oldName" id="renameOldName">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Tên hiện tại</label>
                        <input type="text" class="form-input" id="renameOldDisplay" disabled style="background:#f8fafc;">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Tên mới <span class="required">*</span></label>
                        <input type="text" class="form-input" name="newName" id="renameNewName" required placeholder="Nhập tên danh mục mới...">
                        <div class="input-hint">Tất cả sản phẩm thuộc danh mục này sẽ được cập nhật</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="document.getElementById('renameModal').classList.remove('show')">Hủy</button>
                    <button type="submit" class="btn btn-primary"><i class='bx bx-save'></i> Lưu</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Assign Pet Type Modal -->
    <div class="modal-overlay" id="assignModal">
        <div class="modal-box" style="max-width:450px;">
            <div class="modal-header">
                <h3 class="modal-title">Gán loại thú cưng</h3>
                <button class="modal-close" onclick="document.getElementById('assignModal').classList.remove('show')"><i class='bx bx-x'></i></button>
            </div>
            <form method="post">
                <input type="hidden" name="action" value="assign-pet-type">
                <input type="hidden" name="category" id="assignCategory">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Danh mục</label>
                        <input type="text" class="form-input" id="assignCategoryDisplay" disabled style="background:#f8fafc;">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Loại thú cưng <span class="required">*</span></label>
                        <select class="form-input" name="petTypeId" required>
                            <option value="">-- Chọn loại thú cưng --</option>
                            <c:forEach items="${petTypes}" var="pt">
                                <option value="${pt.id}">${pt.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="document.getElementById('assignModal').classList.remove('show')">Hủy</button>
                    <button type="submit" class="btn btn-primary"><i class='bx bx-save'></i> Gán</button>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="/components/scripts.jsp" />
    <jsp:include page="/components/admin-toast.jsp" />
    <script>
        function openRenameModal(name) {
            document.getElementById('renameOldName').value = name;
            document.getElementById('renameOldDisplay').value = name;
            document.getElementById('renameNewName').value = name;
            document.getElementById('renameModal').classList.add('show');
        }
        function openAssignModal(name) {
            document.getElementById('assignCategory').value = name;
            document.getElementById('assignCategoryDisplay').value = name;
            document.getElementById('assignModal').classList.add('show');
        }
        document.querySelectorAll('.modal-overlay').forEach(o => {
            o.addEventListener('click', e => { if (e.target === o) o.classList.remove('show'); });
        });
    </script>
</body>
</html>
