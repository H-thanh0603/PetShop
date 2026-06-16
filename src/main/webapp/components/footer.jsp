<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<footer class="footer-section">
    <div class="container">
        <div class="row">
            <div class="col-lg-3 col-md-6 mb-4">
                <div class="footer-logo mb-3">
                    <h3 class="text-white fw-bold"><i class='bx bxs-dog'></i> PetShop</h3>
                    <span class="text-white-50">Thế giới phụ kiện thú cưng</span>
                </div>
                <p class="text-white-50 mb-2">Đăng ký nhận tin khuyến mãi</p>
                <form class="subscribe-form d-flex mb-3">
                    <input type="email" class="form-control me-1" placeholder="Email của bạn">
                    <button type="submit" class="btn btn-primary"><i class='bx bx-right-arrow-alt'></i></button>
                </form>
                <div class="social-icons">
                    <a href="#"><i class='bx bxl-facebook'></i></a>
                    <a href="#"><i class='bx bxl-instagram'></i></a>
                    <a href="#"><i class='bx bxl-linkedin'></i></a>
                </div>
            </div>

            <div class="col-lg-2 col-md-6 mb-4">
                <h5 class="text-white mb-3">Sản phẩm</h5>
                <ul class="list-unstyled footer-links">
                    <li><a href="${pageContext.request.contextPath}/shop">Thức ăn</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Đồ chơi</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Phụ kiện</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Chuồng & Nệm</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Vệ sinh</a></li>
                </ul>
            </div>

            <div class="col-lg-2 col-md-6 mb-4">
                <h5 class="text-white mb-3">Hỗ trợ</h5>
                <ul class="list-unstyled footer-links">
                    <li><a href="${pageContext.request.contextPath}/return-policy">Chính sách đổi trả</a></li>
                    <li><a href="${pageContext.request.contextPath}/shipping-policy">Chính sách vận chuyển</a></li>
                    <li><a href="${pageContext.request.contextPath}/buying-guide">Hướng dẫn mua hàng</a></li>
                    <li><a href="${pageContext.request.contextPath}/support">Tư vấn khách hàng</a></li>
                    <li><a href="${pageContext.request.contextPath}/privacy-policy">Chính sách bảo mật</a></li>
                    <li><a href="${pageContext.request.contextPath}/terms">Điều khoản sử dụng</a></li>
                </ul>
            </div>

            <div class="col-lg-5 col-md-6 mb-4">
                <h5 class="text-white mb-3">LIÊN HỆ <span class="float-end">-</span></h5>
                <ul class="list-unstyled text-white-50 contact-info">
                    <li class="mb-2"><strong class="text-white">Hotline:</strong> 1900 123 456</li>
                    <li class="mb-2"><strong class="text-white">Email:</strong> support@petshop.vn</li>
                    <li class="mb-2">
                        <strong class="text-white">Trụ sở chính:</strong><br>
                        123 Đường ABC, Quận 1, TP. Hồ Chí Minh
                    </li>
                    <li class="mb-2">
                        <strong class="text-white">Chi nhánh Hà Nội:</strong><br>
                        456 Đường XYZ, Quận Cầu Giấy, Hà Nội
                    </li>
                    <li class="mt-3 text-white">
                        <strong>Giờ mở cửa:</strong> 8:00 AM - 9:00 PM (Tất cả các ngày trong tuần)
                    </li>
                </ul>
            </div>
        </div>
    </div>
    
    <jsp:include page="/components/toast.jsp" />
    <div class="footer-bottom text-center py-3">
        <p class="text-white-50 m-0">&copy; 2025 PetShop. All Rights Reserved.</p>
    </div>
</footer>

