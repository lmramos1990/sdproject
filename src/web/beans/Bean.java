package web.beans;

import com.opensymphony.xwork2.Action;
import shared.AuctionInterface;
import shared.Encryptor;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.*;

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
    private int rmiPort;

    private int numberOfRetries = 40;

    private ArrayList<UserObject> onlineUsersList;
    private ArrayList<SearchAuctionObject> searchAuctionList;

    private DetailAuctionObject detailAuctionObject;
    private ArrayList<MyAuctionsObject> myAuctionsList;

    private String username;
    private String password;

    private String auctionid;
    private String articlecode;
    private String title;
    private String description;
    private String deadline;
    private String amount;
    private String text;

    private String token;
    private String id;

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

        System.out.println(reply);

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

        if(fAmount <= 0) return Action.ERROR;

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

        ArrayList<SearchAuctionObject> objects = getSearchAuctionList();

        for(int i = 0; i < numberofobjects; i++) {
            title = hreply.get("items_" + i + "_title");
            auctionid = hreply.get("items_" + i + "_id");
            objects.add(new SearchAuctionObject(articlecode, title, auctionid));
        }

        setSearchAuctionList(objects);

        return Action.SUCCESS;
    }

    public String detailauction() {
        int id;

        try {
            id = Integer.parseInt(getAuctionid());
        } catch(Exception e) {
            return Action.ERROR;
        }

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.detailAuction(id);
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN") || reply.equals("type: detail_auction, ok: false")) return Action.ERROR;
        }

        HashMap<String, String> hreply = new HashMap<>();
        Arrays.stream(reply.split(",")).map(s -> s.split(":")).forEach(i -> hreply.put(i[0].trim(), i[1].trim()));

        int numberofmessages = Integer.parseInt(hreply.get("messages_count"));
        int numberofbids = Integer.parseInt(hreply.get("bids_count"));

        ArrayList<MessageObject> messages = new ArrayList<>();
        ArrayList<BidObject> bids = new ArrayList<>();

        for(int i = 0; i < numberofmessages; i++) {
            String username = hreply.get("messages_" + i + "_user");
            String text = hreply.get("messages_" + i + "_text");
            messages.add(new MessageObject(username, text));
        }

        for(int i = 0; i < numberofbids; i++) {
            String username = hreply.get("bids_" + i + "_user");
            String amount = hreply.get("bids_" + i + "_amount");
            bids.add(new BidObject(username, amount));
        }

        DetailAuctionObject details;

        String title = hreply.get("title");
        String description = hreply.get("description");
        String deadline = hreply.get("deadline");

        details = new DetailAuctionObject(getAuctionid(), title, description, deadline, messages, bids);

        setDetailAuctionObject(details);

        return Action.SUCCESS;
    }

    public String myauctions() {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.myAuctions(getUsername());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN") || reply.equals("type: my_auctions, ok: false")) return Action.ERROR;
        }

        HashMap<String, String> hreply = new HashMap<>();
        Arrays.stream(reply.split(",")).map(s -> s.split(":")).forEach(i -> hreply.put(i[0].trim(), i[1].trim()));

        ArrayList<MyAuctionsObject> myAuctions = getMyAuctionsList();

        int numberofauctions = Integer.parseInt(hreply.get("items_count"));

        for(int i = 0; i < numberofauctions; i++) {
            String auctionid = hreply.get("items_" + i + "_id");
            String articlecode = hreply.get("items_" + i + "_code");
            String title = hreply.get("items_" + i + "_title");
            myAuctions.add(new MyAuctionsObject(auctionid, articlecode, title));
        }

        setMyAuctionsList(myAuctions);

        return Action.SUCCESS;
    }

    public String bid(String uuid) {
        float fAmount;
        int id;

        try {
            id = Integer.parseInt(getAuctionid());
        } catch(Exception e) {
            return Action.ERROR;
        }

        try {
            fAmount = Float.parseFloat(getAmount());
        } catch(Exception e) {
            return Action.ERROR;
        }

        if(fAmount <= 0) return Action.ERROR;

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.bid(uuid, getUsername(), id, fAmount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                    System.out.println("[SERVER][BID] FOUND THE RMI SERVER");
                } catch(Exception e2) {System.out.println("[SERVER][BID] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN") || reply.equals("type: bid, ok: false")) return Action.ERROR;
        else return Action.SUCCESS;
    }

    public String editauction(String uuid) {
        String etitle, edescription, edeadline, ecode, eamount;

        if(getTitle() == null || getTitle().equals("")) etitle = "";
        else etitle = getTitle();

        if(getDescription() == null || getDescription().equals("")) edescription = "";
        else edescription = getDescription();

        if(getDeadline() == null || getDeadline().equals("")) edeadline = "";
        else edeadline = getDeadline();

        if(getArticlecode() == null || getArticlecode().equals("")) ecode = "";
        else ecode = getArticlecode();

        if(getAmount() == null || getAmount().equals("")) eamount = "";
        else eamount = getAmount();

        int id;
        float fAmount = -1.0f;
        boolean isNumber;

        try {
            id = Integer.parseInt(getAuctionid());
        } catch(Exception e) {
            return Action.ERROR;
        }

        if(eamount.equals("")) {
            fAmount = -1.0f;
            isNumber = true;
        } else {
            try {
                fAmount = Float.parseFloat(eamount);
                if(fAmount <= 0) return Action.ERROR;
                isNumber = true;
            } catch(Exception e) {
                isNumber = false;
            }
        }

        if(!edeadline.equals("")) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
                LocalDateTime dateTime = LocalDateTime.parse(getDeadline(), formatter);
                Timestamp timestamp = Timestamp.valueOf(dateTime);
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                if(timestamp.before(currentTime)) return Action.ERROR;
            } catch(Exception e) {
                return Action.ERROR;
            }
        }

        if(!isNumber) return Action.ERROR;

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.editAuction(uuid, getUsername(), id, etitle, edescription, edeadline, ecode, fAmount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                    System.out.println("[SERVER][EDIT AUCTION] FOUND THE RMI SERVER");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN") || reply.equals("type: edit_auction, ok: false")) return Action.ERROR;
        else return Action.SUCCESS;
    }

    public String message(String uuid) {
        int id;

        try {
            id = Integer.parseInt(getAuctionid());
        } catch(Exception e) {
            return Action.ERROR;
        }

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.message(uuid, getUsername(), id, getText());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN") || reply.equals("type: message, ok: false")) return Action.ERROR;
        else return Action.SUCCESS;
    }

    public String onlineusers() {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.onlineUsers(getUsername());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                    System.out.println("[SERVER][ONLINE USERS] FOUND THE RMI SERVER");
                } catch(Exception e2) {System.out.println("[SERVER][ONLINE USERS] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN") || reply.equals("type: online_users, ok: false")) reply = Action.ERROR;
        }

        HashMap<String, String> hreply = new HashMap<>();
        Arrays.stream(reply.split(",")).map(s -> s.split(":")).forEach(i -> hreply.put(i[0].trim(), i[1].trim()));

        int numberofusers = Integer.parseInt(hreply.get("users_count"));
        ArrayList<UserObject> onlineUsers = getOnlineUsersList();

        for(int i = 0; i < numberofusers; i++) {
            String username = hreply.get("users_" + i + "_username");
            onlineUsers.add(new UserObject(username));
        }

        setOnlineUsersList(onlineUsers);

        return Action.SUCCESS;
    }

    public String getArticleCodeFromAuctionId() {

        int id;

        try {
            id = Integer.parseInt(getAuctionid());
        } catch(Exception e) {
            return Action.ERROR;
        }

        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.getArticleCodeFromAuctionId(id);
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN") || reply.equals("error")) return Action.ERROR;
        }

        return reply;
    }

    public ArrayList<String> startUpNotifications() {
        ArrayList<String> notifications = new ArrayList<>();
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                notifications = iBei.getNotifications(getUsername());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) return null;
        }

        return notifications;
    }

    public String savefacebookid() {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.saveFacebookID(getUsername(), getToken(), getId());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) return Action.ERROR;
        }

        if(reply.equals("error") || reply.equals("false")) return Action.ERROR;
        else return Action.SUCCESS;
    }

    public String getUserById() {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                reply = iBei.getUserById(getId());
                reconnected = true;
            } catch(Exception e) {
                try {
                    iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) return Action.ERROR;
        }

        if(reply.equals("[ADMIN]")) return Action.ERROR;
        else return reply;
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

    public ArrayList<UserObject> getOnlineUsersList() {
        return onlineUsersList;
    }

    public void setOnlineUsersList(ArrayList<UserObject> users) {
        this.onlineUsersList= users;
    }

    public ArrayList<SearchAuctionObject> getSearchAuctionList() {
        return searchAuctionList;
    }

    public void setSearchAuctionList(ArrayList<SearchAuctionObject> searchAuctionList) {
        this.searchAuctionList = searchAuctionList;
    }

    public DetailAuctionObject getDetailAuctionObject() {
        return detailAuctionObject;
    }

    public void setDetailAuctionObject(DetailAuctionObject detailAuctionObject) {
        this.detailAuctionObject = detailAuctionObject;
    }

    public ArrayList<MyAuctionsObject> getMyAuctionsList() {
        return myAuctionsList;
    }

    public void setMyAuctionsList(ArrayList<MyAuctionsObject> myAuctionsList) {
        this.myAuctionsList = myAuctionsList;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}