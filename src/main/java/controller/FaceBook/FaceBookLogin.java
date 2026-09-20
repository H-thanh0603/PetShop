package controller.FaceBook;
import Constant.IConstant;
import Model.FbAccount.Account;
import Util.SecretConfig;
import Util.SocialAuthUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


public class FaceBookLogin {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static String getToken(String code, String redirectUri) throws IOException {
        if (!SocialAuthUtil.isFacebookConfigured()) {
            throw new IllegalStateException("Facebook login chưa được cấu hình đầy đủ.");
        }
        String clientId = SecretConfig.get("facebook_client_id");
        String clientSecret = SecretConfig.get("facebook_client_secret");
        String link = "https://graph.facebook.com/v19.0/oauth/access_token?"
                + "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&code=" + encode(code);

        String response = sendGet(link);
        JsonObject json = GSON.fromJson(response, JsonObject.class);

        if (json == null || !json.has("access_token")) {
            throw new IllegalStateException("Không lấy được access token từ Facebook.");
        }

        return json.get("access_token").getAsString();
    }
    public static Account getUserInfo(final String accessToken) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token Facebook không hợp lệ.");
        }
        String link = IConstant.facebook_link_get_user_info + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String response = sendGet(link);
        return GSON.fromJson(response, Account.class);
    }

    private static String sendGet(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> httpResponse =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                throw new IOException("Facebook API returned HTTP " + httpResponse.statusCode());
            }
            return httpResponse.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Facebook request was interrupted.", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
