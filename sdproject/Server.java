package sdproject;

import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.sql.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Server {
    private static ServerSocket serverSocket;
    private static int port = 7000;
    private static String rmiServerIP = new String();
    private static String databaseIP = new String();

    public static int numberOfClients = 0;
    public static ArrayList <Socket> clientSockets = new ArrayList<Socket>();
    public static ArrayList<ClientObject> listOfClients = new ArrayList<ClientObject>();
    public static AuctionInterface iBei;
    public static int rmiregistryport = -1;
    public static String rmiRegistryIP = new String();

    private static String user = "bd";
    private static String pass = "oracle";
    // private static String url = "jdbc:oracle:thin:@localhost:1521:XE";
    public static Connection connection;

    public static void main(String args[]) {
        System.setProperty("java.net.preferIPv4Stack" , "true");

        boolean connected = false;
        int connecting = 0;

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPServer");
            return;
        }

        readProperties();

        try {
            Class.forName("oracle.jdbc.OracleDriver");
            String url = "jdbc:oracle:thin:@" + databaseIP + ":1521:XE";
            connection = DriverManager.getConnection(url, user, pass);
        } catch(Exception e) {
            System.out.println("ERROR CONNECTING TO THE DATABASE");
        }

        System.out.println("[SERVER] TRYING TO ESTABLISH A CONNECTION TO THE RMI SERVER");
        while(!connected) {
            try {
                connecting++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiRegistryIP, rmiregistryport).lookup("iBei");
                connected = true;
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("[SERVER] CONNECTION FAILED\n[SERVER] ATTEMPTING ANOTHER TIME");
            }

            if(connecting == 3) {
                System.out.println("[SERVER] CANNOT ESTABLISH A CONNECTION TO THE RMI SERVER AT THIS MOMENT");
                return;
            }

            try {
                Thread.sleep(5000);
            } catch(Exception e) {
                return;
            }
        }

        try {
            selectPort();

            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            InetAddress enumerationAddresses = null;

            while(enumeration.hasMoreElements()) {

                NetworkInterface n = (NetworkInterface) enumeration.nextElement();
                Enumeration ee = n.getInetAddresses();

                enumerationAddresses = (InetAddress) ee.nextElement();
                break;
            }

            String address = enumerationAddresses.getHostAddress();
            new ServerLoad(address, port);

            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("[SERVER] A CLIENT HAS CONNECTED WITH ME");
                Server.numberOfClients++;
                new TCPConnection(clientSocket);
            }
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public static void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = new FileInputStream(propFileName);

            if(inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("ERROR: PROPERTY '" + propFileName + "' NOT FOUND IN THE CLASSPATH");
            }

            rmiRegistryIP = prop.getProperty("rmiRegistryIP");
            rmiServerIP = prop.getProperty("rmiServerIP");
            databaseIP = prop.getProperty("databaseIP");
            rmiregistryport = Integer.parseInt(prop.getProperty("rmiregistryport"));

        } catch(Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private static void selectPort() {
        if(isPortAvailable(port) == false) {
            port += 1;
            selectPort();
        }
    }

    private static Boolean isPortAvailable(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch(Exception e) {
            return false;
        }

        return true;
    }
}

class TCPConnection extends Thread {
    BufferedReader inFromServer = null;
    PrintWriter outToServer;
    private static Socket clientSocket;
    private static ClientObject client;

    private static String username;
    private static boolean online = false;
    private static int connecting = 0;
    private static int logoutCounter = 0;

    public TCPConnection(Socket pclientSocket) {
        this.username = new String();

        try {
            clientSocket = pclientSocket;
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.start();
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }
    }

    public void run() {
        ArrayList<String> requests = new ArrayList<String>();
        String data = new String();
        String reply = new String();

        try {
            while(true) {
                data = inFromServer.readLine();
                data = parseString(data);

                parseFile(requests, data);

                while(!requests.isEmpty()) {
                    String request = requests.get(0);
                    String action = parse("type", request);

                    if(!request.equals("")) System.out.println("[CLIENT] " + request);

                    reply = courseOfAction(action, request);

                    outToServer.println(reply);
                    requests.remove(0);
                }
            }

        } catch(Exception e) {
            System.out.println("[SERVER] A CLIENT HAS DISCONNECTED");
            Server.numberOfClients--;

            if(!username.equals("") && online) {
                try {
                    Statement logOut = Server.connection.createStatement();
                    String logOutQuery = "UPDATE client SET status = 0 WHERE to_char(username) = '" + client.getUsername() + "'";
                    ResultSet logOutResultSet = logOut.executeQuery(logOutQuery);
                    Server.connection.commit();
                } catch(Exception e2) {
                    e2.printStackTrace();
                }



                Server.listOfClients.remove(Server.listOfClients.indexOf(client));
                online = false;
            }


            try {
                this.clientSocket.close();
            } catch(IOException ioe) {
                System.out.println("ERROR WHEN TRYING TO CLOSE THE CLIENT SOCKET: " + ioe.getMessage());
                Thread.currentThread().interrupt();
                return;
            }

            Thread.currentThread().interrupt();
            return;
        }
    }

