/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              CANCEL APPOINTMENT - JAVASCRIPT FUNCTIONALITY                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  UI States Flow:                                                             ║
 * ║  DEFAULT → CONFIRM (Modal) → LOADING → SUCCESS/ERROR → UNDO (optional)      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════════
// GLOBAL VARIABLES
// ═══════════════════════════════════════════════════════════════════════════════

let cancelModal = null;
let successToast = null;
let errorToast = null;
let undoExpiredToast = null;
let undoSuccessToast = null;

let undoTimer = null;
let undoCountdown = 10; // seconds
let currentCancelData = null;

// ═══════════════════════════════════════════════════════════════════════════════
// INITIALIZATION
// ═══════════════════════════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap components
    const modalEl = document.getElementById('cancelAppointmentModal');
    if (modalEl) {
        cancelModal = new bootstrap.Modal(modalEl);
    }
    
    const successToastEl = document.getElementById('cancelSuccessToast');
    if (successToastEl) {
        successToast = new bootstrap.Toast(successToastEl, { autohide: false });
    }
    
    const errorToastEl = document.getElementById('cancelErrorToast');
    if (errorToastEl) {
        errorToast = new bootstrap.Toast(errorToastEl, { delay: 5000 });
    }
    
    const undoExpiredEl = document.getElementById('undoExpiredToast');
    if (undoExpiredEl) {
        undoExpiredToast = new bootstrap.Toast(undoExpiredEl, { delay: 3000 });
    }
    
    const undoSuccessEl = document.getElementById('undoSuccessToast');
    if (undoSuccessEl) {
        undoSuccessToast = new bootstrap.Toast(undoSuccessEl, { delay: 3000 });
    }
    
    // Cancel reason dropdown handler
    const cancelReasonSelect = document.getElementById('cancelReason');
    if (cancelReasonSelect) {
        cancelReasonSelect.addEventListener('change', function() {
            const customWrapper = document.getElementById('customReasonWrapper');
            if (this.value === 'other') {
                customWrapper.classList.remove('d-none');
            } else {
                customWrapper.classList.add('d-none');
            }
        });
    }
});

// ═══════════════════════════════════════════════════════════════════════════════
// STATE 1: DEFAULT → CONFIRM (Show Modal)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Mở modal xác nhận hủy lịch hẹn
 * @param {number} appointmentId - ID lịch hẹn
 * @param {string} customerName - Tên khách hàng
 * @param {string} bookingDate - Ngày hẹn
 * @param {string} doctorName - Tên bác sĩ
 * @param {string} currentStatus - Trạng thái hiện tại
 */
function showCancelModal(appointmentId, customerName, bookingDate, doctorName, currentStatus) {
    // Reset modal state
    resetModalState();
    
    // Populate modal data
    document.getElementById('cancelAppointmentId').value = appointmentId;
    document.getElementById('cancelPreviousStatus').value = currentStatus;
    document.getElementById('cancelCustomerName').textContent = customerName;
    document.getElementById('cancelDate').textContent = bookingDate;
    document.getElementById('cancelDoctor').textContent = doctorName;
    
    // Store data for undo
    currentCancelData = {
        id: appointmentId,
        customerName: customerName,
        bookingDate: bookingDate,
        doctorName: doctorName,
        previousStatus: currentStatus
    };
    
    // Show modal
    cancelModal.show();
}

/**
 * Reset modal về trạng thái ban đầu
 */
function resetModalState() {
    const confirmBtn = document.getElementById('confirmCancelBtn');
    const keepBtn = document.getElementById('keepAppointmentBtn');
    const closeBtn = document.getElementById('modalCloseBtn');
    const reasonSelect = document.getElementById('cancelReason');
    const customWrapper = document.getElementById('customReasonWrapper');
    
    // Reset button state
    confirmBtn.classList.remove('loading');
    confirmBtn.disabled = false;
    keepBtn.disabled = false;
    closeBtn.disabled = false;
    
    // Reset form
    if (reasonSelect) reasonSelect.value = '';
    if (customWrapper) customWrapper.classList.add('d-none');
    
    const customReason = document.getElementById('customReason');
    if (customReason) customReason.value = '';
}

// ═══════════════════════════════════════════════════════════════════════════════
// STATE 2: CONFIRM → LOADING (Execute Cancel)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Thực hiện hủy lịch hẹn
 */
