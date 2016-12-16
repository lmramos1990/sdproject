package console;

import shared.AuctionInterface;
import shared.NotificationCenter;

import java.sql.*;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

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
    public static Connection connection, registerConnection, createAuctionConnection, bidConnection, secondaryBidConnection, editAuctionConnection, secondaryEditAuctionConnection, messageConnection;

    public String user = "bd";
    public String pass = "oracle";
    public String url = "jdbc:oracle:thin:@localhost:1521:XE";

    static ArrayList<NotificationCenter> serverList = new ArrayList<>();
    static String rmiHost;
    private int rmiPort;

    private RMIServer() throws RemoteException {
        super();
    }

    RMIServer(boolean online) throws RemoteException {
        RMIServer rmiServer = new RMIServer();

        readProperties();

        System.setProperty("java.rmi.server.hostname", rmiHost);

        Registry registry = LocateRegistry.createRegistry(rmiPort);

        if(online) {
            try {
                try {
                    System.out.println("[RMISERVER] ESTABLISHING CONNECTION TO THE DATABASE");
                    connection = DriverManager.getConnection(url, user, pass);
                    registerConnection = DriverManager.getConnection(url, user, pass);
                    createAuctionConnection = DriverManager.getConnection(url, user, pass);
                    bidConnection = DriverManager.getConnection(url, user, pass);
                    secondaryBidConnection = DriverManager.getConnection(url, user, pass);
                    editAuctionConnection = DriverManager.getConnection(url, user, pass);
                    secondaryEditAuctionConnection = DriverManager.getConnection(url, user, pass);
                    messageConnection = DriverManager.getConnection(url, user, pass);

                    messageConnection.setAutoCommit(false);
                    secondaryEditAuctionConnection.setAutoCommit(false);
                    editAuctionConnection.setAutoCommit(false);
                    secondaryBidConnection.setAutoCommit(false);
                    bidConnection.setAutoCommit(false);
                    createAuctionConnection.setAutoCommit(false);
                    registerConnection.setAutoCommit(false);
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
                    registerConnection = DriverManager.getConnection(url, user, pass);
                    createAuctionConnection = DriverManager.getConnection(url, user, pass);
                    bidConnection = DriverManager.getConnection(url, user, pass);
                    secondaryBidConnection = DriverManager.getConnection(url, user, pass);
                    editAuctionConnection = DriverManager.getConnection(url, user, pass);
                    secondaryEditAuctionConnection = DriverManager.getConnection(url, user, pass);
                    messageConnection = DriverManager.getConnection(url, user, pass);

                    messageConnection.setAutoCommit(false);
                    secondaryEditAuctionConnection.setAutoCommit(false);
                    editAuctionConnection.setAutoCommit(false);
                    secondaryBidConnection.setAutoCommit(false);
                    bidConnection.setAutoCommit(false);
                    createAuctionConnection.setAutoCommit(false);
                    registerConnection.setAutoCommit(false);
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

    public void subscribe(NotificationCenter nc) throws RemoteException {
        if(serverList.indexOf(nc) == -1) serverList.add(nc);
        else serverList.set(serverList.indexOf(nc), nc);
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
            String propFileName = "config.properties";

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
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void primaryRMIServer() {
        System.out.println("[RMISERVER] IM THE PRIMARY SERVER");
        new PrimaryServer();
    }

    public synchronized String login(String username, String hpassword) throws RemoteException {
        System.out.println("[RMISERVER] LOGIN REQUEST");



        System.out.println("[RMISERVER] CHECKING IF USER IS ONLINE");
        try {
            for(NotificationCenter aServerList : serverList) {
                if(aServerList.isUserOnline(username)) {
                    return "type: login, ok: false";
                }
            }
        } catch(RemoteException re) {
            System.out.println("[RMISERVER] ERROR WHEN CHECKING ONLINE USERS");
            re.printStackTrace();
        }

        try {

            CallableStatement cstmt = connection.prepareCall("{ call signin(?, ?, ?)}");
            cstmt.setString(1, username);
            cstmt.setString(2, hpassword);
            cstmt.registerOutParameter(3, Types.VARCHAR);
            cstmt.executeUpdate();
            String result = cstmt.getString(3);
            cstmt.close();

            return result;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: login, ok: false";
        }
    }

    public synchronized String isUser(String username) throws RemoteException {
        if(getClientId(username) == -1) return "NO";
        else return "YES";
    }

    public synchronized String getSalt(String username) throws RemoteException {
        int clientId = getClientId(username);
        if(clientId == -1) return "";

        try {
            String saltQuery = "SELECT esalt FROM client WHERE client_id = ?";
            PreparedStatement saltStatement = connection.prepareStatement(saltQuery);
            saltStatement.setInt(1, clientId);
            ResultSet saltSet = saltStatement.executeQuery();

            if(!saltSet.next()) {
                saltSet.close();
                return "";
            } else {
                String esalt = saltSet.getString("esalt");
                saltSet.close();
                return esalt;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public synchronized String register(String uuid, String username, String hpassword, String esalt) throws RemoteException {
        System.out.println("[RMISERVER] REGISTER REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: register, ok: true";
        }

        if(getClientId(username) != -1) return "type: register, ok: false";

        try {
            String registerQuery = "INSERT INTO client (client_id, username, hpassword, esalt) VALUES(clients_seq.nextVal, ?, ?, ?)";
            PreparedStatement registerStatement = registerConnection.prepareStatement(registerQuery);
            registerStatement.setString(1, username);
            registerStatement.setString(2, hpassword);
            registerStatement.setString(3, esalt);
            ResultSet registerSet = registerStatement.executeQuery();

            if(!registerSet.next()) {
                System.out.println("[RMISERVER] USER WAS NOT REGISTERED WITH SUCCESS");
                registerSet.close();
                return "type: register, ok: false";
            } else {
                registerSet.close();
                registerConnection.commit();
                System.out.println("[RMISERVER] USER REGISTERED IN THE DATABASE WITH SUCCESS");
                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                return "type: register, ok: true";
            }
        } catch(SQLException e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                registerConnection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
            return "type: register, ok: false";
        }
    }

    public synchronized String createAuction(String uuid, String username, String code, String title, String description, String deadline, float amount) throws RemoteException {
        System.out.println("[RMISERVER] CREATE AUCTION REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: create_auction, ok: true";
        }

        int clientId = getClientId(username);
        if(clientId == -1) return "type: create_auction, ok: false";

        int articleId = getArticleId(code);
        if(articleId == -1) {
            createArticle(code);
            articleId = getArticleId(code);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
        LocalDateTime dateTime = LocalDateTime.parse(deadline, formatter);
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        if(timestamp.before(currentTime)) return "type: create_auction, ok: false";

        try {
            String createAuctionQuery = "INSERT INTO auction (auction_id, client_id, article_id, title, description, initial_value, deadline) VALUES(auction_seq.nextVal, ?, ?, ?, ?, ?, ?)";
            PreparedStatement createAuctionStatement = createAuctionConnection.prepareStatement(createAuctionQuery);
            createAuctionStatement.setInt(1, clientId);
            createAuctionStatement.setInt(2, articleId);
            createAuctionStatement.setString(3, title);
            createAuctionStatement.setString(4, description);
            createAuctionStatement.setFloat(5, amount);
            createAuctionStatement.setTimestamp(6, timestamp);
            ResultSet createAuctionSet = createAuctionStatement.executeQuery();

            if(!createAuctionSet.next()) {
                createAuctionSet.close();
                System.out.println("[RMISERVER] AUCTION WAS NOT REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: create_auction, ok: false";
            } else {
                createAuctionSet.close();
                createAuctionConnection.commit();
                System.out.println("[RMISERVER] AUCTION REGISTERED IN THE DATABASE WITH SUCCESS");

                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                return "type: create_auction, ok: true";
            }
        } catch(Exception e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                createAuctionConnection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
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

            String getBidsQuery = "SELECT client_id, value FROM bid WHERE auction_id = ?";
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

                    String bid = ", bids_" + count + "_user: " + getUserSet.getString("username") + ", bids_" + count + "_amount: " + getBidsSet.getFloat("value");
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

    public synchronized String bid(String uuid, String username, int id, float amount) throws RemoteException {
        System.out.println("[RMISERVER] BID REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: bid, ok: true";
        }

        int clientId = getClientId(username);
        if(clientId == -1) return "type: bid, ok: false";

        int auctionId = getAuctionId(id);
        if(auctionId == -1) return "type: bid, ok: false";

        if(hasEnded(auctionId)) return "type: bid, ok: false";

        if(assessValidBid(clientId, auctionId, amount) == -1) return "type: bid, ok: false";

        try {
            String createBidQuery = "INSERT INTO bid (bid_id, client_id, auction_id, value) VALUES (bid_seq.nextVal, ?, ?, ?)";
            PreparedStatement createBidStatement = bidConnection.prepareStatement(createBidQuery);
            createBidStatement.setInt(1, clientId);
            createBidStatement.setInt(2, auctionId);
            createBidStatement.setFloat(3, amount);
            ResultSet createBidSet = createBidStatement.executeQuery();

            if(!createBidSet.next()) {
                createBidSet.close();
                System.out.println("[RMISERVER] BID WAS NOT REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: bid, ok: false";
            } else {
                createBidSet.close();
                bidConnection.commit();
                String updateQuery = "UPDATE auction SET current_value = ? WHERE auction_id = ?";
                PreparedStatement updateStatement = secondaryBidConnection.prepareStatement(updateQuery);
                updateStatement.setFloat(1, amount);
                updateStatement.setInt(2, auctionId);
                ResultSet updateSet = updateStatement.executeQuery();

                if(!updateSet.next()) {
                    updateSet.close();
                    bidConnection.rollback();
                    System.out.println("[RMISERVER] AUCTION WAS NOT UPDATED WITH SUCCESS");
                    return "type: bid, ok: false";
                } else {
                    updateSet.close();
                    secondaryBidConnection.commit();
                    System.out.println("[RMISERVER] AUCTION WAS UPDATED WITH SUCCESS");

                    for(NotificationCenter aServerList : serverList) {
                        aServerList.updateRequest(uuid);
                    }

                    new BidsPool(username, clientId, auctionId, amount);

                    return "type: bid, ok: true";
                }
            }
        } catch(Exception e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                bidConnection.rollback();
                secondaryBidConnection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
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
            PreparedStatement oldStatement = editAuctionConnection.prepareStatement(oldQuery);
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
            PreparedStatement historyStatement = editAuctionConnection.prepareStatement(historyQuery);
            PreparedStatement updateStatement = secondaryEditAuctionConnection.prepareStatement(updateQuery);
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
                editAuctionConnection.commit();
                System.out.println("[RMISERVER] REGISTER AUCTION INTO HISTORY");
            }

            ResultSet updateSet = updateStatement.executeQuery();

            if(!updateSet.next()) {
                updateSet.close();
                editAuctionConnection.rollback();
                System.out.println("[RMISERVER] DID NOT REGISTER CHANGES IN THE AUCTION");
                return "type: edit_auction, ok: false";
            } else {
                updateSet.close();
                secondaryEditAuctionConnection.commit();
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
                editAuctionConnection.rollback();
                secondaryEditAuctionConnection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
            return "type: edit_auction, ok: false";
        }
    }

    public synchronized String message(String uuid, String username, int id, String text) throws RemoteException {
        System.out.println("[RMISERVER] MESSAGE REQUEST");

        for(NotificationCenter aServerList : serverList) {
            if(aServerList.requestStatus(uuid) == 1) return "type: message, ok: true";
        }

        int clientId = getClientId(username);
        if(clientId == -1) return "type: message, ok: false";

        int auctionId = getAuctionId(id);
        if(auctionId == -1) return "type: message, ok: false";

        try {
            String messageQuery = "INSERT INTO message (message_id, client_id, auction_id, text) VALUES(message_seq.nextVal, ?, ?, ?)";
            PreparedStatement messageStatement = messageConnection.prepareStatement(messageQuery);
            messageStatement.setInt(1, clientId);
            messageStatement.setInt(2, auctionId);
            messageStatement.setString(3, text);
            ResultSet messageSet = messageStatement.executeQuery();

            if(!messageSet.next()) {
                messageSet.close();
                System.out.println("[RMISERVER] MESSAGE WAS NOT REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: message, ok: false";
            } else {
                messageSet.close();
                messageConnection.commit();
                System.out.println("[RMISERVER] MESSAGE WAS REGISTERED IN THE DATABASE WITH SUCCESS");

                for(NotificationCenter aServerList : serverList) {
                    aServerList.updateRequest(uuid);
                }

                new MessagePool(username, clientId, auctionId, text);

                return "type: message, ok: true";
            }
        } catch(Exception e) {
            e.printStackTrace();

            System.out.println("[DATABASE] AN ERROR HAS OCURRED ROLLING BACK CHANGES");
            try {
                messageConnection.rollback();
            } catch(SQLException e1) {e1.printStackTrace();}
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

    private int getClientId(String username) {
        try {
            String getIdQuery = "SELECT client_id FROM client WHERE to_char(username) = ?";
            PreparedStatement getIdStatement = connection.prepareStatement(getIdQuery);
            getIdStatement.setString(1, username);
            ResultSet getIdSet = getIdStatement.executeQuery();

            if(!getIdSet.next()) {
                getIdSet.close();
                return -1;
            } else {
                int id = getIdSet.getInt("client_id");
                getIdSet.close();
                return id;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
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

    private int assessValidBid(int clientId, int auctionId, float amount) {
        try {
            String assessBidQuery = "SELECT client_id, current_value, initial_value FROM auction WHERE auction_id = ?";
            PreparedStatement assessBidStatement = connection.prepareStatement(assessBidQuery);
            assessBidStatement.setInt(1, auctionId);
            ResultSet assessBidSet = assessBidStatement.executeQuery();

            if(!assessBidSet.next()) {
                System.out.println("[RMISERVER] NOT POSSIBLE TO REGISTER THIS BID");
                assessBidSet.close();
                return -1;
            } else {
                if(assessBidSet.getInt("client_id") == clientId) return -1;

                float currentValue = assessBidSet.getFloat("current_value");
                float initialValue = assessBidSet.getFloat("initial_value");

                if((currentValue == 0 && initialValue > amount) || (currentValue != 0 && currentValue > amount)) {
                    System.out.println("[RMISERVER] POSSIBLE TO REGISTER THIS BID");
                    assessBidSet.close();
                    return 1;
                } else {
                    System.out.println("[RMISERVER] NOT POSSIBLE TO REGISTER THIS BID");
                    assessBidSet.close();
                    return -1;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
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

    /*private void test(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }*/
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