    private static void logOut(String username) {

        System.out.println("logout in process");

        while(logoutCounter != 5) {
            try {
                logoutCounter++;
                Server.iBei.logOut(username);
            } catch(Exception e) {}
        }

        logoutCounter = 0;
    }

    private static void logOutReconnect() {
        try {
            logoutCounter++;
            Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
        } catch(Exception e) {
            System.out.println("[SERVER] LOGOUT STILL IN PROCESS");
        }

        try {
            Thread.sleep(5000);
        } catch(Exception e) {}

        if(connecting == 5) {
            System.out.println("[SERVER] CANNOT ESTABLISH A CONNECTION TO THE RMI SERVER AT THIS MOMENT");
            return;
        }
    }

    private static void parseFile(ArrayList<String> requests, String file) {

        String [] lines = file.split("\\r?\\n");

        for(int i = 0; i < lines.length; i++) {
            requests.add(lines[i]);
        }
    }

    private static String courseOfAction(String action, String parameters) {
        String reply = new String();

        if(!online && (action.equals("login") || action.equals("register"))) {

            username = parse("username", parameters);
            String password = parse("password", parameters);

            reply = attemptLoginRegister(action, username, password);

            if(reply.equals("type: login, ok: true") || reply.equals("type: register, ok: true")) {
                client = new ClientObject(clientSocket, username);
                Server.listOfClients.add(client);
                online = true;
            }

        } else if(online && action.equals("create_auction")) {

            String code = parse("code", parameters);
            String title = parse("title", parameters);
            String description = parse("description", parameters);
            String deadline = parse("deadeline", parameters);
            String amount = parse("amount", parameters);

            reply = createAuction(username, code, title, description, deadline, amount);

        } else if(online && action.equals("search_auction")) {
            String code = parse("code", parameters);

            reply = searchAuction(username, code);

        } else if(online && action.equals("detail_auction")) {
            String id = parse("id", parameters);

            reply = detailAuction(username, id);

        } else if(online && action.equals("my_auctions")) {

            reply = myAuctions(username);

        } else if(online && action.equals("bid")) {
            String id = parse("id", parameters);
            String amount = parse("amount", parameters);

            reply = bid(username, id, amount);

        } else if(online && action.equals("edit_auction")) {
            String id = parse("id", parameters);
            String title = parse("title", parameters);
            String description = parse("description", parameters);
            String deadline = parse("deadline", parameters);

            reply = editAuction(username, id, title, description, deadline);

        } else if(online && action.equals("message")) {
            String id = parse("id", parameters);
            String text = parse("text", parameters);

            reply = message(username, id, text);

        } else if(online && action.equals("online_users")) {

            reply = onlineUsers(username);

        } else if(!online) {
            reply = "[SERVER] PLEASE LOG IN BEFORE MAKING REQUESTS";
        } else {
            reply = "[SERVER] THIS IS NOT A VALID REQUEST";
        }

        return reply;
    }

    private static String attemptLoginRegister(String action, String username, String password) {
        String reply = new String();

        if(action.equals("login")) {
            try {
                reply = Server.iBei.login(username, password);
            } catch(RemoteException re) {
                connectToRMI();
                reply = attemptLoginRegister(action, username, password);
            }
        } else {
            try {
                reply = Server.iBei.register(username, password);
            } catch(RemoteException re) {
                connectToRMI();
                reply = attemptLoginRegister(action, username, password);
            }
        }

        connecting = 0;
        return reply;
    }

    private static String createAuction(String username, String code, String title, String description, String deadline, String amount) {
        String reply = new String();

        try {
            reply = Server.iBei.createAuction(username, code, title, description, deadline, amount);

        } catch(RemoteException re) {
            connectToRMI();
            reply = createAuction(username, code, title, description, deadline, amount);
        }

        connecting = 0;
        return reply;
    }

