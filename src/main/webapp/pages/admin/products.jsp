<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Sản phẩm - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        /* Stats Grid Override - Giống Dashboard */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 28px;
        }
        .stat-card {
            border-radius: 14px;
            padding: 24px;
            color: white;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            transition: transform 0.2s, box-shadow 0.2s;
            cursor: pointer;
        }
        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
        }
        .stat-card h3 {
            font-size: 2.2rem;
            margin: 0 0 8px 0;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .stat-card p {
            margin: 0;
            opacity: 0.9;
            font-size: 0.95rem;
        }
        .stat-card.blue { background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%); }
        .stat-card.green { background: linear-gradient(135deg, #059669 0%, #10b981 100%); }
        .stat-card.orange { background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); }
        .stat-card.purple { background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%); }
        
        /* Product Table Styles */
        .product-thumb {
            width: 70px;
            height: 70px;
            object-fit: cover;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08);
        }
        .product-name { 
            font-weight: 600; 
            color: #0f172a;
        }
        .price-current { 
            font-weight: 700; 
            color: #dc2626;
            font-size: 0.95rem;
        }
        .price-old { 
            text-decoration: line-through; 
            color: #94a3b8; 
            font-size: 0.85rem; 
        }
        .discount-badge {
            display: inline-block;
            padding: 5px 12px;
            background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
            color: #dc2626;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 700;
        }
        .no-discount { color: #94a3b8; }

        /* Image Upload Styles - Match blogs.jsp */
        .image-upload-wrapper {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .image-preview-area {
            width: 100%;
            height: 180px;
            border: 2px dashed #e2e8f0;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            background: #f8fafc;
            transition: all 0.3s ease;
            position: relative;
            cursor: pointer;
        }
        .image-preview-area:hover {
            border-color: #3b82f6;
            background: #eff6ff;
        }
        .image-preview-area.has-image {
            border-style: solid;
            border-color: #3b82f6;
        }
        .image-preview-area img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .preview-placeholder {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 8px;
            color: #94a3b8;
        }
        .preview-placeholder i {
            font-size: 2.5rem;
            color: #cbd5e1;
        }
        .preview-placeholder span {
            font-size: 0.9rem;
        }
        .image-upload-actions {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        .btn-upload {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 10px 18px;
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            color: white;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.2s;
            box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
        }
        .btn-upload:hover {
            background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
        }
        .btn-remove-image {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 10px 18px;
            background: white;
            color: #ef4444;
            border: 1px solid #fecaca;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.2s;
        }
        .btn-remove-image:hover {
            background: #fef2f2;
            border-color: #ef4444;
        }
        
        /* Price Input with VND format */
        .price-input-wrapper {
            position: relative;
        }
        .price-input-wrapper input {
            padding-right: 50px;
        }
        .price-suffix {
            position: absolute;
            right: 16px;
            top: 50%;
            transform: translateY(-50%);
            color: #64748b;
            font-weight: 600;
            font-size: 0.85rem;
        }
        .price-display {
            font-size: 0.8rem;
            color: #3b82f6;
            margin-top: 4px;
            font-weight: 500;
        }
    </style>
</head>
<body>

    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="products"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bx-package'></i> Quản lý Sản phẩm</h1>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <!-- Stats Cards - Giống Dashboard -->
        <div class="stats-grid">
            <div class="stat-card blue" onclick="filterByDiscount('')">
                <h3><i class='bx bx-package'></i> ${totalProducts}</h3>
                <p>Tổng sản phẩm</p>
            </div>
            <div class="stat-card orange" onclick="filterByDiscount('yes')">
                <h3><i class='bx bx-purchase-tag'></i> ${discountedProducts}</h3>
                <p>Đang giảm giá</p>
            </div>
            <div class="stat-card green" onclick="filterByDiscount('no')">
                <h3><i class='bx bx-check-circle'></i> ${totalProducts - discountedProducts}</h3>
                <p>Giá gốc</p>
            </div>
            <div class="stat-card purple">
                <h3><i class='bx bx-store'></i> ${totalProducts}</h3>
                <p>Đang bán</p>
            </div>
        </div>

        <!-- Filter Section -->
        <div class="filter-section">
            <div class="search-box">
                <i class='bx bx-search'></i>
                <input type="text" id="searchInput" placeholder="Tìm theo tên sản phẩm..." onkeyup="applyFilters()">
            </div>
            <select class="filter-select" id="filterDiscount" onchange="applyFilters()">
                <option value="">Tất cả</option>
                <option value="yes">Đang giảm giá</option>
                <option value="no">Không giảm giá</option>
            </select>
            <button class="btn-reset" id="resetBtn" onclick="resetFilters()">
                <i class='bx bx-x'></i> Xóa bộ lọc
            </button>
        </div>

        <!-- Table Section -->
        <div class="table-section">
            <div class="table-header">
                <span class="table-title">
                    <i class='bx bx-list-ul'></i> Danh sách sản phẩm
                    <span id="resultCount" style="font-weight: normal; color: #94a3b8; font-size: 0.85rem;"></span>
                </span>
                <button class="btn-add" onclick="openAddModal()">
                    <i class='bx bx-plus'></i> Thêm sản phẩm
                </button>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 50px;">#</th>
                        <th style="width: 90px;">Ảnh</th>
                        <th>Tên sản phẩm</th>
                        <th style="width: 200px;">Mô tả</th>
                        <th style="width: 130px;">Giá bán</th>
                        <th style="width: 90px;">Giảm giá</th>
                        <th style="width: 110px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody id="productsBody">
                    <c:if test="${empty products}">
                        <tr>
                            <td colspan="8">
                                <div class="empty-state">
                                    <i class='bx bx-package'></i>
                                    <p>Chưa có sản phẩm nào</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                    <c:forEach items="${products}" var="p" varStatus="loop">
                        <tr data-id="${p.id}" data-name="${p.name}" data-image="${p.image}" 
                            data-price="${p.price}" data-discount="${p.discount}" data-description="${p.description}">
                            <td><strong>${loop.index + 1}</strong></td>
                            <td>
                                <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${p.image}" 
                                     alt="" class="product-thumb"
                                     onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'" loading="lazy">
                            </td>
                            <td><span class="product-name">${p.name}</span></td>
                            <td>
                                <span class="product-desc" title="${p.description}">
                                    <c:choose>
                                        <c:when test="${not empty p.description}">
                                            ${p.description.length() > 50 ? p.description.substring(0, 50).concat('...') : p.description}
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #94a3b8; font-style: italic;">Chưa có mô tả</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td><span class="price-current">${p.formattedPrice}</span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.discount > 0}">
                                        <span class="discount-badge">-${p.discount}%</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="no-discount">-</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <button class="action-btn edit" onclick="openEditModal(this.closest('tr'))" title="Sửa">
                                        <i class='bx bx-edit-alt'></i>
                                    </button>
                                    <button class="action-btn delete" onclick="openDeleteModal(this.closest('tr'))" title="Xóa">
                                        <i class='bx bx-trash'></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </main>

    <!-- Add/Edit Modal -->
    <div class="modal-overlay" id="productModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title" id="modalTitle">Thêm sản phẩm mới</h3>
                <button class="modal-close" onclick="closeModal()"><i class='bx bx-x'></i></button>
            </div>
            
            <form id="productForm" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="formId">
                <input type="hidden" name="existingImage" id="formExistingImage">
                
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Tên sản phẩm <span class="required">*</span></label>
                        <input type="text" class="form-input" name="name" id="formName" required 
                               placeholder="Nhập tên sản phẩm...">
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">Ảnh sản phẩm</label>
                        <div class="image-upload-wrapper">
                            <div class="image-preview-area" id="imagePreviewArea">
                                <img src="" alt="Preview" id="previewImg" style="display: none;">
                                <div class="preview-placeholder" id="previewPlaceholder">
                                    <i class='bx bx-image-add'></i>
                                    <span>Chọn ảnh hoặc kéo thả vào đây</span>
                                </div>
                            </div>
                            <div class="image-upload-actions">
                                <label class="btn-upload" for="imageFile">
                                    <i class='bx bx-upload'></i> Chọn ảnh
                                </label>
                                <input type="file" id="imageFile" name="imageFile" accept="image/*" 
                                       onchange="handleFileSelect(event)" style="display: none;">
                                <button type="button" class="btn-remove-image" id="btnRemoveImage" 
                                        onclick="removeImage()" style="display: none;">
                                    <i class='bx bx-trash'></i> Xóa ảnh
                                </button>
                            </div>
                            <span class="input-hint">Chấp nhận: JPG, PNG, GIF, WebP. Tối đa 5MB</span>
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">Giá bán <span class="required">*</span></label>
                            <div class="price-input-wrapper">
                                <input type="text" class="form-input" name="priceDisplay" id="formPriceDisplay" required
                                       placeholder="VD: 150,000" oninput="formatPriceInput(this, 'formPrice')">
                                <span class="price-suffix">VNĐ</span>
                            </div>
                            <input type="hidden" name="price" id="formPrice">
                            <div class="price-display" id="pricePreview"></div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Giảm giá (%)</label>
                            <input type="number" class="form-input" name="discount" id="formDiscount"
                                   placeholder="VD: 10" min="0" max="100" value="0" step="1">
                            <div class="input-hint">Nhập phần trăm giảm giá (0-100)</div>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">Mô tả sản phẩm</label>
                        <textarea class="form-input" name="description" id="formDescription" rows="4"
                                  placeholder="Nhập mô tả chi tiết về sản phẩm..." style="resize: vertical;"></textarea>
                    </div>
                </div>
                
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeModal()">
                        <i class='bx bx-x'></i> Hủy bỏ
                    </button>
                    <button type="submit" class="btn btn-primary">
                        <i class='bx bx-save'></i> Lưu sản phẩm
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Delete Modal -->
    <div class="modal-overlay" id="deleteModal">
        <div class="modal-box" style="max-width: 420px;">
            <div class="modal-header">
                <h3 class="modal-title">Xóa sản phẩm?</h3>
                <button class="modal-close" onclick="closeDeleteModal()"><i class='bx bx-x'></i></button>
            </div>
            <div class="modal-body" style="text-align: center; padding: 30px;">
                <div style="width: 72px; height: 72px; border-radius: 50%; background: #fee2e2; color: #ef4444; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; font-size: 2rem;">
                    <i class='bx bx-trash'></i>
                </div>
                <p style="color: #64748b; font-size: 0.95rem; margin: 0;">Sản phẩm sẽ bị xóa vĩnh viễn và không thể khôi phục.</p>
            </div>
            <div class="modal-footer" style="justify-content: center;">
                <button type="button" class="btn btn-secondary" onclick="closeDeleteModal()">
                    <i class='bx bx-x'></i> Hủy bỏ
                </button>
                <form method="post" style="display: inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" id="deleteId">
                    <button type="submit" class="btn btn-danger">
                        <i class='bx bx-trash'></i> Xác nhận xóa
                    </button>
                </form>
            </div>
        </div>
    </div>

    <jsp:include page="/components/scripts.jsp" />
    <jsp:include page="/components/admin-toast.jsp" />

    <script>
        // ========== FORMAT PRICE VND ==========
        function formatVND(number) {
            return new Intl.NumberFormat('vi-VN').format(number);
        }
        
        function parseVND(str) {
            return parseInt(str.replace(/[^\d]/g, '')) || 0;
        }
        
        function formatPriceInput(input, hiddenId) {
            var value = input.value.replace(/[^\d]/g, '');
            var number = parseInt(value) || 0;
            
            if (value) {
                input.value = formatVND(number);
            }
            
            document.getElementById(hiddenId).value = number;
            
            // Update preview
            var previewId = hiddenId === 'formPrice' ? 'pricePreview' : 'oldPricePreview';
            if (number > 0) {
                document.getElementById(previewId).textContent = '= ' + formatVND(number) + ' đ';
            } else {
                document.getElementById(previewId).textContent = '';
            }
        }
        
        // ========== FILTER FUNCTIONS ==========
        document.addEventListener('DOMContentLoaded', function() {
            updateResultCount();
        });

        function updateResultCount() {
            var visible = document.querySelectorAll('#productsBody tr[data-id]:not([style*="display: none"])').length;
            var total = document.querySelectorAll('#productsBody tr[data-id]').length;
            document.getElementById('resultCount').textContent = '(' + visible + '/' + total + ')';
        }

        function applyFilters() {
            var search = document.getElementById('searchInput').value.toLowerCase();
            var discount = document.getElementById('filterDiscount').value;
            var rows = document.querySelectorAll('#productsBody tr[data-id]');
            var hasFilter = search || discount;
            
            rows.forEach(function(row) {
                var name = (row.dataset.name || '').toLowerCase();
                var hasDiscount = parseInt(row.dataset.discount) > 0;
                
                var matchSearch = !search || name.indexOf(search) > -1;
                var matchDiscount = !discount || 
                    (discount === 'yes' && hasDiscount) || 
                    (discount === 'no' && !hasDiscount);
                
                row.style.display = (matchSearch && matchDiscount) ? '' : 'none';
            });
            
            var resetBtn = document.getElementById('resetBtn');
            if (hasFilter) {
                resetBtn.classList.add('show');
            } else {
                resetBtn.classList.remove('show');
            }
            
            updateResultCount();
        }

        function filterByDiscount(value) {
            document.getElementById('filterDiscount').value = value;
            document.getElementById('searchInput').value = '';
            applyFilters();
        }

        function resetFilters() {
            document.getElementById('searchInput').value = '';
            document.getElementById('filterDiscount').value = '';
            applyFilters();
        }

        // ========== MODAL FUNCTIONS ==========
        function openAddModal() {
            document.getElementById('modalTitle').textContent = 'Thêm sản phẩm mới';
            document.getElementById('formAction').value = 'add';
            document.getElementById('formId').value = '';
            document.getElementById('formName').value = '';
            document.getElementById('formExistingImage').value = '';
            document.getElementById('formPriceDisplay').value = '';
            document.getElementById('formPrice').value = '';
            document.getElementById('formDiscount').value = '0';
            document.getElementById('formDescription').value = '';
            document.getElementById('pricePreview').textContent = '';
            resetImagePreview();
            document.getElementById('productModal').classList.add('show');
        }

        function openEditModal(row) {
            document.getElementById('modalTitle').textContent = 'Chỉnh sửa sản phẩm';
            document.getElementById('formAction').value = 'edit';
            document.getElementById('formId').value = row.dataset.id;
            document.getElementById('formName').value = row.dataset.name || '';
            document.getElementById('formExistingImage').value = row.dataset.image || '';
            document.getElementById('formDescription').value = row.dataset.description || '';
            
            // Format price for display
            var price = parseInt(row.dataset.price) || 0;
            
            document.getElementById('formPriceDisplay').value = price > 0 ? formatVND(price) : '';
            document.getElementById('formPrice').value = price;
            document.getElementById('pricePreview').textContent = price > 0 ? '= ' + formatVND(price) + ' đ' : '';
            
            document.getElementById('formDiscount').value = row.dataset.discount || '0';
            
            // Show existing image
            var existingImage = row.dataset.image;
            if (existingImage) {
                var imgUrl = '${pageContext.request.contextPath}/assets/images/shop_pic/' + existingImage;
                showImagePreview(imgUrl);
            } else {
                resetImagePreview();
            }
            
            document.getElementById('productModal').classList.add('show');
        }

        function closeModal() {
            document.getElementById('productModal').classList.remove('show');
        }

        function openDeleteModal(row) {
            document.getElementById('deleteId').value = row.dataset.id;
            document.getElementById('deleteModal').classList.add('show');
        }

        function closeDeleteModal() {
            document.getElementById('deleteModal').classList.remove('show');
        }

        document.querySelectorAll('.modal-overlay').forEach(function(overlay) {
            overlay.addEventListener('click', function(e) {
                if (e.target === overlay) overlay.classList.remove('show');
            });
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
                closeDeleteModal();
            }
        });

        // ========== IMAGE UPLOAD FUNCTIONS (Servlet 3.0 File Upload) ==========
        var previewArea = document.getElementById('imagePreviewArea');
        var previewPlaceholder = document.getElementById('previewPlaceholder');
        var previewImg = document.getElementById('previewImg');
        var btnRemoveImage = document.getElementById('btnRemoveImage');

        // Click to upload
        previewArea.addEventListener('click', function() {
            document.getElementById('imageFile').click();
        });

        // Drag and Drop
        previewArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            previewArea.style.borderColor = '#3b82f6';
            previewArea.style.background = '#eff6ff';
        });

        previewArea.addEventListener('dragleave', function(e) {
            e.preventDefault();
            if (!previewArea.classList.contains('has-image')) {
                previewArea.style.borderColor = '#e2e8f0';
                previewArea.style.background = '#f8fafc';
            }
        });

        previewArea.addEventListener('drop', function(e) {
            e.preventDefault();
            previewArea.style.borderColor = '#e2e8f0';
            previewArea.style.background = '#f8fafc';
            var files = e.dataTransfer.files;
            if (files.length > 0 && files[0].type.startsWith('image/')) {
                // Set file to input
                var dataTransfer = new DataTransfer();
                dataTransfer.items.add(files[0]);
                document.getElementById('imageFile').files = dataTransfer.files;
                previewFile(files[0]);
            }
        });

        // File Select
        function handleFileSelect(event) {
            var file = event.target.files[0];
            if (file && file.type.startsWith('image/')) {
                previewFile(file);
            }
        }

        // Preview file (chỉ hiển thị, không convert Base64)
        function previewFile(file) {
            if (file.size > 5 * 1024 * 1024) {
                alert('File quá lớn! Vui lòng chọn ảnh dưới 5MB.');
                document.getElementById('imageFile').value = '';
                return;
            }

            var reader = new FileReader();
            reader.onload = function(e) {
                showImagePreview(e.target.result);
                // Clear existing image khi chọn file mới
                document.getElementById('formExistingImage').value = '';
            };
            reader.readAsDataURL(file);
        }

        // Show image preview
        function showImagePreview(src) {
            previewImg.src = src;
            previewImg.style.display = 'block';
            previewPlaceholder.style.display = 'none';
            btnRemoveImage.style.display = 'inline-flex';
            previewArea.classList.add('has-image');
        }

        // Reset image preview
        function resetImagePreview() {
            previewImg.src = '';
            previewImg.style.display = 'none';
            previewPlaceholder.style.display = 'flex';
            btnRemoveImage.style.display = 'none';
            previewArea.classList.remove('has-image');
            document.getElementById('imageFile').value = '';
        }

        // Remove Image
        function removeImage() {
            resetImagePreview();
            document.getElementById('formExistingImage').value = '';
        }
    </script>
</body>
</html>
