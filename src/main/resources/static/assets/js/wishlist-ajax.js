(function () {
    function findWishlistForms() {
        return Array.prototype.slice.call(document.querySelectorAll('form[action*="/toggle-wishlist"]'));
    }

    function getProductId(form) {
        var input = form.querySelector('input[name="productId"]');
        var button = form.querySelector('button[type="submit"]');
        return (input && input.value ? input.value : form.dataset.productId || (button ? button.dataset.productId : '') || '').trim();
    }

    function setButtonLoading(button, loading) {
        if (!button) {
            return;
        }

        button.disabled = loading;
        button.classList.toggle('opacity-75', loading);
    }

    function setHeartIcon(icon, wishlisted) {
        if (!icon) {
            return;
        }

        icon.classList.toggle('bxs-heart', wishlisted);
        icon.classList.toggle('bx-heart', !wishlisted);
    }

    function updateWishlistButton(button, wishlisted) {
        if (!button) {
            return;
        }

        var icon = button.querySelector('i');
        var label = wishlisted ? 'Đã yêu thích' : 'Thêm vào yêu thích';

        if (button.classList.contains('btn-wishlist')) {
            button.classList.toggle('active', wishlisted);
            button.title = wishlisted ? 'Đã yêu thích' : 'Yêu thích';
            setHeartIcon(icon, wishlisted);
            return;
        }

        if (button.classList.contains('wishlist-btn')) {
            button.classList.toggle('btn-danger', wishlisted);
            button.classList.toggle('btn-outline-danger', !wishlisted);
            button.title = wishlisted ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích';
            button.innerHTML = "<i class='bx " + (wishlisted ? 'bxs-heart' : 'bx-heart') + " fs-4'></i> " + label;
        }
    }

    function updateMatchingForms(productId, wishlisted) {
        findWishlistForms().forEach(function (form) {
            if (getProductId(form) !== productId) {
                return;
            }

            updateWishlistButton(form.querySelector('button[type="submit"]'), wishlisted);
        });
    }

    function removeWishlistCard(form) {
        var cardColumn = form.closest('.col-12, .col, [class*="col-"]');
        if (!cardColumn) {
            return;
        }

        cardColumn.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
        cardColumn.style.opacity = '0';
        cardColumn.style.transform = 'translateY(-6px)';
        setTimeout(function () {
            cardColumn.remove();
        }, 220);
    }

    function ensureToastContainer() {
        var container = document.getElementById('wishlistAjaxToastContainer');
        if (container) {
            return container;
        }

        container = document.createElement('div');
        container.id = 'wishlistAjaxToastContainer';
        container.className = 'position-fixed top-0 end-0 p-3';
        container.style.zIndex = '105000';
        document.body.appendChild(container);
        return container;
    }

    function showWishlistToast(message, type) {
        var container = ensureToastContainer();
        var alert = document.createElement('div');
        var variant = type === 'error' ? 'danger' : 'success';
        alert.className = 'alert alert-' + variant + ' shadow-sm d-flex align-items-center gap-2 mb-2';
        alert.setAttribute('role', 'alert');
        alert.style.borderRadius = '14px';
        alert.style.minWidth = '280px';

        var icon = document.createElement('i');
        icon.className = type === 'error' ? 'bx bx-error-circle fs-5' : 'bx bx-check-circle fs-5';

        var text = document.createElement('span');
        text.textContent = message || (type === 'error' ? 'Không thể cập nhật yêu thích.' : 'Đã cập nhật yêu thích.');

        alert.appendChild(icon);
        alert.appendChild(text);
        container.appendChild(alert);

        setTimeout(function () {
            alert.style.transition = 'opacity 0.25s ease, transform 0.25s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateX(12px)';
            setTimeout(function () {
                alert.remove();
            }, 260);
        }, 2500);
    }

    function shouldRemoveFromWishlistPage(form, wishlisted) {
        var redirectInput = form.querySelector('input[name="redirect"]');
        var redirect = redirectInput ? redirectInput.value : '';
        var removeIcon = form.querySelector('.bx-trash');
        return !wishlisted && removeIcon && redirect.indexOf('/wishlist') !== -1;
    }

    function getCsrfToken(form) {
        var input = form.querySelector('input[name="csrfToken"]');
        return input ? input.value : '';
    }

    function serializeForm(form) {
        var params = new URLSearchParams();

        Array.prototype.slice.call(form.elements).forEach(function (field) {
            if (!field.name || field.disabled) {
                return;
            }

            if ((field.type === 'checkbox' || field.type === 'radio') && !field.checked) {
                return;
            }

            params.append(field.name, field.value);
        });

        var productId = getProductId(form);
        if (productId) {
            params.set('productId', productId);
        }

        return params.toString();
    }

    function parseWishlistResponse(response) {
        var contentType = response.headers.get('content-type') || '';

        if (response.redirected && response.url && response.url.indexOf('/login') !== -1) {
            return Promise.resolve({
                ok: false,
                status: 401,
                data: {
                    loginUrl: response.url
                }
            });
        }

        if (contentType.indexOf('application/json') !== -1) {
            return response.json().then(function (data) {
                return {
                    ok: response.ok,
                    status: response.status,
                    data: data
                };
            });
        }

        return response.text().then(function () {
            return {
                ok: false,
                status: response.status,
                data: {
                    message: response.status === 403
                        ? 'Phiên làm việc đã hết hạn. Vui lòng tải lại trang.'
                        : 'Máy chủ chưa trả về dữ liệu hợp lệ. Vui lòng thử lại.'
                }
            };
        });
    }

    function handleWishlistSubmit(event) {
        var form = event.currentTarget;
        var button = form.querySelector('button[type="submit"]');
        var productId = getProductId(form);

        event.preventDefault();
        setButtonLoading(button, true);

        fetch(form.action, {
            method: 'POST',
            body: serializeForm(form),
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'X-CSRF-Token': getCsrfToken(form),
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(parseWishlistResponse)
            .then(function (result) {
                if (result.status === 401 && result.data.loginUrl) {
                    window.location.href = result.data.loginUrl;
                    return;
                }

                if (!result.ok || !result.data.success) {
                    throw new Error(result.data.message || 'Không thể cập nhật yêu thích.');
                }

                updateMatchingForms(productId, result.data.wishlisted);

                if (shouldRemoveFromWishlistPage(form, result.data.wishlisted)) {
                    removeWishlistCard(form);
                }

                showWishlistToast(result.data.message, 'success');
            })
            .catch(function (error) {
                showWishlistToast(error.message, 'error');
            })
            .finally(function () {
                setButtonLoading(button, false);
            });
    }

    document.addEventListener('DOMContentLoaded', function () {
        findWishlistForms().forEach(function (form) {
            form.addEventListener('submit', handleWishlistSubmit);
        });
    });
})();