    private static String searchAuction(String username, String code) {
        String reply = new String();

        try {
            reply = Server.iBei.searchAuction(username, code);
        } catch(RemoteException re) {
            connectToRMI();
            reply = searchAuction(username, code);
        }

        connecting = 0;
        return reply;
    }

    private static String detailAuction(String username, String id) {
        String reply = new String();

        try {
            reply = Server.iBei.detailAuction(username, id);
        } catch(RemoteException re) {
            connectToRMI();
            reply = detailAuction(username, id);
        }

        connecting = 0;
        return reply;
    }

    private static String myAuctions(String username) {
        String reply = new String();

        try {
            reply = Server.iBei.myAuctions(username);
        } catch(RemoteException re) {
            connectToRMI();
            reply = myAuctions(username);
        }

        connecting = 0;
        return reply;
    }

    private static String bid(String username, String id, String amount) {
        String reply = new String();

        try {
            reply = Server.iBei.bid(username, id, amount);
        } catch(RemoteException re) {
            connectToRMI();
            reply = bid(username, id, amount);
        }

        connecting = 0;
        return reply;
    }

    private static String editAuction(String username, String id, String title, String description, String deadline) {
        String reply = new String();

        try {
            reply = Server.iBei.editAuction(username, id, title, description, deadline);
        } catch(RemoteException re) {
            connectToRMI();
            reply = editAuction(username, id, title, description, deadline);
        }

        connecting = 0;
        return reply;
    }

    private static String message(String username, String id, String text) {
        String reply = new String();

        try {
            reply = Server.iBei.message(username, id, text);
        } catch(RemoteException re) {
            connectToRMI();
            reply = message(username, id, text);
        }

        connecting = 0;
        return reply;
    }

    private static String onlineUsers(String username) {
        String reply = new String();

        try {
            reply = Server.iBei.onlineUsers(username);
        } catch(RemoteException re) {
            connectToRMI();
            reply = onlineUsers(username);
        }

        connecting = 0;
        return reply;
    }

    private static void connectToRMI() {

        try {
            connecting++;
            Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
        } catch(Exception e) {
            System.out.println("[SERVER] CONNECTION FAILED\n[SERVER] ATTEMPTING ANOTHER TIME");
        }

        if(connecting == 7) {
            System.out.println("[SERVER] CANNOT ESTABLISH A CONNECTION TO THE RMI SERVER AT THIS MOMENT");
            System.exit(0);
        }

        try {
            Thread.sleep(5000);
        } catch(Exception e) {
            return;
        }
    }

    private static String parse(String parameter, String request) {
        int j = 0, k = 0;
        int plen = parameter.length();

        for(int i = 0; i < request.length(); i++) {
            if(j != plen && (request.charAt(i) == parameter.charAt(j))) {
                j++;
            }

            if(j == plen) {
                j = i;
                break;
            }
        }

        for(int i = 0; i < request.length(); i++) {
            if(request.charAt(i) == ',' && j < i) {
                k = i;
                break;
            }
        }

        String string = new String();

        if(k == 0) {
            k = request.length();
            string = request.substring(j + 1, k);
        } else {
            string = request.substring(j + 1, k);
        }

        StringBuilder sb = new StringBuilder(string);

        while(string.charAt(0) == ' ' || string.charAt(0) == ':') {
            sb.deleteCharAt(0);
            string = sb.toString();
        }

        return string;
    }

    private static String parseString(String reply) {

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < reply.length(); i++) {
            if(!(reply.charAt(i) == '\0')) {
                sb.append(reply.charAt(i));
            } else break;
        }

        return sb.toString();
    }
}

class ServerLoad extends Thread {
    private static MulticastSocket mcSocket;
    private static int mcport = 8000;
    private static int port;
    private static String ipAddress;

    public ServerLoad(String address, int tcpport) {
        ipAddress = address;
        port = tcpport;
        this.start();
    }

