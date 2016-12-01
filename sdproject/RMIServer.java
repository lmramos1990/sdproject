package sdproject;

import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.sql.*;
import java.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

class RMIServer extends UnicastRemoteObject implements AuctionInterface {
    private static final long serialVersionUID = 1L;
    private static Properties properties = new Properties();
    private static String rmiRegistryIP = new String();

    private static String user = "bd";
    private static String pass = "oracle";
    private static String url = "jdbc:oracle:thin:@localhost:1521:XE";
    public static Connection connection;

    public static ArrayList<NotificationCenter> serverList = new ArrayList<NotificationCenter>();
    public static String rmiServerIP = new String();
    public static int rmiregistryport = 0;

    protected RMIServer() throws RemoteException {
        super();
    }

    protected RMIServer(boolean online) throws RemoteException {
        RMIServer rmiServer = new RMIServer();

        readProperties();
        Registry registry = LocateRegistry.createRegistry(rmiregistryport);

        if(online == true) {
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
                System.out.println("ERROR: " + e.getMessage());
                return;
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
                SecondaryServer secondaryServer = new SecondaryServer();
            }
        }
    }

    public void subscribe(NotificationCenter nc) throws RemoteException {
        if(serverList.indexOf(nc) == -1) serverList.add(nc);
    }

    public static void main(String[] args) {

        System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new RMISecurityManager());

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java RMIServer");
            return;
        }

        try {
            RMIServer rmiServer = new RMIServer(false);
        } catch(Exception e) {
            SecondaryServer secondaryServer = new SecondaryServer();
        }
    }

    public static void readProperties() {
        InputStream inputStream = null;

        try {
			Properties prop = new Properties();
			String propFileName = "config.properties";

			inputStream = new FileInputStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("ERROR: PROPERTY '" + propFileName + "' NOT FOUND IN THE CLASSPATH");
			}

			rmiRegistryIP = prop.getProperty("rmiRegistryIP");
            rmiServerIP = prop.getProperty("rmiServerIP");
            rmiregistryport = Integer.parseInt(prop.getProperty("rmiregistryport"));

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
            try {
                inputStream.close();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
		}
    }

    private static void primaryRMIServer() {
        System.out.println("[RMISERVER] IM THE PRIMARY SERVER");
        PrimaryServer primaryServer = new PrimaryServer();
    }

    public synchronized String login(String username, String password) throws RemoteException {
        System.out.println("[RMISERVER] LOGIN REQUEST");
        String reply = new String();

        System.out.println("[RMISERVER] CHECKING IF USER IS ONLINE");
        try {
            for(int i = 0; i < serverList.size(); i++) {
                if(serverList.get(i).isUserOnline(username)) {
                    System.out.println("[RMISERVER] SENDING REPLY");
                    return "type: login, ok: false";
                }
            }
        } catch(RemoteException re) {
            System.out.println("[RMISERVER] ERROR WHEN CHECKING ONLINE USERS");
            re.printStackTrace();
        }

        try {
            String loginQuery = "SELECT username, pass FROM client WHERE to_char(username) = ?";
            PreparedStatement loginStatement = connection.prepareStatement(loginQuery);
            loginStatement.setString(1, username);
            ResultSet loginSet = loginStatement.executeQuery();

            if(!loginSet.next()) {
                loginSet.close();
                System.out.println("[RMISERVER] USER DOES NOT EXIST IN THE DATABASE");
                return "type: login, ok: false";
            } else {
                System.out.println("[RMISERVER] CHECKING CREDENTIALS");

                String encryptedPassword = loginSet.getString("pass");
                loginSet.close();

                try {
                    if(validatePassword(password, encryptedPassword)) {
                        System.out.println("[RMISERVER] CREDENTIALS CHECK OUT");
                        return "type: login, ok: true";
                    } else {
                        System.out.println("[RMISERVER] CREDENTIALS DO NOT CHECK OUT");
                        return "type: login, ok: false";
                    }
                } catch(Exception e) {
                    System.out.println("[RMISERVER] SOME ERROR OCURRED WHEN CHECKING CREDENTIALS");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: login, ok: false";
        }

        return "type: login, ok: false";
    }

    public synchronized String register(String username, String password) throws RemoteException {
        System.out.println("[RMISERVER] REGISTER REQUEST");

        int clientId = getClientId(username);

        if(clientId != -1) return "type: register, ok: false";

        String encryptedPassword = new String();

        try {
            encryptedPassword = generateStrongPasswordHash(password);
        } catch(Exception e) {
            System.out.println("[RMISERVER] SOME ERROR OCURRED WHEN ENCRYPTING THE PASSWORD");
            e.printStackTrace();
            return "type: register, ok: false";
        }

        try {
            String registerQuery = "INSERT INTO client (client_id, username, pass) VALUES(clients_seq.nextVal, ?, ?)";
            PreparedStatement registerStatement = connection.prepareStatement(registerQuery);
            registerStatement.setString(1, username);
            registerStatement.setString(2, encryptedPassword);
            ResultSet registerSet = registerStatement.executeQuery();

            if(registerSet.next()) {
                System.out.println("[RMISERVER] USER REGISTERED IN THE DATABASE WITH SUCCESS");
                connection.commit();
                registerSet.close();
                return "type: register, ok: true";
            } else {
                System.out.println("[RMISERVER] USER WAS NOT REGISTERED WITH SUCCESS");
                registerSet.close();
                return "type: register, ok: false";
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: register, ok: false";
        }
    }

    public synchronized String createAuction(String username, String code, String title, String description, String deadline, float amount) throws RemoteException {
        System.out.println("[RMISERVER] CREATE AUCTION REQUEST");

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
            String createAuctionQuery = "INSERT INTO auction (auction_id, client_id, article_id, title, description, initial_value, deadline) VALUES(auction_seq.nextVal, " + clientId + ", " + articleId + ", ?, ?, " + amount + ", ?)";
            PreparedStatement createAuctionStatement = connection.prepareStatement(createAuctionQuery);
            createAuctionStatement.setString(1, title);
            createAuctionStatement.setString(2, description);
            createAuctionStatement.setTimestamp(3, timestamp);
            ResultSet createAuctionSet = createAuctionStatement.executeQuery();

            if(createAuctionSet.next()) {
                connection.commit();
                createAuctionSet.close();
                System.out.println("[RMISERVER] AUCTION REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: create_auction, ok: true";
            } else {
                createAuctionSet.close();
                System.out.println("[RMISERVER] AUCTION WAS NOT REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: create_auction, ok: false";
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: create_auction, ok: false";
        }
    }

    public synchronized String searchAuction(String code) throws RemoteException {
        System.out.println("[RMISERVER] SEARCH AUCTION REQUEST");

        System.out.println("[RMISERVER] CHECKING IF THE ARTICLE EXISTS");
        int articleId = getArticleId(code);

        if(articleId == -1) {
            System.out.println("[RMISERVER] THE ARTICLE DOES NOT EXIST");
            return "type: search_auction, items_count: 0";
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
                String reply = new String();
                ArrayList <String> items = new ArrayList<String>();
                int count = 0;

                articleInAuctionSet.beforeFirst();

                while(articleInAuctionSet.next()) {
                    String item = ", items_" + count + "_id: " + articleInAuctionSet.getInt("auction_id") + ", items_" + count + "_code: " + code + ", items_" + count + "_title: " + articleInAuctionSet.getString("title");
                    count++;
                    items.add(item);
                }

                reply = "type: search_auction, items_count: " + items.size();

                StringBuilder sb = new StringBuilder(reply);

                for(int i = 0;i < items.size(); i++) {
                    sb.append(items.get(i));
                }

                reply = sb.toString();
                articleInAuctionSet.close();
                System.out.println("[RMISERVER] THE ARTICLE BELONGS TO AT LEAST AN AUCTION");
                return reply;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: search_auction, items_count: 0";
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

            String reply = new String();

            if(getAuctionSet.next()) {
                String deadline = new String();
                StringBuilder dlsb = new StringBuilder(getAuctionSet.getString("deadline"));

                for(int i = 1; i < 6; i++) {
                    dlsb.deleteCharAt(getAuctionSet.getString("deadline").length() - i);
                }

                dlsb.replace(dlsb.length() - 3, dlsb.length() - 2, "-");
                deadline = dlsb.toString();

                reply = "type: detail_auction, title: " + getAuctionSet.getString("title") + ", description: " + getAuctionSet.getString("description") + ", deadline: " + deadline + ", messages_count: ";
                getAuctionSet.close();
            } else {
                getAuctionSet.close();
                return "type: detail_auction, ok: false";
            }

            String getMessageQuery = "SELECT client_id, text FROM message WHERE auction_id = ?";
            PreparedStatement getMessageStatement = connection.prepareStatement(getMessageQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getMessageStatement.setInt(1, auctionId);
            ResultSet getMessageSet = getMessageStatement.executeQuery();

            if(!getMessageSet.next()) {
                getMessageSet.close();
                StringBuilder sb = new StringBuilder(reply);
                String messagePartReply = "0";
                sb.append(messagePartReply);
                reply = sb.toString();
            } else {

                ArrayList <String> items = new ArrayList<String>();
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
                for(int i = 0; i < items.size(); i++) {
                    sb.append(items.get(i));
                }

                reply = sb.toString();
            }

            String getBidsQuery = "SELECT client_id, value FROM bid WHERE auction_id = ?";
            PreparedStatement getBidsStatement = connection.prepareStatement(getBidsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getBidsStatement.setInt(1, auctionId);
            ResultSet getBidsSet = getBidsStatement.executeQuery();

            if(!getBidsSet.next()) {
                StringBuilder sb2 = new StringBuilder(reply);
                String endString = ", bids_count: 0";
                sb2.append(endString);
                reply = sb2.toString();
                getBidsSet.close();
                return reply;
            } else {
                ArrayList <String> bids = new ArrayList<String>();
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
                sb2.append(", bids_count: " + bids.size());
                for(int i = 0; i < bids.size(); i++) {
                    sb2.append(bids.get(i));
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
        if(clientId == -1) return "type: my_auctions, items_count: 0";

        try {
            ArrayList<Integer> myAuctions = new ArrayList<Integer>();

            String getAuctionsQuery = "SELECT DISTINCT auction_id FROM auction WHERE (SELECT client_id FROM bid WHERE client_id = ?) = ? OR (SELECT client_id FROM message WHERE client_id = ?) = ? OR client_id = ?";
            PreparedStatement getAuctionsStatement = connection.prepareStatement(getAuctionsQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getAuctionsStatement.setInt(1, clientId);
            getAuctionsStatement.setInt(2, clientId);
            getAuctionsStatement.setInt(3, clientId);
            getAuctionsStatement.setInt(4, clientId);
            getAuctionsStatement.setInt(5, clientId);
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
                    return "type: my_auctions, items_count: 0";
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
            return "type: my_auctions, items_count: 0";
        }
    }

    public synchronized String bid(String username, int id, float amount) throws RemoteException {
        System.out.println("[RMISERVER] BID REQUEST");

        int clientId = getClientId(username);
        if(clientId == -1) return "type: bid, ok: false";

        int auctionId = getAuctionId(id);
        if(auctionId == -1) return "type: bid, ok: false";

        if(hasEnded(auctionId)) return "type: bid, ok: false";

        if(assessValidBid(clientId, auctionId, amount) == -1) return "type: bid, ok: false";

        try {
            String createBidQuery = "INSERT INTO bid (bid_id, client_id, auction_id, value) VALUES (bid_seq.nextVal, ?, ?, ?)";
            PreparedStatement createBidStatement = connection.prepareStatement(createBidQuery);
            createBidStatement.setInt(1, clientId);
            createBidStatement.setInt(2, auctionId);
            createBidStatement.setFloat(3, amount);
            ResultSet createBidSet = createBidStatement.executeQuery();

            if(createBidSet.next()) {
                createBidSet.close();
                connection.commit();
                System.out.println("[RMISERVER] BID REGISTERED IN THE DATABASE WITH SUCCESS");

                String updateQuery = "UPDATE auction SET current_value = ? WHERE auction_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setFloat(1, amount);
                updateStatement.setInt(2, auctionId);
                ResultSet updateSet = updateStatement.executeQuery();

                if(updateSet.next()) {
                    updateSet.close();
                    System.out.println("[RMISERVER] AUCTION WAS UPDATED WITH SUCCESS");

                    BidsPool notifier = new BidsPool(username, clientId, auctionId, amount);

                    return "type: bid, ok: true";
                } else {
                    updateSet.close();
                    System.out.println("[RMISERVER] AUCTION WAS NOT UPDATED WITH SUCCESS");
                    return "type: bid, ok: false";
                }
            } else {
                createBidSet.close();
                System.out.println("[RMISERVER] BID WAS NOT REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: bid, ok: false";
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: bid, ok: false";
        }
    }

    public synchronized String editAuction(String username, int id, String title, String description, String deadline, String code, float amount) throws RemoteException {
        System.out.println("[RMISERVER] EDIT AUCTION REQUEST");

        int atitle = -1;
        int adescription = -1;
        int adeadline = -1;
        int aarticle = -1;
        int aamount = -1;

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

        StringBuilder arrayBuilder = new StringBuilder("");
        arrayBuilder.append(atitle);
        arrayBuilder.append(adescription);
        arrayBuilder.append(aamount);
        arrayBuilder.append(aarticle);
        arrayBuilder.append(adeadline);

        String myArray = arrayBuilder.toString();

        StringBuilder iqueryBuilder = new StringBuilder("INSERT INTO history (history_id, auction_id");
        StringBuilder lqueryBuilder = new StringBuilder(" VALUES (history_seq.nextVal, " + auctionId);

        StringBuilder uiqueryBuilder = new StringBuilder("UPDATE auction SET client_id = " + clientId);
        StringBuilder ulqueryBuilder = new StringBuilder(" WHERE auction_id = " + auctionId);

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
        String updateQuery = uiqueryBuilder.toString() + ulqueryBuilder.toString();

        try {
            PreparedStatement historyStatement = connection.prepareStatement(historyQuery);
            PreparedStatement editAuctionStatement = connection.prepareStatement(updateQuery);
            int counter = 0;
            for(int i = 0; i < myArray.length(); i++) {
                if(i == 0 && (myArray.charAt(i) == '1')) {
                    counter++;
                    historyStatement.setString(counter, title);
                    editAuctionStatement.setString(counter, title);
                } else if(i == 1 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setString(counter, description);
                    editAuctionStatement.setString(counter, description);
                } else if(i == 2 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setFloat(counter, amount);
                    editAuctionStatement.setFloat(counter, amount);
                } else if(i == 3 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setInt(counter, articleId);
                    editAuctionStatement.setInt(counter, articleId);
                } else if(i == 4 && myArray.charAt(i) == '1') {
                    counter++;
                    historyStatement.setTimestamp(counter, timestamp);
                    editAuctionStatement.setTimestamp(counter, timestamp);
                }
            }

            // TODO: ROLLBACKS PA!

            ResultSet historySet = historyStatement.executeQuery();

            if(!historySet.next()) {
                historySet.close();
                System.out.println("[RMISERVER] DID NOT REGISTER AUCTION INTO HISTORY");
                return "type: edit_auction, ok: false";
            } else {
                historySet.close();
                connection.commit();
                System.out.println("[RMISERVER] REGISTER AUCTION INTO HISTORY");
            }

            ResultSet editAuctionSet = editAuctionStatement.executeQuery();

            if(!editAuctionSet.next()) {
                editAuctionSet.close();
                System.out.println("[RMISERVER] DID NOT REGISTER CHANGES IN THE AUCTION");
                return "type: edit_auction, ok: false";
            } else {
                editAuctionSet.close();
                System.out.println("[RMISERVER] REGISTERED CHANGES IN THE AUCTION");
                return "type: edit_auction, ok: true";
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: edit_auction, ok: false";
        }
    }

    public synchronized String message(String username, int id, String text) throws RemoteException {
        System.out.println("[RMISERVER] MESSAGE REQUEST");

        int clientId = getClientId(username);
        if(clientId == -1) return "type: message, ok: false";

        int auctionId = getAuctionId(id);
        if(auctionId == -1) return "type: message, ok: false";

        try {
            String messageQuery = "INSERT INTO message (message_id, client_id, auction_id, text) VALUES(message_seq.nextVal, " + clientId + ", " + auctionId + ", ?)";
            PreparedStatement messageStatement = connection.prepareStatement(messageQuery);
            messageStatement.setString(1, text);
            ResultSet messageSet = messageStatement.executeQuery();

            if(messageSet.next()) {
                messageSet.close();
                connection.commit();
                System.out.println("[RMISERVER] MESSAGE WAS REGISTERED IN THE DATABASE WITH SUCCESS");

                MessagePool notifier = new MessagePool(username, clientId, auctionId, text);

                return "type: message, ok: true";
            } else {
                messageSet.close();
                System.out.println("[RMISERVER] MESSAGE WAS NOT REGISTERED IN THE DATABASE WITH SUCCESS");
                return "type: message, ok: false";
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: message, ok: false";
        }
    }

    public synchronized String onlineUsers(String username) throws RemoteException {
        System.out.println("[RMISERVER] ONLINE USERS REQUEST");

        ArrayList<String> onlineUsers = getOnlineUsers(username);

        StringBuilder sb = new StringBuilder();
        sb.append("type: online_users, users_count: " + onlineUsers.size());
        for(int i = 0; i < onlineUsers.size(); i++) {
            sb.append(", users_" + i + "_username: " + onlineUsers.get(i));
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
                    for(int i = 0; i < serverList.size(); i++) {
                        String message = "[STARTUP NOTIFICATION] FROM: " + getNotificationsSet.getString("whoisfrom") + " MESSAGE: " + getNotificationsSet.getString("message");
                        serverList.get(i).sendNotificationToUser(username, message);
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
                return;
            } else {
                System.out.println("[RMISERVER] START UP NOTIFICATIONS UPDATED SUCCESSFULLY");
                updateSet.close();
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return;
        }
    }

    private boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] testHash = skf.generateSecret(spec).getEncoded();

        int diff = hash.length ^ testHash.length;

        for(int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }

        return diff == 0;
    }

    private String generateStrongPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt().getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);

    }

    private String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }

    private String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0) {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    private byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++) {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private int getClientId(String username) {
        try {
            String getIdQuery = "SELECT client_id FROM client WHERE to_char(username) = ?";
            PreparedStatement getIdStatement = connection.prepareStatement(getIdQuery);
            getIdStatement.setString(1, username);
            ResultSet getIdSet = getIdStatement.executeQuery();

            if(getIdSet.next()) {
                int id = getIdSet.getInt("client_id");
                getIdSet.close();
                return id;
            } else {
                getIdSet.close();
                return -1;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private int getArticleId(String code) {
        int articleId = -1;

        try {
            String getArticleIdQuery = "SELECT article_id FROM article WHERE to_char(code) = ?";
            PreparedStatement getArticleIdStatement = connection.prepareStatement(getArticleIdQuery);
            getArticleIdStatement.setString(1, code);
            ResultSet getArticleIdSet = getArticleIdStatement.executeQuery();

            if(getArticleIdSet.next()) {
                articleId = getArticleIdSet.getInt("article_id");
                getArticleIdSet.close();
                return articleId;
            } else {
                getArticleIdSet.close();
                return -1;
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

            if(createArticleSet.next()) {
                createArticleSet.close();
                System.out.println("[RMISERVER] ARTICLE REGISTERED IN THE DATABASE WITH SUCCESS");
                connection.commit();
            } else {
                createArticleSet.close();
                System.out.println("[RMISERVER] ARTICLE WAS NOT REGISTERED WITH SUCCESS");
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
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

            if(assessBidSet.next()) {

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
            } else {
                System.out.println("[RMISERVER] NOT POSSIBLE TO REGISTER THIS BID");
                assessBidSet.close();
                return -1;
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

            if(assessUserSet.next()) {
                assessUserSet.close();
                return 1;
            } else {
                assessUserSet.close();
                return -1;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private ArrayList<String> getOnlineUsers(String username) {
        ArrayList<String> onlineUsers = new ArrayList<String>();

        System.out.println("[RMISERVER] CHECKING ONLINE USERS");
        try {
            for(int i = 0; i < serverList.size(); i++) {
                for(int j = 0; j < serverList.get(i).getOnlineUsers().size(); j++) {
                    if(!serverList.get(i).getOnlineUsers().get(j).equals(username)) {
                        onlineUsers.add((String) serverList.get(i).getOnlineUsers().get(j));
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
}

class PrimaryServer extends Thread {

    DatagramSocket udpSocket;

    public PrimaryServer() {
        this.start();
    }

    public void run() {
        try {
            udpSocket = new DatagramSocket(9876);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        byte[] receiveData = new byte[1];
        byte[] sendData = new byte[1];

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

    DatagramSocket udpSocket;
    private static int count = 0;

    public SecondaryServer() {
        this.start();
    }

    public void run() {
        try {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(500);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        InetAddress ipAddress = null;

        try {
            // SUBJECT TO CHANGE!!!
            ipAddress = InetAddress.getByName(RMIServer.rmiServerIP);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        byte[] sendData = new byte[1];
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
                RMIServer myServer = new RMIServer(true);
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }

            Thread.currentThread().interrupt();
            return;
        } catch(IOException ioe) {
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("[RMISERVER] IM THE SECONDARY SERVER");

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    udpSocket.setSoTimeout(2000);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }

                InetAddress ipAddress = null;

                try {
                    // SUBJECT TO CHANGE!!!
                    ipAddress = InetAddress.getByName(RMIServer.rmiServerIP);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }

                byte[] sendData = new byte[1];
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
                    System.out.println("PRIMARY SERVER FAILED TO RESPOND");
                } else System.out.println("PRIMARY SERVER IS ALIVE");

                if(count == 12) {
                    try {
                        RMIServer myServer = new RMIServer(true);
                    } catch(Exception e) {
                        System.out.println("ERROR: " + e);
                        timer.cancel();
                        return;
                    }

                    timer.cancel();
                    return;
                }
            }
        }, 0, 2000);

        Thread.currentThread().interrupt();
        return;
    }
}

class BidsPool extends Thread {
    String username;
    int clientId;
    int auctionId;
    float amount;

    public BidsPool(String username, int clientId, int auctionId, float amount) {
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

                ArrayList<Integer> ids = new ArrayList<Integer>();

                while(notificationSet.next()) {
                    ids.add((Integer) notificationSet.getInt("client_id"));
                }
                notificationSet.close();

                for(int i = 0; i < ids.size(); i++) {
                    String getUserQuery = "SELECT username FROM client WHERE client_id = ?";
                    PreparedStatement getUserStatement = RMIServer.connection.prepareStatement(getUserQuery);
                    getUserStatement.setInt(1, ids.get(i));
                    ResultSet getUserSet = getUserStatement.executeQuery();

                    while(getUserSet.next()) {
                        BidsNotifier notifier = new BidsNotifier(getUserSet.getString("username"), username, auctionId, amount);
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

    public BidsNotifier(String username, String from, int auctionId, float amount) {
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

    String username;
    int clientId;
    int auctionId;
    String text;

    public MessagePool(String username, int clientId, int auctionId, String text) {
        this.username = username;
        this.clientId = clientId;
        this.auctionId = auctionId;
        this.text = text;
        this.start();
    }

    public void run() {
        ArrayList<Integer> toNotify = new ArrayList<Integer>();

        int ownerId = getOwnerId(auctionId);

        if(ownerId != clientId) {
            toNotify.add((Integer) ownerId);
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
                    toNotify.add((Integer) notificationSet.getInt("client_id"));
                }
                notificationSet.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            interrupt();
        }

        ArrayList<String> toNotifyNames = new ArrayList<String>();
        toNotifyNames = convertIdsToUsernames(toNotify);

        ArrayList<String> currentlyOnline = new ArrayList<String>();

        currentlyOnline = fetchUsersOnline(username);

        ArrayList<Integer> myArray = new ArrayList<Integer>();

        for(int i = 0; i < toNotifyNames.size(); i++) {
            myArray.add((Integer) 0);
            for(int j = 0; j < currentlyOnline.size(); j++) {
                if(toNotifyNames.get(i).equals(currentlyOnline.get(j))) {
                    myArray.set(i, 1);
                }
            }
        }

        try {
            for(int i = 0; i < myArray.size(); i++) {
                if(myArray.get(i) == 1) {
                    MessageNotifier notifier = new MessageNotifier(toNotifyNames.get(i), auctionId, username, text);
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
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
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

        ArrayList<String> usernames = new ArrayList<String>();

        try {
            for(int i = 0; i < ids.size(); i++) {
                String getNamesQuery = "SELECT username FROM client WHERE client_id = ?";
                PreparedStatement getNamesStatement = RMIServer.connection.prepareStatement(getNamesQuery);
                getNamesStatement.setInt(1, ids.get(i));
                ResultSet getNamesSet = getNamesStatement.executeQuery();

                if(!getNamesSet.next()) {
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
        ArrayList<String> currentlyOnline = new ArrayList<String>();

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
    String username;
    String from;
    int auctionId;
    String text;

    public MessageNotifier(String username, int auctionId, String from, String text) {
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
