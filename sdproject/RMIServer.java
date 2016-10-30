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

class RMIServer extends UnicastRemoteObject implements AuctionInterface {
    private static final long serialVersionUID = 1L;
    private static Properties properties = new Properties();
    private static String rmiRegistryIP = new String();
    private static ArrayList<NotificationCenter> serverList = new ArrayList<NotificationCenter>();

    private static String user = "bd";
    private static String pass = "oracle";
    private static String url = "jdbc:oracle:thin:@localhost:1521:XE";
    public static Connection connection;

    public static String rmiServerIP = new String();
    public static int rmiregistryport = 0;

    public static NotificationCenter notificationCenter;

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
                    Class.forName("oracle.jdbc.OracleDriver");

                    System.out.println("[RMISERVER] ESTABLISHING CONNECTION TO THE DATABASE");
                    connection = DriverManager.getConnection(url, user, pass);
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
        notificationCenter = nc;
        serverList.add(nc);
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
        DateChecker dateChecker = new DateChecker();
    }

    public synchronized String login(String username, String password) throws RemoteException {
        System.out.println("[RMISERVER] LOGIN REQUEST");

        String reply = new String();

        try {
            Statement statement = connection.createStatement();
            String query = "SELECT client_id, username, pass, status FROM client WHERE to_char(username) = " + "'" + username + "' AND to_char(pass) = " + "'" + password + "' AND status = 0";
            ResultSet resultSet = statement.executeQuery(query);

            if(!resultSet.next()) {
                reply = "type: login, ok: false";
            } else {
                int id = resultSet.getInt("client_id");

                String updateQuery = "UPDATE client SET status = 1 WHERE client_id = " + id;
                Statement updateStatement = connection.createStatement();

                ResultSet updateResultSet = updateStatement.executeQuery(updateQuery);

                if(updateResultSet.next()) {
                    System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                    connection.commit();
                } else {
                    System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                }

                updateResultSet.close();

                reply = "type: login, ok: true";
            }
            resultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String register(String username, String password, String uuid) throws RemoteException {
        System.out.println("[RMISERVER] REGISTER REQUEST");

        String reply = new String();

        try {
            Statement verifyUserStatement = connection.createStatement();
            String verifyQuery = "SELECT username, pass, uuid FROM client WHERE to_char(username) = " + "'" + username + "' AND to_char(pass) = " + "'" + password + "'";
            ResultSet verifyResultSet = verifyUserStatement.executeQuery(verifyQuery);

            if(verifyResultSet.next()) {
                if(!verifyResultSet.getString("uuid").equals("check")) {
                    reply = "type: register, ok: true";
                } else {
                    reply = "type: register, ok: false";
                }
            } else {
                Statement getLastId = connection.createStatement();
                String lastIdQuery = "SELECT MAX(client_id) FROM client";

                ResultSet lastIdResultSet = getLastId.executeQuery(lastIdQuery);

                if(!lastIdResultSet.next()) {
                    Statement insertStatement = connection.createStatement();
                    String insertQuery = "INSERT INTO client (client_id, username, pass, status, uuid) VALUES (1, '" + username + "', '" + password + "', 0, '" + uuid + "')";

                    ResultSet insertResultSet = insertStatement.executeQuery(insertQuery);

                    if(insertResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }

                    reply = "type: register, ok: true";

                    insertResultSet.close();
                } else {
                    int lastId = lastIdResultSet.getInt("max(client_id)");
                    lastId += 1;

                    Statement insertStatement = connection.createStatement();
                    String insertQuery = "INSERT INTO client (client_id, username, pass, status, uuid) VALUES (" + lastId + ", '" + username + "', '" + password + "', 0, '" + uuid + "')";
                    ResultSet insertResultSet = insertStatement.executeQuery(insertQuery);

                    if(insertResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }

                    reply = "type: register, ok: true";

                    insertResultSet.close();
                }

                lastIdResultSet.close();
            }

            verifyResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String createAuction(String username, String code, String title, String description, String deadline, String amount, String uuid) throws RemoteException {
        System.out.println("[RMISERVER] CREATE AUCTION REQUEST");

        String reply = new String();
        String format = "yyyy-mm-dd HH24-MI";
        int clientId = 0;
        int articleId = 0;
        int lastAuctionId = 0;

        // type: create_auction, code: 12345, title: teste, description: decricao de teste, deadline: 2017-01-01 23-23, amount: 105.4

        try {

            // VERFICAR SE JA EXISTE
            Statement verifyAuctionStatement = connection.createStatement();
            String verifyAuctionQuery = "SELECT uuid FROM auction WHERE to_char(uuid) = '" + uuid + "'";
            ResultSet verifyAutionResultSet = verifyAuctionStatement.executeQuery(verifyAuctionQuery);

            if(verifyAutionResultSet.next()) {
                verifyAutionResultSet.close();
                return "type: create_auction, ok: true";
            }

            verifyAutionResultSet.close();

            Statement getClientIdStatement = connection.createStatement();
            String getClientIdQuery = "SELECT client_id FROM client WHERE to_char(username) = '" + username + "'";
            ResultSet getClientIdResultSet = getClientIdStatement.executeQuery(getClientIdQuery);

            getClientIdResultSet.next();

            clientId = getClientIdResultSet.getInt("client_id");

            Statement verifyArticleStatement = connection.createStatement();
            String verifyArticleQuery = "SELECT article_id FROM article WHERE to_char(articlecode) = '" + code + "'";
            ResultSet verifyArticleResultSet = verifyArticleStatement.executeQuery(verifyArticleQuery);

            getClientIdResultSet.close();

            if(verifyArticleResultSet.next()) {
                articleId = verifyArticleResultSet.getInt("article_id");
            } else {
                Statement getLastArticleIdStatement = connection.createStatement();
                String getLastArticleIdQuery = "SELECT max(article_id) FROM article";
                ResultSet getLastArticleIdResultSet = getLastArticleIdStatement.executeQuery(getLastArticleIdQuery);

                if(!getLastArticleIdResultSet.next()) {
                    Statement createArticleStatement = connection.createStatement();
                    String createArticleQuery = "INSERT INTO article (article_id, articlecode) VALUES (1, '" + code + "')";
                    ResultSet createArticleResultSet = createArticleStatement.executeQuery(createArticleQuery);

                    if(createArticleResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }
                    createArticleResultSet.close();
                } else {
                    articleId = getLastArticleIdResultSet.getInt("max(article_id)");
                    articleId += 1;

                    Statement createArticleStatement = connection.createStatement();
                    String createArticleQuery = "INSERT INTO article (article_id, articlecode) VALUES (" + articleId + ", '" + code + "')";
                    ResultSet createArticleResultSet = createArticleStatement.executeQuery(createArticleQuery);

                    if(createArticleResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }
                    createArticleResultSet.close();
                }
                getLastArticleIdResultSet.close();
            }
            verifyArticleResultSet.close();

            Statement getLastAuctionIdStatement = connection.createStatement();
            String getLastAuctionIdQuery = "SELECT MAX(auction_id) FROM auction";
            ResultSet getLastAuctionIdResultSet = getLastAuctionIdStatement.executeQuery(getLastAuctionIdQuery);

            if(!getLastAuctionIdResultSet.next()) {
                Statement createAuctionStatement = connection.createStatement();
                String createAuctionQuery = "INSERT INTO auction (auction_id, client_id, article_id, title, description, amount, maximum_value, deadline, closed, uuid) VALUES (1, " + clientId + ", " + articleId + ", '" + title + "', '" + description + "', " + Float.parseFloat(amount) + ", " + Float.parseFloat(amount) + ", to_date('" + deadline + "', '" + format + "'), 0, '" + uuid + "')";
                ResultSet createAuctionResultSet = createAuctionStatement.executeQuery(createAuctionQuery);

                if(!createAuctionResultSet.next()) {
                    System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                    connection.commit();
                } else {
                    System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                }
                createAuctionResultSet.close();
            } else {
                lastAuctionId = getLastAuctionIdResultSet.getInt("max(auction_id)");
                lastAuctionId += 1;

                Statement createAuctionStatement = connection.createStatement();
                String createAuctionQuery = "INSERT INTO auction (auction_id, client_id, article_id, title, description, amount, maximum_value, deadline, closed, uuid) VALUES (" + lastAuctionId + ", " + clientId + ", " + articleId + ", '" + title + "', '" + description + "', " + Float.parseFloat(amount) + ", " + Float.parseFloat(amount) + ", to_date('" + deadline + "', '" + format + "'), 0, '" + uuid + "')";
                ResultSet createAuctionResultSet = createAuctionStatement.executeQuery(createAuctionQuery);

                if(createAuctionResultSet.next()) {
                    System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                    connection.commit();
                    reply = "type: create_auction, ok: true";
                } else {
                    System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    reply = "type: create_auction, ok: false";
                }

                createAuctionResultSet.close();
            }
            getLastAuctionIdResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String searchAuction(String code) throws RemoteException {
        System.out.println("[RMISERVER] SEARCH AUCTION REQUEST");
        String reply = new String();

        try {
            Statement verifyArticleStatement = connection.createStatement();
            String verifyArticleQuery = "SELECT article_id FROM article WHERE to_char(articlecode) = '" + code + "'";
            ResultSet verifyArticleResultSet = verifyArticleStatement.executeQuery(verifyArticleQuery);

            if(!verifyArticleResultSet.next()) {
                reply = "type: search_auction, items_count: 0";
            } else {
                Statement verifyArticleInAuctionStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                String verifyArticleInAuctionQuery = "SELECT auction_id, title, closed FROM auction WHERE article_id = " + verifyArticleResultSet.getInt("article_id");
                ResultSet verifyArticleInAuctionResultSet = verifyArticleInAuctionStatement.executeQuery(verifyArticleInAuctionQuery);

                if(!verifyArticleInAuctionResultSet.next()) {
                    reply = "type: search_auction, items_count: 0";
                } else {
                    ArrayList <String> items = new ArrayList<String>();
                    int count = 0;

                    verifyArticleInAuctionResultSet.beforeFirst();

                    while(verifyArticleInAuctionResultSet.next()) {
                        boolean closed = verifyArticleInAuctionResultSet.getInt("closed") == 0 ? false : true;
                        String item = ", items_" + count + "_id: " + verifyArticleResultSet.getInt("article_id") + ", items_" + count + "_code: " + code + ", items_" + count + "_title: " + verifyArticleInAuctionResultSet.getString("title") + ", items_" + count + "_closed: " + closed;
                        count++;
                        items.add(item);
                    }

                    reply = "type: search_auction, items_count: " + items.size();

                    StringBuilder sb = new StringBuilder(reply);

                    for(int i = 0;i < items.size(); i++) {
                        sb.append(items.get(i));
                    }

                    reply = sb.toString();
                }
                verifyArticleInAuctionResultSet.close();
            }
            verifyArticleResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String detailAuction(String username, String id) throws RemoteException {
        System.out.println("[RMISERVER] DETAIL AUCTION REQUEST");
        String reply = new String();

        try {
            Statement verifyAuctionIdStatement = connection.createStatement();
            String verifyAuctionIdQuery = "SELECT auction_id, title, description, deadline FROM auction WHERE auction_id = " + id;
            ResultSet verifyAuctionIdResultSet = verifyAuctionIdStatement.executeQuery(verifyAuctionIdQuery);

            if(!verifyAuctionIdResultSet.next()) {
                reply = "type: detail_auction, ok: false";
            } else {

                StringBuilder psb = new StringBuilder(verifyAuctionIdResultSet.getString("deadline"));

                for(int i = 1; i < 6; i++) {
                    psb.deleteCharAt(verifyAuctionIdResultSet.getString("deadline").length() - i);
                }

                psb.replace(psb.length() - 3, psb.length() - 2, "-");

                String parsedDeadline = psb.toString();

                String initialReply = "type: detail_auction, title: " + verifyAuctionIdResultSet.getString("title") + ", description: " + verifyAuctionIdResultSet.getString("description") + ", deadline: " + parsedDeadline + ", messages_count: ";

                Statement getAuctionMessagesStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                String getAuctionMessagesQuery = "SELECT client_id, text FROM message WHERE auction_id = " + id;
                ResultSet getAuctionMessagesResultSet = getAuctionMessagesStatement.executeQuery(getAuctionMessagesQuery);

                if(!getAuctionMessagesResultSet.next()) {
                    StringBuilder sb = new StringBuilder(initialReply);
                    String messagePartReply = "0";
                    sb.append(messagePartReply);
                    reply = sb.toString();
                } else {

                    ArrayList <String> items = new ArrayList<String>();
                    int count = 0;

                    getAuctionMessagesResultSet.beforeFirst();

                    while(getAuctionMessagesResultSet.next()) {
                        Statement getUserStatement = connection.createStatement();
                        String getUserQuery = "SELECT username FROM client WHERE client_id = " + getAuctionMessagesResultSet.getInt("client_id");
                        ResultSet getUserResultSet = getUserStatement.executeQuery(getUserQuery);

                        getUserResultSet.next();
                        String item = ", messages_" + count + "_user: " + getUserResultSet.getString("username") + ", messages_" + count + "_text: " + getAuctionMessagesResultSet.getString("text");
                        count++;
                        items.add(item);
                    }
                    getAuctionMessagesResultSet.close();

                    StringBuilder sb = new StringBuilder(initialReply);
                    sb.append(items.size());
                    for(int i = 0; i < items.size(); i++) {
                        sb.append(items.get(i));
                    }

                    reply = sb.toString();
                }

                getAuctionMessagesResultSet.close();

                Statement getAuctionBidsStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                String getAuctionBidsQuery = "SELECT client_id, value FROM bid WHERE auction_id = " + id;
                ResultSet getAuctionBidsResultSet = getAuctionBidsStatement.executeQuery(getAuctionBidsQuery);

                if(!getAuctionBidsResultSet.next()) {
                    StringBuilder sb2 = new StringBuilder(reply);
                    String endString = ", bids_count: 0";
                    sb2.append(endString);
                    reply = sb2.toString();
                } else {
                    ArrayList <String> bids = new ArrayList<String>();
                    int count = 0;
                    getAuctionBidsResultSet.beforeFirst();

                    while(getAuctionBidsResultSet.next()) {
                        Statement getUserFromBidsStatement = connection.createStatement();
                        String getUserFromBidsQuery = "SELECT username FROM client WHERE client_id = " + getAuctionBidsResultSet.getInt("client_id");
                        ResultSet getUserFromBidsResultSet = getUserFromBidsStatement.executeQuery(getUserFromBidsQuery);

                        getUserFromBidsResultSet.next();

                        String bid = ", bids_" + count + "_user: " + getUserFromBidsResultSet.getString("username") + ", bids_" + count + "_amount: " + getAuctionBidsResultSet.getFloat("value");
                        count++;
                        bids.add(bid);
                    }
                    getAuctionBidsResultSet.close();

                    StringBuilder sb2 = new StringBuilder(reply);
                    sb2.append(", bids_count: " + bids.size());
                    for(int i = 0; i < bids.size(); i++) {
                        sb2.append(bids.get(i));
                    }

                    reply = sb2.toString();
                }
                getAuctionBidsResultSet.close();
            }
            verifyAuctionIdResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String myAuctions(String username) throws RemoteException {
        System.out.println("[RMISERVER] MY AUCTION REQUEST");
        String reply = new String();

        try {
            Statement getClientIdStatement = connection.createStatement();
            String getClientIdQuery = "SELECT client_id FROM client WHERE to_char(username) = '" + username + "'";
            ResultSet getClientIdResultSet = getClientIdStatement.executeQuery(getClientIdQuery);

            getClientIdResultSet.next();

            // Statement getUserMessagesStatement = connection.createStatement();
            // String getUserMessagesQuery = "SELECT auction_id FROM message WHERE client_id = " + getClientIdResultSet.getInt("client_id");
            // ResultSet getUserMessagesResultSet = getUserMessagesStatement.executeQuery(getUserMessagesQuery);

            ArrayList<Integer> myAuctions = new ArrayList<Integer>();
            // while(getUserMessagesResultSet.next()) {
            //     if(myAuctions.indexOf(getUserMessagesResultSet.getInt("auction_id")) == -1) {
            //         myAuctions.add(getUserMessagesResultSet.getInt("auction_id"));
            //     }
            // }
            //
            // getUserMessagesResultSet.close();

            Statement getUserBidsStatement = connection.createStatement();
            String getUserBidsQuery = "SELECT auction_id FROM bid WHERE client_id = " + getClientIdResultSet.getInt("client_id");
            ResultSet getUserBidsResultSet = getUserBidsStatement.executeQuery(getUserBidsQuery);

            while(getUserBidsResultSet.next()) {
                if(myAuctions.indexOf(getUserBidsResultSet.getInt("auction_id")) == -1) {
                    myAuctions.add(getUserBidsResultSet.getInt("auction_id"));
                }
            }

            getUserBidsResultSet.close();

            Statement getUserAuctionsStatement = connection.createStatement();
            String getUserAuctionsQuery = "SELECT auction_id FROM auction WHERE client_id = " + getClientIdResultSet.getInt("client_id");
            ResultSet getUserAuctionsResultSet = getUserAuctionsStatement.executeQuery(getUserAuctionsQuery);

            while(getUserAuctionsResultSet.next()) {
                if(myAuctions.indexOf(getUserAuctionsResultSet.getInt("auction_id")) == -1) {
                    myAuctions.add(getUserAuctionsResultSet.getInt("auction_id"));
                }
            }

            getUserAuctionsResultSet.close();

            if(myAuctions.size() == 0) {
                reply = "type: my_auctions, items_count: 0";
            } else {
                String initial = "type: my_auctions, items_count: " + myAuctions.size();

                StringBuilder sb = new StringBuilder(initial);

                for(int i = 0; i < myAuctions.size(); i++) {
                    Statement createStringStatement = connection.createStatement();
                    String createStringQuery = "SELECT a.title, b.articlecode FROM auction a, article b WHERE a.auction_id = " + myAuctions.get(i);
                    ResultSet createStringResultSet = createStringStatement.executeQuery(createStringQuery);
                    createStringResultSet.next();


                    String item = ", items_" + i + "_id: " + myAuctions.get(i) + ", items_" + i + "_code: " + createStringResultSet.getString("articlecode") + ", items_" + i + "_title: " + createStringResultSet.getString("title");
                    sb.append(item);
                    createStringResultSet.close();
                }

                reply = sb.toString();
            }
            getClientIdResultSet.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String bid(String username, String id, String amount) throws RemoteException {
        System.out.println("[RMISERVER] BID REQUEST");
        String reply = new String();

        try {

            Statement verifyUserStatement = connection.createStatement();
            String verifyUserQuery = "SELECT client_id FROM client WHERE to_char(username) = '" + username + "'";
            ResultSet verifyUserResultSet = verifyUserStatement.executeQuery(verifyUserQuery);

            verifyUserResultSet.next();

            Statement verifyAuctionIdStatement = connection.createStatement();
            String verifyAuctionIdQuery = "SELECT auction_id, closed, maximum_value FROM auction WHERE auction_id = " + id;
            ResultSet verifyAuctionIdResultSet = verifyAuctionIdStatement.executeQuery(verifyAuctionIdQuery);

            if(!verifyAuctionIdResultSet.next()) {
                reply = "type: bid, ok: false";
            } else {
                if(verifyAuctionIdResultSet.getInt("closed") == 1 || verifyAuctionIdResultSet.getFloat("maximum_value") < Float.parseFloat(amount)) {
                    reply = "type: bid, ok: false";
                } else {
                    // VERIFICAR O ULTIMO BID ID
                    Statement getLastBidIdStatement = connection.createStatement();
                    String getLastBidIdQuery = "SELECT max(bid_id) FROM bid";
                    ResultSet getLastBidIdResultSet = getLastBidIdStatement.executeQuery(getLastBidIdQuery);

                    if(!getLastBidIdResultSet.next()) {
                        Statement createBidStatement = connection.createStatement();
                        String createBidQuery = "INSERT INTO bid(bid_id, client_id, auction_id, value) VALUES (1, " + verifyUserResultSet.getInt("client_id") + ", " + verifyAuctionIdResultSet.getInt("auction_id") + ", " + amount + ")";
                        ResultSet createBidResultSet = createBidStatement.executeQuery(createBidQuery);

                        if(createBidResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                            reply = "type: bid, ok: true";

                            String notificationMessage = "type: notification_bid, id: " + id + ", user: " + username + ", amount: " + amount;

                            Statement notificationStatement = connection.createStatement();
                            String notificationQuery = "SELECT c.username FROM bid a, client c WHERE auction_id = " + id + " AND c.client_id = a.client_id";
                            ResultSet notificationResultSet = notificationStatement.executeQuery(notificationQuery);

                            ArrayList<String> envolvedUsers = new ArrayList<String>();

                            while(notificationResultSet.next()) {
                                if(!username.equals(notificationResultSet.getString("username"))) {
                                    System.out.println(notificationResultSet.getString("username"));
                                    envolvedUsers.add(notificationResultSet.getString("username"));
                                }
                            }

                            if(!(envolvedUsers.size() == 0)) {
                                for(int i = 0; i < serverList.size(); i++) {
                                    serverList.get(i).receiveNotification(notificationMessage, envolvedUsers);
                                }
                            }

                            notificationStatement.close();

                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }
                        createBidResultSet.close();
                    } else {
                        int lastBidId = getLastBidIdResultSet.getInt("max(bid_id)");
                        lastBidId += 1;

                        Statement createBidStatement = connection.createStatement();
                        String createBidQuery = "INSERT INTO bid(bid_id, client_id, auction_id, value) VALUES (" + lastBidId + ", " + verifyUserResultSet.getInt("client_id") + ", " + verifyAuctionIdResultSet.getInt("auction_id") + ", " + amount + ")";
                        ResultSet createBidResultSet = createBidStatement.executeQuery(createBidQuery);

                        if(createBidResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }
                        createBidResultSet.close();
                    }

                    Statement updateStatement = connection.createStatement();
                    String updateQuery = "UPDATE auction SET maximum_value = " + amount + " WHERE auction_id = " + id;
                    ResultSet updateResultSet = updateStatement.executeQuery(updateQuery);

                    if(updateResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                        reply = "type: bid, ok: true";

                        String notificationMessage = "type: notification_bid, id: " + id + ", user: " + username + ", amount: " + amount;

                        Statement notificationStatement = connection.createStatement();
                        String notificationQuery = "SELECT c.username FROM bid a, client c WHERE auction_id = " + id + " AND c.client_id = a.client_id";
                        ResultSet notificationResultSet = notificationStatement.executeQuery(notificationQuery);

                        ArrayList<String> envolvedUsers = new ArrayList<String>();

                        while(notificationResultSet.next()) {
                            if(!username.equals(notificationResultSet.getString("username"))) {
                                System.out.println(notificationResultSet.getString("username"));
                                envolvedUsers.add(notificationResultSet.getString("username"));
                            }
                        }

                        if(!(envolvedUsers.size() == 0)) {
                            for(int i = 0; i < serverList.size(); i++) {
                                serverList.get(i).receiveNotification(notificationMessage, envolvedUsers);
                            }
                        }

                        notificationStatement.close();

                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }

                    updateResultSet.close();
                    getLastBidIdResultSet.close();
                }
            }
            verifyUserResultSet.close();
            verifyAuctionIdResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(reply.equals("type: bid: ok: true")) {
            System.out.println("[RMISERVER] NOTIFICAR UTILIZADORES");
        }

        return reply;
    }

    public synchronized String editAuction(String username, String id, String title, String description, String deadline, String code, String amount) throws RemoteException {
        System.out.println("[RMISERVER] EDIT AUCTION REQUEST");
        String reply = new String();

        // SAVE A COPY OF THIS AUCTION BEFORE CHANGING THE ACTUAL ONE
        code = "";

        try {

            Statement verifyUserStatement = connection.createStatement();
            String verifyUserQuery = "SELECT a.auction_id FROM auction a, client c WHERE (to_char(c.username) = '" + username + "' AND a.client_id = c.client_id AND auction_id = " + id + ")";
            ResultSet verifyUserResultSet = verifyUserStatement.executeQuery(verifyUserQuery);

            if(!verifyUserResultSet.next()) {
                reply = "type: edit_auction, ok: false";
            } else {
                Statement verifyAuctionIdStatement = connection.createStatement();
                String verifyAuctionIdQuery = "SELECT auction_id FROM auction WHERE auction_id = " + id;
                ResultSet verifyAuctionIdResultSet = verifyAuctionIdStatement.executeQuery(verifyAuctionIdQuery);

                if(!verifyAuctionIdResultSet.next()) {
                    reply = "type: edit_auction, ok: false";
                } else {

                    if(!title.equals("")) {
                        Statement updateTitleStatement = connection.createStatement();
                        String updateTitleQuery = "UPDATE auction SET title = to_char('" + title + "') WHERE auction_id = '" + id + "'";
                        ResultSet updateTitleResultSet = updateTitleStatement.executeQuery(updateTitleQuery);

                        if(updateTitleResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }

                        updateTitleResultSet.close();
                    }

                    if(!description.equals("")) {
                        Statement updateDescriptionStatement = connection.createStatement();
                        String updateDescriptionQuery = "UPDATE auction SET description = to_char('" + description + "') WHERE auction_id = '" + id + "'";
                        ResultSet updateDescriptionResultSet = updateDescriptionStatement.executeQuery(updateDescriptionQuery);

                        if(updateDescriptionResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }

                        updateDescriptionResultSet.close();
                    }

                    if(!deadline.equals("")) {
                        String format = "yyyy-mm-dd HH24-MI";
                        Statement updateDeadlineStatement = connection.createStatement();
                        String updateDeadlineQuery = "UPDATE auction SET deadline = to_date('" + deadline + "', '" + format + "') WHERE auction_id = '" + id + "'";
                        ResultSet updateDeadlineResultSet = updateDeadlineStatement.executeQuery(updateDeadlineQuery);

                        if(updateDeadlineResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }

                        updateDeadlineResultSet.close();
                    }

                    if(!code.equals("")) {
                        Statement updateCodeStatement = connection.createStatement();
                        String updateCodeQuery = "UPDATE auction SET code = to_char('" + code + "') WHERE auction_id = '" + id + "'";
                        ResultSet updateCodeResultSet = updateCodeStatement.executeQuery(updateCodeQuery);

                        if(updateCodeResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }

                        updateCodeResultSet.close();
                    }

                    if(!amount.equals("")) {
                        Statement updateAmountStatement = connection.createStatement();
                        String updateAmountQuery = "UPDATE auction SET amount = to_char('" + Float.parseFloat(amount) + "') WHERE auction_id = '" + id + "'";
                        ResultSet updateAmountResultSet = updateAmountStatement.executeQuery(updateAmountQuery);

                        if(updateAmountResultSet.next()) {
                            System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                            connection.commit();
                        } else {
                            System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                        }

                        updateAmountResultSet.close();
                    }

                    reply = "type: edit_auction, ok: true";
                }
                verifyAuctionIdResultSet.close();

            }
            verifyUserResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String message(String username, String id, String text) throws RemoteException {
        System.out.println("[RMISERVER] MESSAGE REQUEST");
        String reply = new String();

        try {
            Statement verifyUserStatement = connection.createStatement();
            String verifyUserQuery = "SELECT client_id FROM client WHERE to_char(username) = '" + username + "'";
            ResultSet verifyUserResultSet = verifyUserStatement.executeQuery(verifyUserQuery);

            verifyUserResultSet.next();

            Statement verifyAuctionIdStatement = connection.createStatement();
            String verifyAuctionIdQuery = "SELECT auction_id FROM auction WHERE auction_id = " + id;
            ResultSet verifyAuctionIdResultSet = verifyAuctionIdStatement.executeQuery(verifyAuctionIdQuery);

            if(!verifyAuctionIdResultSet.next()) {
                reply = "type: message, ok: false";
            } else {
                Statement getLastMessageIdStatement = connection.createStatement();
                String getLastMessageIdQuery = "SELECT max(message_id) FROM message";
                ResultSet getLastMessageIdResultSet = getLastMessageIdStatement.executeQuery(getLastMessageIdQuery);

                if(!getLastMessageIdResultSet.next()) {
                    Statement createMessageStatement = connection.createStatement();
                    String createMessageQuery = "INSERT INTO message (message_id, client_id, auction_id, text) VALUES (1, " + verifyUserResultSet.getInt("client_id") + ", " + verifyAuctionIdResultSet.getInt("auction_id") + ", '" + text + "')";
                    System.out.println("MESSAGE QUERY: " + createMessageQuery);
                    ResultSet createMessageResultSet = createMessageStatement.executeQuery(createMessageQuery);

                    if(createMessageResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                        reply = "type: message, ok: true";
                        String notificationMessage = "type: notification_message, id: " + id + ", user: " + username + ", text: " + text;

                        Statement notificationStatement = connection.createStatement();
                        String notificationQuery = "SELECT c.username FROM auction a, client c WHERE auction_id = " + id + " AND c.client_id = a.client_id";
                        ResultSet notificationResultSet = notificationStatement.executeQuery(notificationQuery);

                        ArrayList<String> envolvedUsers = new ArrayList<String>();

                        while(notificationResultSet.next()) {
                            envolvedUsers.add(notificationResultSet.getString("username"));
                        }

                        if(!(envolvedUsers.size() == 0)) {
                            for(int i = 0; i < serverList.size(); i++) {
                                serverList.get(i).receiveNotification(notificationMessage, envolvedUsers);
                            }
                        }

                        notificationStatement.close();
                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }
                    createMessageResultSet.close();
                } else {
                    int lastMessageId = getLastMessageIdResultSet.getInt("max(message_id)");
                    lastMessageId += 1;

                    Statement createMessageStatement = connection.createStatement();
                    String createMessageQuery = "INSERT INTO message (message_id, client_id, auction_id, text) VALUES (" + lastMessageId + ", " + verifyUserResultSet.getInt("client_id") + ", " + verifyAuctionIdResultSet.getInt("auction_id") + ", '" + text + "')";
                    ResultSet createMessageResultSet = createMessageStatement.executeQuery(createMessageQuery);

                    if(createMessageResultSet.next()) {
                        System.out.println("[RMISERVER] COMMITING CHANGES TO THE DATABASE");
                        connection.commit();
                        reply = "type: message, ok: true";
                        String notificationMessage = "type: notification_message, id: " + id + ", user: " + username + ", text: " + text;

                        Statement notificationStatement = connection.createStatement();
                        String notificationQuery = "SELECT c.username FROM auction a, client c WHERE auction_id = " + id + " AND c.client_id = a.client_id";
                        ResultSet notificationResultSet = notificationStatement.executeQuery(notificationQuery);

                        ArrayList<String> envolvedUsers = new ArrayList<String>();

                        while(notificationResultSet.next()) {
                            envolvedUsers.add(notificationResultSet.getString("username"));
                        }

                        if(!(envolvedUsers.size() == 0)) {
                            for(int i = 0; i < serverList.size(); i++) {
                                serverList.get(i).receiveNotification(notificationMessage, envolvedUsers);
                            }
                        }

                        notificationStatement.close();
                    } else {
                        System.out.println("[RMISERVER] SOMETHING WENT WRONG NOT COMMITING CHANGES TO THE DATABASE");
                    }
                    createMessageResultSet.close();
                }
                getLastMessageIdResultSet.close();
            }
            verifyUserResultSet.close();
            verifyAuctionIdResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(reply.equals("type: message, ok: true")) {
            System.out.println("[RMISERVER] SEND NOTIFICATION TO THE CLIENTS");
        }

        return reply;
    }

    public synchronized String onlineUsers(String username) throws RemoteException {
        System.out.println("[RMISERVER] ONLINE USERS REQUEST");
        String reply = new String();
        String user = new String();
        ArrayList<String> onlineUsers = new ArrayList<String>();

        try {
            Statement onlineUsersStatement = connection.createStatement();
            String onlineUsersQuery = "SELECT username FROM client WHERE status = 1";
            ResultSet onlineUsersResultSet = onlineUsersStatement.executeQuery(onlineUsersQuery);

            if(onlineUsersResultSet.next()) {

                user = onlineUsersResultSet.getString("username");
                onlineUsers.add(user);

                while(onlineUsersResultSet.next()) {
                    user = onlineUsersResultSet.getString("username");
                    onlineUsers.add(user);
                }

                StringBuilder sb = new StringBuilder();
                sb.append("type: online_users, users_count: " + onlineUsers.size());
                for(int i = 0; i < onlineUsers.size(); i++) {
                    sb.append(", users_" + i + "_username: " + onlineUsers.get(i));
                }

                reply = sb.toString();

            } else {
                reply = "type: online_users , users_count: 0";
            }

            onlineUsersResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }


        return reply;
    }

    public synchronized void logOutUser(String username) throws RemoteException {

        try {
            String updateQuery = "UPDATE client SET status = 0 WHERE to_char(username) = '" + username + "'";
            Statement updateStatement = connection.createStatement();
            ResultSet updateResultSet = updateStatement.executeQuery(updateQuery);

            if(updateResultSet.next()) {
                connection.commit();
            }

            updateResultSet.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return;
    }

    public synchronized void cleanUpUUIDs(String table) throws RemoteException {
        try {
            Statement thing = connection.createStatement();
            String thingQuery = "UPDATE " + table + " SET uuid = 'check'";
            ResultSet thingrs = thing.executeQuery(thingQuery);

            thingrs.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
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

class DateChecker extends Thread {

    public DateChecker() {
        this.start();
    }

    public void run() {
        while(true) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            java.util.Date dateobj = new java.util.Date();

            String date = df.format(dateobj);

            try {
                Statement getDateStatement = RMIServer.connection.createStatement();
                String getDateQuery = "SELECT deadline, auction_id FROM auction WHERE closed = 0";
                ResultSet getDateResultSet = getDateStatement.executeQuery(getDateQuery);

                while(getDateResultSet.next()) {
                    int currentAuctionId = getDateResultSet.getInt("auction_id");
                    StringBuilder psb = new StringBuilder(getDateResultSet.getString("deadline"));

                    for(int i = 1; i < 6; i++) {
                        psb.deleteCharAt(getDateResultSet.getString("deadline").length() - i);
                    }

                    psb.replace(psb.length() - 3, psb.length() - 2, "-");

                    String parsedDeadline = psb.toString();

                    if(date.compareTo(parsedDeadline) >= 0) {
                        Statement updateDateStatement = RMIServer.connection.createStatement();
                        String updateDateQuery = "UPDATE auction SET closed = 1 WHERE auction_id = " + currentAuctionId;
                        ResultSet updateDateResultSet = updateDateStatement.executeQuery(updateDateQuery);

                        if(updateDateResultSet.next()) {
                            System.out.println("[RMISERVER] AN AUCTION HAS JUST ENDED");
                            RMIServer.connection.commit();
                        }
                    }
                }

                getDateResultSet.close();

                Thread.sleep(30000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
