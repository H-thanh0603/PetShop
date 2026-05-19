<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="totalAmount" value="0" />
<%-- Chỉ tính giỏ hàng khi user đã đăng nhập --%>
<c:if test="${not empty sessionScope.user and not empty sessionScope.cart}">
    <c:forEach items="${sessionScope.cart}" var="entry">
        <c:set var="totalAmount" value="${totalAmount + entry.value.totalPrice}" />
    </c:forEach>
</c:if>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Giỏ Hàng - PetShop</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    
    <style>
        body { background-color: #f8f9fa; font-family: 'Segoe UI', sans-serif; }
        .cart-hero { background: linear-gradient(135deg, #0b1a33, #1d4f7a); color: #fff; border-radius: 20px; padding: 26px 28px; margin-bottom: 24px; box-shadow: 0 16px 30px rgba(11,26,51,.14); }
        .cart-hero p { margin: 8px 0 0; opacity: .82; }
        .cart-tip { background: #eef6ff; border: 1px solid #cfe2ff; border-radius: 14px; padding: 14px 16px; color: #1e3a5f; font-size: .95rem; margin-bottom: 18px; }
        .cart-title { color: #10314d; font-weight: 700; text-transform: uppercase; font-size: 1.8rem; margin-bottom: 30px; border-bottom: 2px solid #e0e0e0; padding-bottom: 15px; }
        .table-cart { background: white; border-radius: 10px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); overflow: hidden; width: 100%; border-collapse: separate; border-spacing: 0; }
        .table-cart thead { background-color: #10314d; color: white; }
        .table-cart th { padding: 15px; border: none; text-transform: uppercase; font-size: 0.9rem; }
        .table-cart td { padding: 20px 15px; vertical-align: middle; border-bottom: 1px solid #eee; }
        .cart-product-img { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; border: 1px solid #eee; margin-right: 15px; }
        .qty-input { width: 70px !important; text-align: center; border-radius: 20px; border: 1px solid #ced4da; font-weight: bold; color: #10314d; }
        .btn-remove { color: #dc3545; background: #fff0f1; width: 35px; height: 35px; border-radius: 50%; display: flex; align-items: center; justify-content: center; text-decoration: none; transition: 0.2s; }
        .btn-remove:hover { background: #dc3545; color: white; }
        .cart-summary { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); position: sticky; top: 20px; }
        .summary-row.total { border-top: 2px dashed #eee; padding-top: 15px; margin-top: 15px; font-weight: 800; color: #10314d; font-size: 1.2rem; display: flex; justify-content: space-between; }
        .btn-checkout { background-color: #10314d; color: white; font-weight: 600; padding: 12px; border-radius: 50px; text-transform: uppercase; width: 100%; border: none; transition: 0.3s; }
        .btn-checkout:hover { background-color: #0a2135; color: white; transform: translateY(-2px); }
        .modal-header { background-color: #10314d; color: white; }
        .modal-title { font-weight: 700; }
        .btn-close-white { filter: invert(1) grayscale(100%) brightness(200%); }
        .delete-modal .modal-content { border: none; border-radius: 16px; overflow: hidden; }
        .delete-modal .modal-body { padding: 30px; text-align: center; }
        .delete-icon { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
        .delete-icon i { font-size: 2.5rem; color: #dc3545; }
        .delete-modal h5 { font-weight: 700; color: #10314d; margin-bottom: 10px; }
        .delete-modal p { color: #6c757d; margin-bottom: 25px; }
        .delete-product-name { font-weight: 600; color: #10314d; background: #f8f9fa; padding: 8px 15px; border-radius: 8px; display: inline-block; margin-bottom: 20px; }
        .btn-cancel-delete { background: #f1f5f9; color: #64748b; border: none; padding: 12px 30px; border-radius: 50px; font-weight: 600; transition: 0.3s; }
        .btn-cancel-delete:hover { background: #e2e8f0; color: #475569; }
        .btn-confirm-delete { background: linear-gradient(135deg, #dc3545 0%, #b91c1c 100%); color: white; border: none; padding: 12px 30px; border-radius: 50px; font-weight: 600; transition: 0.3s; }
        .btn-confirm-delete:hover { background: linear-gradient(135deg, #b91c1c 0%, #991b1b 100%); transform: translateY(-2px); box-shadow: 0 4px 15px rgba(220, 53, 69, 0.4); }
    </style>
    <link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
</head>
<body>

    <jsp:include page="/components/navbar.jsp" />
    <jsp:include page="/components/toast.jsp" />

    <div class="container mt-5 mb-5" style="min-height: 600px;">
        <div class="cart-hero">
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                <div>
                    <h2 class="mb-1"><i class='bx bx-cart-alt'></i> Giỏ hàng của bạn</h2>
                    <p>Kiểm tra lại số lượng, tồn kho và tổng tiền trước khi chuyển sang bước thanh toán.</p>
                </div>
                <a href="${pageContext.request.contextPath}/shop" class="btn btn-light fw-semibold">
                    <i class='bx bx-store'></i> Tiếp tục mua sắm
                </a>
            </div>
        </div>
        <div class="cart-tip">
            <i class='bx bx-info-circle'></i> Giá và tồn kho được đồng bộ theo dữ liệu mới nhất để tránh đặt vượt số lượng còn lại.
        </div>

        <%-- Hiển thị thông báo nếu chưa đăng nhập --%>
        <c:if test="${empty sessionScope.user}">
            <div class="text-center py-5 bg-white rounded shadow-sm">
                <img loading="lazy" src="https://cdn-icons-png.flaticon.com/512/6195/6195678.png" width="150" style="opacity: 0.6">
                <h4 class="mt-4 text-muted">Vui lòng đăng nhập để xem giỏ hàng</h4>
                <a href="${pageContext.request.contextPath}/login" class="btn btn-checkout px-5 mt-3" style="width: auto;">
                    <i class='bx bx-log-in'></i> Đăng nhập ngay
                </a>
            </div>
        </c:if>

        <%-- Giỏ hàng trống (đã đăng nhập nhưng không có sản phẩm) --%>
        <c:if test="${not empty sessionScope.user and empty sessionScope.cart}">
            <div class="text-center py-5 bg-white rounded shadow-sm">
                <img loading="lazy" src="https://cdn-icons-png.flaticon.com/512/11329/11329060.png" width="150" style="opacity: 0.6">
                <h4 class="mt-4 text-muted">Giỏ hàng trống</h4>
                <a href="${pageContext.request.contextPath}/shop" class="btn btn-checkout px-5 mt-3" style="width: auto;">Mua sắm ngay</a>
            </div>
        </c:if>
<%--        form chỗ này--%>
        <form action="${pageContext.request.contextPath}/checkout" method="get" id="checkoutForm">
        <%-- Hiển thị giỏ hàng khi đã đăng nhập và có sản phẩm --%>
        <c:if test="${not empty sessionScope.user and not empty sessionScope.cart}">
            <div class="row">
                <div class="col-lg-8 mb-4">
                    <div class="table-responsive">
                        <table class="table table-cart">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th class="text-center">Đơn giá</th>
                                    <th class="text-center">Số lượng</th>
                                    <th class="text-center">Thành tiền</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${sessionScope.cart}" var="entry">
                                    <c:set var="item" value="${entry.value}" />
                                    <tr class="cart-row">
                                        <td>
                                            <div class="d-flex align-items-center">
                                                <img loading="lazy" src="${fn:startsWith(item.product.image, 'http') ? fn:escapeXml(item.product.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(item.product.image)}" 
                                                     class="cart-product-img"
                                                     onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'">
                                                <div>
                                                    <p class="fw-bold mb-0">${fn:escapeXml(item.product.name)}</p>
                                                    <small class="text-muted">ID: ${item.product.id}</small>
                                                    <c:if test="${item.product.stock > 0 and item.product.stock < 10}">
                                                        <div><small class="text-warning fw-semibold">Còn lại: ${item.product.stock} sản phẩm</small></div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="text-center fw-bold text-secondary" data-price="${item.product.price}">
                                            <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                        </td>
                                        <td class="text-center">
                                            <input type="number" min="1" max="${item.product.stock}" value="${item.quantity}" data-product-id="${item.product.id}"
                                                   data-last-valid-quantity="${item.quantity}"
                                                   class="form-control qty-input d-inline-block"
                                                   oninput="updateCart(this)">
                                        </td>
                                        <td class="text-center fw-bold text-primary row-total">
                                            <fmt:formatNumber value="${item.totalPrice}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                        </td>
                                        <td class="text-center">
                                            <button type="button" class="btn-remove" 
                                                    onclick="openDeleteModal(${item.product.id}, '${item.product.name}')">
                                                <i class='bx bx-trash'></i>
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <div class="col-lg-4">
                    <div class="cart-summary">
                        <h5 class="mb-3 fw-bold">Thông tin đơn hàng</h5>
                        <div class="d-flex justify-content-between mb-2">
                            <span>Tạm tính:</span>
                            <span id="cart-subtotal" class="fw-bold">
                                <fmt:formatNumber value="${totalAmount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                            </span>
                        </div>
                        <div class="summary-row total">
                            <span>Tổng cộng:</span>
                            <span id="cart-total" class="text-primary">
                                <fmt:formatNumber value="${totalAmount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                            </span>
                        </div>
                        <p class="small text-muted mt-2 mb-4"><i class='bx bx-check-circle'></i> Đã bao gồm thuế VAT</p>
                        <button type="submit" class="btn btn-checkout">
                            Tiến hành thanh toán <i class='bx bx-right-arrow-alt'></i>
                        </button>
                    </div>
                </div>
            </div>
        </c:if>
<%--        kết form--%>
        </form>
    </div>

    <!-- Modal Xác nhận xóa sản phẩm -->
    <div class="modal fade delete-modal" id="deleteModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-sm">
            <div class="modal-content">
                <div class="modal-body">
                    <div class="delete-icon">
                        <i class='bx bx-trash'></i>
                    </div>
                    <h5>Xóa sản phẩm?</h5>
                    <div class="delete-product-name" id="deleteProductName">Tên sản phẩm</div>
                    <p>Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?</p>
                    <div class="d-flex justify-content-center gap-3">
                        <button type="button" class="btn btn-cancel-delete" data-bs-dismiss="modal">
                            <i class='bx bx-x'></i> Hủy
                        </button>
                        <form action="${pageContext.request.contextPath}/cart" method="post" class="m-0">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="id" id="deleteProductId">
                            <button type="submit" id="confirmDeleteBtn" class="btn btn-confirm-delete">
                                <i class='bx bx-check'></i> Xóa
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        window.isCartUpdating = false;

        function openDeleteModal(productId, productName) {
            document.getElementById('deleteProductName').textContent = productName;
            document.getElementById('deleteProductId').value = productId;
            var deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
            deleteModal.show();
        }

        function formatCurrency(amount) {
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(amount).replace('₫', 'đ');
        }

        function updateCartBadge(totalQuantity) {
            let cartCount = document.getElementById('cart-count');
            if (!cartCount) {
                return;
            }

            if (totalQuantity > 0) {
                cartCount.innerText = totalQuantity;
                cartCount.style.display = '';
            } else {
                cartCount.innerText = '';
                cartCount.style.display = 'none';
            }
        }

        function updateRowTotal(row, quantity) {
            let price = parseFloat(row.querySelector('[data-price]').getAttribute('data-price'));
            row.querySelector('.row-total').innerText = formatCurrency(price * quantity);
        }

        function updateCart(input) {
            const productId = input.dataset.productId;
            const quantity = Number.parseInt(input.value, 10);
            const lastValidQuantity = Number.parseInt(input.dataset.lastValidQuantity || input.defaultValue || '1', 10) || 1;
            const requestedQuantity = Number.isNaN(quantity) ? lastValidQuantity : quantity;

            let row = input.closest('tr');
            let price = parseFloat(row.querySelector('[data-price]').getAttribute('data-price'));
            let newRowTotal = price * requestedQuantity;
            let formattedRowTotal = new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(newRowTotal);

            row.querySelector('.row-total').innerText = formattedRowTotal.replace('₫', 'đ');
            recalculateGrandTotal();

            window.isCartUpdating = true;
            fetch('<%= request.getContextPath() %>/cart', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-Token': '${csrfToken}'
                },
                body: 'action=update'
                    + '&id=' + encodeURIComponent(productId)
                    + '&quantity=' + encodeURIComponent(requestedQuantity)
            })
                .then(response => response.text())
                .then(text => {
                    console.log("Server response:", text);
                    try {
                        const data = JSON.parse(text);
                        if (data.removed) {
                            row.remove();
                            if (!document.querySelector('.cart-row')) {
                                window.location.reload();
                                return;
                            }
                        } else if (data.quantity !== undefined) {
                            input.value = data.quantity;
                            input.dataset.lastValidQuantity = data.quantity;
                            if (data.stock !== undefined && data.stock > 0) {
                                input.max = data.stock;
                            }
                            updateRowTotal(row, data.quantity);
                        } else {
                            input.value = lastValidQuantity;
                            updateRowTotal(row, lastValidQuantity);
                        }

                        recalculateGrandTotal();

                        if (data.totalQuantity !== undefined) {
                            updateCartBadge(data.totalQuantity);
                        }

                        if (!data.success && data.message) {
                            alert(data.message);
                        }
                        window.isCartUpdating = false;
                    } catch (e) {
                        console.error("Response không phải JSON:", text);
                    }
                })
                .catch(error => {
                    console.error('Lỗi update cart:', error);
                    alert('Có lỗi xảy ra khi cập nhật giỏ hàng');
                });
        }

        function recalculateGrandTotal() {
            let grandTotal = 0;
            document.querySelectorAll('.cart-row').forEach(row => {
                let price = parseFloat(row.querySelector('[data-price]').getAttribute('data-price'));
                let quantity = parseInt(row.querySelector('.qty-input').value);
                grandTotal += (price * quantity);
            });
            let formattedGrandTotal = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(grandTotal).replace('₫', 'đ');
            document.getElementById('cart-subtotal').innerText = formattedGrandTotal;
            document.getElementById('cart-total').innerText = formattedGrandTotal;
        }
    </script>
    <script>
        function getRenderedCartState() {
            const items = Array.from(document.querySelectorAll('.qty-input')).map(input => ({
                productId: Number.parseInt(input.dataset.productId, 10),
                quantity: Number.parseInt(input.value, 10) || 0,
                stock: Number.parseInt(input.max || '0', 10) || 0
            })).sort((a, b) => a.productId - b.productId);

            const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0);
            return { items, totalQuantity };
        }

        function hasCartStateChanged(serverState) {
            const currentState = getRenderedCartState();
            const serverItems = (serverState.items || []).map(item => ({
                productId: Number.parseInt(item.productId, 10),
                quantity: Number.parseInt(item.quantity, 10) || 0,
                stock: Number.parseInt(item.stock, 10) || 0
            })).sort((a, b) => a.productId - b.productId);

            if (currentState.totalQuantity !== (serverState.totalQuantity || 0)) {
                return true;
            }

            if (currentState.items.length !== serverItems.length) {
                return true;
            }

            for (let i = 0; i < serverItems.length; i++) {
                const currentItem = currentState.items[i];
                const serverItem = serverItems[i];

                if (!currentItem
                    || currentItem.productId !== serverItem.productId
                    || currentItem.quantity !== serverItem.quantity
                    || currentItem.stock !== serverItem.stock) {
                    return true;
                }
            }

            return false;
        }

        function syncCartStateIfNeeded() {
            if (window.isCartUpdating) {
                return;
            }

            fetch('<%= request.getContextPath() %>/cart?action=state', {
                cache: 'no-store'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success && hasCartStateChanged(data)) {
                        window.location.reload();
                    }
                })
                .catch(error => {
                    console.error('Không đồng bộ được trạng thái giỏ hàng:', error);
                });
        }

        window.addEventListener('focus', syncCartStateIfNeeded);
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden) {
                syncCartStateIfNeeded();
            }
        });
        setInterval(syncCartStateIfNeeded, 10000);
    </script>
</body>
</html>
