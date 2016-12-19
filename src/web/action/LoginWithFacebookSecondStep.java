package web.action;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import web.beans.Bean;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class LoginWithFacebookSecondStep implements SessionAware {
    private Map<String, Object> session;
    private String username;
    private String appsecret;
    private String appid;

    public String execute() {
        readProperties();
        HttpServletRequest servletRequest = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String code = servletRequest.getParameter("code");

        OAuthService service = new ServiceBuilder().provider(FacebookApi.class).apiKey(appid).apiSecret(appsecret).callback("http://localhost:8080/facebooklogin/").scope("publish_actions").build();

        Verifier verifier = new Verifier(code);

        Token accessToken = service.getAccessToken(null, verifier);
        String token = accessToken.getToken();
        String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL, service);
        service.signRequest(accessToken, request);
        Response response = request.send();

        if(response.getCode() != 200) {
            return Action.ERROR;
        }

        JSONObject json = null;

        try {
            json = (JSONObject) new JSONParser().parse(response.getBody());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String id = json.get("id").toString();

        Bean myBean = new Bean();
        myBean.setId(id);

        String reply = myBean.getUserById();

        if(reply.equals(Action.ERROR) || reply.equals("[ADMIN]")) return Action.ERROR;

        session.put("username", reply);
        session.put("loggedin", true);

        return Action.SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    private void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "../../config.properties";

            System.out.println();

            inputStream = new FileInputStream(propFileName);

            prop.load(inputStream);

            appsecret = prop.getProperty("appsecret");
            appid = prop.getProperty("appid");
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
