<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Admin Chat - PetShop</title>
    <jsp:include page="/components/admin-styles.jsp"/>
    <style>
        .chat-container { display: flex; height: calc(100vh - 120px); gap: 16px; }
        .user-list { width: 280px; background: #fff; border-radius: 12px;
                     overflow-y: auto; border: 1px solid #e2e8f0; }
        .user-item { padding: 14px 16px; border-bottom: 1px solid #f1f5f9;
                     cursor: pointer; transition: background 0.2s; }
        .user-item:hover, .user-item.active { background: #eff6ff; }
        .user-name { font-weight: 700; font-size: 0.95rem; }
        .user-last-msg { font-size: 0.8rem; color: #64748b;
                         white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .chat-box { flex: 1; background: #fff; border-radius: 12px;
                    border: 1px solid #e2e8f0; display: flex; flex-direction: column; }
        .chat-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0;
                       font-weight: 700; background: #1e40af; color: white;
                       border-radius: 12px 12px 0 0; }
        .chat-messages { flex: 1; overflow-y: auto; padding: 16px;
                         background: #f8fafc; display: flex; flex-direction: column; gap: 8px; }
        .msg-bubble { max-width: 70%; padding: 10px 14px;
                      border-radius: 16px; font-size: 0.92rem; line-height: 1.5; }
        .msg-admin { background: #1e40af; color: white;
                     align-self: flex-end; border-radius: 16px 16px 4px 16px; }
        .msg-user  { background: #e2e8f0; color: #0f172a;
                     align-self: flex-start; border-radius: 16px 16px 16px 4px; }
        .chat-input { padding: 14px; border-top: 1px solid #e2e8f0;
                      display: flex; gap: 10px; }
        .empty-chat { display: flex; align-items: center; justify-content: center;
                      flex: 1; color: #94a3b8; font-size: 1rem; }
    </style>
</head>
<body class="admin-page">
<jsp:include page="/components/admin-sidebar.jsp">
    <jsp:param name="currentPage" value="chat"/>
</jsp:include>

<div class="admin-main">
    <h4 class="mb-4"><i class='bx bx-chat'></i> Hỗ Trợ Khách Hàng</h4>

    <div class="chat-container">
        <%-- Danh sách user --%>
        <div class="user-list">
            <div class="p-3 fw-bold border-bottom text-primary">
                <i class='bx bx-group'></i> Khách hàng
            </div>
            <c:choose>
                <c:when test="${empty userList}">
                    <div class="p-3 text-muted text-center small">Chưa có tin nhắn nào</div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="u" items="${userList}">
                        <a href="${pageContext.request.contextPath}/admin/chat?userId=${u.id}"
                           class="text-decoration-none">
                            <div class="user-item ${selectedUserId == u.id ? 'active' : ''}">
                                <div class="user-name">${u.fullname}</div>
                                <div class="user-last-msg">${u.lastMessage}</div>
                            </div>
                        </a>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- Khung chat --%>
        <div class="chat-box">
            <c:choose>
                <c:when test="${empty selectedUserId}">
                    <div class="empty-chat">
                        <div class="text-center">
                            <i class='bx bx-conversation' style="font-size:3rem; color:#cbd5e1"></i>
                            <p class="mt-2">Chọn khách hàng để xem tin nhắn</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="chat-header">
                        <i class='bx bx-user'></i> Đang chat với khách #${selectedUserId}
                    </div>
                    <div class="chat-messages" id="chatMessages">
                        <c:forEach var="msg" items="${history}">
                            <div class="msg-bubble ${msg.isAdmin ? 'msg-admin' : 'msg-user'}">
                                ${msg.message}
                            </div>
                        </c:forEach>
                    </div>
                    <form class="chat-input"
                          action="${pageContext.request.contextPath}/admin/chat" method="post">
                        <input type="hidden" name="receiverId" value="${selectedUserId}">
                        <input type="text" name="message" class="form-control"
                               placeholder="Nhập tin nhắn..." required
                               onkeypress="if(event.key==='Enter'){this.form.submit();}">
                        <button type="submit" class="btn btn-primary px-4">
                            <i class='bx bx-send'></i>
                        </button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
    // Scroll xuống cuối khi load
    var chatMessages = document.getElementById("chatMessages");
    if (chatMessages) chatMessages.scrollTop = chatMessages.scrollHeight;
</script>
</body>
</html>