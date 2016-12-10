package web.action;

import com.opensymphony.xwork2.Action;
import web.beans.Bean;

public class SearchAuctionAction {

    private String articlecode;

    public String execute() {
        Bean myBean = new Bean();
        myBean.setArticlecode(getArticlecode());

        String reply = myBean.searchauction();

        if(reply.equals(Action.SUCCESS)) {
            // insere este bean na session!
        }

        return reply;
    }

    public String getArticlecode() {
        return articlecode;
    }

    public void setArticlecode(String articlecode) {
        this.articlecode = articlecode;
    }
}
