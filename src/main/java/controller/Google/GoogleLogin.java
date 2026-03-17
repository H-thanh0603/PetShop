package controller.Google;

import Constant.IConstant;
import Model.GgAccount.GoogleAccount;
import Util.SecretConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class GoogleLogin {
    static String CLIENT_ID = SecretConfig.get("GOOGLE_CLIENT_ID");
    static String CLIENT_SECRET = SecretConfig.get("GOOGLE_CLIENT_SECRET");
    public static String getToken(String code) throws ClientProtocolException, IOException {

        String response = Request.Post(IConstant.GOOGLE_LINK_GET_TOKEN)

                .bodyForm(

                        Form.form()

                                .add("client_id", CLIENT_ID)

                                .add("client_secret", CLIENT_SECRET)

                                .add("redirect_uri", IConstant.GOOGLE_REDIRECT_URI)

                                .add("code", code)

                                .add("grant_type", IConstant.GOOGLE_GRANT_TYPE)

                                .build()

                )

                .execute().returnContent().asString();


        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);

        String accessToken = jobj.get("access_token").toString().replaceAll("\"", "");

        return accessToken;

    }
    public static GoogleAccount getUserInfo(final String accessToken) throws ClientProtocolException, IOException {

        String link = IConstant.GOOGLE_LINK_GET_USER_INFO + accessToken;

        String response = Request.Get(link).execute().returnContent().asString();

        GoogleAccount googlePojo = new Gson().fromJson(response, GoogleAccount.class);

        return googlePojo;

    }
}