    public void run() {
        try {
            mcSocket = new MulticastSocket(mcport);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            mcSocket.joinGroup(group);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        sendMyInformation();
        receiveOthersInfo();
    }

    private static void sendMyInformation() {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String sentence = "ip: " + ipAddress + ", port: " + Integer.toString(port) + ", numberofclients: " + Server.numberOfClients + ", count: 0";

                byte [] sendData = sentence.getBytes();
                DatagramPacket serverInfoPacket = null;

                try {
                    serverInfoPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("228.5.6.7"), mcport);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }

                try {
                    mcSocket.send(serverInfoPacket);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }
            }
        }, 0, 1000);
    }

    private static void receiveOthersInfo() {
        ArrayList<String> serversList = new ArrayList<String>();
        boolean threadRunning = false;
        int count = 0;

        while(true) {
            byte [] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                mcSocket.receive(receivePacket);
                count++;
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }

            String serverInfo = new String(receivePacket.getData());
            serverInfo = parseString(serverInfo);

            if(serversList.size() == 0) {
                serversList.add(serverInfo);
            } else {
                boolean found = false;
                // int foundCounter = 0;
                int foundIndex = 0;

                for(int i = 0; i < serversList.size(); i++) {
                    for(int j = 0; j < serversList.size(); j++) {
                        if(!(parse("ip", serverInfo).equals(parse("ip", serversList.get(j))) && parse("port", serverInfo).equals(parse("port", serversList.get(j))))) {
                            found = false;
                        } else {
                            found = true;
                            foundIndex = j;
                            break;
                        }
                    }

                    if(found && i == serversList.size() - 1) {
                        String resetCounter = "ip: " + parse("ip", serversList.get(foundIndex)) + ", port: " + parse("port", serversList.get(foundIndex)) + ", numberofclients: " + parse("numberofclients", serverInfo) + ", count: 0";
                        serversList.set(foundIndex, resetCounter);
                    } else if(!found && i == serversList.size() - 1) {
                        serversList.add(serverInfo);
                        break;
                    }
                }

                for(int i = 0; i < serversList.size(); i++) {
                    int counter = Integer.parseInt(parse("count", serversList.get(i)));
                    counter += 1;
                    String newString = "ip: " + parse("ip", serversList.get(i)) + ", port: " + parse("port", serversList.get(i)) + ", numberofclients: " + parse("numberofclients", serversList.get(i)) + ", count: " + counter;
                    serversList.set(i, newString);
                }

                for(int i = 0; i < serversList.size(); i++) {
                    for(int j = 0; j < serversList.size(); j++) {
                        int counter = Integer.parseInt(parse("count", serversList.get(j)));
                        if(counter >= 15) {
                            serversList.remove(j);
                            break;
                        }
                    }
                }

                //UPDATE LIST VALUES
            }

            if(!threadRunning) {
                threadRunning = true;
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        ArrayList <Socket> clients = Server.clientSockets;
                        PrintWriter outToServer;
                        // Server.numberOfClients = clients.size();
                        String message = new String();

                        StringBuilder sb = new StringBuilder();
                        sb.append("type: notification_load, server_list: " + serversList.size() + ", ");

                        for(int i = 0; i < serversList.size(); i++) {
                            String aux = "server_" + i + "_hostname: " + parse("ip", serversList.get(i)) + ", server_" + i +
                            "_port: " + parse("port", serversList.get(i)) + ", server_" + i + "_load: " + parse("numberofclients", serversList.get(i));
                            sb.append(aux);

                            if(i != serversList.size() - 1) sb.append(", ");
                        }

                        message = sb.toString();

                        for(int i = 0; i < clients.size(); i++) {
                            Socket clientSocket = clients.get(i);

                            try {
                                outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                            } catch(Exception e) {
                                clients.remove(i);
                                return;
                            }

                            try {
                                outToServer.println(message);
                            } catch(Exception e) {
                                return;
                            }
                        }
                    }
                }, 0, 60000);
            }
        }
    }

    private static String parse(String parameter, String request) {
        int j = 0, k = 0;
        int plen = parameter.length();

        for(int i = 0; i < request.length(); i++) {
            if(j != plen && (request.charAt(i) == parameter.charAt(j))) {
                j++;
            }

            if(j == plen) {
                j = i;
                break;
            }
        }

        for(int i = 0; i < request.length(); i++) {
            if(request.charAt(i) == ',' && j < i) {
                k = i;
                break;
            }
        }

        String string = new String();

        if(k == 0) {
            k = request.length();
            string = request.substring(j + 1, k);
        } else {
            string = request.substring(j + 1, k);
        }

        StringBuilder sb = new StringBuilder(string);

        while(string.charAt(0) == ' ' || string.charAt(0) == ':') {
            sb.deleteCharAt(0);
            string = sb.toString();
        }

        return string;
    }

    private static String parseString(String reply) {

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < reply.length(); i++) {
            if(!(reply.charAt(i) == '\0')) {
                sb.append(reply.charAt(i));
            } else break;
        }

        return sb.toString();
    }
}
