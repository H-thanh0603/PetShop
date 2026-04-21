package Util;

import Constant.IConstant;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class SocialAuthUtil {
    private SocialAuthUtil() {
    }

    public static String buildGoogleRedirectUri(HttpServletRequest request) {
        return buildAppUrl(request, IConstant.GOOGLE_REDIRECT_PATH);
    }

    public static String buildFacebookRedirectUri(HttpServletRequest request) {
        return buildAppUrl(request, IConstant.FACEBOOK_REDIRECT_PATH);
    }

    public static String buildGoogleAuthUrl(HttpServletRequest request) {
        if (!isGoogleConfigured()) {
            return "";
        }
        String redirectUri = urlEncode(buildGoogleRedirectUri(request));
        return "https://accounts.google.com/o/oauth2/auth?scope=email%20profile%20openid"
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&client_id=" + urlEncode(SecretConfig.get("GOOGLE_CLIENT_ID"))
                + "&approval_prompt=force";
    }

    public static String buildFacebookAuthUrl(HttpServletRequest request) {
        if (!isFacebookConfigured()) {
            return "";
        }
        String redirectUri = urlEncode(buildFacebookRedirectUri(request));
        return "https://www.facebook.com/v19.0/dialog/oauth?client_id="
                + urlEncode(SecretConfig.get("facebook_client_id"))
                + "&redirect_uri=" + redirectUri
                + "&scope=email,public_profile";
    }

    public static boolean isGoogleConfigured() {
        return SecretConfig.hasValue("GOOGLE_CLIENT_ID");
    }

    public static boolean isFacebookConfigured() {
        return SecretConfig.hasValue("facebook_client_id");
    }

    private static String buildAppUrl(HttpServletRequest request, String path) {
        return request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath()
                + path;
    }

    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
