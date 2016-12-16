package web.action;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;
import shared.RequestObject;
import web.beans.Bean;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class RegisterAction implements SessionAware {
    private Map<String, Object> session;
    private String username;
    private String password;

    public String execute() {

        if(username == null || password == null) return "stay";

        String uuid = UUID.randomUUID().toString();
        RequestObject requestObject = new RequestObject(uuid, 0);

        ArrayList<RequestObject> requests = (ArrayList<RequestObject>) session.get("requestsmap");

        if(requests == null) {
            requests = new ArrayList<>();
            requests.add(requestObject);
        } else if(!requests.contains(requestObject)) requests.add(requestObject);

        session.put("requestsmap", requests);

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setPassword(getPassword());

        String reply = myBean.register(uuid);

        requests.remove(requestObject);
        session.put("requestsmap", requests);

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