async function executeCancelAppointment() {
    const appointmentId = document.getElementById('cancelAppointmentId').value;
    const reason = getCancelReason();
    
    // Set loading state
    setLoadingState(true);
    setCardLoadingState(appointmentId, true);
    
    try {
        const response = await fetch(contextPath + '/admin/appointments', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `action=cancel&id=${appointmentId}&reason=${encodeURIComponent(reason)}&ajax=true`
        });
        
        const result = await response.json();
        
        if (result.success) {
            // Close modal
            cancelModal.hide();
            
            // Update UI
            updateCardToCancelledState(appointmentId);
            
            // Show success toast with undo
            showSuccessToast();
            startUndoCountdown();
        } else {
            // Show error
            showErrorToast(result.message || 'Có lỗi xảy ra. Vui lòng thử lại.');
        }
    } catch (error) {
        console.error('Cancel appointment error:', error);
        showErrorToast('Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.');
    } finally {
        setLoadingState(false);
        setCardLoadingState(appointmentId, false);
    }
}

/**
 * Lấy lý do hủy từ form
 */
function getCancelReason() {
    const reasonSelect = document.getElementById('cancelReason');
    const customReason = document.getElementById('customReason');
    
    if (reasonSelect.value === 'other' && customReason.value.trim()) {
        return customReason.value.trim();
    }
    
    const reasonMap = {
        'customer_request': 'Khách hàng yêu cầu hủy',
        'schedule_conflict': 'Xung đột lịch trình',
        'doctor_unavailable': 'Bác sĩ không khả dụng',
        'emergency': 'Trường hợp khẩn cấp',
        'other': 'Lý do khác'
    };
    
    return reasonMap[reasonSelect.value] || '';
}

/**
 * Set trạng thái loading cho modal
 */
function setLoadingState(isLoading) {
    const confirmBtn = document.getElementById('confirmCancelBtn');
    const keepBtn = document.getElementById('keepAppointmentBtn');
    const closeBtn = document.getElementById('modalCloseBtn');
    
    if (isLoading) {
        confirmBtn.classList.add('loading');
        confirmBtn.disabled = true;
        keepBtn.disabled = true;
        closeBtn.disabled = true;
    } else {
        confirmBtn.classList.remove('loading');
        confirmBtn.disabled = false;
        keepBtn.disabled = false;
        closeBtn.disabled = false;
    }
}

/**
 * Set trạng thái loading cho card/row cụ thể
 * CHỈ disable thẻ đang xử lý, không khóa toàn trang
 */
