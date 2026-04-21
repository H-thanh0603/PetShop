package controller.Google;

import Constant.IConstant;
import Model.GgAccount.GoogleAccount;
import Util.SocialAuthUtil;
import Util.SecretConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class GoogleLogin {
    public static String getToken(String code, String redirectUri) throws ClientProtocolException, IOException {
        if (!SocialAuthUtil.isGoogleConfigured()) {
            throw new IllegalStateException("Google login chưa được cấu hình đầy đủ.");
        }

        String clientId = SecretConfig.get("GOOGLE_CLIENT_ID");
        String clientSecret = SecretConfig.get("GOOGLE_CLIENT_SECRET");

        String response = Request.Post(IConstant.GOOGLE_LINK_GET_TOKEN)

                .bodyForm(

                        Form.form()

                                .add("client_id", clientId)

                                .add("client_secret", clientSecret)

                                .add("redirect_uri", redirectUri)

                                .add("code", code)

                                .add("grant_type", IConstant.GOOGLE_GRANT_TYPE)

                                .build()

                )

                .execute().returnContent().asString();


        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);

        if (jobj == null || !jobj.has("access_token")) {
            throw new IllegalStateException("Không lấy được access token từ Google.");
        }

        String accessToken = jobj.get("access_token").toString().replaceAll("\"", "");

        return accessToken;

    }
    public static GoogleAccount getUserInfo(final String accessToken) throws ClientProtocolException, IOException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token Google không hợp lệ.");
        }

        String link = IConstant.GOOGLE_LINK_GET_USER_INFO + accessToken;

        String response = Request.Get(link).execute().returnContent().asString();

        GoogleAccount googlePojo = new Gson().fromJson(response, GoogleAccount.class);

        return googlePojo;

    }
}
