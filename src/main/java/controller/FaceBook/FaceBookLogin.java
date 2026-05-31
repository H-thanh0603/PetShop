package controller.FaceBook;
import Constant.IConstant;
import Model.FbAccount.Account;
import Util.SecretConfig;
import Util.SocialAuthUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class FaceBookLogin {
    public static String getToken(String code, String redirectUri) throws ClientProtocolException, IOException {
        if (!SocialAuthUtil.isFacebookConfigured()) {
            throw new IllegalStateException("Facebook login chưa được cấu hình đầy đủ.");
        }
        String clientId = SecretConfig.get("facebook_client_id");
        String clientSecret = SecretConfig.get("facebook_client_secret");
        String link = "https://graph.facebook.com/v19.0/oauth/access_token?"
                + "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        URL url = new URL(link);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(url.openStream()));

        String response = reader.readLine();

        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        if (json == null || !json.has("access_token")) {
            throw new IllegalStateException("Không lấy được access token từ Facebook.");
        }

        return json.get("access_token").getAsString();
    }
    public static Account getUserInfo(final String accessToken) throws ClientProtocolException, IOException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token Facebook không hợp lệ.");
        }
        String link = IConstant.facebook_link_get_user_info + accessToken;
        String response = Request.Get(link).execute().returnContent().asString();
        Account fbAccount= new Gson().fromJson(response, Account .class);
        return fbAccount;
    }
}
