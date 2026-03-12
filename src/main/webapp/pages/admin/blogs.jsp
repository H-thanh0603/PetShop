<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Bài viết - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        /* Content Section Override */
        .content-section {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        }
        .section-header {
            padding: 18px 24px;
            border-bottom: 1px solid #e2e8f0;
            background: #f8fafc;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .section-title { 
            font-weight: 600; 
            color: #0f172a; 
            display: flex; 
            align-items: center; 
            gap: 10px;
            font-size: 1rem;
        }
        .section-title i {
            color: #64748b;
        }
        
        /* Filter Bar Override */
        .filter-bar {
            padding: 16px 24px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            background: white;
        }
    </style>
</head>
<body>

    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="blogs"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bxs-news'></i> Quản lý Bài viết</h1>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <!-- Stats Cards - Giống Dashboard -->
        <div class="stats-grid">
            <div class="stat-card blue" onclick="filterByCategory('')">
                <h3><i class='bx bx-file'></i> ${totalBlogs}</h3>
                <p>Tổng bài viết</p>
            </div>
            <div class="stat-card green" onclick="filterByCategory('Sức khỏe')">
                <h3><i class='bx bx-plus-medical'></i> <span id="countHealth">0</span></h3>
                <p>Sức khỏe</p>
            </div>
            <div class="stat-card purple" onclick="filterByCategory('Chăm sóc')">
                <h3><i class='bx bx-heart'></i> <span id="countCare">0</span></h3>
                <p>Chăm sóc</p>
            </div>
            <div class="stat-card orange" onclick="filterByCategory('Dinh dưỡng')">
                <h3><i class='bx bx-bowl-rice'></i> <span id="countNutrition">0</span></h3>
                <p>Dinh dưỡng</p>
            </div>
        </div>

        <!-- Content Section -->
        <div class="content-section">
            <div class="section-header">
                <span class="section-title">
                    <i class='bx bx-list-ul'></i> Danh sách bài viết
                    <span id="resultCount" style="font-weight: normal; color: var(--text-muted); font-size: 0.85rem;">(${totalBlogs})</span>
                </span>
                <button class="btn-add" onclick="openAddModal()">
                    <i class='bx bx-plus'></i> Thêm bài viết
                </button>
            </div>
            
            <!-- Filter Bar -->
            <div class="filter-bar">
                <div class="search-box">
                    <i class='bx bx-search'></i>
                    <input type="text" id="searchInput" placeholder="Tìm theo tiêu đề, tác giả..." onkeyup="applyFilters()">
                </div>
                <select class="filter-select" id="filterCategory" onchange="applyFilters()">
                    <option value="">Tất cả danh mục</option>
                    <option value="Sức khỏe">Sức khỏe</option>
                    <option value="Dinh dưỡng">Dinh dưỡng</option>
                    <option value="Chăm sóc">Chăm sóc</option>
                    <option value="Huấn luyện">Huấn luyện</option>
                    <option value="Tâm lý thú cưng">Tâm lý thú cưng</option>
                    <option value="Review Sản phẩm">Review Sản phẩm</option>
                    <option value="Chuyện bên lề">Chuyện bên lề</option>
                    <option value="Cấp cứu">Cấp cứu</option>
                    <option value="Góc nhìn">Góc nhìn</option>
                </select>
                <button class="btn-reset" id="resetBtn" onclick="resetFilters()">
                    <i class='bx bx-reset'></i> Xóa bộ lọc
                </button>
                <div class="view-toggle">
                    <button class="view-btn active" onclick="setView('grid')" title="Dạng lưới">
                        <i class='bx bx-grid-alt'></i>
                    </button>
                    <button class="view-btn" onclick="setView('table')" title="Dạng bảng">
                        <i class='bx bx-list-ul'></i>
                    </button>
                </div>
            </div>
            
            <!-- Blog Grid View -->
            <div class="blog-grid" id="blogGrid">
                <c:if test="${empty blogs}">
                    <div class="empty-state" style="grid-column: 1/-1;">
                        <i class='bx bx-news'></i>
                        <p>Chưa có bài viết nào</p>
                        <button class="btn-add" onclick="openAddModal()" style="margin-top: 12px;">
                            <i class='bx bx-plus'></i> Thêm bài viết đầu tiên
                        </button>
                    </div>
                </c:if>
                
                <c:forEach items="${blogs}" var="blog">
                    <div class="blog-card" data-id="${blog.id}" data-title="${blog.title}" 
                         data-category="${blog.category}" data-author="${blog.author}"
                         data-image="${blog.image}" data-summary="${blog.summary}" data-date="${blog.date}">
                        <div class="blog-image-wrapper">
                                    <img src="${pageContext.request.contextPath}/assets/images/community_pic/${blog.image}" 
                                 alt="${blog.title}" class="blog-image" loading="lazy"
                                 onerror="this.src='https://placehold.co/600x400/e2e8f0/1e293b?text=PetShop'">
                            <span class="blog-category">${blog.category}</span>
                        </div>
                        <div class="blog-content">
                            <h3 class="blog-title">${blog.title}</h3>
                            <p class="blog-summary">${blog.summary}</p>
                            <div class="blog-meta">
                                <div class="blog-author">
                                    <i class='bx bx-user-circle'></i>
                                    <span>${blog.author}</span>
                                </div>
                                <span class="blog-date"><i class='bx bx-calendar'></i> ${blog.date}</span>
                            </div>
                        </div>
                        <div class="blog-actions">
                            <button class="action-btn" onclick="openEditModal(this.closest('.blog-card').dataset.id)" title="Chỉnh sửa">
                                <i class='bx bx-edit'></i>
                            </button>
                            <button class="action-btn danger" onclick="confirmDeleteFromCard(this.closest('.blog-card'))" title="Xóa">
                                <i class='bx bx-trash'></i>
                            </button>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Blog Table View (Hidden by default) -->
            <div class="blog-table-wrapper" id="blogTable" style="display: none;">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th style="width: 50px;">#</th>
                            <th style="width: 80px;">Ảnh</th>
                            <th>Tiêu đề</th>
                            <th style="width: 120px;">Danh mục</th>
                            <th style="width: 140px;">Tác giả</th>
                            <th style="width: 110px;">Ngày đăng</th>
                            <th style="width: 100px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="blogTableBody">
                        <c:forEach items="${blogs}" var="blog" varStatus="loop">
                            <tr data-id="${blog.id}" data-title="${blog.title}" 
                                data-category="${blog.category}" data-author="${blog.author}">
                                <td><strong>${loop.index + 1}</strong></td>
                                <td>
                                    <img src="${pageContext.request.contextPath}/assets/images/community_pic/${blog.image}" 
                                         alt="" class="table-thumb" loading="lazy"
                                         onerror="this.src='https://placehold.co/600x400/e2e8f0/1e293b?text=PetShop'">
                                </td>
                                <td>
                                    <div class="table-title">${blog.title}</div>
                                    <div class="table-summary">${blog.summary}</div>
                                </td>
                                <td><span class="category-badge">${blog.category}</span></td>
                                <td>${blog.author}</td>
                                <td>${blog.date}</td>
                                <td>
                                    <div class="table-actions">
                                        <button class="action-btn-sm" onclick="openEditModal(this.closest('tr').dataset.id)" title="Sửa">
                                            <i class='bx bx-edit'></i>
                                        </button>
                                        <button class="action-btn-sm danger" onclick="confirmDeleteFromRow(this.closest('tr'))" title="Xóa">
                                            <i class='bx bx-trash'></i>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</body>
