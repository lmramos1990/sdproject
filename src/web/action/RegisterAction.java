package web.action;

public class RegisterAction {
    private String username;
    private String password;


    public String execute() {
        System.out.println("CHUPA-ME");
        return "success";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
