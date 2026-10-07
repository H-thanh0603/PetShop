package com.petshop.util;

import com.petshop.constant.IConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class SocialAuthUtil {
    private SocialAuthUtil() {
    }

    public static String buildGoogleRedirectUri(HttpServletRequest request) {
        return buildAppUrl(request, IConstant.GOOGLE_REDIRECT_PATH);
    }

    public static String buildFacebookRedirectUri(HttpServletRequest request) {
        return buildAppUrl(request, IConstant.FACEBOOK_REDIRECT_PATH);
    }

    private static final SecureRandom STATE_RANDOM = new SecureRandom();
    public static final String OAUTH_STATE_SESSION_KEY = "oauth_state";

    /** Generates a CSRF state token, stores it in the session, appends &state=. */
    private static String newState(HttpSession session) {
        byte[] bytes = new byte[16];
        STATE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        String state = sb.toString();
        session.setAttribute(OAUTH_STATE_SESSION_KEY, state);
        return state;
    }

    /**
     * OAuth login-CSRF defense: the callback must present the same state the
     * login page generated for this session. Constant-time compare.
     */
    public static boolean isStateValid(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(OAUTH_STATE_SESSION_KEY);
        if (expected == null) {
            return false;
        }
        String provided = request.getParameter("state");
        session.removeAttribute(OAUTH_STATE_SESSION_KEY); // single use
        if (provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.toString().getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    public static String buildGoogleAuthUrl(HttpServletRequest request, HttpSession session) {
        if (!isGoogleConfigured()) {
            return "";
        }
        String redirectUri = urlEncode(buildGoogleRedirectUri(request));
        return "https://accounts.google.com/o/oauth2/auth?scope=email%20profile%20openid"
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&client_id=" + urlEncode(SecretConfig.get("GOOGLE_CLIENT_ID"))
                + "&state=" + newState(session)
                + "&approval_prompt=force";
    }

    public static String buildFacebookAuthUrl(HttpServletRequest request, HttpSession session) {
        if (!isFacebookConfigured()) {
            return "";
        }
        String redirectUri = urlEncode(buildFacebookRedirectUri(request));
        return "https://www.facebook.com/v19.0/dialog/oauth?client_id="
                + urlEncode(SecretConfig.get("facebook_client_id"))
                + "&redirect_uri=" + redirectUri
                + "&scope=email,public_profile"
                + "&state=" + newState(session);
    }

    public static boolean isGoogleConfigured() {
        return SecretConfig.hasValue("GOOGLE_CLIENT_ID")
                && SecretConfig.hasValue("GOOGLE_CLIENT_SECRET");
    }

    public static boolean isFacebookConfigured() {
        return SecretConfig.hasValue("facebook_client_id")
                && SecretConfig.hasValue("facebook_client_secret");
    }

    private static String buildAppUrl(HttpServletRequest request, String path) {
        String configuredBaseUrl = AppConfig.get("app.base-url", "APP_BASE_URL", "PETSHOP_BASE_URL");
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            return stripTrailingSlash(configuredBaseUrl) + path;
        }

        String scheme = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }

        String host = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
        if (host == null || host.isBlank()) {
            host = request.getServerName();
            int port = request.getServerPort();
            if (!isDefaultPort(scheme, port)) {
                host = host + ":" + port;
            }
        }

        return scheme + "://" + host + request.getContextPath() + path;
    }

    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String firstHeaderValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.split(",")[0].trim();
    }

    private static boolean isDefaultPort(String scheme, int port) {
        return ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
    }

    private static String stripTrailingSlash(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
