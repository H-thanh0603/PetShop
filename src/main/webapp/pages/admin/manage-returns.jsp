<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Quản Lý Đổi Trả - Admin PetShop</title>
    <jsp:include page="/components/admin-styles.jsp"/>
</head>
<body class="admin-page">
<jsp:include page="/components/admin-sidebar.jsp">
    <jsp:param name="currentPage" value="order-returns"/>
</jsp:include>

<div class="admin-main">
    <h4 class="mb-4"><i class='bx bx-undo'></i> Quản Lý Yêu Cầu Đổi Trả / Hoàn Tiền</h4>

    <c:if test="${param.msg == 'success'}">
        <div class="alert alert-success">Xử lý yêu cầu thành công!</div>
    </c:if>
    <c:if test="${param.msg == 'fail'}">
        <div class="alert alert-danger">Có lỗi xảy ra, vui lòng thử lại!</div>
    </c:if>

    <div class="card shadow">
        <div class="card-body">
            <c:choose>
                <c:when test="${empty returnList}">
                    <div class="alert alert-info text-center">Chưa có yêu cầu đổi trả nào.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-bordered table-hover align-middle">
                            <thead class="table-dark text-center">
                                <tr>
                                    <th>#</th>
                                    <th>Đơn hàng</th>
                                    <th>Khách hàng</th>
                                    <th>Lý do</th>
                                    <th>Số tiền hoàn</th>
                                    <th>Ngày gửi</th>
                                    <th>Trạng thái</th>
                                    <th>Xử lý</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${returnList}">
                                    <tr>
                                        <td class="text-center">${item.id}</td>
                                        <td class="text-center"><strong>#${item.orderId}</strong></td>
                                        <td class="text-center">#${item.userId}</td>
                                        <td><c:out value="${item.reason}"/></td>
                                        <td class="text-end text-danger fw-bold">
                                            <fmt:formatNumber value="${item.refundAmount}" type="number"/> VNĐ
                                        </td>
                                        <td class="text-center">
                                            <fmt:formatDate value="${item.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td class="text-center">
                                            <c:choose>
                                                <c:when test="${item.status == 'Pending'}">
                                                    <span class="badge bg-warning text-dark">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${item.status == 'Approved'}">
                                                    <span class="badge bg-success">Đã chấp nhận</span>
                                                </c:when>
                                                <c:when test="${item.status == 'Rejected'}">
                                                    <span class="badge bg-danger">Đã từ chối</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${item.status == 'Pending'}">
                                                <form action="${pageContext.request.contextPath}/admin/order-returns"
                                                      method="post">
                                                    <input type="hidden" name="returnId" value="${item.id}">
                                                    <input type="hidden" name="orderId" value="${item.orderId}">
                                                    <div class="mb-2">
                                                        <input type="text" name="adminComment"
                                                               class="form-control form-control-sm"
                                                               placeholder="Ghi chú...">
                                                    </div>
                                                    <div class="d-flex gap-2">
                                                        <button type="submit" name="action" value="APPROVE"
                                                                class="btn btn-success btn-sm flex-fill">
                                                            ✓ Đồng ý
                                                        </button>
                                                        <button type="submit" name="action" value="REJECT"
                                                                class="btn btn-danger btn-sm flex-fill">
                                                            ✗ Từ chối
                                                        </button>
                                                    </div>
                                                </form>
                                            </c:if>
                                            <c:if test="${item.status != 'Pending'}">
                                                <small class="text-muted">
                                                    <c:out value="${item.adminComment}" default="(Không có ghi chú)"/>
                                                </small>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
</body>
</html>