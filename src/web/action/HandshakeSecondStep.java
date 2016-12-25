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

public class HandshakeSecondStep implements SessionAware {
    private Map<String, Object> session;
    private String username;
    private String appsecret;
    private String appid;

    public String execute() {
        readProperties();
        HttpServletRequest servletRequest = (HttpServletRequest) ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
        String code = servletRequest.getParameter("code");

        OAuthService service = new ServiceBuilder().provider(FacebookApi.class).apiKey(appid).apiSecret(appsecret).callback("http://localhost:8080/facebook/").scope("publish_actions").build();

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
        setUsername(session.get("username").toString());

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setToken(token);
        myBean.setId(id);

        String reply;

        reply = myBean.savefacebookid();

        if(reply.equals(Action.SUCCESS)) {
            reply = myBean.getUserById();
        } else return Action.ERROR;

        if(reply.equals(Action.ERROR) || !getUsername().equals(reply)) return Action.ERROR;
        else return Action.SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
