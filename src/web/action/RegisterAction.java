package web.action;

import com.opensymphony.xwork2.Action;
import shared.AuctionInterface;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.registry.LocateRegistry;
import java.util.Map;
import java.util.UUID;

public class RegisterAction implements SessionAware {
    private AuctionInterface iBei;
    private int numberOfRetries = 40;
    private Map<String, Object> session;

    private String username;
    private String password;


    public String execute() {
        String reply;

        reply = checkSession();

        if(reply.equals("someoneOn")) return Action.SUCCESS;

        reply = RMIConnection();

        if(reply.equals("SERVER DOWN")) return Action.ERROR;

        reply = doRegister(getUsername(), getPassword());

        return reply;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    private String checkSession() {
        if(session.get("username") != null && session.get("loggedin") != null) return "someoneOn";
        else return "";
    }

    private String RMIConnection() {
        String reply = "";
        int retries = 0;
        boolean connected = false;
        while(!connected && retries < 10) {
            try {
                retries++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry("localhost", 10000).lookup("iBei");
                connected = true;
                reply = "CONNECTED";
            } catch(Exception e) {
                connected = false;
                reply = "SERVER DOWN";
            }
        }

        return reply;
    }

    private String doRegister(String username, String password) {
        String isUser = isUser(username);

        if(isUser.equals("SERVER DOWN")) return Action.ERROR;
        else if(isUser.equals("YES")) return Action.ERROR;

        String salt;
        String esalt;
        String hpassword;
        String uuid = UUID.randomUUID().toString();

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.register(uuid, username, hpassword, esalt);
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, registryPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN")) reply = Action.ERROR;
        else {
            if(reply.equals("type: register, ok: true")) reply = Action.SUCCESS;
            else reply = Action.ERROR;
            cleanUpUUID(uuid);
        }

        return reply;
    }

    private String isUser(String username) {
        String reply = "";
        int retries = 0;
        boolean connected = false;
        while(!connected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.isUser(username);
                connected = true;
            } catch(Exception e) {
                connected = false;
                reply = RMIConnection();
            }
        }

        return reply;
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
