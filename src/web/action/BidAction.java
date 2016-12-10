package web.action;

import org.apache.struts2.interceptor.SessionAware;
import web.beans.Bean;

import java.util.Map;

public class BidAction implements SessionAware {
    private Map<String, Object> session;
    private String username;
    private String auctionid;
    private String amount;

    public String execute() {

        if(username == null && auctionid == null && amount == null) return "stay";

        setUsername(session.get("username").toString());

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setAuctionid(getAuctionid());
        myBean.setAmount(getAmount());

        return myBean.bid();
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
