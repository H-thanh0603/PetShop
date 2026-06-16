<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý khuyến mãi</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .panel-card { background: #fff; border-radius: 18px; padding: 24px; box-shadow: 0 8px 30px rgba(15,23,42,.06); margin-bottom: 24px; }
        .badge-soft { display: inline-flex; align-items: center; gap: 6px; border-radius: 999px; padding: 6px 12px; font-weight: 700; font-size: .78rem; }
        .badge-active { background: #dcfce7; color: #166534; }
        .badge-inactive { background: #e2e8f0; color: #475569; }
        .badge-flash { background: #fff1f2; color: #be123c; }
        .badge-normal { background: #eff6ff; color: #1d4ed8; }
        .product-picker { max-height: 240px; overflow: auto; border: 1px solid #e2e8f0; border-radius: 14px; padding: 12px; background: #f8fafc; }
        .product-picker label { display: flex; gap: 10px; padding: 8px 10px; border-radius: 10px; margin-bottom: 6px; cursor: pointer; }
        .product-picker label:hover { background: #e2f5f2; }
        .page-subtitle { color: #64748b; margin-top: 6px; }
        .table td, .table th { vertical-align: middle; }
        /* Action buttons in row: icon vừa phải, dễ bấm */
        .promo-action-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 38px;
            height: 38px;
            padding: 0;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            background: #fff;
            color: #475569;
            transition: all .2s;
        }
        .promo-action-btn i { font-size: 1.25rem; line-height: 1; }
        .promo-action-btn.edit:hover { color: #2563eb; border-color: #bfdbfe; background: #eff6ff; }
        .promo-action-btn.toggle:hover { color: #0f172a; border-color: #cbd5e1; background: #f1f5f9; }
        .promo-action-btn.toggle.is-active { color: #16a34a; border-color: #bbf7d0; background: #f0fdf4; }
        .promo-action-btn.toggle.is-active:hover { color: #166534; border-color: #86efac; background: #dcfce7; }
        .promo-action-btn.delete:hover { color: #dc2626; border-color: #fecaca; background: #fef2f2; }
        .promo-actions-cell { white-space: nowrap; }
    </style>
</head>
<body>
<jsp:include page="/components/admin-sidebar.jsp">
    <jsp:param name="currentPage" value="promotions"/>
</jsp:include>

<main class="admin-main">
    <div class="page-header">
        <div>
            <h1 class="page-title"><i class='bx bxs-bolt-circle'></i> Quản lý khuyến mãi</h1>
            <div class="page-subtitle">Quản lý khuyến mãi thường và Flash Sale cho sản phẩm đang bán.</div>
        </div>
    </div>

    <jsp:include page="/components/admin-toast.jsp" />

    <div class="row">
        <div class="col-lg-7">
            <div class="panel-card">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="mb-0 fw-bold">Danh sách khuyến mãi</h5>
                    <span class="text-muted">${fn:length(promotions)} chương trình</span>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Tên</th>
                            <th>Loại</th>
                            <th>Giảm giá</th>
                            <th>Thời gian</th>
                            <th>Trạng thái</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${promotions}" var="promotion">
                            <tr>
                                <td>
                                    <div class="fw-bold">${fn:escapeXml(promotion.name)}</div>
                                    <div class="small text-muted">${fn:escapeXml(promotion.description)}</div>
                                </td>
                                <td>
                                    <span class="badge-soft ${promotion.promotionType == 'FLASH_SALE' ? 'badge-flash' : 'badge-normal'}">
                                        ${promotion.promotionType == 'FLASH_SALE' ? 'Flash Sale' : 'Khuyến mãi thường'}
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${promotion.discountType == 'PERCENT'}">
                                            ${promotion.discountValue}% 
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${promotion.discountValue}" pattern="#,###"/>đ
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${promotion.promotionType == 'FLASH_SALE' && promotion.saleQuantity != null}">
                                        <div class="small text-muted">Đã bán ${promotion.soldQuantity}/${promotion.saleQuantity}</div>
                                    </c:if>
                                </td>
                                <td class="small">
                                    <div><fmt:formatDate value="${promotion.startDate}" pattern="dd/MM/yyyy HH:mm"/></div>
                                    <div class="text-muted">đến <fmt:formatDate value="${promotion.endDate}" pattern="dd/MM/yyyy HH:mm"/></div>
                                </td>
                                <td>
                                    <span class="badge-soft ${promotion.status == 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">
                                        ${promotion.status == 'ACTIVE' ? 'Đang bật' : 'Đang tắt'}
                                    </span>
                                </td>
                                <td class="text-end promo-actions-cell">
                                    <div class="d-flex gap-2 justify-content-end">
                                        <a href="${pageContext.request.contextPath}/admin/promotions?id=${promotion.id}" class="promo-action-btn edit" title="Sửa">
                                            <i class='bx bx-edit-alt'></i>
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/promotions" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="action" value="toggle">
                                            <input type="hidden" name="id" value="${promotion.id}">
                                            <input type="hidden" name="currentStatus" value="${promotion.status}">
                                            <button type="submit" class="promo-action-btn toggle ${promotion.status == 'ACTIVE' ? 'is-active' : ''}" title="${promotion.status == 'ACTIVE' ? 'Tắt khuyến mãi' : 'Bật khuyến mãi'}">
                                                <i class='bx ${promotion.status == 'ACTIVE' ? 'bx-toggle-right' : 'bx-toggle-left'}'></i>
                                            </button>
                                        </form>
                                        <button type="button" class="promo-action-btn delete"
                                                onclick="openDeletePromotionModal(${promotion.id}, '${fn:escapeXml(promotion.name)}')"
                                                title="Xóa">
                                            <i class='bx bx-trash'></i>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty promotions}">
                            <tr><td colspan="6" class="text-center text-muted py-4">Chưa có khuyến mãi nào.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="panel-card">
                <h5 class="fw-bold mb-3">${editingPromotion != null ? 'Cập nhật khuyến mãi' : 'Thêm khuyến mãi mới'}</h5>
                <form action="${pageContext.request.contextPath}/admin/promotions" method="post" id="promotionForm">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <c:if test="${editingPromotion != null}">
                        <input type="hidden" name="id" value="${editingPromotion.id}">
                    </c:if>

                    <div class="mb-3">
                        <label class="form-label">Tên khuyến mãi</label>
                        <input type="text" name="name" class="form-control" required maxlength="120"
                               placeholder="VD: Sale chào hè"
                               value="${editingPromotion != null ? fn:escapeXml(editingPromotion.name) : ''}">
                    </div>

                    <div class="row g-3">
                        <div class="col-12">
                            <label class="form-label">Giảm giá</label>
                            <div class="input-group">
                                <input type="number" min="1" step="1" name="discountValue" class="form-control" required
                                       placeholder="VD: 20"
                                       value="${editingPromotion != null ? editingPromotion.discountValue : ''}">
                                <select name="discountType" class="form-select" style="max-width: 110px;">
                                    <option value="PERCENT" ${editingPromotion == null || editingPromotion.discountType == 'PERCENT' ? 'selected' : ''}>%</option>
                                    <option value="FIXED" ${editingPromotion != null && editingPromotion.discountType == 'FIXED' ? 'selected' : ''}>đ</option>
                                </select>
                            </div>
                            <div class="form-text">% giảm phải ≤ 100. Nếu chọn <b>đ</b> thì là số tiền giảm cố định.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Bắt đầu</label>
                            <input type="datetime-local" name="startDate" class="form-control" required
                                   value="<fmt:formatDate value='${editingPromotion.startDate}' pattern='yyyy-MM-dd\'T\'HH:mm'/>">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Kết thúc</label>
                            <input type="datetime-local" name="endDate" class="form-control" required
                                   value="<fmt:formatDate value='${editingPromotion.endDate}' pattern='yyyy-MM-dd\'T\'HH:mm'/>">
                        </div>

                        <div class="col-12">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" id="flashSaleToggle" name="flashSale" value="1"
                                       <c:if test="${editingPromotion != null && editingPromotion.promotionType == 'FLASH_SALE'}">checked</c:if>
                                       onchange="toggleFlashQuantity()">
                                <label class="form-check-label fw-semibold" for="flashSaleToggle">
                                    <i class='bx bxs-bolt-circle text-danger'></i> Đây là Flash Sale (giới hạn số lượng)
                                </label>
                            </div>
                        </div>

                        <div class="col-12 d-none" id="flashQuantityBox">
                            <label class="form-label">Số lượng Flash Sale cho mỗi sản phẩm</label>
                            <input type="number" min="1" step="1" name="saleQuantity" class="form-control"
                                   placeholder="VD: 50"
                                   value="${editingPromotion != null && editingPromotion.saleQuantity != null ? editingPromotion.saleQuantity : ''}">
                        </div>
                    </div>

                    <div class="mt-3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <label class="form-label mb-0">Sản phẩm áp dụng</label>
                            <div class="d-flex gap-2 small">
                                <a href="javascript:void(0)" onclick="toggleAllProducts(true)">Chọn tất cả</a>
                                <span class="text-muted">|</span>
                                <a href="javascript:void(0)" onclick="toggleAllProducts(false)">Bỏ chọn</a>
                            </div>
                        </div>
                        <input type="text" class="form-control form-control-sm mb-2" placeholder="Tìm sản phẩm…" oninput="filterProducts(this.value)">
                        <div class="product-picker" id="productPicker">
                            <c:forEach items="${products}" var="product">
                                <label data-product-name="${fn:escapeXml(fn:toLowerCase(product.name))}">
                                    <input type="checkbox" name="productIds" value="${product.id}"
                                           <c:if test="${editingPromotion != null && editingPromotion.productIds.contains(product.id)}">checked</c:if>>
                                    <span>
                                        <strong>${fn:escapeXml(product.name)}</strong>
                                        <span class="text-muted d-block small"><fmt:formatNumber value="${product.price}" pattern="#,###"/>đ</span>
                                    </span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="d-flex gap-2 mt-4">
                        <button type="submit" class="btn btn-primary">${editingPromotion != null ? 'Lưu thay đổi' : 'Thêm khuyến mãi'}</button>
                        <a href="${pageContext.request.contextPath}/admin/promotions" class="btn btn-outline-secondary">Làm mới</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</main>

<!-- Modal xác nhận xóa khuyến mãi -->
<div class="modal-overlay" id="deletePromotionModal">
    <div class="modal-box" style="max-width: 440px;">
        <div class="modal-header">
            <h3 class="modal-title">Xóa khuyến mãi?</h3>
            <button type="button" class="modal-close" onclick="closeDeletePromotionModal()"><i class='bx bx-x'></i></button>
        </div>
        <div class="modal-body" style="text-align: center; padding: 28px;">
            <div style="width: 72px; height: 72px; border-radius: 50%; background: #fee2e2; color: #ef4444; display: flex; align-items: center; justify-content: center; margin: 0 auto 18px; font-size: 2rem;">
                <i class='bx bx-trash'></i>
            </div>
            <p class="mb-2" style="color:#0f172a; font-weight:600;">Bạn sắp xóa khuyến mãi:</p>
            <p class="mb-3" style="color:#be123c; font-weight:700;" id="deletePromotionName">—</p>
            <p style="color: #64748b; font-size: 0.92rem; margin: 0;">
                Hành động này không thể hoàn tác. Chỉ những khuyến mãi <b>chưa được dùng trong đơn hàng nào</b> mới có thể xóa.
            </p>
        </div>
        <div class="modal-footer" style="justify-content: center; gap: 10px;">
            <button type="button" class="btn btn-secondary" onclick="closeDeletePromotionModal()">
                <i class='bx bx-x'></i> Hủy bỏ
            </button>
            <form action="${pageContext.request.contextPath}/admin/promotions" method="post" style="display:inline;">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" id="deletePromotionId">
                <button type="submit" class="btn btn-danger">
                    <i class='bx bx-trash'></i> Xác nhận xóa
                </button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/components/scripts.jsp" />
<script>
    function toggleFlashQuantity() {
        var toggle = document.getElementById('flashSaleToggle');
        var box = document.getElementById('flashQuantityBox');
        if (!toggle || !box) {
            return;
        }
        if (toggle.checked) {
            box.classList.remove('d-none');
        } else {
            box.classList.add('d-none');
            var qty = box.querySelector('input[name="saleQuantity"]');
            if (qty) qty.value = '';
        }
    }

    function toggleAllProducts(check) {
        document.querySelectorAll('#productPicker input[type="checkbox"]').forEach(function (cb) {
            var label = cb.closest('label');
            if (label && label.style.display === 'none') return;
            cb.checked = check;
        });
    }

    function filterProducts(keyword) {
        var kw = (keyword || '').toLowerCase().trim();
        document.querySelectorAll('#productPicker label').forEach(function (label) {
            var name = label.getAttribute('data-product-name') || '';
            label.style.display = (!kw || name.indexOf(kw) >= 0) ? '' : 'none';
        });
    }

    // Tự điền ngày mặc định khi tạo mới: bắt đầu = bây giờ, kết thúc = +7 ngày
    (function fillDefaultDates() {
        var startInput = document.querySelector('input[name="startDate"]');
        var endInput = document.querySelector('input[name="endDate"]');
        if (!startInput || !endInput) return;
        if (!startInput.value) {
            var now = new Date();
            now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
            startInput.value = now.toISOString().slice(0, 16);
        }
        if (!endInput.value) {
            var end = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
            end.setMinutes(end.getMinutes() - end.getTimezoneOffset());
            endInput.value = end.toISOString().slice(0, 16);
        }
    })();

    toggleFlashQuantity();

    // Modal xóa khuyến mãi
    function openDeletePromotionModal(promotionId, promotionName) {
        document.getElementById('deletePromotionId').value = promotionId;
        document.getElementById('deletePromotionName').textContent = promotionName || '';
        document.getElementById('deletePromotionModal').classList.add('show');
    }
    function closeDeletePromotionModal() {
        document.getElementById('deletePromotionModal').classList.remove('show');
    }
    // Bấm ra ngoài hoặc Esc để đóng modal
    document.getElementById('deletePromotionModal').addEventListener('click', function (e) {
        if (e.target === this) closeDeletePromotionModal();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeDeletePromotionModal();
    });
</script>
</body>
</html>