<!-- AI Customer Support Floating Chat Widget -->
<div id="ai-chat-widget" class="ai-chat-widget">
    <!-- Chat Toggle Button -->
    <button id="ai-chat-toggle" class="ai-chat-toggle" title="Trợ lý AI hỗ trợ">
        <div class="toggle-icon-wrapper">
            <i class='bx bxs-bot' id="toggle-bot-icon"></i>
            <i class='bx bx-x' id="toggle-close-icon" style="display: none;"></i>
        </div>
        <span class="ai-chat-badge">AI hỗ trợ</span>
        <!-- Red unread badge -->
        <span id="ai-chat-unread-badge" class="ai-unread-badge" style="display: none;">0</span>
    </button>

    <!-- Chat Window Drawer -->
    <div id="ai-chat-window" class="ai-chat-window" style="display: none;">
        <!-- Header -->
        <div class="ai-chat-header">
            <div class="header-info">
                <div class="bot-avatar">
                    <i class='bx bxs-bot'></i>
                    <span class="online-indicator"></span>
                </div>
                <div>
                    <h6 class="m-0 text-white fw-bold">Trợ lý AI PetShop</h6>
                    <small class="text-white-50">Thường phản hồi ngay lập tức</small>
                </div>
            </div>
            <button id="ai-chat-close" class="close-btn" title="Đóng khung chat">
                <i class='bx bx-chevron-down'></i>
            </button>
        </div>

        <!-- Chat Messages Area -->
        <div id="ai-chat-body" class="ai-chat-body">
            <!-- Initial Greeting Message -->
            <div class="chat-message bot">
                <div class="message-avatar"><i class='bx bxs-bot'></i></div>
                <div class="message-content-wrapper">
                    <div class="message-content">
                        Xin chào! Tôi là Trợ lý AI của PetShop. Tôi có thể giúp bạn tư vấn sản phẩm, kiểm tra đơn hàng, hướng dẫn vận chuyển và trả lời các thắc mắc khác. Bạn cần hỗ trợ gì hôm nay?
                    </div>
                </div>
            </div>
        </div>

        <!-- Suggested Quick Questions -->
        <div id="ai-chat-suggestions" class="ai-chat-suggestions">
            <div class="suggestion-tag" onclick="sendQuickQuestion('Tôi muốn kiểm tra đơn hàng')">Kiểm tra đơn hàng</div>
            <div class="suggestion-tag" onclick="sendQuickQuestion('Tư vấn sản phẩm cho mèo con')">Tư vấn hạt cho mèo</div>
            <div class="suggestion-tag" onclick="sendQuickQuestion('Shop có những phương thức thanh toán nào?')">Thanh toán</div>
            <div class="suggestion-tag" onclick="sendQuickQuestion('Phí vận chuyển bao nhiêu?')">Phí vận chuyển</div>
            <div class="suggestion-tag" onclick="sendQuickQuestion('Chính sách đổi trả như thế nào?')">Đổi trả hàng</div>
            <div class="suggestion-tag" onclick="sendQuickQuestion('Làm sao liên hệ shop?')">Liên hệ shop</div>
        </div>

        <!-- Typing Indicator -->
        <div id="ai-chat-typing" class="chat-message bot typing-indicator-msg" style="display: none;">
            <div class="message-avatar"><i class='bx bxs-bot'></i></div>
            <div class="message-content-wrapper">
                <div class="message-content typing-dots">
                    <span></span><span></span><span></span>
                </div>
            </div>
        </div>

        <!-- Footer Input Form -->
        <div class="ai-chat-footer">
            <input type="text" id="ai-chat-input" placeholder="Nhập câu hỏi của bạn..." maxlength="1000">
            <button id="ai-chat-send" title="Gửi câu hỏi">
                <i class='bx bxs-send'></i>
            </button>
        </div>
    </div>
</div>

