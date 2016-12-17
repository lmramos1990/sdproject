package web.action;

import com.opensymphony.xwork2.Action;

public class FacebookAction {
    private String username;
    private String password;

    public String execute() {
        return Action.SUCCESS;
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
