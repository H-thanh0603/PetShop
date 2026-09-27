package com.petshop.web.oauth;

import com.petshop.constant.IConstant;
import com.petshop.model.GgAccount.GoogleAccount;
import com.petshop.util.Json;
import com.petshop.util.SecretConfig;
import com.petshop.util.SocialAuthUtil;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GoogleLogin {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static String getToken(String code, String redirectUri) throws IOException {
        if (!SocialAuthUtil.isGoogleConfigured()) {
            throw new IllegalStateException("Google login chua duoc cau hinh day du.");
        }

        String formBody = formEncode(
                "client_id", SecretConfig.get("GOOGLE_CLIENT_ID"),
                "client_secret", SecretConfig.get("GOOGLE_CLIENT_SECRET"),
                "redirect_uri", redirectUri,
                "code", code,
                "grant_type", IConstant.GOOGLE_GRANT_TYPE
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create(IConstant.GOOGLE_LINK_GET_TOKEN))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> httpResponse = send(request);
        String response = httpResponse.body();

        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IllegalStateException("Google token endpoint returned HTTP "
                    + httpResponse.statusCode() + ": " + abbreviate(response));
        }

        JsonNode body = Json.MAPPER.readTree(response);
        if (body == null || !body.has("access_token")) {
            throw new IllegalStateException("Khong lay duoc access token tu Google: " + abbreviate(response));
        }

        return body.path("access_token").asString();
    }

    public static GoogleAccount getUserInfo(final String accessToken) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token Google khong hop le.");
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(IConstant.GOOGLE_LINK_GET_USER_INFO))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> httpResponse = send(request);
        String response = httpResponse.body();

        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IllegalStateException("Google userinfo endpoint returned HTTP "
                    + httpResponse.statusCode() + ": " + abbreviate(response));
        }

        JsonNode json = Json.MAPPER.readTree(response);
        if (json == null || !json.isObject()) {
            throw new IllegalStateException("Google userinfo response did not include an email: " + abbreviate(response));
        }
        GoogleAccount googleAccount = new GoogleAccount(
                str(json, "id"),
                str(json, "email"),
                str(json, "name"),
                str(json, "first_name"),
                str(json, "given_name"),
                str(json, "family_name"),
                str(json, "picture"),
                bool(json, "verified_email"));
        if (googleAccount == null || googleAccount.getEmail() == null || googleAccount.getEmail().isBlank()) {
            throw new IllegalStateException("Google userinfo response did not include an email: " + abbreviate(response));
        }

        return googleAccount;
    }

    private static String str(JsonNode json, String field) {
        JsonNode value = json.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static boolean bool(JsonNode json, String field) {
        JsonNode value = json.get(field);
        return value != null && !value.isNull() && value.asBoolean();
    }

    private static HttpResponse<String> send(HttpRequest request) throws IOException {
        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Google OAuth request was interrupted.", e);
        }
    }

    private static String formEncode(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            if (i > 0) {
                builder.append('&');
            }
            builder.append(encode(values[i])).append('=').append(encode(values[i + 1]));
        }
        return builder.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
    }
}
