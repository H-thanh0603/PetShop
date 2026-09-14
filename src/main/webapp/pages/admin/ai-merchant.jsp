<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>AI Merchant - PetShop Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .mcht-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        @media (max-width: 1100px) { .mcht-grid { grid-template-columns: 1fr; } }
        .mcht-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; }
        .mcht-log { height: 320px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; background: #f8fafc; margin-bottom: 10px; }
        .mcht-msg { margin-bottom: 10px; padding: 8px 12px; border-radius: 10px; max-width: 90%; }
        .mcht-msg.op { background: #0b1a33; color: #fff; margin-left: auto; }
        .mcht-msg.ai { background: #fff; border: 1px solid #e2e8f0; }
        .chg { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; margin-bottom: 10px; }
        .chg .kind { font-weight: 700; color: #0b1a33; }
        .chg table { width: 100%; font-size: 0.85rem; margin: 8px 0; border-collapse: collapse; }
        .chg td, .chg th { border: 1px solid #e2e8f0; padding: 4px 8px; text-align: left; }
        .btn-admin { padding: 8px 16px; font-size: 0.85rem; font-weight: 600; border-radius: 8px; border: none; cursor: pointer; margin-right: 6px; }
        .btn-admin.primary { background: #0b1a33; color: white; }
        .btn-admin.ok { background: #00bfa5; color: white; }
        .btn-admin.warn { background: #ea580c; color: white; }
        .btn-admin.secondary { background: #e2e8f0; color: #334155; }
        .mcht-input { display: flex; gap: 8px; }
        .mcht-input input { flex: 1; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 10px; }
        pre.digest { white-space: pre-wrap; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; max-height: 300px; overflow-y: auto; }
    </style>
</head>
<body class="admin-page">
<jsp:include page="/components/admin-sidebar.jsp">
    <jsp:param name="currentPage" value="ai-merchant"/>
</jsp:include>

<main class="admin-main">
    <div class="page-header-admin">
        <div>
            <h1 class="page-title"><i class='bx bxs-store'></i> Trợ lý AI Merchant</h1>
            <p class="page-subtitle">Phân tích kinh doanh, stage thay đổi, duyệt &amp; áp dụng. Mọi thay đổi chỉ áp dụng sau khi duyệt tại đây.</p>
        </div>
        <jsp:include page="/components/admin-header-dropdown.jsp" />
    </div>

    <div class="mcht-grid">
        <div class="mcht-card">
            <h5 class="fw-bold mb-3">Trò chuyện merchant</h5>
            <div class="mcht-log" id="mcht-log"></div>
            <div class="mcht-input">
                <input id="mcht-text" placeholder="VD: doanh thu tháng này? / stage giảm giá 10% cho sản phẩm 5..." onkeydown="if(event.key==='Enter')mchtSend()" />
                <button class="btn-admin primary" onclick="mchtSend()">Gửi</button>
            </div>
            <small class="text-muted">Chạy trên provider đã cấu hình (<code>AI_PROVIDER</code>/<code>AI_MODEL</code>). Chat duyệt không áp dụng gì — phải bấm Duyệt &amp; Áp dụng.</small>
        </div>
        <div class="mcht-card">
            <h5 class="fw-bold mb-3">Thay đổi đang chờ duyệt <button class="btn-admin secondary" onclick="loadPending()">Tải lại</button></h5>
            <div id="pending-list"><p class="text-muted">Nhấn "Tải lại" để xem hàng đợi.</p></div>
        </div>
    </div>
    <div class="mcht-card mt-3">
        <h5 class="fw-bold mb-3">Digest buổi sáng <button class="btn-admin secondary" onclick="loadDigest()">Tạo digest</button></h5>
        <pre class="digest" id="digest">Chưa có digest.</pre>
    </div>
</main>

<script>
const CTX = '<c:out value="${pageContext.request.contextPath}"/>';
function esc(s){return String(s==null?'':s).replace(/&/g,'&amp;').replace(/</g,'&lt;');}
function addMsg(cls, text){const l=document.getElementById('mcht-log');const d=document.createElement('div');d.className='mcht-msg '+cls;d.textContent=text;l.appendChild(d);l.scrollTop=l.scrollHeight;}
async function post(url, body){
    const r = await fetch(CTX + url, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body||{})});
    return r.json();
}
async function mchtSend(){
    const inp = document.getElementById('mcht-text');
    const text = inp.value.trim(); if(!text) return;
    inp.value=''; addMsg('op', text); addMsg('ai', 'Đang xử lý...');
    try {
        const res = await post('/admin/ai-merchant/chat', {message: text});
        document.querySelectorAll('#mcht-log .mcht-msg.ai:last-child');
        const log = document.getElementById('mcht-log');
        log.lastChild.textContent = res.answer || JSON.stringify(res);
        log.scrollTop = log.scrollHeight;
        if (res.cards && res.cards.length) loadPending();
    } catch(e){ addMsg('ai', 'Lỗi kết nối.'); }
}
async function loadPending(){
    const r = await fetch(CTX + '/admin/ai-merchant/pending');
    const list = await r.json();
    const box = document.getElementById('pending-list');
    if(!list.length){ box.innerHTML = '<p class="text-muted">Không có thay đổi nào đang chờ.</p>'; return; }
    box.innerHTML = '';
    list.forEach(c => {
        const d = document.createElement('div'); d.className='chg';
        let rows = (c.items||[]).map(i=>'<tr><td>'+esc(i.target)+'</td><td>'+esc(i.field)+'</td><td>'+esc(i.before)+'</td><td>'+esc(i.after)+'</td></tr>').join('');
        d.innerHTML = '<div class="kind">'+esc(c.kind)+' — '+esc(c.changeId)+'</div><div>'+esc(c.summary)+'</div>'
            + '<table><tr><th>Mục tiêu</th><th>Trường</th><th>Trước</th><th>Sau</th></tr>'+rows+'</table>'
            + '<div><button class="btn-admin ok" data-a="approve">Duyệt</button>'
            + '<button class="btn-admin primary" data-a="apply">Duyệt &amp; Áp dụng</button>'
            + '<button class="btn-admin warn" data-a="discard">Hủy</button></div>';
        d.querySelectorAll('button').forEach(b => b.onclick = async () => {
            const a = b.getAttribute('data-a');
            if (a === 'apply') { await post('/admin/ai-merchant/approve', {changeId: c.changeId}); }
            const res = await post('/admin/ai-merchant/' + a, {changeId: c.changeId});
            alert(res.error ? ('Từ chối: ' + res.error) : 'Xong: ' + a + ' ' + c.changeId);
            loadPending();
        });
        box.appendChild(d);
    });
}
async function loadDigest(){
    const r = await fetch(CTX + '/admin/ai-merchant/digest');
    const j = await r.json();
    document.getElementById('digest').textContent = j.digest || JSON.stringify(j);
}
</script>
</body>
</html>
