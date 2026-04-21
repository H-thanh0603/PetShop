<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quản lý Review - Admin</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        /* Stats Grid Override */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
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
        .stat-card.orange { background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); }

        /* Star rating styles */
        .star-rating {
            display: inline-flex;
            gap: 2px;
        }
        .star-rating i {
            font-size: 1rem;
        }
        .star-rating i.bxs-star {
            color: #f59e0b;
        }
        .star-rating i.bx-star {
            color: #cbd5e1;
        }

        /* Comment cell */
        .comment-text {
            max-width: 250px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    </style>
</head>
<body>

    <jsp:include page="/components/admin-sidebar.jsp">
        <jsp:param name="currentPage" value="reviews"/>
    </jsp:include>

    <main class="admin-main">
        <div class="page-header">
            <h1 class="page-title"><i class='bx bxs-star-half'></i> Quản lý Review</h1>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>

        <!-- Stats Cards -->
        <div class="stats-grid">
            <div class="stat-card blue">
                <h3><i class='bx bx-message-square-detail'></i> ${totalReviews}</h3>
                <p>Tổng đánh giá</p>
            </div>
            <div class="stat-card orange">
                <h3><i class='bx bx-error-circle'></i> ${lowRatingCount}</h3>
                <p>Đánh giá thấp (≤ 2 sao)</p>
            </div>
        </div>

        <!-- Filter Section -->
        <div class="filter-section">
            <div class="search-box" style="flex: 0; min-width: auto;">
                <label style="font-weight: 600; font-size: 0.9rem; color: #334155; white-space: nowrap;">Lọc theo rating:</label>
            </div>
            <select class="filter-select" id="filterMaxRating" onchange="applyRatingFilter()">
                <option value="" ${selectedMaxRating == 0 ? 'selected' : ''}>Tất cả</option>
                <option value="1" ${selectedMaxRating == 1 ? 'selected' : ''}>≤ 1 sao</option>
                <option value="2" ${selectedMaxRating == 2 ? 'selected' : ''}>≤ 2 sao</option>
                <option value="3" ${selectedMaxRating == 3 ? 'selected' : ''}>≤ 3 sao</option>
            </select>
        </div>

        <!-- Table Section -->
        <div class="table-section">
            <div class="table-header">
                <span class="table-title">
                    <i class='bx bx-list-ul'></i> Danh sách đánh giá
                    <span style="font-weight: normal; color: #94a3b8; font-size: 0.85rem;">(${totalReviews})</span>
                </span>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 50px;">#</th>
                        <th>Sản phẩm</th>
                        <th>Người đánh giá</th>
                        <th style="width: 130px;">Đánh giá</th>
                        <th>Bình luận</th>
                        <th style="width: 110px;">Ngày</th>
                        <th style="width: 90px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty reviews}">
                        <tr>
                            <td colspan="7">
                                <div class="empty-state">
                                    <i class='bx bx-message-square-detail'></i>
                                    <p>Chưa có đánh giá nào</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                    <c:forEach items="${reviews}" var="r" varStatus="loop">
                        <tr>
                            <td><strong>${loop.index + 1}</strong></td>
                            <td><span style="font-weight: 600; color: #0f172a;">${r.productName}</span></td>
                            <td>${r.userName}</td>
                            <td>
                                <div class="star-rating">
                                    <c:forEach begin="1" end="5" var="i">
                                        <c:choose>
                                            <c:when test="${i <= r.rating}">
                                                <i class='bx bxs-star'></i>
                                            </c:when>
                                            <c:otherwise>
                                                <i class='bx bx-star'></i>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                            </td>
                            <td>
                                <span class="comment-text" title="${r.comment}">
                                    <c:choose>
                                        <c:when test="${not empty r.comment}">
                                            ${r.comment.length() > 80 ? r.comment.substring(0, 80).concat('...') : r.comment}
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #94a3b8; font-style: italic;">Không có bình luận</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td>${r.createdAt}</td>
                            <td>
                                <div class="table-actions">
                                    <button class="action-btn delete" onclick="openDeleteModal(${r.id})" title="Xóa">
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

    <!-- Delete Modal -->
    <div class="modal-overlay" id="deleteModal">
        <div class="modal-box" style="max-width: 420px;">
            <div class="modal-header">
                <h3 class="modal-title">Xóa đánh giá?</h3>
                <button class="modal-close" onclick="closeDeleteModal()"><i class='bx bx-x'></i></button>
            </div>
            <div class="modal-body" style="text-align: center; padding: 30px;">
                <div style="width: 72px; height: 72px; border-radius: 50%; background: #fee2e2; color: #ef4444; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; font-size: 2rem;">
                    <i class='bx bx-trash'></i>
                </div>
                <p style="color: #64748b; font-size: 0.95rem; margin: 0;">Đánh giá sẽ bị xóa vĩnh viễn và không thể khôi phục.</p>
            </div>
            <div class="modal-footer" style="justify-content: center;">
                <button type="button" class="btn btn-secondary" onclick="closeDeleteModal()">
                    <i class='bx bx-x'></i> Hủy bỏ
                </button>
                <form method="post" style="display: inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="reviewId" id="deleteReviewId">
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
        var contextPath = '${pageContext.request.contextPath}';

        function applyRatingFilter() {
            var val = document.getElementById('filterMaxRating').value;
            window.location.href = contextPath + '/pages/admin/reviews' + (val ? '?maxRating=' + val : '');
        }

        function openDeleteModal(reviewId) {
            document.getElementById('deleteReviewId').value = reviewId;
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
                closeDeleteModal();
            }
        });
    </script>
</body>
</html>
