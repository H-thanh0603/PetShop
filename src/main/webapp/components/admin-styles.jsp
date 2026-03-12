<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    ADMIN STYLES COMPONENT
    CSS chung cho tất cả trang admin (dashboard, appointments, products, blogs)
    Include: <jsp:include page="/components/admin-styles.jsp" />
--%>
<style>
    /* ===== BASE STYLES ===== */
    * {
        font-family: 'Nunito', 'Segoe UI', system-ui, -apple-system, sans-serif !important;
    }
    body {
        background: #f1f5f9;
        margin: 0;
        font-family: 'Nunito', 'Segoe UI', system-ui, -apple-system, sans-serif !important;
    }
    
    /* Main Content */
    .admin-main {
        margin-left: 250px !important;
        padding: 28px 32px;
        min-height: 100vh;
    }
    
    /* ===== PAGE HEADER ===== */
    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 28px;
    }
    .page-title {
        font-size: 1.5rem;
        font-weight: 700;
        color: #0f172a;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .page-title i {
        color: #3b82f6;
    }
    .admin-badge {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 18px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.9rem;
        color: #334155;
        box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .admin-badge i {
        color: #3b82f6;
        font-size: 1.2rem;
    }
    
    /* Admin User Dropdown */
    .admin-user-dropdown {
        position: relative;
    }
    .admin-badge {
        cursor: pointer;
        transition: all 0.2s;
    }
    .admin-badge:hover {
        background: #f1f5f9;
    }
    .admin-badge .dropdown-arrow {
        margin-left: 4px;
        transition: transform 0.2s;
    }
    .admin-user-dropdown.open .dropdown-arrow {
        transform: rotate(180deg);
    }
    .admin-dropdown-menu {
        position: absolute;
        top: calc(100% + 8px);
        right: 0;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        box-shadow: 0 10px 40px rgba(0,0,0,0.12);
        min-width: 200px;
        z-index: 1000;
        opacity: 0;
        visibility: hidden;
        transform: translateY(-10px);
        transition: all 0.2s;
    }
    .admin-user-dropdown.open .admin-dropdown-menu {
        opacity: 1;
        visibility: visible;
        transform: translateY(0);
    }
    .admin-dropdown-menu a {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 12px 16px;
        color: #334155;
        text-decoration: none;
        font-size: 0.9rem;
        transition: all 0.2s;
    }
    .admin-dropdown-menu a:first-child {
        border-radius: 10px 10px 0 0;
    }
    .admin-dropdown-menu a:last-child {
        border-radius: 0 0 10px 10px;
    }
    .admin-dropdown-menu a:hover {
        background: #f1f5f9;
    }
    .admin-dropdown-menu a i {
        font-size: 1.1rem;
        color: #64748b;
    }
    .admin-dropdown-menu a.logout-link {
        color: #dc2626;
        border-top: 1px solid #e2e8f0;
    }
    .admin-dropdown-menu a.logout-link i {
        color: #dc2626;
    }
    .admin-dropdown-menu a.logout-link:hover {
        background: #fef2f2;
    }
    
    /* ===== ALERT ===== */
    .alert {
        padding: 14px 18px;
        border-radius: 10px;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 10px;
        font-weight: 500;
    }
    .alert-success { 
        background: #dcfce7; 
        color: #15803d; 
        border: 1px solid #bbf7d0; 
    }
    .alert-error { 
        background: #fee2e2; 
        color: #b91c1c; 
        border: 1px solid #fecaca; 
    }
    .alert-close { 
        margin-left: auto; 
        background: none; 
        border: none; 
        cursor: pointer; 
        opacity: 0.7;
        padding: 4px;
        border-radius: 4px;
    }
    .alert-close:hover { opacity: 1; background: rgba(0,0,0,0.05); }
    
    /* ===== STATS GRID ===== */
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
    .stat-card.red { background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%); }
    .stat-card.cyan { background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%); }
    
    /* ===== CARD / SECTION ===== */
    .card, .content-section, .table-section {
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 14px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        overflow: hidden;
    }
    .card-header, .section-header, .table-header {
        padding: 18px 24px;
        border-bottom: 1px solid #e2e8f0;
        background: #f8fafc;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .card-header h5, .section-title, .table-title {
        margin: 0;
        font-size: 1rem;
        font-weight: 600;
        color: #0f172a;
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .card-header h5 i, .section-title i, .table-title i {
        color: #64748b;
    }
    .card-body {
        padding: 0;
    }
    
    /* ===== BUTTONS ===== */
    .btn {
        padding: 10px 20px;
        border-radius: 8px;
        font-size: 0.9rem;
        font-weight: 600;
        cursor: pointer;
        border: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        transition: all 0.2s;
    }
    .btn-primary {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
    }
    .btn-primary:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        transform: translateY(-1px);
    }
    .btn-success {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
    }
    .btn-success:hover {
        background: linear-gradient(135deg, #059669 0%, #047857 100%);
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
    }
    .btn-danger {
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
    }
    .btn-danger:hover {
        background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
    }
    .btn-secondary {
        background: white;
        color: #334155;
        border: 1px solid #e2e8f0;
        box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .btn-secondary:hover {
        background: #f8fafc;
        border-color: #cbd5e1;
    }
    
    /* Add Button (Thêm mới) */
    .btn-add {
        padding: 12px 24px;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        border: none;
        border-radius: 10px;
        font-size: 0.95rem;
        font-weight: 600;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        transition: all 0.2s;
    }
    .btn-add:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
        transform: translateY(-2px);
    }
    
    /* ===== VIEW TOGGLE ===== */
    .view-toggle {
        display: flex;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        overflow: hidden;
        box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .view-btn {
        padding: 10px 16px;
        background: white;
        border: none;
        cursor: pointer;
        color: #64748b;
        font-size: 1rem;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        gap: 6px;
    }
    .view-btn:not(:last-child) {
        border-right: 1px solid #e2e8f0;
    }
    .view-btn:hover {
        background: #f1f5f9;
        color: #334155;
    }
    .view-btn.active {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
    }
    
    /* ===== FILTER BAR ===== */
    .filter-bar, .filter-section {
        padding: 16px 20px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        margin-bottom: 20px;
        display: flex;
        gap: 12px;
        align-items: center;
        flex-wrap: wrap;
    }
    .search-box {
        flex: 1;
        min-width: 250px;
        position: relative;
    }
    .search-box input {
        width: 100%;
        padding: 12px 16px 12px 44px;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.9rem;
        transition: all 0.2s;
        background: #f8fafc;
    }
    .search-box input:focus {
        outline: none;
        border-color: #3b82f6;
        background: white;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    .search-box i {
        position: absolute;
        left: 16px;
        top: 50%;
        transform: translateY(-50%);
        color: #94a3b8;
        font-size: 1.1rem;
    }
    .filter-select {
        padding: 12px 16px;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.9rem;
        min-width: 180px;
        background: #f8fafc;
        cursor: pointer;
        transition: all 0.2s;
    }
    .filter-select:focus {
        outline: none;
        border-color: #3b82f6;
        background: white;
    }
    .btn-reset {
        padding: 10px 16px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        font-size: 0.85rem;
        color: #64748b;
        cursor: pointer;
        display: none;
        align-items: center;
        gap: 6px;
        transition: all 0.2s;
    }
    .btn-reset:hover {
        background: #fee2e2;
        border-color: #fecaca;
        color: #dc2626;
    }
    .btn-reset.show { display: flex; }
    
    /* ===== TABLE ===== */
    .data-table, .table {
        width: 100%;
        border-collapse: collapse;
        margin: 0;
    }
    .data-table th, .table th {
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
    .data-table td, .table td {
        padding: 16px 20px;
        border-bottom: 1px solid #f1f5f9;
        font-size: 0.9rem;
        color: #334155;
        vertical-align: middle;
    }
    .data-table tr:hover, .table tr:hover {
        background: #f8fafc;
    }
    
    /* Table Actions */
    .table-actions {
        display: flex;
        gap: 8px;
    }
    .action-btn {
        width: 36px;
        height: 36px;
        border: 1px solid #e2e8f0;
        background: white;
        border-radius: 8px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        font-size: 1.1rem;
        transition: all 0.2s;
    }
    .action-btn:hover {
        background: #f1f5f9;
    }
    .action-btn.view:hover {
        border-color: #3b82f6;
        color: #3b82f6;
        background: #eff6ff;
    }
    .action-btn.edit:hover {
        border-color: #f59e0b;
        color: #f59e0b;
        background: #fffbeb;
    }
    .action-btn.delete:hover {
        border-color: #ef4444;
        color: #ef4444;
        background: #fef2f2;
    }
    .action-btn.approve:hover {
        border-color: #10b981;
        color: #10b981;
        background: #ecfdf5;
    }
    
    /* ===== STATUS BADGES ===== */
    .status-badge {
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    .status-pending {
        background: #fef3c7;
        color: #b45309;
    }
    .status-confirmed {
        background: #d1fae5;
        color: #047857;
    }
    .status-completed {
        background: #e0e7ff;
        color: #4338ca;
    }
    .status-cancelled, .status-rejected {
        background: #fee2e2;
        color: #b91c1c;
    }
    
    /* ===== MODAL ===== */
    .modal-overlay {
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(15, 23, 42, 0.6);
        backdrop-filter: blur(4px);
        z-index: 2000;
        display: none;
        align-items: center;
        justify-content: center;
    }
    .modal-overlay.show { display: flex; }
    .modal-box {
        background: white;
        border-radius: 16px;
        width: 100%;
        max-width: 520px;
        max-height: 90vh;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        box-shadow: 0 25px 50px rgba(0,0,0,0.25);
        margin: 20px;
    }
    .modal-header {
        padding: 20px 24px;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-shrink: 0;
    }
    .modal-title {
        font-size: 1.15rem;
        font-weight: 700;
        margin: 0;
        color: #0f172a;
    }
    .modal-close {
        width: 36px; height: 36px;
        border: none;
        background: #f1f5f9;
        border-radius: 8px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        font-size: 1.2rem;
        transition: all 0.2s;
    }
    .modal-close:hover {
        background: #fee2e2;
        color: #ef4444;
    }
    .modal-body {
        padding: 24px;
        overflow-y: auto;
        flex: 1;
        max-height: calc(90vh - 160px);
    }
    .modal-footer {
        padding: 16px 24px;
        border-top: 1px solid #e2e8f0;
        display: flex;
        gap: 12px;
        justify-content: flex-end;
        flex-shrink: 0;
        background: #f8fafc;
    }
    
    /* ===== FORM ===== */
    .form-group {
        margin-bottom: 20px;
    }
    .form-label {
        display: block;
        font-size: 0.9rem;
        font-weight: 600;
        color: #334155;
        margin-bottom: 8px;
    }
    .form-label .required {
        color: #ef4444;
    }
    .form-input, .form-select, .form-textarea {
        width: 100%;
        padding: 12px 16px;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.95rem;
        transition: all 0.2s;
        background: #f8fafc;
    }
    .form-input:focus, .form-select:focus, .form-textarea:focus {
        outline: none;
        border-color: #3b82f6;
        background: white;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    .form-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 16px;
    }
    .input-hint {
        font-size: 0.8rem;
        color: #94a3b8;
        margin-top: 6px;
    }
    
    /* ===== EMPTY STATE ===== */
    .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #94a3b8;
    }
    .empty-state i {
        font-size: 4rem;
        margin-bottom: 16px;
        opacity: 0.5;
    }
    .empty-state p {
        font-size: 1rem;
        margin: 0;
    }
    
    /* ===== GRID VIEW (Blogs) ===== */
    .blog-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 24px;
        padding: 24px;
    }
    .blog-card {
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 14px;
        overflow: hidden;
        transition: all 0.3s;
        box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    }
    .blog-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0,0,0,0.1);
        border-color: #cbd5e1;
    }
    .blog-card-image {
        height: 180px;
        overflow: hidden;
    }
    .blog-card-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s;
    }
    .blog-card:hover .blog-card-image img {
        transform: scale(1.05);
    }
    .blog-card-body {
        padding: 20px;
    }
    .blog-card-category {
        display: inline-block;
        padding: 4px 12px;
        background: #eff6ff;
        color: #3b82f6;
        border-radius: 20px;
        font-size: 0.75rem;
        font-weight: 600;
        margin-bottom: 12px;
    }
    .blog-card-title {
        font-size: 1.05rem;
        font-weight: 600;
        color: #0f172a;
        margin: 0 0 10px 0;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
    .blog-card-meta {
        display: flex;
        align-items: center;
        gap: 16px;
        font-size: 0.85rem;
        color: #64748b;
        margin-bottom: 16px;
    }
    .blog-card-meta span {
        display: flex;
        align-items: center;
        gap: 6px;
    }
    .blog-card-actions {
        display: flex;
        gap: 8px;
        padding-top: 16px;
        border-top: 1px solid #f1f5f9;
    }
    
    /* ===== RESPONSIVE ===== */
    @media (max-width: 1200px) {
        .stats-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }
    @media (max-width: 768px) {
        .admin-main {
            margin-left: 0;
            padding: 20px;
        }
        .stats-grid {
            grid-template-columns: 1fr;
        }
        .filter-bar, .filter-section {
            flex-direction: column;
        }
        .search-box {
            width: 100%;
        }
    }
</style>

<style>
    /* ===== NEW ADMIN STYLES ===== */
    
    /* Page Header Admin */
    .page-header-admin {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 28px;
        flex-wrap: wrap;
        gap: 16px;
    }
    .page-header-admin .page-title {
        font-size: 1.6rem;
        font-weight: 700;
        color: #0f172a;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .page-header-admin .page-title i { color: #3b82f6; }
    .page-header-admin .page-subtitle {
        color: #64748b;
        font-size: 0.95rem;
        margin: 4px 0 0 0;
    }
    
    /* Button Primary Admin */
    .btn-primary-admin {
        padding: 12px 24px;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        border: none;
        border-radius: 10px;
        font-size: 0.95rem;
        font-weight: 600;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        transition: all 0.2s;
        text-decoration: none;
    }
    .btn-primary-admin:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
        transform: translateY(-2px);
        color: white;
    }
    
    /* Stat Card Admin */
    .stat-card-admin {
        background: white;
        border-radius: 16px;
        padding: 20px 24px;
        box-shadow: 0 4px 15px rgba(0,0,0,0.06);
        display: flex;
        align-items: center;
        gap: 18px;
        border: 1px solid #e2e8f0;
        transition: all 0.3s;
    }
    .stat-card-admin:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(0,0,0,0.1);
    }
    .stat-icon-admin {
        width: 56px;
        height: 56px;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        color: white;
    }
    .stat-icon-admin.bg-primary { background: linear-gradient(135deg, #3b82f6, #2563eb); }
    .stat-icon-admin.bg-success { background: linear-gradient(135deg, #10b981, #059669); }
    .stat-icon-admin.bg-warning { background: linear-gradient(135deg, #f59e0b, #d97706); }
    .stat-icon-admin.bg-danger { background: linear-gradient(135deg, #ef4444, #dc2626); }
    .stat-icon-admin.bg-info { background: linear-gradient(135deg, #06b6d4, #0891b2); }
    .stat-number-admin {
        font-size: 1.8rem;
        font-weight: 800;
        color: #0f172a;
        line-height: 1;
    }
    .stat-label-admin {
        font-size: 0.9rem;
        color: #64748b;
        margin-top: 4px;
    }
    
    /* Card Admin */
    .card-admin {
        background: white;
        border-radius: 16px;
        box-shadow: 0 4px 15px rgba(0,0,0,0.06);
        border: 1px solid #e2e8f0;
        overflow: hidden;
    }
    .card-header-admin {
        padding: 18px 24px;
        border-bottom: 1px solid #e2e8f0;
        background: #f8fafc;
    }
    .card-header-admin h5 {
        margin: 0;
        font-size: 1rem;
        font-weight: 600;
        color: #0f172a;
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .card-header-admin h5 i { color: #64748b; }
    .card-body-admin { padding: 0; }
    
    /* Table Admin */
    .table-admin {
        width: 100%;
        border-collapse: collapse;
    }
    .table-admin th {
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
    .table-admin td {
        padding: 16px 20px;
        border-bottom: 1px solid #f1f5f9;
        font-size: 0.9rem;
        color: #334155;
        vertical-align: middle;
    }
    .table-admin tr:hover { background: #f8fafc; }
    
    /* Avatar Admin */
    .avatar-admin {
        width: 44px;
        height: 44px;
        border-radius: 10px;
        object-fit: cover;
    }
    .avatar-placeholder {
        background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        font-size: 1.2rem;
    }
    
    /* Badge Admin */
    .badge-admin {
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    .badge-success { background: #d1fae5; color: #047857; }
    .badge-warning { background: #fef3c7; color: #b45309; }
    .badge-danger { background: #fee2e2; color: #b91c1c; }
    .badge-primary { background: #dbeafe; color: #1d4ed8; }
    .badge-secondary { background: #f1f5f9; color: #64748b; }
    
    /* Action Buttons */
    .action-buttons { display: flex; gap: 8px; }
    .btn-action-admin {
        width: 36px;
        height: 36px;
        border: 1px solid #e2e8f0;
        background: white;
        border-radius: 8px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        font-size: 1.1rem;
        transition: all 0.2s;
    }
    .btn-action-admin:hover { background: #f1f5f9; }
    .btn-action-admin.btn-edit:hover {
        border-color: #f59e0b;
        color: #f59e0b;
        background: #fffbeb;
    }
    .btn-action-admin.btn-delete:hover {
        border-color: #ef4444;
        color: #ef4444;
        background: #fef2f2;
    }
    .btn-action-admin.btn-warning {
        border-color: #f59e0b;
        color: #f59e0b;
        background: #fffbeb;
    }
    
    /* Nav Tabs */
    .nav-tabs {
        border-bottom: 2px solid #e2e8f0;
    }
    .nav-tabs .nav-link {
        border: none;
        color: #64748b;
        font-weight: 600;
        padding: 12px 20px;
        border-bottom: 2px solid transparent;
        margin-bottom: -2px;
    }
    .nav-tabs .nav-link:hover {
        border-color: transparent;
        color: #3b82f6;
    }
    .nav-tabs .nav-link.active {
        color: #3b82f6;
        border-bottom-color: #3b82f6;
        background: transparent;
    }
</style>
