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
    private ArrayList<NotificationCenter> serverList = new ArrayList<NotificationCenter>();

    private static String user = "bd";
    private static String pass = "oracle";
    private static String url = "jdbc:oracle:thin:@localhost:1521:XE";
    public static Connection connection;

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
        // DateChecker dateChecker = new DateChecker();
    }

    public synchronized String login(String username, String password) throws RemoteException {
        System.out.println("[RMISERVER] LOGIN REQUEST");
        String reply = new String();

        System.out.println("[RMISERVER] CHECKING IF USER IS ONLINE");
        try {
            for(int i = 0; i < serverList.size(); i++) {
                if(serverList.get(i).checkUsersOnline(username)) {
                    System.out.println("[RMISERVER] SENDING REPLY");
                    return "type: login, ok: false";
                }
            }
        } catch(RemoteException re) {
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

    public synchronized String createAuction(String username, String code, String title, String description, String deadline, String amount) throws RemoteException {
        System.out.println("[RMISERVER] CREATE AUCTION REQUEST");

        int clientId = getClientId(username);
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
            PreparedStatement createAuctionStatement = connection.prepareStatement(createAuctionQuery);
            createAuctionStatement.setInt(1, clientId);
            createAuctionStatement.setInt(2, articleId);
            createAuctionStatement.setString(3, title);
            createAuctionStatement.setString(4, description);
            createAuctionStatement.setFloat(5, Float.parseFloat(amount));
            createAuctionStatement.setTimestamp(6, timestamp);
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

            String articleInAuctionQuery = "SELECT auction.auction_id, auction.title, article.articlecode FROM auction, article WHERE article.article_id = ?";
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

    public synchronized String detailAuction(String id) throws RemoteException {
        System.out.println("[RMISERVER] DETAIL AUCTION REQUEST");

        int articleId = getArticleId(id);
        if(articleId == -1) return "type: detail_auction, ok: false";

        try {

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return "type: detail_auction, ok: false";
        }



        // String reply = new String();
        //
        // try {
        //     Statement verifyAuctionIdStatement = connection.createStatement();
        //     String verifyAuctionIdQuery = "SELECT auction_id, title, description, deadline FROM auction WHERE auction_id = " + id;
        //     ResultSet verifyAuctionIdResultSet = verifyAuctionIdStatement.executeQuery(verifyAuctionIdQuery);
        //
        //     if(!verifyAuctionIdResultSet.next()) {
        //         reply = "type: detail_auction, ok: false";
        //     } else {
        //
        //         StringBuilder psb = new StringBuilder(verifyAuctionIdResultSet.getString("deadline"));
        //
        //         for(int i = 1; i < 6; i++) {
        //             psb.deleteCharAt(verifyAuctionIdResultSet.getString("deadline").length() - i);
        //         }
        //
        //         psb.replace(psb.length() - 3, psb.length() - 2, "-");
        //
        //         String parsedDeadline = psb.toString();
        //
        //         String initialReply = "type: detail_auction, title: " + verifyAuctionIdResultSet.getString("title") + ", description: " + verifyAuctionIdResultSet.getString("description") + ", deadline: " + parsedDeadline + ", messages_count: ";
        //
        //         Statement getAuctionMessagesStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        //         String getAuctionMessagesQuery = "SELECT client_id, text FROM message WHERE auction_id = " + id;
        //         ResultSet getAuctionMessagesResultSet = getAuctionMessagesStatement.executeQuery(getAuctionMessagesQuery);
        //
        //         if(!getAuctionMessagesResultSet.next()) {
        //             StringBuilder sb = new StringBuilder(initialReply);
        //             String messagePartReply = "0";
        //             sb.append(messagePartReply);
        //             reply = sb.toString();
        //         } else {
        //
        //             ArrayList <String> items = new ArrayList<String>();
        //             int count = 0;
        //
        //             getAuctionMessagesResultSet.beforeFirst();
        //
        //             while(getAuctionMessagesResultSet.next()) {
        //                 Statement getUserStatement = connection.createStatement();
        //                 String getUserQuery = "SELECT username FROM client WHERE client_id = " + getAuctionMessagesResultSet.getInt("client_id");
        //                 ResultSet getUserResultSet = getUserStatement.executeQuery(getUserQuery);
        //
        //                 getUserResultSet.next();
        //                 String item = ", messages_" + count + "_user: " + getUserResultSet.getString("username") + ", messages_" + count + "_text: " + getAuctionMessagesResultSet.getString("text");
        //                 count++;
        //                 items.add(item);
        //             }
        //             getAuctionMessagesResultSet.close();
        //
        //             StringBuilder sb = new StringBuilder(initialReply);
        //             sb.append(items.size());
        //             for(int i = 0; i < items.size(); i++) {
        //                 sb.append(items.get(i));
        //             }
        //
        //             reply = sb.toString();
        //         }
        //
        //         getAuctionMessagesResultSet.close();
        //
        //         Statement getAuctionBidsStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        //         String getAuctionBidsQuery = "SELECT client_id, value FROM bid WHERE auction_id = " + id;
        //         ResultSet getAuctionBidsResultSet = getAuctionBidsStatement.executeQuery(getAuctionBidsQuery);
        //
        //         if(!getAuctionBidsResultSet.next()) {
        //             StringBuilder sb2 = new StringBuilder(reply);
        //             String endString = ", bids_count: 0";
        //             sb2.append(endString);
        //             reply = sb2.toString();
        //         } else {
        //             ArrayList <String> bids = new ArrayList<String>();
        //             int count = 0;
        //             getAuctionBidsResultSet.beforeFirst();
        //
        //             while(getAuctionBidsResultSet.next()) {
        //                 Statement getUserFromBidsStatement = connection.createStatement();
        //                 String getUserFromBidsQuery = "SELECT username FROM client WHERE client_id = " + getAuctionBidsResultSet.getInt("client_id");
        //                 ResultSet getUserFromBidsResultSet = getUserFromBidsStatement.executeQuery(getUserFromBidsQuery);
        //
        //                 getUserFromBidsResultSet.next();
        //
        //                 String bid = ", bids_" + count + "_user: " + getUserFromBidsResultSet.getString("username") + ", bids_" + count + "_amount: " + getAuctionBidsResultSet.getFloat("value");
        //                 count++;
        //                 bids.add(bid);
        //             }
        //             getAuctionBidsResultSet.close();
        //
        //             StringBuilder sb2 = new StringBuilder(reply);
        //             sb2.append(", bids_count: " + bids.size());
        //             for(int i = 0; i < bids.size(); i++) {
        //                 sb2.append(bids.get(i));
        //             }
        //
        //             reply = sb2.toString();
        //         }
        //         getAuctionBidsResultSet.close();
        //     }
        //     verifyAuctionIdResultSet.close();
        // } catch(Exception e) {
        //     e.printStackTrace();
        // }
        //
        // return reply;
    }

    public synchronized String myAuctions(String username) throws RemoteException {
        System.out.println("[RMISERVER] MY AUCTION REQUEST");
        String reply = new String();

        try {
            Statement getClientIdStatement = connection.createStatement();
            String getClientIdQuery = "SELECT client_id FROM client WHERE to_char(username) = '" + username + "'";
            ResultSet getClientIdResultSet = getClientIdStatement.executeQuery(getClientIdQuery);

            int clientId = 0;

            if(getClientIdResultSet.next()) {
                clientId = getClientIdResultSet.getInt("client_id");
                getClientIdResultSet.close();
            } else {
                reply = "type: my_auctions, ok: false";
                getClientIdResultSet.close();
                return reply;
            }

            ArrayList<Integer> myAuctions = new ArrayList<Integer>();

            Statement getUserBidsStatement = connection.createStatement();
            String getUserBidsQuery = "SELECT auction_id FROM bid WHERE client_id = " + clientId;
            ResultSet getUserBidsResultSet = getUserBidsStatement.executeQuery(getUserBidsQuery);

            while(getUserBidsResultSet.next()) {
                if(myAuctions.indexOf(getUserBidsResultSet.getInt("auction_id")) == -1) {
                    myAuctions.add(getUserBidsResultSet.getInt("auction_id"));
                }
            }

            getUserBidsResultSet.close();

            Statement getUserAuctionsStatement = connection.createStatement();
            String getUserAuctionsQuery = "SELECT auction_id FROM auction WHERE client_id = " + clientId;
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
                    String createStringQuery = "SELECT a.title, b.articlecode FROM auction a, article b WHERE a.auction_id = " + myAuctions.get(i) + " AND a.article_id = b.article_id";
                    ResultSet createStringResultSet = createStringStatement.executeQuery(createStringQuery);

                    if(createStringResultSet.next()) {
                        String item = ", items_" + i + "_id: " + myAuctions.get(i) + ", items_" + i + "_code: " + createStringResultSet.getString("articlecode") + ", items_" + i + "_title: " + createStringResultSet.getString("title");
                        sb.append(item);
                    }

                    createStringResultSet.close();
                }

                reply = sb.toString();
            }


        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String bid(String username, String id, String amount) throws RemoteException {
        System.out.println("[RMISERVER] BID REQUEST");

        int clientId = getClientId(username);
        int auctionId = getAuctionId(id);
        float fAmount = Float.parseFloat(amount);

        if(auctionId == -1) return "type: bid, ok: false";
        if(assessValidBid(id, fAmount) == -1) return "type: bid, ok: false";

        try {
            String createBidQuery = "INSERT INTO bid (bid_id, client_id, auction_id, value) VALUES (bid_seq.nextVal, ?, ?, ?)";
            PreparedStatement createBidStatement = connection.prepareStatement(createBidQuery);
            createBidStatement.setInt(1, clientId);
            createBidStatement.setInt(2, auctionId);
            createBidStatement.setFloat(3, fAmount);
            ResultSet createBidSet = createBidStatement.executeQuery();

            if(createBidSet.next()) {
                createBidSet.close();
                connection.commit();
                System.out.println("[RMISERVER] BID REGISTERED IN THE DATABASE WITH SUCCESS");

                String updateQuery = "UPDATE auction SET current_value = ? WHERE auction_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setFloat(1, fAmount);
                updateStatement.setInt(2, Integer.parseInt(id));
                ResultSet updateSet = updateStatement.executeQuery();

                if(updateSet.next()) {
                    updateSet.close();
                    System.out.println("[RMISERVER] AUCTION WAS UPDATED WITH SUCCESS");

                    // TODO: ENVIAR NOTIFICAÇOES!

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

        int clientId = getClientId(username);
        int auctionId = getAuctionId(id);

        if(auctionId == -1) return "type: message, ok: false";

        try {
            String messageQuery = "INSERT INTO message (message_id, client_id, auction_id, text) VALUES(message_seq.nextVal, ?, ?, ?)";
            PreparedStatement messageStatement = connection.prepareStatement(messageQuery);
            messageStatement.setInt(1, clientId);
            messageStatement.setInt(2, auctionId);
            messageStatement.setString(3, text);
            ResultSet messageSet = messageStatement.executeQuery();

            if(messageSet.next()) {
                messageSet.close();
                connection.commit();
                System.out.println("[RMISERVER] MESSAGE WAS REGISTERED IN THE DATABASE WITH SUCCESS");

                // TODO: ENVIAR NOTIFICACAO

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

    public synchronized void getNotifications(String username) throws RemoteException {
        try {
            Statement getNotifications = connection.createStatement();
            String getNotificationsQ = "SELECT a.message, c.client_id FROM client c, notification a WHERE to_char(c.username) = '" + username + "' and c.client_id = a.client_id and read = 0";
            ResultSet getNotificationsRS = getNotifications.executeQuery(getNotificationsQ);

            ArrayList<String> involvedUsers = new ArrayList<String>();

            involvedUsers.add(username);
            int clientId = 0;

            while(getNotificationsRS.next()) {
                clientId = getNotificationsRS.getInt("client_id");
                for(int j = 0; j < serverList.size(); j++) {
                    serverList.get(j).receiveNotification(getNotificationsRS.getString("message"), involvedUsers);
                }
            }

            Statement update = connection.createStatement();
            String queryupdate = "UPDATE notification SET read = 1 WHERE client_id = " + clientId;
            ResultSet resultseth = update.executeQuery(queryupdate);

            resultseth.close();
            getNotificationsRS.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            String getArticleIdQuery = "SELECT article_id FROM article WHERE to_char(articlecode) = ?";
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
            String createArticleQuery = "INSERT INTO article (article_id, articlecode) VALUES (article_seq.nextVal, ?)";
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

    private int getAuctionId(String id) {
        try {
            String getAuctionQuery = "SELECT auction_id FROM auction WHERE auction_id = ?";
            PreparedStatement getAuctionStatement = connection.prepareStatement(getAuctionQuery);
            getAuctionStatement.setInt(1, Integer.parseInt(id));
            ResultSet getAuctionSet = getAuctionStatement.executeQuery();

            if(!getAuctionSet.next()) {
                getAuctionSet.close();
                return -1;
            } else {
                getAuctionSet.close();
                return Integer.parseInt(id);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[DATABASE] AN ERROR HAS OCURRED");
            return -1;
        }
    }

    private int assessValidBid(String id, float amount) {
        try {
            String assessBidQuery = "SELECT deadline, current_value, initial_value FROM auction WHERE auction_id = ?";
            PreparedStatement assessBidStatement = connection.prepareStatement(assessBidQuery);
            assessBidStatement.setInt(1, Integer.parseInt(id));
            ResultSet assessBidSet = assessBidStatement.executeQuery();

            if(assessBidSet.next()) {
                Timestamp deadline = assessBidSet.getTimestamp("deadline");
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                if(!deadline.after(currentTime)) {
                    System.out.println("[RMISERVER] NOT POSSIBLE TO REGISTER THIS BID");
                    assessBidSet.close();
                    return -1;
                }

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
                        ResultSet updateDateResultSet = null;
                        synchronized (this) {
                            updateDateResultSet = updateDateStatement.executeQuery(updateDateQuery);
                        }

                        if(updateDateResultSet.next()) {
                            System.out.println("[RMISERVER] AN AUCTION HAS JUST ENDED");
                            RMIServer.connection.commit();
                        }
                    }
                }

                getDateResultSet.close();

                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
