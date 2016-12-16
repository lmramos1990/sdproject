package web.action;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;
import web.beans.Bean;

import java.util.ArrayList;
import java.util.Map;

public class OnlineUsersAction implements SessionAware {
    private Map<String, Object> session;
    private String username;


    public String execute() {
        setUsername(session.get("username").toString());
        session.remove("notifications");

        Bean myBeans = new Bean();

        myBeans.setUsername(getUsername());
        myBeans.setOnlineUsersList(new ArrayList<>());

        String reply = myBeans.onlineusers();

        if(reply.equals(Action.SUCCESS)) {
            session.put("onlinebean", myBeans);
            return Action.SUCCESS;
        } else return Action.ERROR;
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
}
