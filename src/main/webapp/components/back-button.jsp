<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    Component: back-button.jsp
    Mô tả: Nút quay lại trang trước - Fixed position góc trái
    Sử dụng: <jsp:include page="/components/back-button.jsp" />
--%>

<style>
    /* Back Button - Fixed position */
    .btn-back-fixed {
        position: fixed;
        bottom: 30px;
        left: 30px;
        z-index: 99998;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        background: linear-gradient(135deg, #6366f1, #4f46e5);
        color: white;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4);
        transition: all 0.3s ease;
        text-decoration: none;
    }
    
    .btn-back-fixed:hover {
        transform: translateY(-3px) scale(1.05);
        box-shadow: 0 8px 30px rgba(99, 102, 241, 0.5);
        color: white;
    }
    
    .btn-back-fixed:active {
        transform: translateY(0) scale(0.98);
    }
    
    .btn-back-fixed i {
        font-size: 1.5rem;
    }
    
    /* Tooltip */
    .btn-back-fixed::before {
        content: 'Quay lại';
        position: absolute;
        left: 60px;
        background: #1f2937;
        color: white;
        padding: 8px 14px;
        border-radius: 8px;
        font-size: 0.85rem;
        font-weight: 600;
        white-space: nowrap;
        opacity: 0;
        visibility: hidden;
        transition: all 0.2s ease;
        pointer-events: none;
    }
    
    .btn-back-fixed:hover::before {
        opacity: 1;
        visibility: visible;
    }
    
    /* Responsive */
    @media (max-width: 768px) {
        .btn-back-fixed {
            bottom: 20px;
            left: 20px;
            width: 45px;
            height: 45px;
        }
        
        .btn-back-fixed::before {
            display: none;
        }
    }
</style>

<a href="javascript:history.back()" class="btn-back-fixed" title="Quay lại trang trước">
    <i class='bx bx-arrow-back'></i>
</a>
