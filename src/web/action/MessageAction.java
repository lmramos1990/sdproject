package web.action;

import org.apache.struts2.interceptor.SessionAware;
import web.beans.Bean;

import java.util.Map;

public class MessageAction implements SessionAware {
    private Map<String, Object> session;
    private String username;
    private String auctionid;
    private String text;

    public String execute() {
        if(username == null && auctionid == null && text == null) return "stay";

        setUsername(session.get("username").toString());

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setAuctionid(getAuctionid());
        myBean.setText(getText());

        return myBean.message();
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
