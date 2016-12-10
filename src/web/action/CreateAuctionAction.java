package web.action;

import org.apache.struts2.interceptor.SessionAware;
import web.beans.Bean;

import java.util.Map;
import java.util.UUID;

public class CreateAuctionAction implements SessionAware {
    private Map<String, Object> session;

    private String username;
    private String articlecode;
    private String title;
    private String description;
    private String deadline;
    private String amount;

    public String execute() {

        if(username == null && articlecode == null && title == null && description == null && deadline == null && amount == null) return "stay";

        setUsername(session.get("username").toString());

        Bean myBean = new Bean();
        myBean.setUsername(getUsername());
        myBean.setArticlecode(getArticlecode());
        myBean.setTitle(getTitle());
        myBean.setDescription(getDescription());
        myBean.setDeadline(getDeadline());
        myBean.setAmount(getAmount());

        String uuid = UUID.randomUUID().toString();

        return myBean.createauction(uuid);
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

    public String getArticlecode() {
        return articlecode;
    }

    public void setArticlecode(String articlecode) {
        this.articlecode = articlecode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
