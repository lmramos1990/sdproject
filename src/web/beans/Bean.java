package web.beans;

import com.opensymphony.xwork2.Action;
import shared.AuctionInterface;
import shared.Encryptor;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Bean {
    private AuctionInterface iBei;

    private String rmiHost;
    private String machineHost;
    private int rmiPort;

    private int numberOfRetries = 40;

    private ArrayList<Auction> auctions;
    private ArrayList<User> users;
    private ArrayList<Bid> bids;
    private ArrayList<Message> messages;

    private String username;
    private String password;

    public Bean() {

        readProperties();

        int retries = 0;
        boolean connected = false;

        while (!connected && retries < 10) {
            try {
                retries++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                connected = true;
            } catch (Exception e) {
                connected = false;
            }
        }
    }

    private void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "../../config.properties";

            System.out.println();

            inputStream = new FileInputStream(propFileName);

            prop.load(inputStream);

            rmiHost = prop.getProperty("rmiHost");
            machineHost = prop.getProperty("machineHost");
            rmiPort = Integer.parseInt(prop.getProperty("rmiPort"));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String login() {
        String isUser = isUser(getUsername());

        if (isUser.equals("SERVER DOWN") || isUser.equals("NO")) return Action.ERROR;

        String esalt = getSalt(getUsername());

        if (esalt.equals("SERVER DOWN")) return Action.ERROR;

        String dsalt;
        dsalt = Encryptor.decrypt(esalt);

        String hpassword;

        try {
            hpassword = generateStrongPasswordHash(getPassword(), dsalt);
        } catch (Exception e) {
            e.printStackTrace();
            return Action.ERROR;
        }

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while (!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.login(getUsername(), hpassword);
                reconnected = true;
            } catch (Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch (Exception ignored) {}
            }

            if (!reconnected) reply = reconnect(retries);
        }

        if (reply.equals("type: login, ok: true")) return Action.SUCCESS;
        else return Action.ERROR;
    }

    public String register(String uuid, String username, String password) {
        String isUser = isUser(username);

        if(isUser.equals("SERVER DOWN") || isUser.equals("YES")) return Action.ERROR;

        String salt;
        String hpassword;

        try {
            salt = getSalt();
            hpassword = generateStrongPasswordHash(password, salt);
        } catch (Exception e) {
            e.printStackTrace();
            return Action.ERROR;
        }

        String esalt = Encryptor.encrypt(salt);

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while (!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.register(uuid, username, hpassword, esalt);
                reconnected = true;
            } catch (Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch (Exception ignored) {}
            }

            if (!reconnected) reply = reconnect(retries);
        }

        if (reply.equals("type: register, ok: true")) return Action.SUCCESS;
        else return Action.ERROR;
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
            } catch (Exception e) {
                connected = false;
                reply = reconnect(retries);
            }
        }

        return reply;
    }

    private String reconnect(int retries) {
        if (retries == numberOfRetries) {
            return "SERVER DOWN";
        } else {
            try {
                Thread.sleep(1000);
                return "JUST SLEPT";
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "SLEEP ERROR";
            }
        }
    }

    private String getSalt(String username) {
        String reply = "";
        int retries = 0;
        boolean connected = false;

        while (!connected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.getSalt(username);
                connected = true;
            } catch (Exception e) {
                connected = false;
                reply = reconnect(retries);
            }
        }

        return reply;
    }

    private String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Arrays.toString(salt);
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

    public ArrayList<Auction> getAuctions() {
        return auctions;
    }

    public void setAuctions(ArrayList<Auction> auctions) {
        this.auctions = auctions;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Bid> getBids() {
        return bids;
    }

    public void setBids(ArrayList<Bid> bids) {
        this.bids = bids;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
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