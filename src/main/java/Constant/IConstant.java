package Constant;

public interface IConstant {
    public static final String facebook_redirect_uri = "http://localhost:8080/PetShop_war/LoginByFacebookServlet";
    public static final String facebook_link_get_token = "https://graph.facebook.com/v19.0/oauth/access_token";
    public static final String facebook_link_get_user_info = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=";

    public static final String GOOGLE_REDIRECT_URI = "http://localhost:8080/PetShop_war/LoginByGoogleServlet";

    public static final String GOOGLE_GRANT_TYPE = "authorization_code";

    public static final String GOOGLE_LINK_GET_TOKEN = "https://accounts.google.com/o/oauth2/token";

    public static final String GOOGLE_LINK_GET_USER_INFO = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";
}
