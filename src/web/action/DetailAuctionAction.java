package web.action;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;
import web.beans.Bean;
import web.beans.DetailAuctionObject;

import java.util.Map;

public class DetailAuctionAction implements SessionAware {
    private Map<String, Object> session;
    private String auctionid;

    public String execute() {
        if(auctionid == null) return "stay";
        session.remove("notifications");

        Bean myBean = new Bean();
        myBean.setAuctionid(getAuctionid());
        myBean.setDetailAuctionObject(new DetailAuctionObject());

        String reply = myBean.detailauction();

        if(reply.equals(Action.SUCCESS)) {
            session.put("detailauctionbean", myBean);
        }

        return reply;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }
}
