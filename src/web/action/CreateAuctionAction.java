package web.action;

import web.beans.Bean;

import java.util.UUID;

public class CreateAuctionAction {

    private String username;
    private String articlecode;
    private String title;
    private String description;
    private String deadline;
    private String amount;

    public String execute() {

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