<!-- Premium AI Chat CSS styles -->
<style>
    /* Widget container floating at bottom right */
    .ai-chat-widget {
        position: fixed;
        bottom: 30px;
        right: 30px;
        z-index: 9999999 !important;
        font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    
    /* AI support red unread badge */
    .ai-unread-badge {
        position: absolute;
        top: -6px;
        right: -6px;
        background-color: #dc3545;
        color: white;
        font-size: 11px;
        font-weight: 700;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 10px rgba(220, 53, 69, 0.4);
        border: 2px solid white;
        z-index: 21000 !important;
    }
    
    /* Toggle Button styles */
    .ai-chat-toggle {
        background: linear-gradient(135deg, #00bfa5 0%, #00796b 100%);
        color: white;
        border: none;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        box-shadow: 0 8px 30px rgba(0, 191, 165, 0.4);
        cursor: pointer;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
    .ai-chat-toggle:hover {
        transform: scale(1.08) translateY(-3px);
        box-shadow: 0 12px 35px rgba(0, 191, 165, 0.5);
    }
    .ai-chat-toggle:active {
        transform: scale(0.95);
    }
    .toggle-icon-wrapper i {
        font-size: 28px;
        transition: transform 0.3s ease;
    }
    
    /* AI support badge on toggle button */
    .ai-chat-badge {
        position: absolute;
        right: 70px;
        background: #0b1a33;
        color: white;
        font-size: 12px;
        font-weight: 600;
        padding: 6px 14px;
        border-radius: 20px;
        box-shadow: 0 4px 15px rgba(11,26,51,0.15);
        white-space: nowrap;
        pointer-events: none;
        opacity: 0.9;
        animation: floatBadge 3s ease-in-out infinite;
        border: 1px solid rgba(255,255,255,0.05);
    }
    @keyframes floatBadge {
        0%, 100% { transform: translateY(0); }
        50% { transform: translateY(-4px); }
    }
    
    /* Chat Window Layout */
    .ai-chat-window {
        position: absolute;
        bottom: 75px;
        right: 0;
        width: 380px;
        height: 540px;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(15px);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 24px;
        box-shadow: 0 15px 45px rgba(15, 23, 42, 0.15);
        display: flex;
        flex-direction: column;
        overflow: hidden;
        animation: openWindow 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.15) forwards;
        transform-origin: bottom right;
    }
    @keyframes openWindow {
        from { transform: scale(0.7); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }
    
    /* Header styling with smooth gradient */
    .ai-chat-header {
        background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%);
        padding: 16px 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid rgba(255,255,255,0.05);
    }
    .header-info {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .bot-avatar {
        background: rgba(0, 191, 165, 0.15);
        color: #00bfa5;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
        position: relative;
    }
    .online-indicator {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 10px;
        height: 10px;
        background-color: #00e676;
        border: 2px solid #0b1a33;
        border-radius: 50%;
    }
    .close-btn {
        background: rgba(255, 255, 255, 0.1);
        border: none;
        color: white;
        width: 32px;
        height: 32px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        transition: background-color 0.2s;
    }
    .close-btn:hover { background: rgba(255, 255, 255, 0.2); }
    
    /* Chat Content bubbles */
    .ai-chat-body {
        flex: 1;
        padding: 20px;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        gap: 16px;
        background: #f8fafc;
        scroll-behavior: smooth;
    }
    .chat-message {
        display: flex;
        gap: 10px;
        max-width: 85%;
    }
    .chat-message.bot {
        align-self: flex-start;
    }
    .chat-message.user {
        align-self: flex-end;
        flex-direction: row-reverse;
        max-width: 80%;
    }
    .message-avatar {
        background: rgba(11, 26, 51, 0.1);
        color: #0b1a33;
        width: 30px;
        height: 30px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        flex-shrink: 0;
    }
    .chat-message.bot .message-avatar {
        background: rgba(0, 191, 165, 0.1);
        color: #00bfa5;
    }
    .message-content-wrapper {
        display: flex;
        flex-direction: column;
        gap: 6px;
    }
    .message-content {
        padding: 12px 16px;
        border-radius: 18px;
        font-size: 13.5px;
        line-height: 1.5;
    }
    .chat-message.bot .message-content {
        background: white;
        color: #1e293b;
        box-shadow: 0 2px 8px rgba(15,23,42,0.03);
        border-top-left-radius: 4px;
        border: 1px solid #f1f5f9;
    }
    .chat-message.user .message-content {
        background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%);
        color: white;
        border-top-right-radius: 4px;
        box-shadow: 0 4px 12px rgba(11,26,51,0.1);
    }
    
    /* Suggested questions panel */
    .ai-chat-suggestions {
        padding: 10px 20px;
        display: flex;
        flex-wrap: nowrap;
        gap: 8px;
        overflow-x: auto;
        background: #f8fafc;
        border-top: 1px solid #e2e8f0;
        scrollbar-width: none;
    }
    .ai-chat-suggestions::-webkit-scrollbar { display: none; }
    .suggestion-tag {
        background: white;
        color: #00796b;
        border: 1px solid #00bfa5;
        font-size: 11.5px;
        font-weight: 500;
        padding: 6px 14px;
        border-radius: 30px;
        cursor: pointer;
        white-space: nowrap;
        transition: all 0.2s ease;
    }
    .suggestion-tag:hover {
        background: #00bfa5;
        color: white;
        transform: translateY(-1px);
    }
    
    /* Typing indicator dots */
    .typing-dots span {
        display: inline-block;
        width: 6px;
        height: 6px;
        background-color: #64748b;
        border-radius: 50%;
        margin-right: 4px;
        animation: typingDot 1.2s infinite;
    }
    .typing-dots span:nth-child(2) { animation-delay: 0.2s; }
    .typing-dots span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes typingDot {
        0%, 100% { transform: translateY(0); }
        50% { transform: translateY(-5px); }
    }
    
    /* Footer input bar */
    .ai-chat-footer {
        padding: 14px 20px;
        background: white;
        display: flex;
        gap: 12px;
        align-items: center;
        border-top: 1px solid #e2e8f0;
    }
    .ai-chat-footer input {
        flex: 1;
        border: 1px solid #cbd5e1;
        border-radius: 12px;
        padding: 10px 14px;
        font-size: 13.5px;
        outline: none;
        transition: border-color 0.2s;
    }
    .ai-chat-footer input:focus {
        border-color: #00bfa5;
    }
    .ai-chat-footer button {
        background: #0b1a33;
        color: white;
        border: none;
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        font-size: 18px;
        transition: all 0.2s;
    }
    .ai-chat-footer button:hover {
        background: #00bfa5;
        transform: scale(1.05);
    }
    .ai-chat-footer button:active {
        transform: scale(0.95);
    }
    
    /* Cards layout for products returned by AI */
    .product-cards-container {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-top: 8px;
        width: 100%;
    }
    .chat-product-card {
        display: flex;
        gap: 10px;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        padding: 8px;
        text-decoration: none;
        color: inherit;
        transition: all 0.2s;
    }
    .chat-product-card:hover {
        background: #f1f5f9;
        border-color: #cbd5e1;
        transform: translateY(-1px);
    }
    .chat-product-card img {
        width: 50px;
        height: 50px;
        border-radius: 8px;
        object-fit: cover;
        background: white;
        border: 1px solid #f1f5f9;
    }
    .product-card-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;
    }
    .product-card-name {
        font-size: 12.5px;
        font-weight: 600;
        color: #1e293b;
        margin: 0 0 2px 0;
        line-height: 1.3;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
    .product-card-prices {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 11.5px;
    }
    .product-card-original {
        text-decoration: line-through;
        color: #94a3b8;
    }
    .product-card-final {
        font-weight: 700;
        color: #0f766e;
    }
    
    /* Card layout for order info returned by AI */
    .chat-order-card {
        background: #f8fafc;
        border: 1px solid #cbd5e1;
        border-radius: 12px;
        padding: 12px;
        margin-top: 8px;
        font-size: 12.5px;
        line-height: 1.4;
        color: #334155;
    }
    .chat-order-card-header {
        display: flex;
        justify-content: space-between;
        font-weight: 700;
        color: #0f172a;
        margin-bottom: 6px;
        border-bottom: 1px solid #e2e8f0;
        padding-bottom: 6px;
    }
    .order-status-badge {
        font-size: 10px;
        padding: 2px 8px;
        border-radius: 10px;
        font-weight: 700;
    }
    
    @media (max-width: 480px) {
        .ai-chat-window {
            width: calc(100vw - 40px);
            right: -10px;
            height: 480px;
        }
        .ai-chat-widget {
            bottom: 20px;
            right: 20px;
        }
    }
</style>

<!-- Floating Chat Widget JS -->
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const toggleBtn = document.getElementById("ai-chat-toggle");
        const chatWindow = document.getElementById("ai-chat-window");
        const closeBtn = document.getElementById("ai-chat-close");
        const botIcon = document.getElementById("toggle-bot-icon");
        const closeIcon = document.getElementById("toggle-close-icon");
        const chatInput = document.getElementById("ai-chat-input");
        const chatSend = document.getElementById("ai-chat-send");
        const chatBody = document.getElementById("ai-chat-body");
        const chatTyping = document.getElementById("ai-chat-typing");
        
        let currentSessionId = localStorage.getItem("petshop_ai_session_id") || 0;
        
        // Open/Close chat box
        toggleBtn.addEventListener("click", function () {
            if (chatWindow.style.display === "none") {
                chatWindow.style.display = "flex";
                botIcon.style.display = "none";
                closeIcon.style.display = "block";
                chatInput.focus();
                
                // Hide badge when open
                document.getElementById("ai-chat-unread-badge").style.display = "none";
                
                // Load conversation history if we have an active sessionId
                if (currentSessionId > 0 && chatBody.children.length <= 2) {
                    loadChatMessages(currentSessionId);
                }
            } else {
                chatWindow.style.display = "none";
                botIcon.style.display = "block";
                closeIcon.style.display = "none";
                // Update badge when closed
                updateAiChatUnreadBadge();
            }
        });
        
        closeBtn.addEventListener("click", function () {
            chatWindow.style.display = "none";
            botIcon.style.display = "block";
            closeIcon.style.display = "none";
            // Update badge when closed
            updateAiChatUnreadBadge();
        });
        
        // Send message event
        chatSend.addEventListener("click", sendMessage);
        chatInput.addEventListener("keypress", function (e) {
            if (e.key === "Enter") {
                sendMessage();
            }
        });
        
        function loadChatMessages(sessionId) {
            const contextPath = "${pageContext.request.contextPath}";
            fetch(contextPath + "/ai-support/messages?sessionId=" + sessionId)
                .then(res => {
                    if (res.status === 403 || res.status === 404) {
                        // Session expired or belongs to someone else
                        localStorage.removeItem("petshop_ai_session_id");
                        currentSessionId = 0;
                        return null;
                    }
                    return res.json();
                })
                .then(data => {
                    if (!data) return;
                    
                    // Clear messages (except first greeting)
                    const greeting = chatBody.children[0];
                    chatBody.innerHTML = "";
                    chatBody.appendChild(greeting);
                    
                    data.forEach(msg => {
                        appendMessageBubble(msg.senderType, msg.message, null, null);
                    });
                    scrollToBottom();
                })
                .catch(err => logError("Error loading messages: " + err));
        }
        
        function sendMessage() {
            const text = chatInput.value.trim();
            if (!text) return;
            
            chatInput.value = "";
            appendMessageBubble("USER", text);
            scrollToBottom();
            
            // Show Typing indicator
            chatTyping.style.display = "flex";
            scrollToBottom();
            
            const contextPath = "${pageContext.request.contextPath}";
            const payload = {
                sessionId: Number(currentSessionId),
                message: text
            };
            
            fetch(contextPath + "/ai-support/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-Token": "${csrfToken}"
                },
                body: JSON.stringify(payload)
            })
            .then(res => {
                if (res.status === 429) {
                    // Rate limited
                    return { answer: "Bạn đã gửi quá nhiều tin nhắn liên tiếp. Vui lòng chờ 1 phút trước khi thử lại." };
                }
                if (!res.ok) {
                    throw new Error("HTTP error " + res.status);
                }
                return res.json();
            })
            .then(data => {
                chatTyping.style.display = "none";
                
                if (data.sessionId) {
                    currentSessionId = data.sessionId;
                    localStorage.setItem("petshop_ai_session_id", data.sessionId);
                }
                
                appendMessageBubble("AI", data.answer, data.relatedProducts, data.relatedOrder);
                scrollToBottom();
            })
            .catch(err => {
                chatTyping.style.display = "none";
                appendMessageBubble("AI", "Xin lỗi, hiện tại hệ thống AI đang bận xử lý. Vui lòng thử lại sau giây lát hoặc liên hệ admin.");
                scrollToBottom();
            });
        }
        
        function appendMessageBubble(sender, text, products, order) {
            const msgDiv = document.createElement("div");
            msgDiv.className = "chat-message " + (sender === "USER" ? "user" : "bot");
            
            // Avatar for bot
            if (sender !== "USER") {
                const avatar = document.createElement("div");
                avatar.className = "message-avatar";
                avatar.innerHTML = "<i class='bx bxs-bot'></i>";
                msgDiv.appendChild(avatar);
            }
            
            const contentWrapper = document.createElement("div");
            contentWrapper.className = "message-content-wrapper";
            
            const content = document.createElement("div");
            content.className = "message-content";
            content.innerText = text;
            contentWrapper.appendChild(content);
            
            // Append Products Cards
            if (products && products.length > 0) {
                const pContainer = document.createElement("div");
                pContainer.className = "product-cards-container";
                
                products.forEach(p => {
                    const link = document.createElement("a");
                    link.className = "chat-product-card";
                    link.href = "${pageContext.request.contextPath}/product-detail?id=" + p.id;
                    
                    const img = document.createElement("img");
                    img.src = p.image.startsWith("http") ? p.image : "${pageContext.request.contextPath}/assets/images/shop_pic/" + p.image;
                    link.appendChild(img);
                    
                    const info = document.createElement("div");
                    info.className = "product-card-info";
                    
                    const name = document.createElement("div");
                    name.className = "product-card-name";
                    name.innerText = p.name;
                    info.appendChild(name);
                    
                    const prices = document.createElement("div");
                    prices.className = "product-card-prices";
                    
                    if (p.discount > 0) {
                        const orig = document.createElement("span");
                        orig.className = "product-card-original";
                        orig.innerText = formatCurrency(p.price);
                        prices.appendChild(orig);
                    }
                    
                    const finalPrice = document.createElement("span");
                    finalPrice.className = "product-card-final";
                    // calculate discount
                    const discounted = p.price * (1 - p.discount/100.0);
                    finalPrice.innerText = formatCurrency(discounted);
                    prices.appendChild(finalPrice);
                    
                    info.appendChild(prices);
                    link.appendChild(info);
                    pContainer.appendChild(link);
                });
                contentWrapper.appendChild(pContainer);
            }
            
            // Append Order Card
            if (order) {
                const oCard = document.createElement("div");
                oCard.className = "chat-order-card";
                
                const oHeader = document.createElement("div");
                oHeader.className = "chat-order-card-header";
                
                const code = document.createElement("span");
                code.innerText = "Đơn hàng #" + order.id;
                oHeader.appendChild(code);
                
                const statusBadge = document.createElement("span");
                statusBadge.className = "order-status-badge " + getOrderStatusClass(order.status);
                statusBadge.innerText = getOrderStatusLabel(order.status);
                oHeader.appendChild(statusBadge);
                
                oCard.appendChild(oHeader);
                
                const oBody = document.createElement("div");
                oBody.innerHTML = "<div><strong>Ngày đặt:</strong> " + formatDate(order.createdAt) + "</div>" +
                                  "<div><strong>Người nhận:</strong> " + order.recipientFullname + "</div>" +
                                  "<div><strong>Tổng tiền:</strong> <strong class='text-danger'>" + formatCurrency(order.totalAmount) + "</strong></div>" +
                                  "<div class='mt-1'><a href='${pageContext.request.contextPath}/my-orders?action=view&id=" + order.id + "' class='btn btn-sm btn-primary py-0 px-2' style='font-size:10px;'>Xem chi tiết</a></div>";
                oCard.appendChild(oBody);
                contentWrapper.appendChild(oCard);
            }
            
            msgDiv.appendChild(contentWrapper);
            chatBody.appendChild(msgDiv);
        }
        
        window.sendQuickQuestion = function (text) {
            chatInput.value = text;
            sendMessage();
        };
        
        function scrollToBottom() {
            chatBody.scrollTop = chatBody.scrollHeight;
        }
        
        function formatCurrency(val) {
            return Number(val).toLocaleString('vi-VN') + "đ";
        }
        
        function formatDate(ts) {
            if (!ts) return "";
            // ts could be string or timestamp object
            const date = new Date(ts);
            return date.toLocaleDateString("vi-VN") + " " + date.toLocaleTimeString("vi-VN", {hour: '2-digit', minute:'2-digit'});
        }
        
        function getOrderStatusClass(status) {
            if (status === "Pending") return "badge-soft warning";
            if (status === "Confirmed") return "badge-soft info";
            if (status === "Paid") return "badge-soft success";
            if (status === "Shipping") return "badge-soft info";
            if (status === "Delivered") return "badge-soft success";
            if (status === "Completed") return "badge-soft success";
            if (status === "Cancelled") return "badge-soft danger";
            return "badge-soft info";
        }
        
        function getOrderStatusLabel(status) {
            if (status === "Awaiting Payment") return "Chờ thanh toán";
            if (status === "Pending") return "Chờ xử lý";
            if (status === "Confirmed") return "Đã xác nhận";
            if (status === "Paid") return "Đã thanh toán";
            if (status === "Shipping") return "Đang giao";
            if (status === "Delivered") return "Đã giao hàng";
            if (status === "Completed") return "Hoàn thành";
            if (status === "Cancelled") return "Đã hủy";
            return status;
        }
        
        function logError(msg) {
            console.error("[AI Chatbot] " + msg);
        }
        
        function updateAiChatUnreadBadge() {
            const tempSessionId = localStorage.getItem("petshop_ai_session_id") || currentSessionId;
            if (!tempSessionId || tempSessionId <= 0) {
                document.getElementById("ai-chat-unread-badge").style.display = "none";
                return;
            }
            if (chatWindow.style.display === "flex") {
                document.getElementById("ai-chat-unread-badge").style.display = "none";
                return;
            }
            const contextPath = "${pageContext.request.contextPath}";
            fetch(contextPath + "/ai-support/unread-count?sessionId=" + tempSessionId)
                .then(res => res.json())
                .then(data => {
                    const badge = document.getElementById("ai-chat-unread-badge");
                    if (data && data.unreadCount > 0) {
                        badge.innerText = data.unreadCount;
                        badge.style.display = "flex";
                    } else {
                        badge.style.display = "none";
                    }
                })
                .catch(err => console.error("Error fetching AI unread count:", err));
        }
        
        // Initial badge count check and periodic polling
        updateAiChatUnreadBadge();
        setInterval(updateAiChatUnreadBadge, 10000);
    });
</script>

<style>
    .footer-section {
        background-color: #0b1a33;
        color: #fff;
        padding-top: 60px;
        font-size: 14px;
        position: relative;
        z-index: 10;
    }

    .subscribe-form .form-control {
        border-radius: 8px;
        border: none;
        padding: 10px 15px;
    }

    .subscribe-form .btn {
        border-radius: 8px;
        background-color: #00bfa5;
        border: none;
        padding: 10px 15px;
        color: white;
    }

    .social-icons a {
        color: #fff;
        font-size: 24px;
        margin-right: 15px;
        text-decoration: none;
        transition: color 0.2s;
    }
    .social-icons a:hover { color: #00bfa5; }

    .footer-links li {
        margin-bottom: 12px;
    }

    .footer-links a {
        color: #94a3b8;
        text-decoration: none;
        transition: all 0.2s;
    }

    .footer-links a:hover {
        color: #00bfa5;
        padding-left: 5px;
    }

    .contact-info li {
        line-height: 1.6;
    }

    .footer-bottom {
        border-top: 1px solid rgba(255,255,255,0.1);
        background-color: #081426;
        margin-top: 40px;
    }
</style>
