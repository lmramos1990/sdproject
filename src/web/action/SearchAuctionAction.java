package web.action;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;
import web.beans.Bean;
import web.beans.SearchAuctionObject;

import java.util.ArrayList;
import java.util.Map;

public class SearchAuctionAction implements SessionAware {
    private Map<String, Object> session;
    private String articlecode;

    public String execute() {
        if(articlecode == null) return "stay";

        ArrayList<SearchAuctionObject> hello = new ArrayList<>();

        Bean myBean = new Bean();
        myBean.setArticlecode(getArticlecode());
        myBean.setSearchAuctionObjects(hello);

        String reply = myBean.searchauction();

        if(reply.equals(Action.SUCCESS)) {
            session.put("bean", myBean);
            return Action.SUCCESS;
        } else return Action.ERROR;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getArticlecode() {
        return articlecode;
    }

    public void setArticlecode(String articlecode) {
        this.articlecode = articlecode;
    }
}
