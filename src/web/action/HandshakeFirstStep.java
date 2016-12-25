package web.action;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuthService;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class HandshakeFirstStep {
    private String authorizationUrl;

    private String appsecret;
    private String appid;

    public String execute() {
        readProperties();

        OAuthService service = new ServiceBuilder().provider(FacebookApi.class).apiKey(appid).apiSecret(appsecret).callback("http://localhost:8080/facebook/").scope("publish_actions").build();

        String authorizationUrl = service.getAuthorizationUrl(null);

        setAuthorizationUrl(authorizationUrl);

        return "redirect";
    }

    private void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();

            inputStream = getClass().getClassLoader().getResourceAsStream("/config.properties");

            prop.load(inputStream);

            appsecret = prop.getProperty("appSecret");
            appid = prop.getProperty("appId");
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }
}
