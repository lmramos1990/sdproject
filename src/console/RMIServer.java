package console;

import shared.AuctionInterface;
import shared.NotificationCenter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.sql.*;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class RMIServer extends UnicastRemoteObject implements AuctionInterface {
    private static final long serialVersionUID = 1L;
    public static Connection connection;

    static ArrayList<NotificationCenter> serverList = new ArrayList<>();
    static String rmiHost;
    private int rmiPort;

    private RMIServer() throws RemoteException {
        super();
    }

    RMIServer(boolean online) throws RemoteException {
        RMIServer rmiServer = new RMIServer();

        String user = "bd";
        String pass = "oracle";
        String url = "jdbc:oracle:thin:@localhost:1521:XE";

        readProperties();

        System.setProperty("java.rmi.server.hostname", rmiHost);

        Registry registry = LocateRegistry.createRegistry(rmiPort);

        if(online) {
            try {
                try {
                    System.out.println("[RMISERVER] ESTABLISHING CONNECTION TO THE DATABASE");
                    connection = DriverManager.getConnection(url, user, pass);
                    connection.setAutoCommit(false);
                    System.out.println("[RMISERVER] CONNECTION TO THE DATABASE ESTABLISHED");
                } catch(Exception e) {
                    System.out.println("ERROR: CREATING THE CONNECTION TO THE DATABASE");
                    System.exit(0);
                }

                registry.rebind("iBei", rmiServer);

                primaryRMIServer();
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                try {
                    Class.forName("oracle.jdbc.OracleDriver");

                    System.out.println("[RMISERVER] ESTABLISHING CONNECTION TO THE DATABASE");
                    connection = DriverManager.getConnection(url, user, pass);
                    connection.setAutoCommit(false);
                    System.out.println("[RMISERVER] CONNECTION TO THE DATABASE ESTABLISHED");
                } catch(Exception e) {
                    System.out.println("ERROR: CREATING THE CONNECTION TO THE DATABASE");
                    System.exit(0);
                }

                registry.bind("iBei", rmiServer);
                primaryRMIServer();

            } catch(AlreadyBoundException abe) {
                new SecondaryServer();
            }
        }
    }

    public synchronized void subscribe(NotificationCenter nc) throws RemoteException {
        if(serverList.indexOf(nc) == -1) serverList.add(nc);
        else serverList.set(serverList.indexOf(nc), nc);
    }

    public synchronized void removeSubscription(NotificationCenter nc) throws RemoteException {
        if(serverList.indexOf(nc) != -1) serverList.remove(serverList.indexOf(nc));
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        System.getProperties().put("java.security.policy", "policy.all");
        //noinspection deprecation
        System.setSecurityManager(new RMISecurityManager());

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java RMIServer");
            return;
        }

        try {
            new RMIServer(false);
        } catch(Exception e) {
            new SecondaryServer();
        }
    }

    private void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "src/config.properties";

            inputStream = new FileInputStream(propFileName);

            prop.load(inputStream);

            rmiHost = prop.getProperty("rmiHost");
            rmiPort = Integer.parseInt(prop.getProperty("rmiPort"));
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void primaryRMIServer() {
        System.out.println("[RMISERVER] IM THE PRIMARY SERVER");
        new PrimaryServer();
    }

    public String login(String username, String hpassword) throws RemoteException {
        System.out.println("[RMISERVER] LOGIN REQUEST");

        for(NotificationCenter server: serverList) {
            if(server.isUserOnline(username)) return "type: login, ok: false";
        }

        try {
            CallableStatement loginStatement = connection.prepareCall("{ call login(?, ?, ?)}");
            loginStatement.setString(1, username);
            loginStatement.setString(2, hpassword);
            loginStatement.registerOutParameter(3, Types.VARCHAR);
            loginStatement.executeUpdate();

            String result = loginStatement.getString(3);
            loginStatement.close();

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return "type: login, ok: false";
        }
    }

    public String isUser(String username) throws RemoteException {
        if(getClientId(username) == -1) return "NO";
        else return "YES";
    }

    public String getSalt(String username) throws RemoteException {
        try {
            CallableStatement saltStatement = connection.prepareCall("{ ? = call getsalt(?)}");
            saltStatement.registerOutParameter(1, Types.CLOB);
            saltStatement.setString(2, username);
            saltStatement.executeUpdate();

            String result = saltStatement.getString(1);
            saltStatement.close();

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String register(String uuid, String username, String hpassword, String esalt) throws RemoteException {
        System.out.println("[RMISERVER] REGISTER REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: register, ok: true";
        }

        try {
            CallableStatement registerStatement = connection.prepareCall("{ call signup(?, ?, ?, ?)}");
            registerStatement.setString(1, username);
            registerStatement.setString(2, hpassword);
            registerStatement.setString(3, esalt);
            registerStatement.registerOutParameter(4, Types.VARCHAR);
            registerStatement.executeUpdate();

            String result = registerStatement.getString(4);
            registerStatement.close();

            if(result.equals("type: register, ok: true")) {
                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }
            }

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return "type: register, ok: false";
        }
    }

    public String createAuction(String uuid, String username, String code, String title, String description, String deadline, float amount) throws RemoteException {
        System.out.println("[RMISERVER] CREATE AUCTION REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: create_auction, ok: true";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
        LocalDateTime dateTime = LocalDateTime.parse(deadline, formatter);
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        if(timestamp.before(currentTime)) return "type: create_auction, ok: false";

        try {
            CallableStatement createAuctionStatement = connection.prepareCall("{ call createauction(?, ?, ?, ?, ?, ?, ?)}");
            createAuctionStatement.setString(1, username);
            createAuctionStatement.setString(2, code);
            createAuctionStatement.setString(3, title);
            createAuctionStatement.setString(4, description);
            createAuctionStatement.setFloat(5, amount);
            createAuctionStatement.setTimestamp(6, timestamp);
            createAuctionStatement.registerOutParameter(7, Types.VARCHAR);
            createAuctionStatement.executeUpdate();

            String result = createAuctionStatement.getString(7);
            createAuctionStatement.close();

            if(result.equals("type: create_auction, ok: true")) {
                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                int clientid = getClientId(username);

                String token = getToken(clientid);
                String facebookclientid = getFacebookClientId(clientid);

                int auctionid = getLastAuction();

                if(!(token == null || facebookclientid == null || token.equals("error") || token.equals("non existent") || facebookclientid.equals("error") || facebookclientid.equals("non existent"))) new PostOnFacebook(token, facebookclientid, auctionid);
            }

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return "type: create_auction, ok: false";
        }
    }

    public synchronized String searchAuction(String code) throws RemoteException {
        System.out.println("[RMISERVER] SEARCH AUCTION REQUEST");

        System.out.println("[RMISERVER] CHECKING IF THE ARTICLE EXISTS");
        int articleId = getArticleId(code);

        if(articleId == -1) {
            System.out.println("[RMISERVER] THE ARTICLE DOES NOT EXIST");
            return "type: search_auction, ok: false";
        }

        try {
            String articleInAuctionQuery = "SELECT auction.auction_id, auction.title, article.code FROM auction, article WHERE article.article_id = auction.article_id AND auction.article_id = ?";
            PreparedStatement articleInAuctionStatement = connection.prepareStatement(articleInAuctionQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            articleInAuctionStatement.setInt(1, articleId);
            ResultSet articleInAuctionSet = articleInAuctionStatement.executeQuery();

            if(!articleInAuctionSet.next()) {
                articleInAuctionSet.close();
                System.out.println("[RMISERVER] THE ARTICLE DOES NOT BELONG TO ANY AUCTION");
                return "type: search_auction, items_count: 0";
            } else {
                String reply;
                ArrayList <String> items = new ArrayList<>();
                int count = 0;

                articleInAuctionSet.beforeFirst();

                while(articleInAuctionSet.next()) {
                    String item = ", items_" + count + "_id: " + articleInAuctionSet.getInt("auction_id") + ", items_" + count + "_code: " + code + ", items_" + count + "_title: " + articleInAuctionSet.getString("title");
                    count++;
                    items.add(item);
                }

                reply = "type: search_auction, items_count: " + items.size();

                StringBuilder sb = new StringBuilder(reply);

                for(String item : items) {
                    sb.append(item);
                }

                reply = sb.toString();
                articleInAuctionSet.close();
                System.out.println("[RMISERVER] THE ARTICLE BELONGS TO AT LEAST AN AUCTION");
                return reply;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: search_auction, ok: false";
        }
    }

    public synchronized String detailAuction(int id) throws RemoteException {
        System.out.println("[RMISERVER] DETAIL AUCTION REQUEST");

        int auctionId = getAuctionId(id);
        if(auctionId == -1) return "type: detail_auction, ok: false";

        try {
            String getAuctionQuery = "SELECT title, description, deadline FROM auction WHERE auction_id = ?";
            PreparedStatement getAuctionStatement = connection.prepareStatement(getAuctionQuery);
            getAuctionStatement.setInt(1, auctionId);
            ResultSet getAuctionSet = getAuctionStatement.executeQuery();

            String reply;

            if(!getAuctionSet.next()) {
                getAuctionSet.close();
                return "type: detail_auction, ok: false";
            } else {
                String deadline;
                StringBuilder dlsb = new StringBuilder(getAuctionSet.getString("deadline"));

                for(int i = 1; i < 6; i++) {
                    dlsb.deleteCharAt(getAuctionSet.getString("deadline").length() - i);
                }

                dlsb.replace(dlsb.length() - 3, dlsb.length() - 2, "-");
                deadline = dlsb.toString();

                reply = "type: detail_auction, title: " + getAuctionSet.getString("title") + ", description: " + getAuctionSet.getString("description") + ", deadline: " + deadline + ", messages_count: ";
                getAuctionSet.close();
            }

            String getMessageQuery = "SELECT client_id, text FROM message WHERE auction_id = ?";
            PreparedStatement getMessageStatement = connection.prepareStatement(getMessageQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getMessageStatement.setInt(1, auctionId);
            ResultSet getMessageSet = getMessageStatement.executeQuery();

            if(!getMessageSet.next()) {
                getMessageSet.close();
                String messagePartReply = "0";
                reply = reply + messagePartReply;
            } else {

                ArrayList <String> items = new ArrayList<>();
                int count = 0;

                getMessageSet.beforeFirst();

                while(getMessageSet.next()) {
                    String getUserQuery = "SELECT username FROM client WHERE client_id = ?";
                    PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery);
                    getUserStatement.setInt(1, getMessageSet.getInt("client_id"));
                    ResultSet getUserSet = getUserStatement.executeQuery();

                    getUserSet.next();
                    String item = ", messages_" + count + "_user: " + getUserSet.getString("username") + ", messages_" + count + "_text: " + getMessageSet.getString("text");
                    count++;
                    items.add(item);
                    getUserSet.close();
                }
                getMessageSet.close();

                StringBuilder sb = new StringBuilder(reply);
                sb.append(items.size());
                for(String item : items) {
                    sb.append(item);
                }

                reply = sb.toString();
            }

            String getBidsQuery = "SELECT client_id, amount FROM bid WHERE auction_id = ?";
            PreparedStatement getBidsStatement = connection.prepareStatement(getBidsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getBidsStatement.setInt(1, auctionId);
            ResultSet getBidsSet = getBidsStatement.executeQuery();

            if(!getBidsSet.next()) {
                String endString = ", bids_count: 0";
                reply = reply + endString;
                getBidsSet.close();
                return reply;
            } else {
                ArrayList <String> bids = new ArrayList<>();
                int count = 0;
                getBidsSet.beforeFirst();

                while(getBidsSet.next()) {
                    String getUserQuery = "SELECT username FROM client WHERE client_id = ?";
                    PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery);
                    getUserStatement.setInt(1, getBidsSet.getInt("client_id"));
                    ResultSet getUserSet = getUserStatement.executeQuery();

                    getUserSet.next();

                    String bid = ", bids_" + count + "_user: " + getUserSet.getString("username") + ", bids_" + count + "_amount: " + getBidsSet.getFloat("amount");
                    count++;
                    bids.add(bid);
                    getUserSet.close();
                }

                StringBuilder sb2 = new StringBuilder(reply);
                sb2.append(", bids_count: ").append(bids.size());
                for(String bid : bids) {
                    sb2.append(bid);
                }

                reply = sb2.toString();
                getBidsSet.close();
                return reply;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: detail_auction, ok: false";
        }
    }

    public synchronized String myAuctions(String username) throws RemoteException {
        System.out.println("[RMISERVER] MY AUCTION REQUEST");

        int clientId = getClientId(username);
        if(clientId == -1) return "type: my_auctions, ok: false";

        try {
            ArrayList<Integer> myAuctions = new ArrayList<>();

            String getAuctionsQuery = "SELECT DISTINCT bid.auction_id FROM bid WHERE bid.client_id = ? UNION SELECT DISTINCT message.auction_id FROM message WHERE message.client_id = ? UNION SELECT DISTINCT auction.auction_id FROM auction WHERE auction.client_id = ?";
            PreparedStatement getAuctionsStatement = connection.prepareStatement(getAuctionsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getAuctionsStatement.setInt(1, clientId);
            getAuctionsStatement.setInt(2, clientId);
            getAuctionsStatement.setInt(3, clientId);
            ResultSet getAuctionsSet = getAuctionsStatement.executeQuery();

            if(!getAuctionsSet.next()) {
                getAuctionsSet.close();
                return "type: my_auctions, items_count: 0";
            } else {

                getAuctionsSet.beforeFirst();

                while(getAuctionsSet.next()) {
                    myAuctions.add(getAuctionsSet.getInt("auction_id"));
                }

                getAuctionsSet.close();
            }

            String reply = "type: my_auctions, items_count: " + myAuctions.size();
            StringBuilder sb = new StringBuilder(reply);

            for(int i = 0; i < myAuctions.size(); i++) {
                String getInfoQuery = "SELECT article.code, auction.title FROM auction, article WHERE article.article_id = auction.article_id AND auction.auction_id = ?";
                PreparedStatement getInfoStatement = connection.prepareStatement(getInfoQuery);
                getInfoStatement.setInt(1, myAuctions.get(i));
                ResultSet getInfoSet = getInfoStatement.executeQuery();

                if(!getInfoSet.next()) {
                    getInfoSet.close();
                    return "type: my_auctions, ok: false";
                } else {
                    String item = ", items_" + i + "_id: " + myAuctions.get(i) + ", items_" + i + "_code: " + getInfoSet.getString("code") + ", items_" + i + "_title: " + getInfoSet.getString("title");
                    sb.append(item);
                    getInfoSet.close();
                }
            }

            reply = sb.toString();
            return reply;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: my_auctions, ok: false";
        }
    }

    public String bid(String uuid, String username, int id, float amount) throws RemoteException {
        System.out.println("[RMISERVER] BID REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if (aServerList.requestStatus(uuid) == 1) return "type: bid, ok: true";
        }

        int clientId = getClientId(username);
        if(clientId == -1) return "type: bid, ok: false";

        try {
            CallableStatement bidStatement = connection.prepareCall("{ call createbid(?, ?, ?, ?)}");
            bidStatement.setFloat(1, amount);
            bidStatement.setInt(2, id);
            bidStatement.setInt(3, clientId);
            bidStatement.registerOutParameter(4, Types.VARCHAR);
            bidStatement.executeUpdate();
            String result = bidStatement.getString(4);
            bidStatement.close();

            if(result.equals("type: bid, ok: true")) {
                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                new BidsPool(username, clientId, id, amount);
            }

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return "type: bid, ok: false";
        }
    }

    public synchronized String editAuction(String uuid, String username, int id, String title, String description, String deadline, String code, float amount) throws RemoteException {
        System.out.println("[RMISERVER] EDIT AUCTION REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: edit_auction, ok: true";
        }

        int atitle;
        int adescription;
        int adeadline;
        int aarticle = -1;
        int aamount;

        int articleId = -1;
        Timestamp timestamp = null;

        if(amount == -1.0f) aamount = 0;
        else aamount = 1;

        int clientId = getClientId(username);
        if(clientId == -1) return "type: edit_auction, ok: false";

        int auctionId = getAuctionId(id);
        if(auctionId == -1) return "type: edit_auction, ok: false";

        if(hasEnded(auctionId)) return "type: edit_auction, ok: false";

        if(assessUserAuction(clientId, auctionId) == -1) return "type: edit_auction, ok: false";

        if(title.equals("")) atitle = 0;
        else atitle = 1;

        if(description.equals("")) adescription = 0;
        else adescription = 1;

        if(!deadline.equals("")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
            LocalDateTime dateTime = LocalDateTime.parse(deadline, formatter);
            timestamp = Timestamp.valueOf(dateTime);
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            if(timestamp.before(currentTime)) return "type: edit_auction, ok: false";
            adeadline = 1;
        } else adeadline = 0;

        if(!(code.equals(""))) {
            articleId = getArticleId(code);
            if(articleId == -1) {
                createArticle(code);
                articleId = getArticleId(code);
                aarticle = 1;
            }
        } else aarticle = 0;

        if(atitle == 0 && adescription == 0 && adeadline == 0 && aarticle == 0 && aamount == 0) return "type: edit_auction, ok: false";

        String myArray = "" + atitle +
                adescription +
                aamount +
                aarticle +
                adeadline;

        StringBuilder iqueryBuilder = new StringBuilder("INSERT INTO history (history_id, auction_id");
        StringBuilder lqueryBuilder = new StringBuilder(" VALUES (history_seq.nextVal, " + auctionId);
        StringBuilder uiqueryBuilder = new StringBuilder("UPDATE auction SET client_id = " + clientId);

        if(myArray.charAt(0) == '1') {
            iqueryBuilder.append(", title");
            lqueryBuilder.append(", ?");
            uiqueryBuilder.append(", title = ?");
        }

        if(myArray.charAt(1) == '1') {
            iqueryBuilder.append(", description");
            lqueryBuilder.append(", ?");
            uiqueryBuilder.append(", description = ?");
        }

        if(myArray.charAt(2) == '1') {
            iqueryBuilder.append(", initial_value");
            lqueryBuilder.append(", ?");
            uiqueryBuilder.append(", initial_value = ?");
        }

        if(myArray.charAt(3) == '1') {
            iqueryBuilder.append(", article_id");
            lqueryBuilder.append(", ?");
            uiqueryBuilder.append(", article_id = ?");
        }

        if(myArray.charAt(4) == '1') {
            iqueryBuilder.append(", deadline");
            lqueryBuilder.append(", ?");
            uiqueryBuilder.append(", deadline = ?");
        }

        iqueryBuilder.append(")");
        lqueryBuilder.append(")");

        String historyQuery = iqueryBuilder.toString() + lqueryBuilder.toString();
        String updateQuery = uiqueryBuilder.toString() + " WHERE auction_id = " + auctionId;

        String oldTitle = "";
        String oldDescription = "";
        Timestamp oldDeadline = null;
        int oldArticleId = -1;
        float oldAmount = -1;

        try {
            String oldQuery = "SELECT title, description, deadline, initial_value, article_id FROM auction WHERE auction_id = ?";
            PreparedStatement oldStatement = connection.prepareStatement(oldQuery);
            oldStatement.setInt(1, id);
            ResultSet oldSet = oldStatement.executeQuery();

            if(!oldSet.next()) {
                System.out.println("SOMETHING WENT VERY WRONG");
                oldSet.close();
            } else {
                oldTitle = oldSet.getString("title");
                oldDescription = oldSet.getString("description");
                oldDeadline = oldSet.getTimestamp("deadline");
                oldArticleId = oldSet.getInt("article_id");
                oldAmount = oldSet.getFloat("initial_value");
                oldSet.close();
            }
        } catch(SQLException e) {e.printStackTrace();}

        try {
            PreparedStatement historyStatement = connection.prepareStatement(historyQuery);
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            int counter = 0;
            for(int i = 0; i < myArray.length(); i++) {
                if(i == 0 && (myArray.charAt(i) == '1')) {
                    counter++;
                    historyStatement.setString(counter, oldTitle);
                    updateStatement.setString(counter, title);
                } else if(i == 1 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setString(counter, oldDescription);
                    updateStatement.setString(counter, description);
                } else if(i == 2 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setFloat(counter, oldAmount);
                    updateStatement.setFloat(counter, amount);
                } else if(i == 3 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setInt(counter, oldArticleId);
                    updateStatement.setInt(counter, articleId);
                } else if(i == 4 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setTimestamp(counter, oldDeadline);
                    updateStatement.setTimestamp(counter, timestamp);
                }
            }

            ResultSet historySet = historyStatement.executeQuery();

            if(!historySet.next()) {
                historySet.close();
                System.out.println("[RMISERVER] DID NOT REGISTER AUCTION INTO HISTORY");
                return "type: edit_auction, ok: false";
            } else {
                historySet.close();
                System.out.println("[RMISERVER] REGISTER AUCTION INTO HISTORY");
            }

            ResultSet updateSet = updateStatement.executeQuery();

            if(!updateSet.next()) {
                updateSet.close();
                System.out.println("[RMISERVER] DID NOT REGISTER CHANGES IN THE AUCTION");
                return "type: edit_auction, ok: false";
            } else {
                updateSet.close();
                connection.commit();
                System.out.println("[RMISERVER] REGISTERED CHANGES IN THE AUCTION");

                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                return "type: edit_auction, ok: true";
            }

        } catch(Exception e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                connection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
            return "type: edit_auction, ok: false";
        }
    }

    public String message(String uuid, String username, int id, String text) throws RemoteException {
        System.out.println("[RMISERVER] MESSAGE REQUEST");

        int clientid = getClientId(username);
        if(clientid == -1) return "type: message, ok: false";

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: message, ok: true";
        }

        try {
            CallableStatement messageStatement = connection.prepareCall("{ call createmessage(?, ?, ?, ?)}");
            messageStatement.setString(1, username);
            messageStatement.setInt(2, id);
            messageStatement.setString(3, text);
            messageStatement.registerOutParameter(4, Types.VARCHAR);
            messageStatement.executeUpdate();

            String result = messageStatement.getString(4);
            messageStatement.close();

            if(result.equals("type: message, ok: true")) {
                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                new MessagePool(username, clientid, id, text);
            }

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return "type: message, ok: false";
        }
    }

    public synchronized String onlineUsers(String username) throws RemoteException {
        System.out.println("[RMISERVER] ONLINE USERS REQUEST");

        ArrayList<String> onlineUsers = getOnlineUsers(username);

        StringBuilder sb = new StringBuilder();
        assert onlineUsers != null;
        sb.append("type: online_users, users_count: ").append(onlineUsers.size());
        for(int i = 0; i < onlineUsers.size(); i++) {
            sb.append(", users_").append(i).append("_username: ").append(onlineUsers.get(i));
        }

        return sb.toString();
    }

    public synchronized void startUpNotifications(String username) throws RemoteException {

        int clientId = getClientId(username);

        if(clientId == -1) return;

        try {
            String getNotificationsQuery = "SELECT whoisfrom, message FROM notification WHERE client_id = ? AND read = 0";
            PreparedStatement getNotificationsStatement = connection.prepareStatement(getNotificationsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getNotificationsStatement.setInt(1, clientId);
            ResultSet getNotificationsSet = getNotificationsStatement.executeQuery();

            if(!getNotificationsSet.next()) {
                getNotificationsSet.close();
                System.out.println("[RMISERVER] NO NEW NOTIFICATION FOR THE USER");
                return;
            } else {
                getNotificationsSet.beforeFirst();

                while(getNotificationsSet.next()) {
                    for(NotificationCenter aServerList : serverList) {
                        String message = "[STARTUP NOTIFICATION] FROM: " + getNotificationsSet.getString("whoisfrom") + " MESSAGE: " + getNotificationsSet.getString("message");
                        aServerList.sendNotificationToUser(username, message);
                    }
                }

                getNotificationsSet.close();
            }

            String updateQuery = "UPDATE notification SET read = 1 WHERE client_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setInt(1, clientId);
            ResultSet updateSet = updateStatement.executeQuery();

            if(!updateSet.next()) {
                System.out.println("[RMISERVER] START UP NOTIFICATIONS DID NOT UPDATE SUCCESSFULLY");
                updateSet.close();
            } else {
                System.out.println("[RMISERVER] START UP NOTIFICATIONS UPDATED SUCCESSFULLY");
                connection.commit();
                updateSet.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
        }
    }

    public synchronized ArrayList<String> getNotifications(String username) throws RemoteException {
        int clientId = getClientId(username);

        if(clientId == -1) return null;

        ArrayList<String> notifications = new ArrayList<>();

        try {
            String getNotificationsQuery = "SELECT whoisfrom, message FROM notification WHERE client_id = ? AND read = 0";
            PreparedStatement getNotificationsStatement = connection.prepareStatement(getNotificationsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getNotificationsStatement.setInt(1, clientId);
            ResultSet getNotificationsSet = getNotificationsStatement.executeQuery();

            if(!getNotificationsSet.next()) {
                getNotificationsSet.close();
                System.out.println("[RMISERVER] NO NEW NOTIFICATION FOR THE USER");
                return null;
            } else {
                getNotificationsSet.beforeFirst();

                while(getNotificationsSet.next()) {
                    String notification = "you got a message from " + getNotificationsSet.getString("whoisfrom") + " that says: " + getNotificationsSet.getString("message");
                    notifications.add(notification);
                }
                getNotificationsSet.close();
            }

            String updateQuery = "UPDATE notification SET read = 1 WHERE client_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setInt(1, clientId);
            ResultSet updateSet = updateStatement.executeQuery();

            if(!updateSet.next()) {
                System.out.println("[RMISERVER] START UP NOTIFICATIONS DID NOT UPDATE SUCCESSFULLY");
                updateSet.close();
            } else {
                System.out.println("[RMISERVER] START UP NOTIFICATIONS UPDATED SUCCESSFULLY");
                connection.commit();
                updateSet.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
        }

        return notifications;
    }

    public synchronized String saveFacebookID(String username, String token, String id) throws RemoteException {
        System.out.println("[RMISERVER] SAVING FACEBOOK STUFF");
        int clientid = getClientId(username);

        if(clientid == -1) return "false";
        String verifyid = verifyId(id);
        if(verifyid.equals("yes")) {
            return updateToken(clientid, token);
        } else if(verifyid.equals("error")) return "false";

        try {
            String registerQuery = "UPDATE client SET token = ?, id = ? WHERE client_id = ?";
            PreparedStatement registerStatement = connection.prepareStatement(registerQuery);
            registerStatement.setString(1, token);
            registerStatement.setString(2, id);
            registerStatement.setInt(3, clientid);
            ResultSet registerSet = registerStatement.executeQuery();

            if(!registerSet.next()) {
                System.out.println("[RMISERVER] TOKEN WAS NOT REGISTERED WITH SUCCESS");
                registerSet.close();
                return "false";
            } else {
                registerSet.close();
                connection.commit();
                System.out.println("[RMISERVER] REGISTERED THE TOKEN IN THE DATABASE WITH SUCCESS");

                return "true";
            }
        } catch(SQLException e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                connection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
            return "false";
        }
    }

    public synchronized String getUserById(String id) throws RemoteException {
        try {
            String query = "SELECT username FROM client WHERE to_char(id) = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return "[ADMIN]";
            } else {
                String username = rs.getString("username");
                rs.close();

                for(NotificationCenter server : serverList) {
                    if(server.isUserOnline(username)) return "[ADMIN]";
                }


                return username;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "[ADMIN]";
        }
    }

    public synchronized String getArticleCodeFromAuctionId(int auctionid) throws RemoteException {
        try {
            String query = "SELECT article.code FROM article, auction WHERE auction.auction_id = ? AND auction.article_id = article.article_id";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, auctionid);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return "error";
            } else {
                String articlecode = rs.getString("code");
                rs.close();
                return articlecode;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private int getClientId(String username) {

        try {
            CallableStatement clientStatement = connection.prepareCall("{ ? = call getclientidbyusername(?)}");
            clientStatement.registerOutParameter(1, Types.INTEGER);
            clientStatement.setString(2, username);
            clientStatement.executeUpdate();

            Integer result = clientStatement.getInt(1);
            clientStatement.close();

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return  -1;
        }
    }

    private int getArticleId(String code) {
        int articleId;

        try {
            String getArticleIdQuery = "SELECT article_id FROM article WHERE to_char(code) = ?";
            PreparedStatement getArticleIdStatement = connection.prepareStatement(getArticleIdQuery);
            getArticleIdStatement.setString(1, code);
            ResultSet getArticleIdSet = getArticleIdStatement.executeQuery();

            if(!getArticleIdSet.next()) {
                getArticleIdSet.close();
                return -1;
            } else {
                articleId = getArticleIdSet.getInt("article_id");
                getArticleIdSet.close();
                return articleId;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private void createArticle(String code) {
        try {
            String createArticleQuery = "INSERT INTO article (article_id, code) VALUES (article_seq.nextVal, ?)";
            PreparedStatement createArticleStatement = connection.prepareStatement(createArticleQuery);
            createArticleStatement.setString(1, code);
            ResultSet createArticleSet = createArticleStatement.executeQuery();

            if(!createArticleSet.next()) {
                createArticleSet.close();
                System.out.println("[RMISERVER] ARTICLE WAS NOT REGISTERED WITH SUCCESS");
            } else {
                createArticleSet.close();
                System.out.println("[RMISERVER] ARTICLE REGISTERED IN THE DATABASE WITH SUCCESS");
                connection.commit();
            }
        } catch(Exception e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                connection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
        }
    }

    private int getAuctionId(int id) {
        try {
            String getAuctionQuery = "SELECT auction_id FROM auction WHERE auction_id = ?";
            PreparedStatement getAuctionStatement = connection.prepareStatement(getAuctionQuery);
            getAuctionStatement.setInt(1, id);
            ResultSet getAuctionSet = getAuctionStatement.executeQuery();

            if(!getAuctionSet.next()) {
                getAuctionSet.close();
                return -1;
            } else {
                getAuctionSet.close();
                return id;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private boolean hasEnded(int auctionId) {
        try {
            String hasEndedQuery = "SELECT deadline FROM auction WHERE auction_id = ?";
            PreparedStatement hasEndedStatement = connection.prepareStatement(hasEndedQuery);
            hasEndedStatement.setInt(1, auctionId);
            ResultSet hasEndedSet = hasEndedStatement.executeQuery();

            if(!hasEndedSet.next()) {
                hasEndedSet.close();
                return true;
            } else {
                Timestamp deadline = hasEndedSet.getTimestamp("deadline");
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                if(deadline.after(currentTime)) {
                    hasEndedSet.close();
                    return false;
                } else {
                    hasEndedSet.close();
                    return true;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return true;
        }
    }

    private int assessUserAuction(int clientId, int auctionId) {
        try {
            String assessUserQuery = "SELECT * FROM auction WHERE auction_id = ? AND client_id = ?";
            PreparedStatement assessUserStatement = connection.prepareStatement(assessUserQuery);
            assessUserStatement.setInt(1, auctionId);
            assessUserStatement.setInt(2, clientId);
            ResultSet assessUserSet = assessUserStatement.executeQuery();

            if(!assessUserSet.next()) {
                assessUserSet.close();
                return -1;
            } else {
                assessUserSet.close();
                return 1;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private ArrayList<String> getOnlineUsers(String username) {
        ArrayList<String> onlineUsers = new ArrayList<>();

        System.out.println("[RMISERVER] CHECKING ONLINE USERS");
        try {
            for(NotificationCenter aServerList : serverList) {
                for(int j = 0; j < aServerList.getOnlineUsers().size(); j++) {
                    if(!aServerList.getOnlineUsers().get(j).equals(username)) {
                        onlineUsers.add((String) aServerList.getOnlineUsers().get(j));
                    }
                }
            }
        } catch(RemoteException re) {
            re.printStackTrace();
            System.out.println("[RMISERVER] ERROR WHEN CHECKING ONLINE USERS");
            return null;
        }

        return onlineUsers;
    }

    private String verifyId(String id) {
        try {
            String query = "SELECT id FROM client WHERE to_char(id) = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return "no";
            } else {
                rs.close();
                return "yes";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String updateToken(int clientid, String token) {
        try {
            String query = "UPDATE client SET token = ? WHERE client_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, token);
            statement.setInt(2, clientid);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return "error";
            } else {
                rs.close();
                return "updated";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String getToken(int clientid) {
        try {
            String query = "SELECT token FROM client WHERE client_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clientid);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return "non existent";
            } else {
                String token = rs.getString("token");
                rs.close();
                return token;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String getFacebookClientId(int clientid) {
        try {
            String query = "SELECT id FROM client WHERE client_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clientid);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return "non existent";
            } else {
                String id = rs.getString("id");
                rs.close();
                return id;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private int getLastAuction() {
        try {
            String query = "SELECT max(auction_id) FROM auction";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            if(!rs.next()) {
                rs.close();
                return -1;
            } else {
                int auctionid = rs.getInt("max(auction_id)");
                rs.close();
                return auctionid;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void test(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class PrimaryServer extends Thread {
    PrimaryServer() {
        this.start();
    }

    public void run() {

        DatagramSocket udpSocket;

        try {
            udpSocket = new DatagramSocket(9876);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        byte[] receiveData = new byte[1];
        byte[] sendData;

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                udpSocket.receive(receivePacket);
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }

            String sentence = "Y";

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();

            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                udpSocket.send(sendPacket);
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

class SecondaryServer extends Thread {
    private DatagramSocket udpSocket;
    private int count = 0;

    SecondaryServer() {
        this.start();
    }

    public void run() {

        try {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(500);
        } catch(Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return;
        }

        InetAddress ipAddress;

        try {
            ipAddress = InetAddress.getByName(RMIServer.rmiHost);
        } catch(Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return;
        }

        byte[] sendData;
        byte[] receiveData = new byte[1];
        String sentence = "A";

        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 9876);

        try {
            udpSocket.send(sendPacket);
        } catch(Exception e) {
            Thread.currentThread().interrupt();
            return;
        }

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            udpSocket.receive(receivePacket);
        } catch(SocketTimeoutException ste) {

            try {
                new RMIServer(true);
            } catch(Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }

            Thread.currentThread().interrupt();
            return;
        } catch(IOException ioe) {
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("[SECONDARY RMISERVER] STARTING THE TESTS OF THE PRIMARY RMISERVER");

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    udpSocket.setSoTimeout(2000);
                } catch(Exception e) {
                    e.printStackTrace();
                    timer.cancel();
                    return;
                }

                InetAddress ipAddress;

                try {
                    ipAddress = InetAddress.getByName(RMIServer.rmiHost);
                } catch(Exception e) {
                    e.printStackTrace();
                    timer.cancel();
                    return;
                }

                byte[] sendData;
                byte[] receiveData = new byte[1];
                String sentence = "A";

                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 9876);

                try {
                    udpSocket.send(sendPacket);
                } catch(Exception e) {
                    timer.cancel();
                    return;
                }

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    udpSocket.receive(receivePacket);
                } catch(SocketTimeoutException ste) {
                    count++;
                } catch(IOException ioe) {
                    timer.cancel();
                    return;
                }

                String receivedSentence = new String(receivePacket.getData());
                StringBuilder sb = new StringBuilder();

                for(int i = 0; i < receivedSentence.length(); i++) {
                    if(!(receivedSentence.charAt(i) == '\0')) {
                        sb.append(receivedSentence.charAt(i));
                    } else break;
                }

                String newString = sb.toString();

                if(!(newString.equals("Y"))) {
                    System.out.println("[SECONDARY RMISERVER] PRIMARY RMISERVER FAILED TO RESPOND");
                } else System.out.println("[SECONDARY RMISERVER] PRIMARY RMISERVER IS ALIVE");

                if(count == 6) {
                    try {
                        new RMIServer(true);
                    } catch(Exception e) {
                        e.printStackTrace();
                        timer.cancel();
                        return;
                    }

                    timer.cancel();
                }
            }
        }, 0, 2000);

        Thread.currentThread().interrupt();
    }
}

class BidsPool extends Thread {
    private String username;
    private int clientId;
    private int auctionId;
    private float amount;

    BidsPool(String username, int clientId, int auctionId, float amount) {
        this.username = username;
        this.clientId = clientId;
        this.auctionId = auctionId;
        this.amount = amount;
        this.start();
    }

    public void run() {
        try {
            String notificationQuery = "SELECT DISTINCT client_id FROM bid WHERE auction_id = ? AND client_id != ?";
            PreparedStatement notificationStatement = RMIServer.connection.prepareStatement(notificationQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            notificationStatement.setInt(1, auctionId);
            notificationStatement.setInt(2, clientId);
            ResultSet notificationSet = notificationStatement.executeQuery();

            if(!notificationSet.next()) {
                notificationSet.close();
                System.out.println("[RMISERVER] NO CLIENTS NEED TO BE NOTIFIED");
                return;
            } else {
                notificationSet.beforeFirst();

                ArrayList<Integer> ids = new ArrayList<>();

                while(notificationSet.next()) {
                    ids.add(notificationSet.getInt("client_id"));
                }
                notificationSet.close();

                for(Integer id : ids) {
                    String getUserQuery = "SELECT username FROM client WHERE client_id = ?";
                    PreparedStatement getUserStatement = RMIServer.connection.prepareStatement(getUserQuery);
                    getUserStatement.setInt(1, id);
                    ResultSet getUserSet = getUserStatement.executeQuery();

                    while (getUserSet.next()) {
                        new BidsNotifier(getUserSet.getString("username"), username, auctionId, amount);
                    }
                    getUserSet.close();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
        }
        interrupt();
    }
}

class BidsNotifier extends Thread {
    private String username;
    private String from;
    private int auctionId;
    private float amount;

    BidsNotifier(String username, String from, int auctionId, float amount) {
        this.username = username;
        this.from = from;
        this.auctionId = auctionId;
        this.amount = amount;
        this.start();
    }

    public void run() {
        String message = "type: notification_bid, id: " + auctionId + ", user: " + from + ", amount: " + amount;

        for(int i = 0; i < RMIServer.serverList.size(); i++) {
            try {
                RMIServer.serverList.get(i).sendNotificationToUser(username, message);
            } catch(RemoteException re) {
                System.out.println("[NOTIFICATION CENTER] COULD NOT NOTIFY " + username);
            }
        }

        interrupt();
    }
}

class MessagePool extends Thread {

    private String username;
    private int clientId;
    private int auctionId;
    private String text;

    MessagePool(String username, int clientId, int auctionId, String text) {
        this.username = username;
        this.clientId = clientId;
        this.auctionId = auctionId;
        this.text = text;
        this.start();
    }

    public void run() {
        ArrayList<Integer> toNotify = new ArrayList<>();

        int ownerId = getOwnerId(auctionId);

        if(ownerId != clientId) {
            toNotify.add(ownerId);
        }

        try {
            String notificationQuery = "SELECT DISTINCT client_id FROM message WHERE auction_id = ? AND client_id != ? AND client_id != ?";
            PreparedStatement notificationStatement = RMIServer.connection.prepareStatement(notificationQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            notificationStatement.setInt(1, auctionId);
            notificationStatement.setInt(2, clientId);
            notificationStatement.setInt(3, ownerId);
            ResultSet notificationSet = notificationStatement.executeQuery();

            if(!notificationSet.next()) {
                notificationSet.close();
            } else {
                notificationSet.beforeFirst();

                while(notificationSet.next()) {
                    toNotify.add(notificationSet.getInt("client_id"));
                }
                notificationSet.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            interrupt();
        }

        ArrayList<String> toNotifyNames = convertIdsToUsernames(toNotify);
        ArrayList<String> currentlyOnline = fetchUsersOnline(username);
        ArrayList<Integer> myArray = new ArrayList<>();

        assert toNotifyNames != null;
        for(int i = 0; i < toNotifyNames.size(); i++) {
            myArray.add(0);
            assert currentlyOnline != null;
            for(String aCurrentlyOnline : currentlyOnline) {
                if (toNotifyNames.get(i).equals(aCurrentlyOnline)) {
                    myArray.set(i, 1);
                }
            }
        }

        try {
            for(int i = 0; i < myArray.size(); i++) {
                if(myArray.get(i) == 1) {
                    new MessageNotifier(toNotifyNames.get(i), auctionId, username, text);
                } else {
                    String saveQuery = "INSERT INTO notification (notification_id, client_id, whoisfrom, message, read) VALUES (notification_seq.nextVal, ?, ?, ?, 0)";
                    PreparedStatement saveStatement = RMIServer.connection.prepareStatement(saveQuery);
                    saveStatement.setInt(1, toNotify.get(i));
                    saveStatement.setString(2, username);
                    saveStatement.setString(3, text);
                    ResultSet saveSet = saveStatement.executeQuery();

                    if(!saveSet.next()) {
                        saveSet.close();
                        System.out.println("[NOTIFIER] SOMETHING WRONG HAPPENED WHEN SAVING THE NOTIFICATION");
                        interrupt();
                    } else {
                        saveSet.close();
                        RMIServer.connection.commit();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                RMIServer.connection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
            interrupt();
        }

        interrupt();
    }

    private int getOwnerId(int auctionId) {
        try {
            String ownerIdQuery = "SELECT client_id FROM auction WHERE auction_id = ?";
            PreparedStatement ownerIdStatement = RMIServer.connection.prepareStatement(ownerIdQuery);
            ownerIdStatement.setInt(1, auctionId);
            ResultSet ownerIdSet = ownerIdStatement.executeQuery();

            if(!ownerIdSet.next()) {
                ownerIdSet.close();
                return -1;
            } else {
                int ownerId = ownerIdSet.getInt("client_id");
                ownerIdSet.close();
                return ownerId;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private ArrayList<String> convertIdsToUsernames(ArrayList<Integer> ids) {

        ArrayList<String> usernames = new ArrayList<>();

        try {
            for(Integer id : ids) {
                String getNamesQuery = "SELECT username FROM client WHERE client_id = ?";
                PreparedStatement getNamesStatement = RMIServer.connection.prepareStatement(getNamesQuery);
                getNamesStatement.setInt(1, id);
                ResultSet getNamesSet = getNamesStatement.executeQuery();

                if (!getNamesSet.next()) {
                    System.out.println("[NOTIFIER] SOME ERROR OCURRED WHEN FETCHING USERNAMES");
                    getNamesSet.close();
                    return null;
                } else {
                    usernames.add(getNamesSet.getString("username"));
                    getNamesSet.close();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return null;
        }

        return usernames;
    }

    private ArrayList<String> fetchUsersOnline(String username) {
        ArrayList<String> currentlyOnline = new ArrayList<>();

        System.out.println("[NOTIFIER] CHECKING ONLINE USERS");
        try {
            for(int i = 0; i < RMIServer.serverList.size(); i++) {
                for(int j = 0; j < RMIServer.serverList.get(i).getOnlineUsers().size(); j++) {
                    if(!RMIServer.serverList.get(i).getOnlineUsers().get(j).equals(username)) {
                        currentlyOnline.add((String) RMIServer.serverList.get(i).getOnlineUsers().get(j));
                    }
                }
            }
        } catch(RemoteException re) {
            re.printStackTrace();
            System.out.println("[NOTIFIER] ERROR WHEN CHECKING ONLINE USERS");
            return null;
        }

        return currentlyOnline;
    }
}

class MessageNotifier extends Thread {
    private String username;
    private String from;
    private int auctionId;
    private String text;

    MessageNotifier(String username, int auctionId, String from, String text) {
        this.username = username;
        this.auctionId = auctionId;
        this.from = from;
        this.text = text;
        this.start();
    }

    public void run() {
        String message = "type: notification_message, id: " + auctionId + ", user: " + from + ", text: " + text;

        for(int i = 0; i < RMIServer.serverList.size(); i++) {
            try {
                RMIServer.serverList.get(i).sendNotificationToUser(username, message);
            } catch(RemoteException re) {
                System.out.println("[NOTIFICATION CENTER] COULD NOT NOTIFY " + username);
            }
        }

        interrupt();
    }
}

class PostOnFacebook extends Thread {

    private String token;
    private String id;
    private int auctionid;

    PostOnFacebook(String token, String id, int auctionid) {
        this.token = token;
        this.id = id;
        this.auctionid = auctionid;
        this.start();
    }

    public void run() {
        String message = "Created a new auction on iBei check it out\nhttp://localhost:8080/detailauction?auctionid=" + auctionid;

        try {
            String url = "https://graph.facebook.com/" + id + "/feed?message=" + URLEncoder.encode(message, "UTF-8") + "&access_token=" + token;
            URL toPost = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) toPost.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("[RMISERVER] JUST POSTED ON FACEBOOK WITHOUT ERRORS");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
