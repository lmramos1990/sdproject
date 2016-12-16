package web.action;

import org.apache.struts2.interceptor.SessionAware;
import shared.RequestObject;
import web.beans.Bean;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class MessageAction implements SessionAware {
    private Map<String, Object> session;
    private String username;
    private String auctionid;
    private String text;

    public String execute() {

        if(username == null && auctionid == null && text == null) return "stay";

        setUsername(session.get("username").toString());
        session.remove("notifications");

        String uuid = UUID.randomUUID().toString();
        RequestObject requestObject = new RequestObject(uuid, 0);

        ArrayList<RequestObject> requests = (ArrayList<RequestObject>) session.get("requestsmap");

        if(requests == null) {
            requests = new ArrayList<>();
            requests.add(requestObject);
        } else if(!requests.contains(requestObject)) requests.add(requestObject);

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setAuctionid(getAuctionid());
        myBean.setText(getText());

        String reply = myBean.message(uuid);

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

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
