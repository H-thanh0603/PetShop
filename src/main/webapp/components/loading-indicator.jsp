<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
    LOADING INDICATOR COMPONENT
    Include: <jsp:include page="/components/loading-indicator.jsp" />
    
    Tự động thêm loading indicator cho tất cả form khi submit
--%>

<style>
/* Loading Overlay */
.loading-overlay {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.85);
    z-index: 999999;
    justify-content: center;
    align-items: center;
    flex-direction: column;
}
.loading-overlay.show {
    display: flex;
}
.loading-spinner {
    width: 50px;
    height: 50px;
    border: 4px solid #e0e0e0;
    border-top-color: #00bfa5;
    border-radius: 50%;
    animation: loading-spin 0.8s linear infinite;
}
.loading-text {
    margin-top: 15px;
    color: #333;
    font-size: 1rem;
    font-weight: 500;
}
@keyframes loading-spin {
    to { transform: rotate(360deg); }
}

/* Button Loading State */
.btn-loading {
    position: relative;
    pointer-events: none;
    opacity: 0.7;
}
.btn-loading::after {
    content: '';
    position: absolute;
    width: 16px;
    height: 16px;
    top: 50%;
    left: 50%;
    margin-left: -8px;
    margin-top: -8px;
    border: 2px solid transparent;
    border-top-color: currentColor;
    border-radius: 50%;
    animation: loading-spin 0.6s linear infinite;
}
.btn-loading .btn-text {
    visibility: hidden;
}
</style>

<!-- Loading Overlay -->
<div class="loading-overlay" id="loadingOverlay">
    <div class="loading-spinner"></div>
    <div class="loading-text">Đang xử lý...</div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Tự động thêm loading cho tất cả form
    var forms = document.querySelectorAll('form:not([data-no-loading])');
    
    forms.forEach(function(form) {
        form.addEventListener('submit', function(e) {
            // Kiểm tra form có valid không
            if (!form.checkValidity()) {
                return;
            }
            
            // Hiển thị loading overlay
            var overlay = document.getElementById('loadingOverlay');
            if (overlay) {
                overlay.classList.add('show');
            }
            
            // Disable submit button và thêm loading state
            var submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('btn-loading');
                submitBtn.disabled = true;
                
                // Wrap text trong span nếu chưa có
                if (!submitBtn.querySelector('.btn-text') && submitBtn.tagName === 'BUTTON') {
                    submitBtn.innerHTML = '<span class="btn-text">' + submitBtn.innerHTML + '</span>';
                }
            }
        });
    });
    
    // Ẩn loading khi trang load xong (cho trường hợp redirect)
    window.addEventListener('pageshow', function() {
        var overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.classList.remove('show');
        }
        
        // Reset tất cả button
        document.querySelectorAll('.btn-loading').forEach(function(btn) {
            btn.classList.remove('btn-loading');
            btn.disabled = false;
        });
    });
});

// Function để show/hide loading manually
function showLoading(message) {
    var overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        if (message) {
            overlay.querySelector('.loading-text').textContent = message;
        }
        overlay.classList.add('show');
    }
}

function hideLoading() {
    var overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.classList.remove('show');
    }
}
</script>
