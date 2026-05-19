<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${fn:escapeXml(policyTitle)} | PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
    <style>
        .policy-shell {
            max-width: 880px;
            margin: 48px auto;
            padding: 0 16px;
        }
        .policy-heading {
            margin-bottom: 24px;
        }
        .policy-heading h1 {
            font-size: clamp(2rem, 4vw, 3.2rem);
            font-weight: 800;
            color: #0f172a;
        }
        .policy-heading p {
            max-width: 68ch;
            color: #475569;
            font-size: 1.05rem;
        }
        .policy-list {
            display: grid;
            gap: 14px;
            padding: 0;
            margin: 0;
            list-style: none;
        }
        .policy-list li {
            padding: 16px 18px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            background: #ffffff;
            color: #1e293b;
            line-height: 1.65;
        }
    </style>
</head>
<body>
<jsp:include page="/components/navbar.jsp"/>
<main class="policy-shell">
    <div class="policy-heading">
        <h1>${fn:escapeXml(policyTitle)}</h1>
        <p>${fn:escapeXml(policyLead)}</p>
    </div>
    <ul class="policy-list">
        <c:forEach var="section" items="${policySections}">
            <li>${fn:escapeXml(section)}</li>
        </c:forEach>
    </ul>
</main>
<jsp:include page="/components/footer.jsp"/>
</body>
</html>
