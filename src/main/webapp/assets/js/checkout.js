(function () {
    const configEl = document.getElementById("checkoutConfig");
    if (!configEl) {
        return;
    }

    const checkoutConfig = {
        contextPath: configEl.dataset.contextPath || "",
        csrfToken: configEl.dataset.csrfToken || "",
        provincesApiBaseUrl: configEl.dataset.provincesApiBaseUrl || "https://provinces.open-api.vn/api/v1",
        bankId: configEl.dataset.bankId || "VPB",
        bankDisplayName: configEl.dataset.bankDisplayName || "VP Bank",
        bankAccountNumber: configEl.dataset.bankAccountNumber || "0368600557",
        bankAccountName: configEl.dataset.bankAccountName || "NGUYEN HUU THANH",
        bankTransferPrefix: configEl.dataset.bankTransferPrefix || "PETSHOP",
        bankTransferReference: configEl.dataset.bankTransferReference || "",
        bankPaymentTtlSeconds: parseInt(configEl.dataset.bankPaymentTtlSeconds || "600", 10) || 600,
    };

    let provincesLoaded = false;
    let currentTransferReference = checkoutConfig.bankTransferReference;

    document.addEventListener("DOMContentLoaded", function () {
        bindPrimaryAddressDetailValidation();
        bindAddressDetailValidation("addressDetail", "addressDetailError");
        bindLocationSelectors();
        bindCouponNoteSync();
        bindPaymentSelection();
        bindCheckoutSubmit();
        startCheckoutTimer();
    });

    function bindPrimaryAddressDetailValidation() {
        const addressInput = document.getElementById("addressDetail");
        const errorEl = document.getElementById("addressDetailError");
        if (!addressInput || !errorEl || !addressInput.form) {
            return;
        }

        addressInput.addEventListener("input", function () {
            const error = validateAddressDetailInput(addressInput.value);
            errorEl.textContent = error;
            addressInput.classList.toggle("is-invalid", Boolean(error));
        });

        addressInput.form.addEventListener("submit", function (e) {
            const error = validateAddressDetailInput(addressInput.value);
            if (error) {
                e.preventDefault();
                errorEl.textContent = error;
                addressInput.classList.add("is-invalid");
                addressInput.focus();
            }
        });
    }

    function sanitizeAddressDetailValue(value) {
        return value
            .replace(/[^\p{L}0-9\s,./-]/gu, "")
            .replace(/\s+/g, " ");
    }

    function validateAddressDetailInput(value) {
        const input = value.trim().replace(/\s+/g, " ");

        if (!input) {
            return "Vui lòng nhập chi tiết địa chỉ.";
        }
        if (input.length < 5) {
            return "Chi tiết địa chỉ phải có ít nhất 5 ký tự.";
        }
        if (input.length > 255) {
            return "Chi tiết địa chỉ không được vượt quá 255 ký tự.";
        }
        if (!/^[\p{L}0-9\s,./-]+$/u.test(input)) {
            return "Chi tiết địa chỉ chỉ được chứa chữ, số và các ký tự , . / -.";
        }
        if (!/[\p{L}]/u.test(input)) {
            return "Chi tiết địa chỉ phải có ít nhất một chữ cái.";
        }
        if (/[,.\/-]{2,}/.test(input)) {
            return "Chi tiết địa chỉ không được chứa nhiều ký tự đặc biệt liên tiếp.";
        }
        if (/^[,./-]|[,./-]$/.test(input)) {
            return "Chi tiết địa chỉ không được bắt đầu hoặc kết thúc bằng dấu câu.";
        }
        if (/^[0-9\s,./-]+$/u.test(input)) {
            return "Chi tiết địa chỉ không được chỉ gồm số và ký tự đặc biệt.";
        }
        if (/^([\p{L}0-9])\1{4,}$/u.test(input)) {
            return "Chi tiết địa chỉ không hợp lệ.";
        }

        return "";
    }

    function syncAddressDetailValidation(input, errorEl) {
        const error = validateAddressDetailInput(input.value);
        errorEl.textContent = error;
        input.classList.toggle("is-invalid", Boolean(error));
        return error;
    }

    function bindAddressDetailValidation(inputId, errorId) {
        const input = document.getElementById(inputId);
        const errorEl = document.getElementById(errorId);

        if (!input || !errorEl || !input.form) {
            return;
        }

        input.addEventListener("input", function () {
            const sanitized = sanitizeAddressDetailValue(input.value);
            if (sanitized !== input.value) {
                input.value = sanitized;
            }
            syncAddressDetailValidation(input, errorEl);
        });

        input.addEventListener("blur", function () {
            input.value = input.value.trim().replace(/\s+/g, " ");
            syncAddressDetailValidation(input, errorEl);
        });
    }

    function bindPaymentSelection() {
        document.querySelectorAll('input[name="payment"]').forEach(function (radio) {
            radio.addEventListener("change", function () {
                const bankInfo = document.getElementById("bankInfo");
                if (!bankInfo) {
                    return;
                }

                if (this.value === "bank_transfer") {
                    bankInfo.style.display = "block";
                    showBankTransferPreview();
                } else {
                    bankInfo.style.display = "none";
                }
            });
        });
    }

    function getFinalTotal() {
        const el = document.querySelector(".info-row.fs-5.fw-bold span:last-child");
        if (!el) {
            return 0;
        }
        const raw = el.textContent.replace(/[^\d]/g, "");
        return parseInt(raw, 10) || 0;
    }

    function buildQrUrl(amount, content) {
        return "https://img.vietqr.io/image/" +
            checkoutConfig.bankId + "-" + checkoutConfig.bankAccountNumber + "-compact2.png?amount=" +
            amount + "&addInfo=" + encodeURIComponent(content) + "&accountName=" +
            encodeURIComponent(checkoutConfig.bankAccountName);
    }

    function updateBankQR(referenceOverride) {
        const amount = getFinalTotal();
        const content = referenceOverride || currentTransferReference;
        if (!content) {
            setBankTransferQrNote("Không tạo được nội dung chuyển khoản. Vui lòng tải lại trang.");
            return;
        }
        currentTransferReference = content;

        const bankAmount = document.getElementById("bankAmount");
        const bankContent = document.getElementById("bankContent");
        const bankQrImg = document.getElementById("bankQrImg");
        const bankQrNote = document.getElementById("bankQrNote");

        if (bankAmount) {
            bankAmount.textContent = amount.toLocaleString("vi-VN") + " đ";
        }
        if (bankContent) {
            bankContent.textContent = content;
        }
        if (bankQrImg) {
            bankQrImg.src = buildQrUrl(amount, content);
            bankQrImg.hidden = false;
        }
        if (bankQrNote) {
            bankQrNote.textContent = "Quét mã QR để chuyển khoản đúng nội dung đơn hàng.";
        }
    }

    function showBankTransferPreview() {
        if (currentTransferReference) {
            updateBankQR(currentTransferReference);
            return;
        }

        const bankAmount = document.getElementById("bankAmount");
        const bankContent = document.getElementById("bankContent");
        const bankQrImg = document.getElementById("bankQrImg");
        const bankQrNote = document.getElementById("bankQrNote");
        const amount = getFinalTotal();

        if (bankAmount) {
            bankAmount.textContent = amount.toLocaleString("vi-VN") + " đ";
        }
        if (bankContent) {
            bankContent.textContent = "Sẽ được tạo sau khi bấm Đặt hàng";
        }
        if (bankQrImg) {
            bankQrImg.hidden = true;
            bankQrImg.removeAttribute("src");
        }
        if (bankQrNote) {
            bankQrNote.textContent = "Không tạo được nội dung chuyển khoản. Vui lòng tải lại trang.";
        }
    }

    function setBankTransferQrNote(message) {
        const bankQrNote = document.getElementById("bankQrNote");
        if (bankQrNote) {
            bankQrNote.textContent = message;
        }
    }

    function bindCheckoutSubmit() {
        const checkoutButton = document.getElementById("btnCheckout");
        if (!checkoutButton) {
            return;
        }

        checkoutButton.addEventListener("click", function () {
            const selectedPaymentInput = document.querySelector('input[name="payment"]:checked');
            const selectedPayment = selectedPaymentInput ? selectedPaymentInput.value : "";
            const note = document.getElementById("note") ? document.getElementById("note").value : "";
            const paymentResult = document.getElementById("paymentResult");
            const fullname = document.getElementById("fullname") ? document.getElementById("fullname").value : "";
            const phone = document.getElementById("phone") ? document.getElementById("phone").value : "";

            if (!fullname || fullname.trim() === "") {
                paymentResult.innerHTML = '<div class="alert alert-danger">Vui lòng cập nhật họ tên.</div>';
                return;
            }
            if (!phone || phone.trim() === "") {
                paymentResult.innerHTML = '<div class="alert alert-danger">Vui lòng cập nhật số điện thoại.</div>';
                return;
            }
            if (!selectedPayment) {
                paymentResult.innerHTML = '<div class="alert alert-danger">Vui lòng chọn phương thức thanh toán.</div>';
                return;
            }

            paymentResult.innerHTML = '<div class="alert alert-info">Đang xử lý đơn hàng...</div>';
            checkoutButton.disabled = true;
            const isBuyNow = new URLSearchParams(window.location.search).get("buyNow") === "true";

            const bodyData = new URLSearchParams({
                action: "placeOrder",
                csrfToken: checkoutConfig.csrfToken,
                paymentMethod: selectedPayment,
                payment: selectedPayment,
                note: note,
                buyNow: isBuyNow ? "true" : "false"
            }).toString();

            fetch(checkoutConfig.contextPath + "/checkout" + (isBuyNow ? "?buyNow=true" : ""), {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Accept": "application/json",
                    "X-CSRF-Token": checkoutConfig.csrfToken
                },
                body: bodyData
            })
                .then(response => {
                    if (!response.ok && response.status !== 200) {
                        return response.text().then(text => {
                            throw new Error("Server trả về lỗi " + response.status + ": " + text.substring(0, 200));
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        if (data.redirectUrl) {
                            window.location.href = data.redirectUrl;
                            return;
                        }

                        if (selectedPayment === "bank_transfer") {
                            applyServerBankTransferData(data);
                            checkoutButton.dataset.keepDisabled = "true";
                            paymentResult.innerHTML = '<div class="alert alert-success">Đơn hàng đã được tạo. QR bên trên có hiệu lực trong 10 phút, vui lòng hoàn tất chuyển khoản đúng nội dung. <a class="alert-link" href="' + checkoutConfig.contextPath + '/order-success">Xem đơn hàng</a></div>';
                            return;
                        }

                        paymentResult.innerHTML = '<div class="alert alert-success">' + escapeHtml(data.message || "Đặt hàng thành công!") + "</div>";
                        setTimeout(() => {
                            window.location.href = checkoutConfig.contextPath + "/order-success";
                        }, 1200);
                    } else {
                        paymentResult.innerHTML = '<div class="alert alert-danger">' + escapeHtml(data.message || "Có lỗi xảy ra.") + "</div>";
                    }
                })
                .catch(error => {
                    paymentResult.innerHTML = '<div class="alert alert-danger">Có lỗi xảy ra: ' + escapeHtml(String(error)) + "</div>";
                })
                .finally(() => {
                    checkoutButton.disabled = checkoutButton.dataset.keepDisabled === "true";
                });
        });
    }

    function applyServerBankTransferData(data) {
        checkoutConfig.bankId = data.bankId || checkoutConfig.bankId;
        checkoutConfig.bankDisplayName = data.bankDisplayName || checkoutConfig.bankDisplayName;
        checkoutConfig.bankAccountNumber = data.bankAccountNumber || checkoutConfig.bankAccountNumber;
        checkoutConfig.bankAccountName = data.bankAccountName || checkoutConfig.bankAccountName;

        const bankDisplayName = document.getElementById("bankDisplayName");
        const bankAccountNumber = document.getElementById("bankAccountNumber");
        const bankAccountName = document.getElementById("bankAccountName");
        const bankInfo = document.getElementById("bankInfo");

        if (bankDisplayName) {
            bankDisplayName.textContent = checkoutConfig.bankDisplayName;
        }
        if (bankAccountNumber) {
            bankAccountNumber.textContent = checkoutConfig.bankAccountNumber;
        }
        if (bankAccountName) {
            bankAccountName.textContent = checkoutConfig.bankAccountName;
        }
        if (bankInfo) {
            bankInfo.style.display = "block";
        }

        updateBankQR(data.transferReference || "");
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function clearAddressErrors() {
        [
            "provinceError",
            "districtError",
            "wardError",
            "addressDetailError"
        ].forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.textContent = "";
            }
        });
    }

    function setAddressApiStatus(message) {
        const el = document.getElementById("addressApiStatus");
        if (el) {
            el.textContent = message || "";
        }
    }

    async function loadProvinces() {
        const provinceSelect = document.getElementById("province");
        if (!provinceSelect) {
            return;
        }

        provinceSelect.innerHTML = '<option value="">Đang tải tỉnh/thành...</option>';
        provinceSelect.disabled = true;

        try {
            const res = await fetch(checkoutConfig.provincesApiBaseUrl + "/p/");
            if (!res.ok) {
                throw new Error("HTTP " + res.status);
            }

            const provinces = await res.json();
            provinceSelect.innerHTML = '<option value="">-- Chọn tỉnh/thành --</option>';

            provinces.forEach(p => {
                const option = document.createElement("option");
                option.value = p.name;
                option.textContent = p.name;
                option.dataset.code = p.code;
                provinceSelect.appendChild(option);
            });

            provinceSelect.disabled = false;
            provincesLoaded = true;
            setAddressApiStatus("");
        } catch (e) {
            console.error("Lỗi load tỉnh:", e);
            provinceSelect.innerHTML = '<option value="">Không tải được tỉnh/thành</option>';
            setAddressApiStatus("Không tải được danh sách tỉnh/thành. Vui lòng thử lại sau hoặc tải lại trang.");
        }
    }

    async function loadDistricts(provinceCode) {
        const districtSelect = document.getElementById("district");
        const wardSelect = document.getElementById("ward");

        districtSelect.innerHTML = '<option value="">Đang tải quận/huyện...</option>';
        wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';
        districtSelect.disabled = true;
        wardSelect.disabled = true;

        if (!provinceCode) {
            districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';
            return;
        }

        try {
            const res = await fetch(checkoutConfig.provincesApiBaseUrl + "/p/" + provinceCode + "?depth=2");
            if (!res.ok) {
                throw new Error("HTTP " + res.status);
            }

            const province = await res.json();
            districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';

            (province.districts || []).forEach(d => {
                const option = document.createElement("option");
                option.value = d.name;
                option.textContent = d.name;
                option.dataset.code = d.code;
                districtSelect.appendChild(option);
            });

            districtSelect.disabled = false;
            setAddressApiStatus("");
        } catch (e) {
            console.error("Lỗi load quận/huyện:", e);
            districtSelect.innerHTML = '<option value="">Không tải được quận/huyện</option>';
            setAddressApiStatus("Không tải được quận/huyện. Vui lòng chọn lại tỉnh/thành hoặc thử lại sau.");
        }
    }

    async function loadWards(districtCode) {
        const wardSelect = document.getElementById("ward");
        wardSelect.innerHTML = '<option value="">Đang tải phường/xã...</option>';
        wardSelect.disabled = true;

        if (!districtCode) {
            wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';
            return;
        }

        try {
            const res = await fetch(checkoutConfig.provincesApiBaseUrl + "/d/" + districtCode + "?depth=2");
            if (!res.ok) {
                throw new Error("HTTP " + res.status);
            }

            const district = await res.json();
            wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';

            (district.wards || []).forEach(w => {
                const option = document.createElement("option");
                option.value = w.name;
                option.textContent = w.name;
                option.dataset.code = w.code;
                wardSelect.appendChild(option);
            });

            wardSelect.disabled = false;
            setAddressApiStatus("");
        } catch (e) {
            console.error("Lỗi load phường/xã:", e);
            wardSelect.innerHTML = '<option value="">Không tải được phường/xã</option>';
            setAddressApiStatus("Không tải được phường/xã. Vui lòng chọn lại quận/huyện hoặc thử lại sau.");
        }
    }

    window.validateAddressForm = function () {
        clearAddressErrors();

        const province = document.getElementById("province").value.trim();
        const district = document.getElementById("district").value.trim();
        const ward = document.getElementById("ward").value.trim();
        const addressInput = document.getElementById("addressDetail");
        addressInput.value = addressInput.value.trim().replace(/\s+/g, " ");
        const addressDetail = addressInput.value;

        let isValid = true;

        if (!province) {
            document.getElementById("provinceError").textContent = "Vui lòng chọn tỉnh/thành.";
            isValid = false;
        }
        if (!district) {
            document.getElementById("districtError").textContent = "Vui lòng chọn quận/huyện.";
            isValid = false;
        }
        if (!ward) {
            document.getElementById("wardError").textContent = "Vui lòng chọn phường/xã.";
            isValid = false;
        }

        const addressDetailError = validateAddressDetailInput(addressDetail);
        if (addressDetailError && addressDetail.length >= 5) {
            document.getElementById("addressDetailError").textContent = addressDetailError;
            addressInput.classList.add("is-invalid");
            return false;
        }

        if (!addressDetail) {
            document.getElementById("addressDetailError").textContent = "Vui lòng nhập địa chỉ chi tiết.";
            isValid = false;
        } else if (addressDetail.length < 5) {
            document.getElementById("addressDetailError").textContent = "Địa chỉ chi tiết phải có ít nhất 5 ký tự.";
            isValid = false;
        }

        if (isValid) {
            addressInput.classList.remove("is-invalid");
        }
        return isValid;
    };

    window.toggleForm = async function (forceShow) {
        const form = document.getElementById("addressForm");
        if (!form) {
            return;
        }
        const shouldShow = (typeof forceShow === "boolean")
            ? forceShow
            : (form.style.display === "none" || form.style.display === "");

        form.style.display = shouldShow ? "block" : "none";
        if (shouldShow && !provincesLoaded) {
            await loadProvinces();
        }
    };

    window.validateForm = function () {
        const phone = document.getElementById("phone").value;
        return /^[0-9]{9,11}$/.test(phone);
    };

    function bindLocationSelectors() {
        const provinceSelect = document.getElementById("province");
        const districtSelect = document.getElementById("district");

        if (provinceSelect) {
            provinceSelect.addEventListener("change", async function () {
                clearAddressErrors();
                setAddressApiStatus("");
                const selected = this.options[this.selectedIndex];
                const provinceCode = selected ? selected.dataset.code : "";
                await loadDistricts(provinceCode);
            });
        }

        if (districtSelect) {
            districtSelect.addEventListener("change", async function () {
                clearAddressErrors();
                setAddressApiStatus("");
                const selected = this.options[this.selectedIndex];
                const districtCode = selected ? selected.dataset.code : "";
                await loadWards(districtCode);
            });
        }
    }

    function startCheckoutTimer() {
        let time = checkoutConfig.bankPaymentTtlSeconds;
        const timer = document.getElementById("timer");
        if (!timer) {
            return;
        }

        const intervalId = setInterval(() => {
            const minutes = Math.floor(time / 60);
            const seconds = time % 60;
            timer.innerHTML = String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0");
            time--;

            if (time < 0) {
                clearInterval(intervalId);
                timer.innerHTML = "00:00";
                const warning = document.getElementById("checkoutTimerWarning");
                const checkoutButton = document.getElementById("btnCheckout");
                const paymentResult = document.getElementById("paymentResult");
                if (warning) {
                    warning.hidden = false;
                }
                if (checkoutButton) {
                    checkoutButton.disabled = true;
                }
                if (paymentResult) {
                    paymentResult.innerHTML = '<div class="alert alert-warning">Thời gian giữ giỏ đã hết. Vui lòng quay lại giỏ hàng để kiểm tra tồn kho và giá trước khi đặt đơn.</div>';
                }
            }
        }, 1000);
    }

    function bindCouponNoteSync() {
        const couponForm = document.getElementById("couponForm");
        const noteInput = document.getElementById("note");
        const couponNoteHidden = document.getElementById("couponNoteHidden");
        if (couponForm && noteInput && couponNoteHidden) {
            couponForm.addEventListener("submit", function () {
                couponNoteHidden.value = noteInput.value;
            });
        }
    }
})();
