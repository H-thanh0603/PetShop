<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    ╔══════════════════════════════════════════════════════════════════════════════╗
    ║                    CANCEL APPOINTMENT MODAL COMPONENT                        ║
    ║                                                                              ║
    ║  UI States: Default → Confirm → Loading → Success/Error                      ║
    ║  Features: Undo support, Reason dropdown, Card state management              ║
    ╚══════════════════════════════════════════════════════════════════════════════╝
--%>

<!-- ═══════════════════════════════════════════════════════════════════════════════
     CANCEL CONFIRMATION MODAL
     State: CONFIRM
═══════════════════════════════════════════════════════════════════════════════ -->
<div class="modal fade" id="cancelAppointmentModal" tabindex="-1" aria-labelledby="cancelModalLabel" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <!-- Modal Header -->
            <div class="modal-header border-0 pb-0">
                <div class="d-flex align-items-center">
                    <div class="cancel-icon-wrapper me-3">
                        <i class='bx bx-calendar-x'></i>
                    </div>
                    <h5 class="modal-title fw-bold" id="cancelModalLabel">
                        Xác nhận hủy lịch hẹn?
                    </h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng" id="modalCloseBtn"></button>
            </div>
            
            <!-- Modal Body -->
            <div class="modal-body pt-2">
                <!-- Appointment Info Summary -->
                <div class="appointment-summary p-3 rounded mb-3">
                    <p class="mb-0" id="cancelDescription">
                        Lịch hẹn <strong id="cancelCustomerName">-</strong> – 
                        <strong id="cancelDate">-</strong> – 
                        Bác sĩ <strong id="cancelDoctor">-</strong> sẽ bị hủy.
                    </p>
                </div>
                
                <!-- Optional: Cancel Reason Dropdown -->
                <div class="mb-3">
                    <label class="form-label text-muted small">
                        <i class='bx bx-message-detail'></i> 
                        Bạn có muốn để lại lý do hủy không? <span class="text-muted">(Tùy chọn)</span>
                    </label>
                    <select class="form-select" id="cancelReason">
                        <option value="">-- Chọn lý do --</option>
                        <option value="customer_request">Khách hàng yêu cầu hủy</option>
                        <option value="schedule_conflict">Xung đột lịch trình</option>
                        <option value="doctor_unavailable">Bác sĩ không khả dụng</option>
                        <option value="emergency">Trường hợp khẩn cấp</option>
                        <option value="other">Lý do khác</option>
                    </select>
                </div>
                
                <!-- Custom Reason Input (shown when "other" selected) -->
                <div class="mb-3 d-none" id="customReasonWrapper">
                    <textarea class="form-control" id="customReason" rows="2" 
                              placeholder="Nhập lý do cụ thể..."></textarea>
                </div>
                
                <!-- Hidden fields -->
                <input type="hidden" id="cancelAppointmentId" value="">
                <input type="hidden" id="cancelPreviousStatus" value="">
            </div>
            
            <!-- Modal Footer -->
            <div class="modal-footer border-0 pt-0">
                <button type="button" class="btn btn-outline-secondary px-4" data-bs-dismiss="modal" id="keepAppointmentBtn">
                    <i class='bx bx-calendar-check'></i> Giữ lịch
                </button>
                <button type="button" class="btn btn-danger px-4" id="confirmCancelBtn" onclick="executeCancelAppointment()">
                    <span class="btn-text">
                        <i class='bx bx-x-circle'></i> Hủy lịch hẹn
                    </span>
                    <span class="btn-loading d-none">
                        <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                        Đang hủy...
                    </span>
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════════════════════
     TOAST NOTIFICATIONS
     States: SUCCESS, ERROR, UNDO
═══════════════════════════════════════════════════════════════════════════════ -->
<div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 1100;">
    
    <!-- Success Toast with Undo -->
    <div id="cancelSuccessToast" class="toast align-items-center border-0" role="alert" aria-live="assertive" aria-atomic="true" data-bs-autohide="false">
        <div class="toast-body d-flex align-items-center justify-content-between bg-success text-white rounded">
            <div class="d-flex align-items-center">
                <i class='bx bx-check-circle fs-4 me-2'></i>
                <span>Đã hủy lịch hẹn thành công</span>
            </div>
            <div class="d-flex align-items-center">
                <button type="button" class="btn btn-sm btn-light me-2" id="undoBtn" onclick="undoCancelAppointment()">
                    <i class='bx bx-undo'></i> Hoàn tác
                    <span class="undo-countdown badge bg-warning text-dark ms-1">10</span>
                </button>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Đóng"></button>
            </div>
        </div>
        <!-- Undo Progress Bar -->
        <div class="undo-progress-bar"></div>
    </div>
    
    <!-- Error Toast -->
    <div id="cancelErrorToast" class="toast align-items-center border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-body d-flex align-items-center justify-content-between bg-danger text-white rounded">
            <div class="d-flex align-items-center">
                <i class='bx bx-error-circle fs-4 me-2'></i>
                <span id="errorMessage">Có lỗi xảy ra. Vui lòng thử lại.</span>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Đóng"></button>
        </div>
    </div>
    
    <!-- Undo Expired Toast -->
    <div id="undoExpiredToast" class="toast align-items-center border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-body d-flex align-items-center justify-content-between bg-secondary text-white rounded">
            <div class="d-flex align-items-center">
                <i class='bx bx-time-five fs-4 me-2'></i>
                <span>Thời gian hoàn tác đã hết</span>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Đóng"></button>
        </div>
    </div>
    
    <!-- Undo Success Toast -->
    <div id="undoSuccessToast" class="toast align-items-center border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-body d-flex align-items-center justify-content-between bg-info text-white rounded">
            <div class="d-flex align-items-center">
                <i class='bx bx-revision fs-4 me-2'></i>
                <span>Đã khôi phục lịch hẹn thành công</span>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Đóng"></button>
        </div>
    </div>
</div>
