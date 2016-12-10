package web.action;

import web.beans.Bean;

import java.util.UUID;

public class RegisterAction {

    private String username;
    private String password;

    public String execute() {
        if(username == null || password == null) return "stay";

        Bean myBean = new Bean();

        myBean.setUsername(username);
        myBean.setPassword(password);

        String uuid = UUID.randomUUID().toString();

        return myBean.register(uuid);
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