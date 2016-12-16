package web.action;

import web.beans.Bean;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;

import java.util.ArrayList;
import java.util.Map;

public class LoginAction implements SessionAware {
    private Map<String, Object> session;

    private String username;
    private String password;

    public String execute() throws Exception {
        String reply;

        if(username == null || password == null) return "stay";

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setPassword(getPassword());

        reply = myBean.login();

        if(reply.equals(Action.SUCCESS)) {
            ArrayList<String> notifications = myBean.startUpNotifications();

            session.put("notifications", notifications);
            session.put("username", getUsername());
            session.put("loggedin", true);
        }

        return reply;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
