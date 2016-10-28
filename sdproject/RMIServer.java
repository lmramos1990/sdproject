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

    private static String user = "bd";
    private static String pass = "oracle";
    private static String url = "jdbc:oracle:thin:@localhost:1521:XE";
    private static Connection connection;

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
                registry.rebind("iBei", rmiServer);

                try {
                    Class.forName("oracle.jdbc.OracleDriver");

                    System.out.println("[RMISERVER] ESTABLISHING CONNECTION TO THE DATABASE");
                    connection = DriverManager.getConnection(url, user, pass);
                    System.out.println("[RMISERVER] CONNECTION TO THE DATABASE ESTABLISHED");
                } catch(Exception e) {
                    System.out.println("ERROR: CREATING THE CONNECTION TO THE DATABASE");
                    System.exit(0);
                }

                primaryRMIServer();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                return;
            }
        } else {
            try {
                registry.bind("iBei", rmiServer);

                try {
                    Class.forName("oracle.jdbc.OracleDriver");

                    System.out.println("[RMISERVER] ESTABLISHING CONNECTION TO THE DATABASE");
                    connection = DriverManager.getConnection(url, user, pass);
                    System.out.println("[RMISERVER] CONNECTION TO THE DATABASE ESTABLISHED");
                } catch(Exception e) {
                    System.out.println("ERROR: CREATING THE CONNECTION TO THE DATABASE");
                    System.exit(0);
                }

                primaryRMIServer();

            } catch(AlreadyBoundException abe) {
                SecondaryServer secondaryServer = new SecondaryServer();
            }
        }
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

        try {
            Statement statement = connection.createStatement();
            String query = "SELECT client_id, username, pass, status FROM client WHERE to_char(username) = " + "'" + username + "' AND to_char(pass) = " + "'" + password + "' AND status = 0";

            ResultSet resultSet = statement.executeQuery(query);

            if(!resultSet.next()) {
                reply = "type: login, ok: false";
            } else {
                int id = resultSet.getInt("client_id");

                String updateQuery = "UPDATE client SET status = 1 WHERE client_id = '" + id + "'";
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

    public synchronized String register(String username, String password) throws RemoteException {
        System.out.println("[RMISERVER] REGISTER REQUEST");

        String reply = new String();

        try {
            Statement verifyUserStatement = connection.createStatement();
            String verifyQuery = "SELECT username, pass FROM client WHERE to_char(username) = " + "'" + username + "' AND to_char(pass) = " + "'" + password + "'";

            ResultSet verifyResultSet = verifyUserStatement.executeQuery(verifyQuery);

            if(verifyResultSet.next()) {
                reply = "type: register, ok: false";
            } else {
                Statement getLastId = connection.createStatement();
                String lastIdQuery = "SELECT MAX(client_id) FROM client";

                ResultSet lastIdResultSet = getLastId.executeQuery(lastIdQuery);

                if(!lastIdResultSet.next()) {
                    Statement insertStatement = connection.createStatement();
                    String insertQuery = "INSERT INTO client (client_id, username, pass, status) VALUES (1, '" + username + "', '" + password + "', 0)";

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
                    String insertQuery = "INSERT INTO client (client_id, username, pass, status) VALUES (" + lastId + ", '" + username + "', '" + password + "', 0)";
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

    public synchronized String createAuction(String username, String code, String title, String description, String deadline, String amount) throws RemoteException {
        System.out.println("[RMISERVER] CREATE AUCTION REQUEST");

        String reply = new String();
        String format = "yyyy-mm-dd hh24-mi";
        int clientId = 0;
        int articleId = 0;
        int lastAuctionId = 0;

        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
        // LocalDateTime dateTime = LocalDateTime.parse(deadline, formatter);
        //
        // System.out.println(dateTime.toString());

        // type: create_auction, code: 12345, title: teste, description: decricao de teste, deadline: 2017-01-01 23-23, amount: 105.4

        // VERFICAR SE JA EXISTE

        try {

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
                String createAuctionQuery = "INSERT INTO auction (auction_id, client_id, article_id, title, description, maximum_value, deadline, closed) VALUES (1, " + clientId + ", " + articleId + ", '" + title + "', '" + description + "', " + Float.parseFloat(amount) + ", to_date('" + deadline + "', '" + format + "'), 0)";
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
                String createAuctionQuery = "INSERT INTO auction (auction_id, client_id, article_id, title, description, maximum_value, deadline, closed) VALUES (" + lastAuctionId + ", " + clientId + ", " + articleId + ", '" + title + "', '" + description + "', " + Float.parseFloat(amount) + ", to_date('" + deadline + "', '" + format + "'), 0)";
                System.out.println(createAuctionQuery);
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


            Statement test = connection.createStatement();
            String query = "SELECT deadline FROM auction";
            System.out.println("oa");

            ResultSet rs = test.executeQuery(query);

            while(rs.next()) {
                System.out.println("DATA: " + rs.getDate("deadline"));
            }

            rs.close();


        } catch(Exception e) {
            e.printStackTrace();
        }

        return reply;
    }

    public synchronized String searchAuction(String username, String code) throws RemoteException {
        return "search_auction";
    }
    public synchronized String detailAuction(String username, String id) throws RemoteException {
        return "detail_auction";
    }
    public synchronized String myAuctions(String username) throws RemoteException {
        return "my_auctions";
    }
    public synchronized String bid(String username, String id, String amount) throws RemoteException {
        return "bid";
    }
    public synchronized String editAuction(String username, String id, String title, String description, String deadline) throws RemoteException {
        return "edit_auction";
    }

    public synchronized String message(String username, String id, String text) throws RemoteException {
        return "message";
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
                    sb.append(" users_" + i + "_username: " + onlineUsers.get(i));
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

    private static int getPort() {
        int port = 0;

        while(port <= 1024) {
            System.out.print("INSERT PORT: ");
            try {
                Scanner reader = new Scanner(System.in);
                port = reader.nextInt();

                if(port <= 1024) {
                    System.out.println("THIS IS NOT A VALID VALUE FOR THE PORT");
                    port = 0;
                }
            } catch(Exception e) {
                System.out.println("THIS IS NOT A VALID VALUE FOR THE PORT");
                port = 0;
            }
        }

        return port;
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
        }, 0, 2500);

        Thread.currentThread().interrupt();
        return;
    }
}
