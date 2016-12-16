package web.action;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Map;

public class LogOutAction implements SessionAware {
    private Map<String, Object> session;

    public String execute() {
        session.remove("loggedin");
        session.remove("username");
        session.remove("notifications");

        return Action.SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