</html>

<!-- Add/Edit Modal -->
<div class="modal-overlay" id="blogModal">
    <div class="modal-box">
        <div class="modal-header">
            <span class="modal-title" id="modalTitle">Thêm bài viết mới</span>
            <button class="modal-close" onclick="closeModal()"><i class='bx bx-x'></i></button>
        </div>
        <form id="blogForm" method="post" enctype="multipart/form-data">
            <input type="hidden" name="action" id="formAction" value="add">
            <input type="hidden" name="id" id="formId">
            <input type="hidden" name="date" id="formDate">
            <input type="hidden" name="existingImage" id="formExistingImage">
            
            <div class="modal-body">
                <div class="form-group">
                    <label class="form-label">Tiêu đề bài viết <span class="required">*</span></label>
                    <input type="text" class="form-input" name="title" id="formTitle" required 
                           placeholder="Nhập tiêu đề bài viết...">
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">Danh mục <span class="required">*</span></label>
                        <div class="select-wrapper">
                            <select class="form-select" name="category" id="formCategory" required>
                                <option value="">Chọn danh mục</option>
                                <option value="Sức khỏe">Sức khỏe</option>
                                <option value="Dinh dưỡng">Dinh dưỡng</option>
                                <option value="Chăm sóc">Chăm sóc</option>
                                <option value="Huấn luyện">Huấn luyện</option>
                                <option value="Tâm lý thú cưng">Tâm lý thú cưng</option>
                                <option value="Review Sản phẩm">Review Sản phẩm</option>
                                <option value="Chuyện bên lề">Chuyện bên lề</option>
                                <option value="Cấp cứu">Cấp cứu</option>
                                <option value="Góc nhìn">Góc nhìn</option>
                            </select>
                            <i class='bx bx-chevron-down select-icon'></i>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Tác giả <span class="required">*</span></label>
                        <input type="text" class="form-input" name="author" id="formAuthor" required
                               placeholder="Tên tác giả">
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="form-label">Ảnh bìa</label>
                    <div class="image-upload-wrapper">
                        <div class="image-preview" id="imagePreview">
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
                            <input type="file" name="imageFile" id="imageFile" accept="image/*" 
                                   onchange="previewImage(this)" style="display: none;">
                            <button type="button" class="btn-remove-image" id="btnRemoveImage" 
                                    onclick="removeImage()" style="display: none;">
                                <i class='bx bx-trash'></i> Xóa ảnh
                            </button>
                        </div>
                        <span class="input-hint">Chấp nhận: JPG, PNG, GIF, WebP. Tối đa 10MB</span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="form-label">Tóm tắt nội dung <span class="required">*</span></label>
                    <textarea class="form-textarea" name="summary" id="formSummary" required
                              placeholder="Nhập tóm tắt nội dung bài viết..." rows="4"></textarea>
                </div>
            </div>
            
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">
                    <i class='bx bx-x'></i> Hủy bỏ
                </button>
                <button type="submit" class="btn btn-primary" id="submitBtn">
                    <i class='bx bx-save'></i> Lưu bài viết
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Delete Confirm Modal -->
<div class="modal-overlay" id="deleteModal">
    <div class="modal-box modal-sm">
        <div class="modal-header" style="border: none; padding-bottom: 0;">
            <span></span>
            <button class="modal-close" onclick="closeDeleteModal()"><i class='bx bx-x'></i></button>
        </div>
        <div class="modal-confirm-content">
            <div class="modal-icon danger">
                <i class='bx bx-trash'></i>
            </div>
            <h3 class="modal-confirm-title">Xóa bài viết?</h3>
            <p class="modal-confirm-desc" id="deleteMessage">Bài viết sẽ bị xóa vĩnh viễn.</p>
        </div>
        <div class="modal-footer" style="justify-content: center;">
            <button class="btn btn-secondary" onclick="closeDeleteModal()">Hủy</button>
            <form method="post" style="display: inline;">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" id="deleteId">
                <button type="submit" class="btn btn-danger"><i class='bx bx-trash'></i> Xóa</button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/components/scripts.jsp" />
