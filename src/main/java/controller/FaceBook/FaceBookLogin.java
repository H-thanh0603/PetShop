package controller.FaceBook;
import Constant.IConstant;
import Model.FbAccount.Account;
import Util.SecretConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class FaceBookLogin {
    static String CLIENT_ID = SecretConfig.get("facebook_client_id");
    static String CLIENT_SECRET = SecretConfig.get("facebook_client_secret");
    public static String getToken(String code) throws ClientProtocolException, IOException {
        String link = "https://graph.facebook.com/v19.0/oauth/access_token?"
                + "client_id=" + CLIENT_ID
                + "&client_secret=" + CLIENT_SECRET
                + "&redirect_uri=" + IConstant.facebook_redirect_uri
                + "&code=" + code;

        URL url = new URL(link);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(url.openStream()));

        String response = reader.readLine();

        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        return json.get("access_token").getAsString();
    }
    public static Account getUserInfo(final String accessToken) throws ClientProtocolException, IOException {
        String link = IConstant.facebook_link_get_user_info + accessToken;
        String response = Request.Get(link).execute().returnContent().asString();
        Account fbAccount= new Gson().fromJson(response, Account .class);
        return fbAccount;
    }
}
