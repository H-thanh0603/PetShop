<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Quản Lý Đổi Trả Hoàn Tiền</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="container-fluid mt-4">
    <div class="card shadow">
        <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Danh Sách Yêu Cầu Đổi Trả / Hoàn Tiền (Petshop)</h5>
            <span class="badge bg-secondary">Hệ thống Admin</span>
        </div>
        <div class="card-body">

            <c:if test="${empty returnList}">
                <div class="alert alert-info text-center">Hiện tại chưa có yêu cầu đổi trả hoặc hoàn tiền nào từ khách hàng.</div>
            </c:if>

            <c:if test="${not empty returnList}">
                <div class="table-responsive">
                    <table class="table table-bordered table-hover align-middle">
                        <thead class="table-secondary text-center">
                            <tr>
                                <th>Mã Yêu Cầu</th>
                                <th>Mã Đơn Hàng</th>
                                <th>Mã Khách Hàng</th>
                                <th>Lý Do Đổi Trả</th>
                                <th>Số Tiền Hoàn trả</th>
                                <th>Ngày Gửi</th>
                                <th>Trạng Thái</th>
                                <th style="width: 250px;">Hành Động (Xử Lý)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${returnList}">
                                <tr>
                                    <td class="text-center">#${item.id}</td>
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
                                            <c:when test="${item.status == 'PENDING'}">
                                                <span class="badge bg-warning text-dark">Chờ duyệt</span>
                                            </c:when>
                                            <c:when test="${item.status == 'APPROVED'}">
                                                <span class="badge bg-success">Đã chấp nhận</span>
                                            </c:when>
                                            <c:when test="${item.status == 'REJECTED'}">
                                                <span class="badge bg-danger">Đã từ chối</span>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${item.status == 'PENDING'}">
                                            <form action="order-returns" method="post" class="d-inline">
                                                <input type="hidden" name="returnId" value="${item.id}">
                                                <input type="hidden" name="orderId" value="${item.orderId}">

                                                <div class="mb-2">
                                                    <input type="text" name="adminComment" class="form-control form-control-sm"
                                                           placeholder="Ghi chú / Lý do từ chối...">
                                                </div>

                                                <div class="d-flex justify-content-between">
                                                    <button type="submit" name="action" value="APPROVE" class="btn btn-success btn-sm w-48">
                                                        ✓ Đồng ý
                                                    </button>
                                                    <button type="submit" name="action" value="REJECT" class="btn btn-danger btn-sm w-48">
                                                        ✗ Từ chối
                                                    </button>
                                                </div>
                                            </form>
                                        </c:if>
                                        <c:if test="${item.status != 'PENDING'}">
                                            <small class="text-muted">Phản hồi: <c:out value="${item.adminComment}" default="(Không có)"/></small>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>

        </div>
    </div>
</div>
</body>
</html>