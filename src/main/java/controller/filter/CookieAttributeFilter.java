package controller.filter;

import Util.AppConfig;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

public class CookieAttributeFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        boolean secureCookies = AppConfig.getBoolean("app.cookies.secure", request.isSecure());

        chain.doFilter(req, new CookieResponseWrapper(response, secureCookies));
    }

    private static final class CookieResponseWrapper extends HttpServletResponseWrapper {
        private final boolean secureCookies;

        private CookieResponseWrapper(HttpServletResponse response, boolean secureCookies) {
            super(response);
            this.secureCookies = secureCookies;
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, decorateCookieHeader(name, value));
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, decorateCookieHeader(name, value));
        }

        private String decorateCookieHeader(String name, String value) {
            if (value == null || !"Set-Cookie".equalsIgnoreCase(name)) {
                return value;
            }

            String decorated = value;
            String lower = value.toLowerCase();
            if (!lower.contains("samesite=")) {
                decorated += "; SameSite=Lax";
            }
            if (secureCookies && !lower.contains("secure")) {
                decorated += "; Secure";
            }
            return decorated;
        }
    }
}
