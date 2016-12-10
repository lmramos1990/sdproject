package web.beans;

import com.opensymphony.xwork2.Action;
import shared.AuctionInterface;
import shared.Encryptor;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import java.math.BigInteger;

import java.rmi.registry.LocateRegistry;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bean {
    private AuctionInterface iBei;

    private String rmiHost;
    private String machineHost;
    private int rmiPort;

    private int numberOfRetries = 40;

    private ArrayList<User> users;
    private ArrayList<SearchAuctionObject> searchAuctionObjects;

    private String username;
    private String password;

    private String auctionid;
    private String articlecode;
    private String title;
    private String description;
    private String deadline;
    private String amount;

    private String text;

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

    public String register(String uuid) {
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

    public String createauction(String uuid) {
        float fAmount;

        try {
            fAmount = Float.parseFloat(getAmount());
        } catch(Exception e) {
            return Action.ERROR;
        }

        if(getArticlecode().length() != 13) {
            return Action.ERROR;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
            LocalDateTime dateTime = LocalDateTime.parse(getDeadline(), formatter);
            Timestamp timestamp = Timestamp.valueOf(dateTime);
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            if(timestamp.before(currentTime)) return Action.ERROR;
        } catch(Exception e) {
            return Action.ERROR;
        }

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.createAuction(uuid, getUsername(), getArticlecode(), getTitle(), getDescription(), getDeadline(), fAmount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN") || reply.equals("type: create_auction, ok: false")) return Action.ERROR;
        else return Action.SUCCESS;
    }

    public String searchauction() {
        System.out.println("search auction");
        if(getArticlecode().length() != 13) return Action.ERROR;

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.searchAuction(getArticlecode());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN") || reply.equals("type: search_auction, ok: false")) return Action.ERROR;
        }

        HashMap<String, String> hreply = new HashMap<>();
        Arrays.stream(reply.split(",")).map(s -> s.split(":")).forEach(i -> hreply.put(i[0].trim(), i[1].trim()));

        if(Integer.parseInt(hreply.get("items_count")) == 0) return "no items";

        int numberofobjects = Integer.parseInt(hreply.get("items_count"));

        String title;
        String auctionid;
        String articlecode = hreply.get("items_0_code");

        ArrayList<SearchAuctionObject> objects = getSearchAuctionObjects();

        for(int i = 0; i < numberofobjects; i++) {
            title = hreply.get("items_" + i + "_title");
            auctionid = hreply.get("items_" + i + "_id");
            objects.add(new SearchAuctionObject(articlecode, title, auctionid));
        }

        setSearchAuctionObjects(objects);

        return Action.SUCCESS;
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

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
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

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<SearchAuctionObject> getSearchAuctionObjects() {
        return searchAuctionObjects;
    }

    public void setSearchAuctionObjects(ArrayList<SearchAuctionObject> searchAuctionObjects) {
        this.searchAuctionObjects = searchAuctionObjects;
    }
}