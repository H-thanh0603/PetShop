package Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.URI;

public final class AuthRedirectUtil {
    private AuthRedirectUtil() {
    }

    public static void storeRedirectAfterLogin(HttpServletRequest request) {
        String redirectUrl = request.getParameter("redirect");

        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = request.getHeader("Referer");
        }

        String normalizedRedirectUrl = normalizeInternalRedirect(request, redirectUrl);
        if (normalizedRedirectUrl != null) {
            // Ghi nhớ đúng trang/chức năng đang cần đăng nhập để login xong quay lại chỗ đó.
            request.getSession().setAttribute("redirectAfterLogin", normalizedRedirectUrl);
        }
    }

    public static String consumeRedirectAfterLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
        // Chỉ dùng một lần để tránh lần login sau bị kéo về URL cũ.
        session.removeAttribute("redirectAfterLogin");

        return normalizeInternalRedirect(request, redirectUrl);
    }

    private static String normalizeInternalRedirect(HttpServletRequest request, String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return null;
        }

        try {
            URI redirectUri = URI.create(redirectUrl.trim());
            String contextPath = request.getContextPath();
            String path = redirectUri.getPath();

            // Chỉ cho phép redirect nội bộ trong chính ứng dụng, không nhận URL ngoài.
            if (path == null || !path.startsWith(contextPath + "/")) {
                return null;
            }

            // Không lưu ngược lại các trang auth để tránh vòng lặp login -> login.
            if (path.equals(contextPath + "/login") || path.equals(contextPath + "/register")) {
                return null;
            }

            StringBuilder normalizedUrl = new StringBuilder(path);
            if (redirectUri.getRawQuery() != null && !redirectUri.getRawQuery().isBlank()) {
                normalizedUrl.append('?').append(redirectUri.getRawQuery());
            }

            return normalizedUrl.toString();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
