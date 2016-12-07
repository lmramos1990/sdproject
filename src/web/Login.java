package web;

import console.AuctionInterface;
import console.Encryptor;
import org.apache.struts2.interceptor.SessionAware;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class Login implements SessionAware {

    private AuctionInterface iBei;
    private int numberOfRetries = 40;
    private Map<String, Object> session;

    private String username;
    private String password;

    public String execute() {
        String reply;

        reply = checkSession();

        if(reply.equals("redirect")) return "redirect";

        reply = RMIConnection();

        if(reply.equals("SERVER DOWN")) return "error";

        reply = doLogin(getUsername(), getPassword());

        if(reply.equals("success")) {
            session.put("username", username);
            session.put("loggedin", true);
        }

        return reply;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    private String checkSession() {
        if(session.get("username") != null && session.get("loggedin") != null) return "redirect";
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

    private String doLogin(String username, String password) {
        String isUser = isUser(username);

        if(isUser.equals("SERVER DOWN")) return "error";
        else if(isUser.equals("NO")) return "failure";

        String esalt = getSalt(username);

        if(esalt.equals("SERVER DOWN")) return "error";

        String dsalt;
        dsalt = Encryptor.decrypt(esalt);

        String hpassword;

        try {
            hpassword = generateStrongPasswordHash(password, dsalt);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }

        String reply;

        try {
            reply = iBei.login(username, hpassword);
        } catch (Exception e) {
            return "error";
        }

        if (reply.equals("type: login, ok: true")) return "success";
        else return "failure";
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

    private String getSalt(String username) {
        String reply = "";
        int retries = 0;
        boolean connected = false;

        while(!connected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.getSalt(username);
                connected = true;
            } catch(Exception e) {
                connected = false;
                reply = RMIConnection();
            }
        }

        return reply;
    }

    private String generateStrongPasswordHash(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] bsalt = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, bsalt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(bsalt) + ":" + toHex(hash);

    }

    private String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
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
