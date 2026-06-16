<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Kho - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
            gap: 20px;
            margin-bottom: 28px;
        }
        .stat-card {
            border-radius: 14px;
            padding: 24px;
            color: white;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            transition: transform 0.2s;
        }
        .stat-card h3 { font-size: 2.2rem; margin: 0 0 8px 0; font-weight: 700; display: flex; align-items: center; gap: 10px; }
        .stat-card p { margin: 0; opacity: 0.9; font-size: 0.95rem; }
        .stat-card.blue { background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%); }
        .stat-card.orange { background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); }
        .stat-card.red { background: linear-gradient(135deg, #b91c1c 0%, #ef4444 100%); }
        .stat-card.yellow { background: linear-gradient(135deg, #ca8a04 0%, #f59e0b 100%); }

        .product-thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 8px; }
        .status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 700; }
        .status-low { background: #fff7ed; color: #9a3412; }
        .status-out { background: #fef2f2; color: #991b1b; }
        .status-ok { background: #f0fdf4; color: #166534; }
        
        .expiry-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 700; }
        .expiry-expired { background: #fee2e2; color: #991b1b; }
        .expiry-near { background: #fef3c7; color: #92400e; }
        .expiry-healthy { background: #dcfce7; color: #166534; }
        .expiry-none { background: #f1f5f9; color: #475569; }

        .batch-table { font-size: 0.85rem; }
        .inventory-container { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
    </style>
</head>
<body class="admin-page">
    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="inventory" />
    </jsp:include>

    <main class="admin-main">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h4 class="fw-bold mb-1">Quản lý Kho hàng</h4>
                <p class="text-muted small mb-0">Theo dõi tồn kho, quản lý lô hàng và hạn sử dụng</p>
            </div>
            <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addBatchModal">
                <i class='bx bx-plus-circle'></i> Nhập lô hàng mới
            </button>
        </div>

        <div class="stats-grid">
            <div class="stat-card blue">
                <h3><i class='bx bx-package'></i> ${fn:length(products)}</h3>
                <p>Tổng mặt hàng</p>
            </div>
            <div class="stat-card orange">
                <h3><i class='bx bx-error-circle'></i> ${lowStockCount}</h3>
                <p>Sản phẩm sắp hết</p>
            </div>
            <div class="stat-card yellow">
                <h3><i class='bx bx-time-five'></i> ${nearExpiryCount}</h3>
                <p>Lô hàng sắp hết hạn</p>
            </div>
            <div class="stat-card red">
                <h3><i class='bx bx-alarm-exclamation'></i> ${expiredCount}</h3>
                <p>Lô hàng đã hết hạn</p>
            </div>
        </div>

        <c:if test="${not empty sessionScope.message}">
            <div class="alert alert-${sessionScope.messageType == 'success' ? 'success' : 'danger'} alert-dismissible fade show" role="alert">
                ${sessionScope.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <% session.removeAttribute("message"); session.removeAttribute("messageType"); %>
        </c:if>

        <div class="inventory-container">
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Tồn kho</th>
                            <th>Trạng thái lô</th>
                            <th>HSD sớm nhất</th>
                            <th class="text-end">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${products}">
                            <c:set var="inv" value="${inventoryByProduct[p.id]}" />
                            <tr>
                                <td>
                                    <div class="d-flex align-items-center gap-3">
                                        <img src="${pageContext.request.contextPath}/${p.image}" class="product-thumb" alt="${p.name}">
                                        <div>
                                            <div class="fw-bold">${p.name}</div>
                                            <div class="text-muted small">ID: ${p.id}</div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <span class="fw-bold">${p.stock}</span>
                                    <c:choose>
                                        <c:when test="${p.stock == 0}"><span class="status-badge status-out ms-2">Hết hàng</span></c:when>
                                        <c:when test="${p.stock < 10}"><span class="status-badge status-low ms-2">Sắp hết</span></c:when>
                                        <c:otherwise><span class="status-badge status-ok ms-2">Tốt</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty inv}">
                                            <span class="expiry-badge expiry-none">Chưa có lô</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="expiry-badge expiry-${inv.expiryStatus}">
                                                ${inv.expiryStatusLabel}
                                            </span>
                                            <div class="small text-muted mt-1">${inv.activeBatchCount} lô đang hoạt động</div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty inv and not empty inv.earliestExpiryDate}">
                                            <div class="fw-medium">${inv.formattedEarliestExpiryDate}</div>
                                            <div class="small ${inv.daysUntilEarliestExpiry <= 7 ? 'text-danger' : 'text-muted'}">
                                                (Còn ${inv.daysUntilEarliestExpiry} ngày)
                                            </div>
                                        </c:when>
                                        <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openAddBatch(${p.id}, '${fn:escapeXml(p.name)}')">
                                        <i class='bx bx-plus'></i> Nhập hàng
                                    </button>
                                    <a href="${pageContext.request.contextPath}/admin/inventory?productId=${p.id}" class="btn btn-sm btn-outline-info">
                                        <i class='bx bx-list-ul'></i> Chi tiết lô
                                    </a>
                                </td>
                            </tr>
                            
                            <!-- Bảng chi tiết lô hàng (hiển thị nếu được chọn) -->
                            <c:if test="${selectedProductId == p.id}">
                                <tr class="table-info">
                                    <td colspan="5">
                                        <div class="p-3 bg-white rounded shadow-sm">
                                            <div class="d-flex justify-content-between align-items-center mb-2">
                                                <h6 class="mb-0 fw-bold text-primary">Danh sách lô hàng: ${p.name}</h6>
                                                <a href="${pageContext.request.contextPath}/admin/inventory" class="btn-close btn-sm"></a>
                                            </div>
                                            <table class="table table-sm batch-table mb-0">
                                                <thead>
                                                    <tr>
                                                        <th>Mã lô</th>
                                                        <th>Ngày nhập</th>
                                                        <th>Số lượng nhập</th>
                                                        <th>Còn lại</th>
                                                        <th>Giá nhập</th>
                                                        <th>Hạn sử dụng</th>
                                                        <th>Ghi chú</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="batch" items="${selectedProductBatches}">
                                                        <tr>
                                                            <td><code>${batch.batchCode}</code></td>
                                                            <td><fmt:formatDate value="${batch.receivedAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                                            <td>${batch.receivedQuantity}</td>
                                                            <td class="fw-bold">${batch.remainingQuantity}</td>
                                                            <td><fmt:formatNumber value="${batch.unitCost}" type="currency" currencySymbol="₫" maxFractionDigits="0" /></td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${not empty batch.expiryDate}">
                                                                        <fmt:formatDate value="${batch.expiryDate}" pattern="dd/MM/yyyy" />
                                                                    </c:when>
                                                                    <c:otherwise><span class="text-muted">Không có</span></c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td>${batch.note}</td>
                                                        </tr>
                                                    </c:forEach>
                                                    <c:if test="${empty selectedProductBatches}">
                                                        <tr><td colspan="7" class="text-center py-3 text-muted">Không có lô hàng khả dụng</td></tr>
                                                    </c:if>
                                                </tbody>
                                            </table>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <!-- Modal Nhập lô hàng mới -->
    <div class="modal fade" id="addBatchModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/admin/inventory" method="post">
                    <input type="hidden" name="action" value="addBatch">
                    <div class="modal-header">
                        <h5 class="modal-title">Nhập lô hàng mới</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Sản phẩm</label>
                            <select name="productId" id="modalProductId" class="form-select" required>
                                <option value="">Chọn sản phẩm...</option>
                                <c:forEach var="p" items="${products}">
                                    <option value="${p.id}">${p.name} (ID: ${p.id})</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Số lượng nhập</label>
                                <input type="number" name="quantity" class="form-control" min="1" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Giá nhập (mỗi SP)</label>
                                <input type="number" name="unitCost" class="form-control" min="0" step="1000" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Mã lô hàng (tùy chọn)</label>
                            <input type="text" name="batchCode" class="form-control" placeholder="Ví dụ: LOHANG-2026-001">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Hạn sử dụng</label>
                            <input type="date" name="expiryDate" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Ghi chú</label>
                            <textarea name="note" class="form-control" rows="2"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary">Xác nhận nhập kho</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function openAddBatch(productId, productName) {
            document.getElementById('modalProductId').value = productId;
            const modalElement = document.getElementById('addBatchModal');
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        }
    </script>
</body>
</html>