<jsp:include page="/components/admin-toast.jsp" />

<style>
    /* Blog Grid - Modern Design with Performance Optimization */
    .blog-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 24px;
        padding: 24px;
        background: #f8fafc;
    }
    .blog-card {
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 14px;
        overflow: hidden;
        cursor: pointer;
        display: flex;
        flex-direction: column;
        box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        /* Performance: chỉ animate transform và box-shadow */
        transition: transform 0.15s ease-out, box-shadow 0.15s ease-out, border-color 0.15s ease-out;
        will-change: transform;
        /* Lazy render khi scroll */
        content-visibility: auto;
        contain-intrinsic-size: 0 420px;
    }
    .blog-card:hover { 
        box-shadow: 0 8px 24px rgba(0,0,0,0.1); 
        transform: translateY(-4px);
        border-color: #3b82f6;
    }
    .blog-image-wrapper {
        position: relative;
        height: 200px;
        overflow: hidden;
    }
    .blog-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        /* Giảm animation để tăng performance */
        transition: transform 0.2s ease-out;
        will-change: transform;
    }
    .blog-card:hover .blog-image { transform: scale(1.03); }
    .blog-category {
        position: absolute;
        top: 14px;
        left: 14px;
        padding: 6px 14px;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        border-radius: 20px;
        font-size: 0.75rem;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
    }
    .blog-content { padding: 20px; flex: 1; }
    .blog-title {
        font-size: 1.05rem;
        font-weight: 700;
        color: #0f172a;
        margin: 0 0 10px 0;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
    .blog-summary {
        font-size: 0.9rem;
        color: #64748b;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin: 0 0 14px 0;
    }
    .blog-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 0.85rem;
        color: #94a3b8;
    }
    .blog-author { display: flex; align-items: center; gap: 6px; }
    .blog-author i { font-size: 1.1rem; color: #3b82f6; }
    .blog-date { display: flex; align-items: center; gap: 4px; }
    .blog-actions {
        display: flex;
        gap: 10px;
        padding: 16px 20px;
        border-top: 1px solid #f1f5f9;
        justify-content: flex-end;
        background: #f8fafc;
    }
    .action-btn {
        width: 38px; height: 38px;
        border: 1px solid #e2e8f0;
        background: white;
        border-radius: 10px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        font-size: 1.1rem;
        transition: background 0.15s, color 0.15s, border-color 0.15s;
    }
    .action-btn:hover { 
        background: #eff6ff;
        color: #3b82f6; 
        border-color: #3b82f6; 
    }
    .action-btn.danger:hover { 
        background: #fef2f2;
        color: #ef4444;
        border-color: #ef4444; 
    }

    /* Table View - Modern Design */
    .blog-table-wrapper { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; }
    .data-table th {
        text-align: left;
        padding: 14px 20px;
        font-size: 0.8rem;
        font-weight: 600;
        color: #64748b;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        background: #f8fafc;
        border-bottom: 1px solid #e2e8f0;
    }
    .data-table td {
        padding: 16px 20px;
        border-bottom: 1px solid #f1f5f9;
        font-size: 0.9rem;
        color: #334155;
        vertical-align: middle;
    }
    .data-table tr:hover { background: #f8fafc; }
    .table-thumb {
        width: 70px;
        height: 50px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }
    .table-title {
        font-weight: 600;
        color: #0f172a;
        margin-bottom: 4px;
    }
    .table-summary {
        font-size: 0.85rem;
        color: #94a3b8;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
    .category-badge {
        display: inline-block;
        padding: 5px 12px;
        background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
        color: #2563eb;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 600;
    }
    .table-actions { display: flex; gap: 8px; }
    .action-btn-sm {
        width: 34px; height: 34px;
        border: 1px solid #e2e8f0;
        background: white;
        border-radius: 8px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        transition: all 0.2s;
    }
    .action-btn-sm:hover { 
        background: #eff6ff; 
        color: #3b82f6;
        border-color: #3b82f6;
    }
    .action-btn-sm.danger:hover { 
        background: #fef2f2;
        color: #ef4444;
        border-color: #ef4444;
    }

    /* Modal - Modern Design */
    .modal-overlay {
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(15, 23, 42, 0.6);
        z-index: 2000;
        display: none;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(4px);
    }
    .modal-overlay.show { display: flex; }
    .modal-box {
        background: white;
        border-radius: 20px;
        width: 100%;
        max-width: 560px;
        max-height: 90vh;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        box-shadow: 0 25px 60px rgba(0,0,0,0.25);
        margin: 20px;
    }
    .modal-box.modal-sm { max-width: 420px; }
    .modal-header {
        padding: 24px 28px;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: #f8fafc;
    }
    .modal-title { 
        font-size: 1.2rem; 
        font-weight: 700; 
        color: #0f172a;
        margin: 0;
    }
    .modal-close {
        width: 38px; height: 38px;
        border: none;
        background: white;
        border-radius: 10px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        font-size: 1.3rem;
        transition: all 0.2s;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
    .modal-close:hover { background: #fee2e2; color: #ef4444; }
    .modal-body { padding: 28px; overflow-y: auto; flex: 1; }
    .modal-footer {
        padding: 20px 28px;
        border-top: 1px solid #e2e8f0;
        display: flex;
        gap: 12px;
        justify-content: flex-end;
        background: #f8fafc;
    }
    .modal-confirm-content { text-align: center; padding: 0 28px 28px; }
    .modal-icon {
        width: 72px; height: 72px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
        font-size: 2rem;
    }
    .modal-icon.danger { background: #fee2e2; color: #ef4444; }
    .modal-confirm-title { 
        margin: 0 0 10px 0; 
        color: #0f172a;
        font-size: 1.2rem;
        font-weight: 700;
    }
    .modal-confirm-desc { color: #64748b; font-size: 0.95rem; margin: 0; }
    
    /* Form - Modern Design */
    .form-group { margin-bottom: 22px; }
    .form-label {
        display: block;
        font-size: 0.9rem;
        font-weight: 600;
        color: #334155;
        margin-bottom: 8px;
    }
    .form-label .required { color: #ef4444; }
    .form-input, .form-select, .form-textarea {
        width: 100%;
        padding: 14px 16px;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.95rem;
        transition: all 0.2s;
        box-sizing: border-box;
        background: #f8fafc;
    }
    .form-input:focus, .form-select:focus, .form-textarea:focus {
        outline: none;
        border-color: #3b82f6;
        background: white;
        box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
    }
    .form-textarea { resize: vertical; min-height: 120px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .input-hint { font-size: 0.8rem; color: #94a3b8; margin-top: 6px; display: block; }
    
    /* Buttons - Modern Design */
    .btn {
        padding: 12px 24px;
        border-radius: 10px;
        font-size: 0.95rem;
        cursor: pointer;
        border: none;
        transition: all 0.2s;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    .btn-secondary { 
        background: white; 
        color: #334155;
        border: 1px solid #e2e8f0;
        box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .btn-secondary:hover { background: #f1f5f9; border-color: #cbd5e1; }
    .btn-primary { 
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); 
        color: white;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }
    .btn-primary:hover { 
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
        transform: translateY(-1px);
    }
    .btn-danger { 
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); 
        color: white;
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
    }
    .btn-danger:hover { 
        background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
        box-shadow: 0 6px 20px rgba(239, 68, 68, 0.4);
    }
    
    /* Alert - Modern Design */
    .alert {
        padding: 16px 20px;
        border-radius: 12px;
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        gap: 12px;
        font-weight: 500;
    }
    .alert-success { 
        background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%); 
        color: #15803d; 
        border: 1px solid #86efac; 
    }
    .alert-error { 
        background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); 
        color: #b91c1c; 
        border: 1px solid #fca5a5; 
    }
    .alert-close { 
        margin-left: auto; 
        background: none; 
        border: none; 
        cursor: pointer; 
        opacity: 0.7;
        padding: 6px;
        border-radius: 6px;
    }
    .alert-close:hover { opacity: 1; background: rgba(0,0,0,0.05); }
    
    /* Empty State - Modern Design */
    .empty-state {
        text-align: center;
        padding: 80px 20px;
        color: #94a3b8;
    }
    .empty-state i { font-size: 4rem; margin-bottom: 20px; opacity: 0.4; }
    .empty-state p { margin: 0 0 20px 0; font-size: 1.05rem; }

    /* Select Wrapper with Icon */
    .select-wrapper {
        position: relative;
    }
    .select-wrapper .form-select {
        appearance: none;
        -webkit-appearance: none;
        -moz-appearance: none;
        padding-right: 40px;
    }
    .select-wrapper .select-icon {
        position: absolute;
        right: 14px;
        top: 50%;
        transform: translateY(-50%);
        color: #64748b;
        font-size: 1.2rem;
        pointer-events: none;
        transition: transform 0.2s;
    }
    .select-wrapper .form-select:focus + .select-icon {
        color: #3b82f6;
    }

    /* Image Upload Styles */
    .image-upload-wrapper {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }
    .image-preview {
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
    }
    .image-preview:hover {
        border-color: #3b82f6;
        background: #eff6ff;
    }
    .image-preview.has-image {
        border-style: solid;
        border-color: #3b82f6;
    }
    .image-preview img {
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
</style>

<script>
    var currentView = 'grid';
    
    // Initialize
    document.addEventListener('DOMContentLoaded', function() {
        updateCategoryCounts();
        updateResultCount();
    });
    
    // Update category counts
    function updateCategoryCounts() {
        var cards = document.querySelectorAll('.blog-card');
        var healthCount = 0, careCount = 0, nutritionCount = 0;
        
        cards.forEach(function(card) {
            var cat = card.dataset.category;
            if (cat === 'Sức khỏe') healthCount++;
            else if (cat === 'Chăm sóc') careCount++;
            else if (cat === 'Dinh dưỡng') nutritionCount++;
        });
        
        document.getElementById('countHealth').textContent = healthCount;
        document.getElementById('countCare').textContent = careCount;
        document.getElementById('countNutrition').textContent = nutritionCount;
    }
    
    // Filter by category from stats card
    function filterByCategory(category) {
        document.querySelectorAll('.stat-card').forEach(function(c) { c.classList.remove('active'); });
        if (category) {
            event.currentTarget.classList.add('active');
        }
        document.getElementById('filterCategory').value = category;
        applyFilters();
    }
    
    // Apply filters
    function applyFilters() {
        var search = document.getElementById('searchInput').value.toLowerCase();
        var category = document.getElementById('filterCategory').value;
        
        var visibleCount = 0;
        
        // Filter grid view
        document.querySelectorAll('.blog-card').forEach(function(card) {
            var title = card.dataset.title.toLowerCase();
            var author = card.dataset.author.toLowerCase();
            var cardCategory = card.dataset.category;
            
            var matchSearch = !search || title.indexOf(search) !== -1 || author.indexOf(search) !== -1;
            var matchCategory = !category || cardCategory === category;
            
            var show = matchSearch && matchCategory;
            card.style.display = show ? '' : 'none';
            if (show) visibleCount++;
        });
        
        // Filter table view
        document.querySelectorAll('#blogTableBody tr').forEach(function(row) {
            var title = row.dataset.title.toLowerCase();
            var author = row.dataset.author.toLowerCase();
            var rowCategory = row.dataset.category;
            
            var matchSearch = !search || title.indexOf(search) !== -1 || author.indexOf(search) !== -1;
            var matchCategory = !category || rowCategory === category;
            
            row.style.display = (matchSearch && matchCategory) ? '' : 'none';
        });
        
        updateResultCount(visibleCount);
        updateResetButton();
    }
    
    // Reset filters
    function resetFilters() {
        document.getElementById('searchInput').value = '';
        document.getElementById('filterCategory').value = '';
        document.querySelectorAll('.stat-card').forEach(function(c) { c.classList.remove('active'); });
        applyFilters();
    }
    
    // Update result count
    function updateResultCount(count) {
        var total = document.querySelectorAll('.blog-card').length;
        count = count !== undefined ? count : total;
        var text = count < total ? '(' + count + '/' + total + ')' : '(' + total + ')';
        document.getElementById('resultCount').textContent = text;
    }
    
    // Update reset button visibility
    function updateResetButton() {
        var hasFilters = document.getElementById('searchInput').value || 
                          document.getElementById('filterCategory').value;
        var btn = document.getElementById('resetBtn');
        btn.classList.toggle('show', hasFilters);
    }
    
    // Toggle view
    function setView(view) {
        currentView = view;
        document.querySelectorAll('.view-btn').forEach(function(b) { b.classList.remove('active'); });
        event.currentTarget.classList.add('active');
        
        document.getElementById('blogGrid').style.display = view === 'grid' ? 'grid' : 'none';
        document.getElementById('blogTable').style.display = view === 'table' ? 'block' : 'none';
    }
    
    // Open Add Modal
    function openAddModal() {
        document.getElementById('modalTitle').textContent = 'Thêm bài viết mới';
        document.getElementById('formAction').value = 'add';
        document.getElementById('formId').value = '';
        document.getElementById('formDate').value = '';
        document.getElementById('formTitle').value = '';
        document.getElementById('formCategory').value = '';
        document.getElementById('formAuthor').value = '';
        document.getElementById('formExistingImage').value = '';
        document.getElementById('formSummary').value = '';
        document.getElementById('submitBtn').innerHTML = '<i class="bx bx-save"></i> Lưu bài viết';
        
        // Reset image preview
        resetImagePreview();
        
        document.getElementById('blogModal').classList.add('show');
    }
    
    // Open Edit Modal
    function openEditModal(id) {
        var card = document.querySelector('.blog-card[data-id="' + id + '"]');
        if (!card) return;
        
        document.getElementById('modalTitle').textContent = 'Chỉnh sửa bài viết';
        document.getElementById('formAction').value = 'edit';
        document.getElementById('formId').value = id;
        document.getElementById('formDate').value = card.dataset.date;
        document.getElementById('formTitle').value = card.dataset.title;
        document.getElementById('formCategory').value = card.dataset.category;
        document.getElementById('formAuthor').value = card.dataset.author;
        document.getElementById('formExistingImage').value = card.dataset.image;
        document.getElementById('formSummary').value = card.dataset.summary;
        document.getElementById('submitBtn').innerHTML = '<i class="bx bx-save"></i> Cập nhật';
        
        // Show existing image preview
        var existingImage = card.dataset.image;
        if (existingImage) {
            var imgUrl = '${pageContext.request.contextPath}/assets/images/community_pic/' + existingImage;
            showImagePreview(imgUrl);
        } else {
            resetImagePreview();
        }
        
        document.getElementById('blogModal').classList.add('show');
    }
    
    // Preview image when file selected
    function previewImage(input) {
        if (input.files && input.files[0]) {
            var file = input.files[0];
            
            // Validate file size (10MB)
            if (file.size > 10 * 1024 * 1024) {
                alert('File quá lớn! Vui lòng chọn file nhỏ hơn 10MB.');
                input.value = '';
                return;
            }
            
            // Validate file type
            if (!file.type.match(/^image\/(jpeg|png|gif|webp)$/)) {
                alert('Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WebP)!');
                input.value = '';
                return;
            }
            
            var reader = new FileReader();
            reader.onload = function(e) {
                showImagePreview(e.target.result);
            };
            reader.readAsDataURL(file);
        }
    }
    
    // Show image preview
    function showImagePreview(src) {
        var previewImg = document.getElementById('previewImg');
        var placeholder = document.getElementById('previewPlaceholder');
        var removeBtn = document.getElementById('btnRemoveImage');
        var previewWrapper = document.getElementById('imagePreview');
        
        previewImg.src = src;
        previewImg.style.display = 'block';
        placeholder.style.display = 'none';
        removeBtn.style.display = 'inline-flex';
        previewWrapper.classList.add('has-image');
    }
    
    // Reset image preview
    function resetImagePreview() {
        var previewImg = document.getElementById('previewImg');
        var placeholder = document.getElementById('previewPlaceholder');
        var removeBtn = document.getElementById('btnRemoveImage');
        var previewWrapper = document.getElementById('imagePreview');
        var fileInput = document.getElementById('imageFile');
        
        previewImg.src = '';
        previewImg.style.display = 'none';
        placeholder.style.display = 'flex';
        removeBtn.style.display = 'none';
        previewWrapper.classList.remove('has-image');
        fileInput.value = '';
    }
    
    // Remove image
    function removeImage() {
        resetImagePreview();
        document.getElementById('formExistingImage').value = '';
    }
    
    // Drag and drop support
    document.addEventListener('DOMContentLoaded', function() {
        var previewArea = document.getElementById('imagePreview');
        if (previewArea) {
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
                if (files.length > 0) {
                    var fileInput = document.getElementById('imageFile');
                    fileInput.files = files;
                    previewImage(fileInput);
                }
            });
            
            // Click to upload
            previewArea.addEventListener('click', function() {
                document.getElementById('imageFile').click();
            });
        }
    });
    
    // Close Modal
    function closeModal() {
        document.getElementById('blogModal').classList.remove('show');
    }
    
    // Confirm Delete from card
    function confirmDeleteFromCard(card) {
        var id = card.dataset.id;
        var title = card.dataset.title;
        confirmDelete(id, title);
    }
    
    // Confirm Delete from table row
    function confirmDeleteFromRow(row) {
        var id = row.dataset.id;
        var title = row.dataset.title;
        confirmDelete(id, title);
    }
    
    // Confirm Delete
    function confirmDelete(id, title) {
        document.getElementById('deleteId').value = id;
        document.getElementById('deleteMessage').textContent = 'Bài viết "' + title + '" sẽ bị xóa vĩnh viễn.';
        document.getElementById('deleteModal').classList.add('show');
    }
    
    // Close Delete Modal
    function closeDeleteModal() {
        document.getElementById('deleteModal').classList.remove('show');
    }
    
    // Close on Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
            closeDeleteModal();
        }
    });
    
    // Close modal on overlay click
    document.querySelectorAll('.modal-overlay').forEach(function(overlay) {
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) {
                closeModal();
                closeDeleteModal();
            }
        });
    });
</script>