function setCardLoadingState(appointmentId, isLoading) {
    // For card view
    const card = document.querySelector(`.appointment-card[data-id="${appointmentId}"]`);
    if (card) {
        if (isLoading) {
            card.classList.add('card-loading');
        } else {
            card.classList.remove('card-loading');
        }
    }
    
    // For table view
    const row = document.querySelector(`tr[data-appointment-id="${appointmentId}"]`);
    if (row) {
        if (isLoading) {
            row.classList.add('row-loading');
        } else {
            row.classList.remove('row-loading');
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STATE 3: SUCCESS - Update UI
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Cập nhật UI card sang trạng thái "Đã hủy"
 */
function updateCardToCancelledState(appointmentId) {
    // For card view
    const card = document.querySelector(`.appointment-card[data-id="${appointmentId}"]`);
    if (card) {
        // Add inactive class
        card.classList.add('card-inactive');
        card.classList.remove('border-pending', 'border-confirmed');
        card.classList.add('border-cancelled');
        
        // Update status badge
        const badge = card.querySelector('.status-badge');
        if (badge) {
            badge.className = 'status-badge status-cancelled animate';
            badge.innerHTML = '<i class="bx bx-block"></i> Đã hủy';
        }
        
        // Update action buttons
        const actionsContainer = card.querySelector('.card-actions');
        if (actionsContainer) {
            actionsContainer.innerHTML = `
                <button class="btn btn-primary btn-sm btn-rebook" onclick="rebookAppointment(${appointmentId})">
                    <i class='bx bx-calendar-plus'></i> Đặt lại
                </button>
                <button class="btn btn-outline-secondary btn-sm btn-view-details" onclick="viewAppointmentDetails(${appointmentId})">
                    <i class='bx bx-show'></i> Xem chi tiết
                </button>
            `;
        }
    }
    
    // For table view
    const row = document.querySelector(`tr[data-appointment-id="${appointmentId}"]`);
    if (row) {
        row.classList.add('row-inactive');
        row.setAttribute('data-status', 'Cancelled');
        
        // Update status cell
        const statusCell = row.querySelector('td:nth-child(8)');
        if (statusCell) {
            statusCell.innerHTML = `
                <span class="status-badge status-cancelled animate">
                    <i class='bx bx-block'></i> Đã hủy
                </span>
            `;
        }
        
        // Update actions cell
        const actionsCell = row.querySelector('td:nth-child(9)');
        if (actionsCell) {
            actionsCell.innerHTML = `
                <button class="btn btn-primary action-btn btn-rebook" onclick="rebookAppointment(${appointmentId})" title="Đặt lại">
                    <i class='bx bx-calendar-plus'></i>
                </button>
                <button class="btn btn-outline-secondary action-btn btn-view-details" onclick="viewAppointmentDetails(${appointmentId})" title="Xem chi tiết">
                    <i class='bx bx-show'></i>
                </button>
            `;
        }
    }
}

/**
 * Hiển thị toast thành công với nút Undo
 */
function showSuccessToast() {
    if (successToast) {
        // Reset progress bar animation
        const progressBar = document.querySelector('.undo-progress-bar');
        if (progressBar) {
            progressBar.style.animation = 'none';
            progressBar.offsetHeight; // Trigger reflow
            progressBar.style.animation = 'undoCountdown 10s linear forwards';
        }
        
        successToast.show();
    }
}

/**
 * Hiển thị toast lỗi
 */
function showErrorToast(message) {
    const errorMsg = document.getElementById('errorMessage');
    if (errorMsg) {
        errorMsg.textContent = message;
    }
    if (errorToast) {
        errorToast.show();
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STATE 4: UNDO FUNCTIONALITY
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Bắt đầu đếm ngược Undo (10 giây)
 */
function startUndoCountdown() {
    undoCountdown = 10;
    updateUndoCountdownDisplay();
    
    // Clear existing timer
    if (undoTimer) {
        clearInterval(undoTimer);
    }
    
    undoTimer = setInterval(() => {
        undoCountdown--;
        updateUndoCountdownDisplay();
        
        if (undoCountdown <= 0) {
            clearInterval(undoTimer);
            undoTimer = null;
            
            // Hide success toast
            if (successToast) {
                successToast.hide();
            }
            
            // Show expired toast
            if (undoExpiredToast) {
                undoExpiredToast.show();
            }
            
            // Clear undo data
            currentCancelData = null;
        }
    }, 1000);
}

/**
 * Cập nhật hiển thị countdown
 */
function updateUndoCountdownDisplay() {
    const countdownEl = document.querySelector('.undo-countdown');
    if (countdownEl) {
        countdownEl.textContent = undoCountdown;
    }
}

/**
 * Thực hiện Undo - Khôi phục lịch hẹn
 */
async function undoCancelAppointment() {
    if (!currentCancelData) {
        showErrorToast('Không thể hoàn tác. Dữ liệu đã hết hạn.');
        return;
    }
    
    // Stop countdown
    if (undoTimer) {
        clearInterval(undoTimer);
        undoTimer = null;
    }
    
    // Hide success toast
    if (successToast) {
        successToast.hide();
    }
    
    const appointmentId = currentCancelData.id;
    const previousStatus = currentCancelData.previousStatus;
    
    // Set loading state on card
    setCardLoadingState(appointmentId, true);
    
    try {
        const response = await fetch(contextPath + '/admin/appointments', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `action=restore&id=${appointmentId}&status=${previousStatus}&ajax=true`
        });
        
        const result = await response.json();
        
        if (result.success) {
            // Restore UI
            restoreCardState(appointmentId, previousStatus);
            
            // Show undo success toast
            if (undoSuccessToast) {
                undoSuccessToast.show();
            }
        } else {
            showErrorToast(result.message || 'Không thể khôi phục lịch hẹn.');
        }
    } catch (error) {
        console.error('Undo cancel error:', error);
        showErrorToast('Không thể kết nối đến server.');
    } finally {
        setCardLoadingState(appointmentId, false);
        currentCancelData = null;
    }
}

/**
 * Khôi phục UI card về trạng thái trước đó
 */
function restoreCardState(appointmentId, previousStatus) {
    const statusConfig = {
        'Pending': {
            badgeClass: 'status-pending',
            borderClass: 'border-pending',
            icon: 'bx-time',
            text: 'Chờ duyệt'
        },
        'Confirmed': {
            badgeClass: 'status-confirmed',
            borderClass: 'border-confirmed',
            icon: 'bx-check',
            text: 'Đã duyệt'
        }
    };
    
    const config = statusConfig[previousStatus] || statusConfig['Pending'];
    
    // For card view
    const card = document.querySelector(`.appointment-card[data-id="${appointmentId}"]`);
    if (card) {
        card.classList.remove('card-inactive', 'border-cancelled');
        card.classList.add(config.borderClass, 'transitioning');
        
        const badge = card.querySelector('.status-badge');
        if (badge) {
            badge.className = `status-badge ${config.badgeClass} animate`;
            badge.innerHTML = `<i class="bx ${config.icon}"></i> ${config.text}`;
        }
        
        // Restore original action buttons (would need to reload or store original HTML)
        // For simplicity, reload the page or restore from stored data
        setTimeout(() => {
            card.classList.remove('transitioning');
        }, 300);
    }
    
    // For table view
    const row = document.querySelector(`tr[data-appointment-id="${appointmentId}"]`);
    if (row) {
        row.classList.remove('row-inactive');
        row.setAttribute('data-status', previousStatus);
        
        const statusCell = row.querySelector('td:nth-child(8)');
        if (statusCell) {
            statusCell.innerHTML = `
                <span class="status-badge ${config.badgeClass} animate">
                    <i class='bx ${config.icon}'></i> ${config.text}
                </span>
            `;
        }
        
        // Restore action buttons - simplified, may need page reload for full restore
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// EDGE CASES HANDLERS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Xử lý khi API lỗi
 */
function handleApiError(error, appointmentId) {
    console.error('API Error:', error);
    
    // Remove loading state
    setCardLoadingState(appointmentId, false);
    setLoadingState(false);
    
    // Show appropriate error message
    let errorMessage = 'Có lỗi xảy ra. Vui lòng thử lại.';
    
    if (error.status === 404) {
        errorMessage = 'Lịch hẹn không tồn tại hoặc đã bị xóa.';
    } else if (error.status === 409) {
        errorMessage = 'Lịch hẹn đã được cập nhật bởi người khác. Vui lòng tải lại trang.';
    } else if (error.status === 500) {
        errorMessage = 'Lỗi server. Vui lòng thử lại sau.';
    } else if (!navigator.onLine) {
        errorMessage = 'Mất kết nối mạng. Vui lòng kiểm tra và thử lại.';
    }
    
    showErrorToast(errorMessage);
}

/**
 * Xử lý hủy nhiều lần (double-click prevention)
 */
let isCancelling = false;

function preventDoubleCancellation() {
    if (isCancelling) {
        return false;
    }
    isCancelling = true;
    
    // Reset after 3 seconds
    setTimeout(() => {
        isCancelling = false;
    }, 3000);
    
    return true;
}

/**
 * Kiểm tra trạng thái trước khi hủy
 */
function validateBeforeCancel(appointmentId, currentStatus) {
    // Không cho hủy nếu đã hủy
    if (currentStatus === 'Cancelled') {
        showErrorToast('Lịch hẹn này đã được hủy trước đó.');
        return false;
    }
    
    // Không cho hủy nếu đã hoàn thành
    if (currentStatus === 'Completed') {
        showErrorToast('Không thể hủy lịch hẹn đã hoàn thành.');
        return false;
    }
    
    // Không cho hủy nếu đã từ chối
    if (currentStatus === 'Rejected') {
        showErrorToast('Lịch hẹn này đã bị từ chối.');
        return false;
    }
    
    return true;
}

// ═══════════════════════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Đặt lại lịch hẹn (placeholder)
 */
function rebookAppointment(appointmentId) {
    // Redirect to booking page with pre-filled data
    window.location.href = contextPath + '/booking?rebook=' + appointmentId;
}

/**
 * Xem chi tiết lịch hẹn (placeholder)
 */
function viewAppointmentDetails(appointmentId) {
    // Open details modal or redirect
    window.location.href = contextPath + '/admin/appointments?view=' + appointmentId;
}

// Context path variable (should be set in JSP)
const contextPath = typeof window.contextPath !== 'undefined' ? window.contextPath : '';
