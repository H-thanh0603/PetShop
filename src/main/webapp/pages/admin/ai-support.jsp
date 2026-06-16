<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>AI Support Center - PetShop Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .ai-support-container { display: flex; flex-direction: column; gap: 24px; }
        
        /* Modern tabs selector */
        .tab-menu { display: flex; gap: 10px; border-bottom: 2px solid #e2e8f0; padding-bottom: 2px; margin-bottom: 15px; }
        .tab-btn { background: none; border: none; padding: 12px 20px; font-size: 0.95rem; font-weight: 600; color: #64748b; cursor: pointer; border-bottom: 2px solid transparent; transition: all 0.2s; display: flex; align-items: center; gap: 8px; }
        .tab-btn:hover { color: #0b1a33; }
        .tab-btn.active { color: #00bfa5; border-bottom-color: #00bfa5; }
        
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        
        /* Dashboard styling */
        .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 20px; }
        .stat-card { padding: 24px; border-radius: 16px; color: white; position: relative; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .stat-card.teal { background: linear-gradient(135deg, #00bfa5 0%, #00796b 100%); }
        .stat-card.blue { background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%); }
        .stat-card.orange { background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%); }
        .stat-card h3 { font-size: 2.2rem; font-weight: 800; margin: 0 0 6px 0; }
        .stat-card p { margin: 0; font-size: 0.95rem; opacity: 0.9; }
        .stat-card i { position: absolute; right: 20px; bottom: 20px; font-size: 4rem; opacity: 0.15; }
        
        /* Two columns layouts for chats */
        .chat-pane { display: grid; grid-template-columns: 320px 1fr; gap: 20px; background: white; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; height: calc(100vh - 280px); min-height: 500px; }
        
        /* Left Column: Sessions List */
        .sessions-sidebar { border-right: 1px solid #e2e8f0; display: flex; flex-direction: column; height: 100%; min-height: 0; overflow: hidden; }
        .sidebar-header { padding: 16px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; font-weight: 700; color: #1e293b; }
        .sessions-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; }
        .session-item { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; cursor: pointer; transition: all 0.2s; display: flex; flex-direction: column; gap: 6px; }
        .session-item:hover { background: #f8fafc; }
        .session-item.active { background: #e0f2fe; border-left: 4px solid #00bfa5; }
        .session-meta { display: flex; justify-content: space-between; align-items: center; font-size: 0.8rem; color: #94a3b8; }
        .session-title { font-weight: 600; color: #1e293b; font-size: 0.9rem; }
        .session-lastmsg { font-size: 0.8rem; color: #64748b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        
        /* Right Column: Chat messages detail */
        .chat-area { display: flex; flex-direction: column; background: #f8fafc; height: 100%; min-height: 0; overflow: hidden; }
        .chat-area-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: white; display: flex; justify-content: space-between; align-items: center; }
        .chat-messages-container { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; }
        .chat-bubble-wrapper { display: flex; gap: 10px; max-width: 80%; }
        .chat-bubble-wrapper.admin { align-self: flex-end; flex-direction: row-reverse; }
        .chat-bubble-wrapper.ai { align-self: flex-start; }
        .chat-bubble-wrapper.user { align-self: flex-start; }
        
        .chat-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
        .chat-bubble-wrapper.admin .chat-avatar { background: #0b1a33; color: white; }
        .chat-bubble-wrapper.ai .chat-avatar { background: #00bfa5; color: white; }
        .chat-bubble-wrapper.user .chat-avatar { background: #cbd5e1; color: #334155; }
        
        .chat-bubble-content { padding: 10px 16px; border-radius: 14px; font-size: 13.5px; line-height: 1.5; }
        .chat-bubble-wrapper.admin .chat-bubble-content { background: #0b1a33; color: white; border-top-right-radius: 0; }
        .chat-bubble-wrapper.ai .chat-bubble-content { background: white; color: #1e293b; border-top-left-radius: 0; box-shadow: 0 1px 3px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }
        .chat-bubble-wrapper.user .chat-bubble-content { background: #e2e8f0; color: #1e293b; border-top-left-radius: 0; }
        .bubble-info { font-size: 10px; color: #94a3b8; margin-top: 4px; display: block; }
        
        /* Chat reply editor footer */
        .chat-area-footer { padding: 14px 20px; background: white; border-top: 1px solid #e2e8f0; display: flex; gap: 12px; align-items: center; }
        .chat-area-footer textarea { flex: 1; border: 1px solid #cbd5e1; border-radius: 10px; padding: 10px 14px; font-size: 13.5px; outline: none; resize: none; height: 38px; }
        .chat-area-footer button { background: #0b1a33; color: white; border: none; padding: 0 20px; height: 38px; border-radius: 10px; font-size: 13.5px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
        .chat-area-footer button:hover { background: #00bfa5; }
        
        /* Status Badges */
        .badge-soft { display: inline-flex; padding: 4px 10px; border-radius: 999px; font-size: .75rem; font-weight: 700; }
        .badge-soft.warning { background: #fef3c7; color: #b45309; }
        .badge-soft.success { background: #dcfce7; color: #15803d; }
        .badge-soft.danger { background: #fee2e2; color: #dc2626; }
        .badge-soft.info { background: #dbeafe; color: #1d4ed8; }
        
        /* Knowledge Base Styles */
        .knowledge-actions { display: flex; justify-content: flex-end; margin-bottom: 15px; }
        .table-card { background: white; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 12px rgba(15,23,42,.04); overflow: hidden; }
        .admin-table { width: 100%; border-collapse: collapse; text-align: left; }
        .admin-table th, .admin-table td { padding: 14px 20px; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
        .admin-table th { background: #f8fafc; color: #64748b; font-weight: 700; font-size: 0.75rem; text-transform: uppercase; }
        .action-icon-btn { background: none; border: none; cursor: pointer; font-size: 1.2rem; padding: 4px; border-radius: 4px; transition: all 0.2s; }
        .action-icon-btn.edit { color: #1e3a8a; }
        .action-icon-btn.edit:hover { background: #dbeafe; }
        .action-icon-btn.delete { color: #b91c1c; }
        .action-icon-btn.delete:hover { background: #fee2e2; }
        
        /* Config form styling */
        .config-card { background: white; border-radius: 16px; border: 1px solid #e2e8f0; padding: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.02); max-width: 700px; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; font-weight: 600; color: #1e293b; margin-bottom: 8px; font-size: 0.9rem; }
        .form-control-admin { width: 100%; padding: 10px 14px; border: 1px solid #cbd5e1; border-radius: 10px; font-size: 0.9rem; outline: none; }
        .form-control-admin:focus { border-color: #00bfa5; }
        
        /* Modal dialog styles */
        .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15,23,42,0.3); display: flex; align-items: center; justify-content: center; z-index: 10000; }
        .modal-box { background: white; width: 600px; border-radius: 20px; box-shadow: 0 20px 50px rgba(15,23,42,0.15); overflow: hidden; animation: modalIn 0.2s ease-out; }
        @keyframes modalIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
        .modal-header { padding: 18px 24px; background: #0b1a33; color: white; display: flex; justify-content: space-between; align-items: center; }
        .modal-header h5 { margin: 0; font-weight: 700; }
        .modal-close { background: none; border: none; color: white; font-size: 1.5rem; cursor: pointer; }
        .modal-body { padding: 24px; }
        .modal-footer { padding: 16px 24px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 10px; }
        
        .btn-admin { padding: 10px 20px; font-size: 0.9rem; font-weight: 600; border-radius: 10px; border: none; cursor: pointer; transition: all 0.2s; }
        .btn-admin.primary { background: #0b1a33; color: white; }
        .btn-admin.primary:hover { background: #00bfa5; }
        .btn-admin.secondary { background: #e2e8f0; color: #334155; }
        .btn-admin.secondary:hover { background: #cbd5e1; }
    </style>
</head>
<body class="admin-page">
<jsp:include page="/components/admin-sidebar.jsp">
    <jsp:param name="currentPage" value="ai-support"/>
</jsp:include>

<main class="admin-main">
    <div class="page-header-admin">
        <div>
            <h1 class="page-title"><i class='bx bxs-bot'></i> Trợ lý AI Customer Support</h1>
            <p class="page-subtitle">Quản lý tri thức và giám sát cuộc trò chuyện AI</p>
        </div>
        <jsp:include page="/components/admin-header-dropdown.jsp" />
    </div>

    <div class="ai-support-container">
        <!-- Tab Menu -->
        <div class="tab-menu">
            <button class="tab-btn active" onclick="switchTab('dashboard')"><i class='bx bxs-dashboard'></i> Dashboard</button>
            <button class="tab-btn" onclick="switchTab('chats')"><i class='bx bxs-chat'></i> Cuộc chat khách hàng</button>
            <button class="tab-btn" onclick="switchTab('waiting')"><i class='bx bxs-error-circle'></i> Cần xử lý <span class="badge-soft danger ms-1" id="waiting-badge" style="display: none;">0</span></button>
            <button class="tab-btn" onclick="switchTab('knowledge')"><i class='bx bxs-book-bookmark'></i> FAQ / Chính sách</button>
            <button class="tab-btn" onclick="switchTab('config')"><i class='bx bxs-cog'></i> Cấu hình AI</button>
        </div>

        <!-- 1. DASHBOARD TAB -->
        <div id="tab-dashboard" class="tab-content active">
            <div class="stats-grid">
                <div class="stat-card blue">
                    <h3 id="stat-total">0</h3>
                    <p>Cuộc chat hôm nay</p>
                    <i class='bx bx-chat'></i>
                </div>
                <div class="stat-card teal">
                    <h3 id="stat-ai">0</h3>
                    <p>AI tự động trả lời</p>
                    <i class='bx bx-bot'></i>
                </div>
                <div class="stat-card orange">
                    <h3 id="stat-admin">0</h3>
                    <p>Cần admin hỗ trợ</p>
                    <i class='bx bx-support'></i>
                </div>
            </div>
            
            <div class="row">
                <div class="col-md-6 mb-4">
                    <div class="table-card">
                        <div class="sidebar-header"><i class='bx bx-pie-chart-alt-2'></i> Thống kê chủ đề được hỏi nhiều nhất</div>
                        <table class="admin-table" id="intents-table">
                            <thead>
                                <tr>
                                    <th>Intent (Chủ đề)</th>
                                    <th>Số lượt hỏi</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr><td colspan="2" class="text-center text-muted">Chưa có dữ liệu</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- 2. ALL CHATS TAB -->
        <div id="tab-chats" class="tab-content">
            <div class="chat-pane">
                <!-- Sidebar: Session List -->
                <div class="sessions-sidebar">
                    <div class="sidebar-header">Danh sách cuộc chat</div>
                    <div class="sessions-list" id="all-sessions-list">
                        <!-- Loaded via AJAX -->
                    </div>
                </div>
                <!-- Chat Area -->
                <div class="chat-area">
                    <div class="chat-area-header">
                        <h6 class="m-0 fw-bold" id="chat-header-title">Chọn cuộc chat để xem chi tiết</h6>
                        <button class="btn btn-sm btn-outline-danger" id="close-session-btn" style="display: none;">Đóng phiên chat</button>
                    </div>
                    <div class="chat-messages-container" id="chat-messages">
                        <div class="text-center text-muted my-5">Nội dung cuộc trò chuyện sẽ hiển thị ở đây</div>
                    </div>
                    <!-- Footer Reply Form -->
                    <div class="chat-area-footer" id="chat-footer-form" style="display: none;">
                        <textarea id="reply-text" placeholder="Nhập câu trả lời trực tiếp cho khách hàng..."></textarea>
                        <button onclick="sendAdminReply()">Gửi câu trả lời</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. ESCALATED CHATS TAB -->
        <div id="tab-waiting" class="tab-content">
            <div class="chat-pane">
                <!-- Sidebar: Session List -->
                <div class="sessions-sidebar">
                    <div class="sidebar-header">Đang chờ admin hỗ trợ</div>
                    <div class="sessions-list" id="waiting-sessions-list">
                        <!-- Loaded via AJAX -->
                    </div>
                </div>
                <!-- Chat Area -->
                <div class="chat-area">
                    <div class="chat-area-header">
                        <h6 class="m-0 fw-bold" id="waiting-header-title">Chọn cuộc chat cần hỗ trợ</h6>
                        <button class="btn btn-sm btn-outline-danger" id="close-waiting-session-btn" style="display: none;">Đóng phiên chat</button>
                    </div>
                    <div class="chat-messages-container" id="waiting-messages">
                        <div class="text-center text-muted my-5">Nội dung cuộc trò chuyện sẽ hiển thị ở đây</div>
                    </div>
                    <!-- Footer Reply Form -->
                    <div class="chat-area-footer" id="waiting-footer-form" style="display: none;">
                        <textarea id="waiting-reply-text" placeholder="Nhập câu trả lời trực tiếp cho khách hàng..."></textarea>
                        <button onclick="sendWaitingAdminReply()">Gửi câu trả lời</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 4. FAQ / KNOWLEDGE BASE TAB -->
        <div id="tab-knowledge" class="tab-content">
            <div class="knowledge-actions">
                <button class="btn-admin primary" onclick="openKnowledgeModal()"><i class='bx bx-plus'></i> Thêm chính sách / FAQ</button>
            </div>
            <div class="table-card">
                <table class="admin-table" id="knowledge-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tiêu đề</th>
                            <th>Category</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="knowledge-tbody">
                        <!-- Loaded via AJAX -->
                    </tbody>
                </table>
            </div>
        </div>

        <!-- 5. AI CONFIG TAB -->
        <div id="tab-config" class="tab-content">
            <div class="config-card">
                <h5 class="fw-bold mb-4"><i class='bx bx-slider'></i> Cấu hình tham số AI Trợ lý</h5>
                <form id="ai-settings-form" onsubmit="saveSettings(event)">
                    <div class="form-group">
                        <label>Trạng thái hoạt động chatbot AI</label>
                        <select class="form-control-admin" id="cfg-enabled" name="AI_SUPPORT_ENABLED">
                            <option value="true">Bật hoạt động</option>
                            <option value="false">Tắt hoạt động (Tạm dừng)</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>DeepSeek Model</label>
                        <input type="text" class="form-control-admin" id="cfg-model" name="DEEPSEEK_MODEL">
                    </div>
                    <div class="form-group">
                        <label>Số sản phẩm gợi ý tối đa trong Context</label>
                        <input type="number" class="form-control-admin" id="cfg-max-products" name="MAX_PRODUCTS_IN_CONTEXT" min="1" max="15">
                    </div>
                    <div class="form-group">
                        <label>Số đơn hàng hiển thị tối đa trong Context</label>
                        <input type="number" class="form-control-admin" id="cfg-max-orders" name="MAX_ORDERS_IN_CONTEXT" min="1" max="10">
                    </div>
                    <div class="form-group">
                        <label>Tự động chuyển tiếp Admin khi AI có độ tin cậy thấp / lỗi</label>
                        <select class="form-control-admin" id="cfg-escalate" name="AUTO_ESCALATE_TO_ADMIN">
                            <option value="true">Có, tự chuyển tiếp</option>
                            <option value="false">Không</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Độ dài câu hỏi tối đa của khách hàng (ký tự)</label>
                        <input type="number" class="form-control-admin" id="cfg-max-length" name="MAX_MESSAGE_LENGTH">
                    </div>
                    <div class="text-end mt-4">
                        <button type="submit" class="btn-admin primary">Lưu cấu hình</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</main>

<!-- Modal: Add / Edit Knowledge -->
<div class="modal-overlay" id="knowledge-modal" style="display: none;">
    <div class="modal-box">
        <div class="modal-header">
            <h5 id="modal-title">Thêm chính sách / FAQ</h5>
            <button class="modal-close" onclick="closeKnowledgeModal()">&times;</button>
        </div>
        <form id="knowledge-form" onsubmit="saveKnowledge(event)">
            <input type="hidden" id="kb-id" name="id">
            <div class="modal-body">
                <div class="form-group">
                    <label>Tiêu đề</label>
                    <input type="text" class="form-control-admin" id="kb-title" name="title" required>
                </div>
                <div class="form-group">
                    <label>Danh mục phân loại (Category)</label>
                    <select class="form-control-admin" id="kb-category" name="category" required>
                        <option value="SHIPPING">Vận chuyển (SHIPPING)</option>
                        <option value="PAYMENT">Thanh toán (PAYMENT)</option>
                        <option value="RETURN_POLICY">Đổi trả (RETURN_POLICY)</option>
                        <option value="REFUND_POLICY">Hoàn tiền (REFUND_POLICY)</option>
                        <option value="WARRANTY">Bảo hành (WARRANTY)</option>
                        <option value="ACCOUNT">Tài khoản (ACCOUNT)</option>
                        <option value="CONTACT">Liên hệ shop (CONTACT)</option>
                        <option value="WORKING_HOURS">Giờ làm việc (WORKING_HOURS)</option>
                        <option value="FAQ">Câu hỏi FAQ (FAQ)</option>
                        <option value="OTHER">Danh mục khác (OTHER)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Nội dung chính sách / Tri thức</label>
                    <textarea class="form-control-admin" id="kb-content" name="content" rows="6" required></textarea>
                </div>
                <div class="form-group">
                    <label>
                        <input type="checkbox" id="kb-active" name="isActive" value="true" checked> Đang kích hoạt (Cho AI đọc)
                    </label>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-admin secondary" onclick="closeKnowledgeModal()">Hủy</button>
                <button type="submit" class="btn-admin primary">Lưu tri thức</button>
            </div>
        </form>
    </div>
</div>

<script>
    const contextPath = "${pageContext.request.contextPath}";
    let selectedActiveSessionId = 0;
    let selectedWaitingSessionId = 0;
    let currentTab = 'dashboard';

    function getFriendlyIntentName(intent) {
        if (!intent) return "Không xác định";
        const map = {
            'PRODUCT_ADVICE': 'Tư vấn sản phẩm',
            'ORDER_STATUS': 'Kiểm tra đơn hàng',
            'UNKNOWN': 'Không xác định',
            'PAYMENT': 'Thanh toán',
            'FAQ': 'Câu hỏi thường gặp',
            'RETURN_REFUND': 'Đổi trả / Hoàn tiền'
        };
        return map[intent.toUpperCase()] || intent;
    }

    document.addEventListener("DOMContentLoaded", function() {
        // Load default tab
        loadDashboardStats();
        
        // Auto refresh current tab silently
        setInterval(function() {
            if (currentTab === 'dashboard') {
                loadDashboardStats();
            } else if (currentTab === 'chats') {
                loadSessions('all', true);
                if (selectedActiveSessionId > 0) {
                    loadMessages('all', selectedActiveSessionId, true);
                }
            } else if (currentTab === 'waiting') {
                loadSessions('waiting', true);
                if (selectedWaitingSessionId > 0) {
                    loadMessages('waiting', selectedWaitingSessionId, true);
                }
            }
        }, 5000);
    });

    function switchTab(tabName) {
        currentTab = tabName;
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        
        event.currentTarget.classList.add('active');
        document.getElementById('tab-' + tabName).classList.add('active');

        if (tabName === 'dashboard') {
            loadDashboardStats();
        } else if (tabName === 'chats') {
            loadSessions('all');
        } else if (tabName === 'waiting') {
            loadSessions('waiting');
        } else if (tabName === 'knowledge') {
            loadKnowledgeList();
        } else if (tabName === 'config') {
            loadSettings();
        }
    }

    // 1. DASHBOARD
    function loadDashboardStats() {
        fetch(contextPath + "/admin/ai-support/dashboard")
            .then(res => res.json())
            .then(data => {
                document.getElementById("stat-total").innerText = data.totalChatsToday;
                document.getElementById("stat-ai").innerText = data.answeredByAI;
                document.getElementById("stat-admin").innerText = data.needAdminSupport;

                const badge = document.getElementById("waiting-badge");
                if (data.needAdminSupport > 0) {
                    badge.innerText = data.needAdminSupport;
                    badge.style.display = "inline-flex";
                } else {
                    badge.style.display = "none";
                }

                // Intents table
                const tbody = document.querySelector("#intents-table tbody");
                tbody.innerHTML = "";
                if (data.topIntents && data.topIntents.length > 0) {
                    data.topIntents.forEach(item => {
                        const tr = document.createElement("tr");
                        tr.innerHTML = `<td><code>\${getFriendlyIntentName(item.intent)}</code></td><td><strong>\${item.count}</strong></td>`;
                        tbody.appendChild(tr);
                    });
                } else {
                    tbody.innerHTML = `<tr><td colspan="2" class="text-center text-muted">Không có dữ liệu hôm nay</td></tr>`;
                }
            })
            .catch(err => console.error("Error stats:", err));
    }

    // 2. CHATS & WAITING SESSIONS
    function loadSessions(filter, isSilent) {
        fetch(contextPath + "/admin/ai-support/sessions?filter=" + filter)
            .then(res => res.json())
            .then(data => {
                const listContainer = document.getElementById(filter === 'waiting' ? 'waiting-sessions-list' : 'all-sessions-list');
                
                let html = "";
                if (data.length === 0) {
                    html = `<div class="text-center text-muted p-4">Không có cuộc chat nào</div>`;
                } else {
                    data.forEach(s => {
                        let statusBadge = "";
                        if (s.status === "WAITING_ADMIN") {
                            statusBadge = `<span class="badge-soft danger">Cần hỗ trợ</span>`;
                        } else if (s.status === "CLOSED") {
                            statusBadge = `<span class="badge-soft info">Đã đóng</span>`;
                        } else {
                            statusBadge = `<span class="badge-soft success">AI hỗ trợ</span>`;
                        }
                        
                        const isActive = (filter === 'waiting' ? selectedWaitingSessionId : selectedActiveSessionId) === s.id;
                        const activeClass = isActive ? " active" : "";

                        html += `
                            <div class="session-item\${activeClass}" id="\${filter === 'waiting' ? 'waiting-item-' : 'all-item-'}\${s.id}" 
                                 onclick="selectSession('\${filter}', \${s.id}, '\${s.displayName.replace(/'/g, "\\'")}')">
                                <div class="session-meta">
                                    <span>#\${s.id}</span>
                                    \${statusBadge}
                                </div>
                                <div class="session-title">\${s.displayName}</div>
                                <div class="session-lastmsg">\${s.lastMessage ? s.lastMessage : 'Bắt đầu cuộc chat'}</div>
                            </div>
                        `;
                    });
                }
                
                if (listContainer.innerHTML !== html) {
                    listContainer.innerHTML = html;
                }
            })
            .catch(err => console.error("Error loading sessions:", err));
    }

    function selectSession(filter, sessionId, name) {
        document.querySelectorAll('.session-item').forEach(item => item.classList.remove('active'));
        
        const activeItem = document.getElementById((filter === 'waiting' ? 'waiting-item-' : 'all-item-') + sessionId);
        if (activeItem) activeItem.classList.add('active');
        
        if (filter === 'waiting') {
            selectedWaitingSessionId = sessionId;
            document.getElementById("waiting-header-title").innerText = "Đang hỗ trợ: " + name;
            document.getElementById("waiting-footer-form").style.display = "flex";
            document.getElementById("close-waiting-session-btn").style.display = "block";
            loadMessages('waiting', sessionId);
        } else {
            selectedActiveSessionId = sessionId;
            document.getElementById("chat-header-title").innerText = "Đang xem: " + name;
            document.getElementById("chat-footer-form").style.display = "flex";
            document.getElementById("close-session-btn").style.display = "block";
            loadMessages('all', sessionId);
        }
    }

    function loadMessages(filter, sessionId, isSilent) {
        const msgContainer = document.getElementById(filter === 'waiting' ? 'waiting-messages' : 'chat-messages');
        if (!isSilent) {
            msgContainer.innerHTML = `<div class="text-center text-muted p-4"><div class="spinner-border text-primary" role="status"></div> Loading...</div>`;
        }

        fetch(contextPath + "/admin/ai-support/sessions/detail?sessionId=" + sessionId)
            .then(res => res.json())
            .then(data => {
                let html = "";
                if (data.length === 0) {
                    html = `<div class="text-center text-muted p-4">Không có tin nhắn nào</div>`;
                } else {
                    data.forEach(m => {
                        let avatarHtml = "";
                        if (m.senderType === 'AI') {
                            avatarHtml = "<i class='bx bxs-bot'></i>";
                        } else if (m.senderType === 'ADMIN') {
                            avatarHtml = "<i class='bx bxs-user-rectangle'></i>";
                        } else {
                            avatarHtml = "<i class='bx bxs-user'></i>";
                        }
                        
                        let detailsHtml = "";
                        if (m.senderType === 'AI') {
                            detailsHtml = `<small class="bubble-info">Intent: <code>\${getFriendlyIntentName(m.intent)}</code> (\${m.confidence * 100}%) | Cần admin: <strong>\${m.needAdminSupport}</strong></small>`;
                        }
                        
                        html += `
                            <div class="chat-bubble-wrapper \${m.senderType.toLowerCase()}">
                                <div class="chat-avatar">\${avatarHtml}</div>
                                <div class="chat-bubble-content">
                                    \${m.message}
                                    \${detailsHtml}
                                </div>
                            </div>
                        `;
                    });
                }
                
                if (msgContainer.innerHTML !== html) {
                    const oldScrollTop = msgContainer.scrollTop;
                    const wasAtBottom = (msgContainer.scrollHeight - msgContainer.clientHeight <= msgContainer.scrollTop + 50);

                    msgContainer.innerHTML = html;

                    if (wasAtBottom || !isSilent) {
                        msgContainer.scrollTop = msgContainer.scrollHeight;
                    } else {
                        msgContainer.scrollTop = oldScrollTop;
                    }
                }
            })
            .catch(err => console.error("Error messages detail:", err));
    }

    function sendAdminReply() {
        const text = document.getElementById("reply-text").value.trim();
        if (!text) return;
        
        const params = new URLSearchParams();
        params.append("sessionId", selectedActiveSessionId);
        params.append("message", text);
        
        fetch(contextPath + "/admin/ai-support/sessions/reply", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-Token": "${csrfToken}"
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                document.getElementById("reply-text").value = "";
                loadMessages('all', selectedActiveSessionId);
                loadSessions('all');
            }
        });
    }

    function sendWaitingAdminReply() {
        const text = document.getElementById("waiting-reply-text").value.trim();
        if (!text) return;
        
        const params = new URLSearchParams();
        params.append("sessionId", selectedWaitingSessionId);
        params.append("message", text);
        
        fetch(contextPath + "/admin/ai-support/sessions/reply", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-Token": "${csrfToken}"
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                document.getElementById("waiting-reply-text").value = "";
                loadMessages('waiting', selectedWaitingSessionId);
                loadSessions('waiting');
                loadDashboardStats();
            }
        });
    }

    // Close session button events
    document.getElementById("close-session-btn").onclick = function() {
        closeSession(selectedActiveSessionId, 'all');
    };
    document.getElementById("close-waiting-session-btn").onclick = function() {
        closeSession(selectedWaitingSessionId, 'waiting');
    };

    function closeSession(sessionId, type) {
        if (!confirm("Bạn có chắc chắn muốn đóng phiên chat này?")) return;
        
        const params = new URLSearchParams();
        params.append("sessionId", sessionId);
        
        fetch(contextPath + "/admin/ai-support/sessions/close", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-Token": "${csrfToken}"
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert("Phiên chat đã được đóng thành công.");
                loadSessions(type === 'waiting' ? 'waiting' : 'all');
                loadDashboardStats();
                
                if (type === 'waiting') {
                    document.getElementById("waiting-messages").innerHTML = `<div class="text-center text-muted my-5">Nội dung cuộc trò chuyện sẽ hiển thị ở đây</div>`;
                    document.getElementById("waiting-footer-form").style.display = "none";
                    document.getElementById("close-waiting-session-btn").style.display = "none";
                    document.getElementById("waiting-header-title").innerText = "Chọn cuộc chat cần hỗ trợ";
                } else {
                    document.getElementById("chat-messages").innerHTML = `<div class="text-center text-muted my-5">Nội dung cuộc trò chuyện sẽ hiển thị ở đây</div>`;
                    document.getElementById("chat-footer-form").style.display = "none";
                    document.getElementById("close-session-btn").style.display = "none";
                    document.getElementById("chat-header-title").innerText = "Chọn cuộc chat để xem chi tiết";
                }
            }
        });
    }

    // 3. FAQ / KNOWLEDGE BASE
    function loadKnowledgeList() {
        fetch(contextPath + "/admin/ai-support/knowledge")
            .then(res => res.json())
            .then(data => {
                const tbody = document.getElementById("knowledge-tbody");
                tbody.innerHTML = "";
                
                data.forEach(item => {
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>\${item.id}</td>
                        <td><strong>\${item.title}</strong></td>
                        <td><code>\${item.category}</code></td>
                        <td><span class="badge-soft \${item.isActive ? 'success' : 'danger'}">\${item.isActive ? 'Kích hoạt' : 'Tạm ẩn'}</span></td>
                        <td>
                            <button class="action-icon-btn edit" onclick="editKnowledge(\${item.id}, '\${item.title.replace(/'/g, "\\\\'")}', '\${item.category}', '\${item.content.replace(/\\n/g, '\\\\n').replace(/'/g, "\\\\'")}', \${item.isActive})" title="Sửa"><i class='bx bxs-edit'></i></button>
                            <button class="action-icon-btn delete" onclick="deleteKnowledge(\${item.id})" title="Xóa"><i class='bx bxs-trash'></i></button>
                        </td>
                    `;
                    tbody.appendChild(tr);
                });
            })
            .catch(err => console.error("Error loading knowledge list:", err));
    }

    function openKnowledgeModal() {
        document.getElementById("modal-title").innerText = "Thêm chính sách / FAQ";
        document.getElementById("kb-id").value = "";
        document.getElementById("kb-title").value = "";
        document.getElementById("kb-category").value = "FAQ";
        document.getElementById("kb-content").value = "";
        document.getElementById("kb-active").checked = true;
        document.getElementById("knowledge-modal").style.display = "flex";
    }

    function editKnowledge(id, title, category, content, isActive) {
        document.getElementById("modal-title").innerText = "Cập nhật chính sách / FAQ #" + id;
        document.getElementById("kb-id").value = id;
        document.getElementById("kb-title").value = title;
        document.getElementById("kb-category").value = category;
        document.getElementById("kb-content").value = content;
        document.getElementById("kb-active").checked = isActive;
        document.getElementById("knowledge-modal").style.display = "flex";
    }

    function closeKnowledgeModal() {
        document.getElementById("knowledge-modal").style.display = "none";
    }

    function saveKnowledge(event) {
        event.preventDefault();
        const id = document.getElementById("kb-id").value;
        const title = document.getElementById("kb-title").value;
        const category = document.getElementById("kb-category").value;
        const content = document.getElementById("kb-content").value;
        const isActive = document.getElementById("kb-active").checked;
        
        const params = new URLSearchParams();
        params.append("action", id ? "update" : "create");
        if (id) params.append("id", id);
        params.append("title", title);
        params.append("category", category);
        params.append("content", content);
        params.append("isActive", isActive);
        
        fetch(contextPath + "/admin/ai-support/knowledge", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-Token": "${csrfToken}"
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                closeKnowledgeModal();
                loadKnowledgeList();
                alert("Đã lưu thông tin tri thức thành công!");
            } else {
                alert("Có lỗi xảy ra khi lưu.");
            }
        });
    }

    function deleteKnowledge(id) {
        if (!confirm("Bạn có chắc chắn muốn xóa tri thức #" + id + " này?")) return;
        
        const params = new URLSearchParams();
        params.append("action", "delete");
        params.append("id", id);
        
        fetch(contextPath + "/admin/ai-support/knowledge", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-Token": "${csrfToken}"
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                loadKnowledgeList();
                alert("Đã xóa thành công!");
            } else {
                alert("Xóa thất bại.");
            }
        });
    }

    // 4. CONFIG AI SETTINGS
    function loadSettings() {
        fetch(contextPath + "/admin/ai-support/settings")
            .then(res => res.json())
            .then(data => {
                document.getElementById("cfg-enabled").value = data.AI_SUPPORT_ENABLED || "true";
                document.getElementById("cfg-model").value = data.DEEPSEEK_MODEL || "deepseek-v4-flash";
                document.getElementById("cfg-max-products").value = data.MAX_PRODUCTS_IN_CONTEXT || "5";
                document.getElementById("cfg-max-orders").value = data.MAX_ORDERS_IN_CONTEXT || "3";
                document.getElementById("cfg-escalate").value = data.AUTO_ESCALATE_TO_ADMIN || "true";
                document.getElementById("cfg-max-length").value = data.MAX_MESSAGE_LENGTH || "1000";
            })
            .catch(err => console.error("Error loading settings:", err));
    }

    function saveSettings(event) {
        event.preventDefault();
        
        const fields = [
            { key: "AI_SUPPORT_ENABLED", val: document.getElementById("cfg-enabled").value },
            { key: "DEEPSEEK_MODEL", val: document.getElementById("cfg-model").value },
            { key: "MAX_PRODUCTS_IN_CONTEXT", val: document.getElementById("cfg-max-products").value },
            { key: "MAX_ORDERS_IN_CONTEXT", val: document.getElementById("cfg-max-orders").value },
            { key: "AUTO_ESCALATE_TO_ADMIN", val: document.getElementById("cfg-escalate").value },
            { key: "MAX_MESSAGE_LENGTH", val: document.getElementById("cfg-max-length").value }
        ];

        let savePromises = fields.map(f => {
            const params = new URLSearchParams();
            params.append("settingKey", f.key);
            params.append("settingValue", f.val);
            return fetch(contextPath + "/admin/ai-support/settings", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-CSRF-Token": "${csrfToken}"
                },
                body: params
            }).then(res => res.json());
        });

        Promise.all(savePromises)
            .then(results => {
                const allSuccess = results.every(r => r.success);
                if (allSuccess) {
                    alert("Cấu hình AI đã được cập nhật thành công!");
                } else {
                    alert("Một số thiết lập lưu thất bại.");
                }
                loadSettings();
            })
            .catch(err => alert("Có lỗi xảy ra: " + err));
    }
</script>
</body>
</html>
