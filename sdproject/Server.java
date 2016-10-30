package sdproject;

import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class Server extends UnicastRemoteObject implements NotificationCenter {
    public static ServerSocket serverSocket;
    private static int port = 7000;
    private static String rmiServerIP = new String();
    private static String databaseIP = new String();

    public static int numberOfClients = 0;
    public static ArrayList <Socket> clientSockets = new ArrayList<Socket>();
    public static ArrayList<ClientObject> listOfClients = new ArrayList<ClientObject>();
    public static AuctionInterface iBei;
    public static int rmiregistryport = -1;
    public static String rmiRegistryIP = new String();

    Server() throws RemoteException {
        super();
    }

    public static void main(String args[]) {
        System.setProperty("java.net.preferIPv4Stack" , "true");

        boolean connected = false;
        int connecting = 0;

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPServer");
            return;
        }

        readProperties();

        System.out.println("[SERVER] TRYING TO ESTABLISH A CONNECTION TO THE RMI SERVER");
        Server server = null;

        try {
            server = new Server();
        } catch(RemoteException re) {
            re.printStackTrace();
        }


        while(!connected) {
            try {
                connecting++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiRegistryIP, rmiregistryport).lookup("iBei");
                iBei.subscribe((NotificationCenter) server);
                connected = true;
            } catch(Exception e) {}

            if(connecting == 6) {
                System.out.println("[SERVER] CANNOT ESTABLISH A CONNECTION TO THE RMI SERVER AT THIS MOMENT");
                return;
            }

            if(!connected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception e) {
                    return;
                }
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

    public void receiveNotification(String notification, ArrayList<String> envolvedUsers) {
        PrintWriter toTheClient = null;

        for(int i = 0; i < envolvedUsers.size(); i++) {
            for(int j = 0; j < Server.listOfClients.size(); j++) {
                if(envolvedUsers.get(i).equals(Server.listOfClients.get(j).getUsername())) {
                    System.out.println(Server.listOfClients.get(j).getUsername());
                    try {
                        toTheClient = new PrintWriter(Server.listOfClients.get(j).getClientSocket().getOutputStream(), true);
                        toTheClient.println(notification);
                    } catch(Exception e) {e.printStackTrace();}
                }
            }
        }
    }

}

class TCPConnection extends Thread {
    BufferedReader inFromServer = null;
    PrintWriter outToServer;
    Socket clientSocket;
    ClientObject client;

    private String username;
    private int connecting = 0;

    public TCPConnection(Socket pclientSocket) {
        this.username = new String();
        this.clientSocket = pclientSocket;

        try {
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

                if(!data.isEmpty()) {
                    data = parseString(data);
                    parseFile(requests, data);

                    String action = parse("type", data);
                    action = cleanUpStrings(action);

                    if(!data.equals("")) System.out.println("[CLIENT] " + data);

                    reply = courseOfAction(action, data);

                    outToServer.println(reply);
                } else {
                    reply = "[SERVER] THIS IS NOT A VALID REQUEST";
                    outToServer.println(reply);
                }
            }
        } catch(Exception e) {
            System.out.println("[SERVER] A CLIENT HAS DISCONNECTED");
            Server.numberOfClients--;

            if(!username.equals("")) {
                Server.listOfClients.remove(Server.listOfClients.indexOf(client));

                logOutUser(username);

                try {
                    clientSocket.close();
                } catch(IOException ioe) {
                    System.out.println("ERROR WHEN TRYING TO CLOSE THE CLIENT SOCKET: " + ioe.getMessage());
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            Thread.currentThread().interrupt();
            return;
        }
    }

    private void parseFile(ArrayList<String> requests, String file) {

        String [] lines = file.split("\\r?\\n");

        for(int i = 0; i < lines.length; i++) {
            requests.add(lines[i]);
        }
    }

    private String courseOfAction(String action, String parameters) {
        String reply = new String();

        if(action.equals("login")) {
            if(!parameters.contains("username") || !parameters.contains("password")) {
                reply = "type: login, ok: false";
            } else {
                username = parse("username", parameters);
                String password = parse("password", parameters);

                reply = logIn(username, password);

                if(reply.equals("type: login, ok: true")) {
                    client = new ClientObject(clientSocket, username);
                    Server.listOfClients.add(client);
                }
            }
        } else if(action.equals("register")) {
            String uuid = UUID.randomUUID().toString();
            if(!parameters.contains("username") || !parameters.contains("password")) {
                reply = "type: register, ok: false";
            } else {
                String registryUsername = parse("username", parameters);
                String password = parse("password", parameters);

                reply = register(registryUsername, password, uuid);
                cleanUpUUIDs("client");
            }
        } else if(action.equals("create_auction")) {
            String uuid = UUID.randomUUID().toString();
            if(!parameters.contains("code") || !parameters.contains("title") || !parameters.contains("description") || !parameters.contains("deadline") || !parameters.contains("amount")) {
                reply = "type: create_auction, ok: false";
            } else {
                String code = parse("code", parameters);
                String title = parse("title", parameters);
                String description = parse("description", parameters);
                String deadline = parse("deadline", parameters);
                String amount = parse("amount", parameters);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm");
                java.util.Date dateobj = new java.util.Date();
                String date = df.format(dateobj);

                if(date.compareTo(deadline) >= 0) {
                    reply = "type: create_auction, ok: false";
                } else {
                    reply = createAuction(username, code, title, description, deadline, amount, uuid);
                    cleanUpUUIDs("auction");
                }
            }
        } else if(action.equals("search_auction")) {
            if(!parameters.contains("code")) {
                reply = "type: search_auction, items_count: 0";
            } else {
                String code = parse("code", parameters);
                reply = searchAuction(code);
            }
        } else if(action.equals("detail_auction")) {
            if(!parameters.contains("id")) {
                reply = "type: detail_auction, ok: false";
            } else {
                String id = parse("id", parameters);
                reply = detailAuction(username, id);
            }
        } else if(action.equals("my_auctions")) {
            reply = myAuctions(username);
        } else if(action.equals("bid")) {
            if(!parameters.contains("id") || !parameters.contains("amount")) {
                reply = "type: bid, ok: false";
            } else {
                String [] splitedString = parameters.split("bid");
                String id = parse("id", splitedString[1]);
                String amount = parse("amount", splitedString[1]);
                reply = bid(username, id, amount);
            }
        } else if(action.equals("edit_auction")) {
            String id = parse("id", parameters);
            String title = parse("title", parameters);
            String description = parse("description", parameters);
            String deadline = parse("deadline", parameters);
            String code = parse("code", parameters);
            String amount = parse("amount", parameters);

            if(!parameters.contains("title")) title = "";
            if(!parameters.contains("description")) description = "";
            if(!parameters.contains("deadline")) deadline = "";
            if(!parameters.contains("code")) code = "";
            if(!parameters.contains("amount")) amount = "";

            reply = editAuction(username, id, title, description, deadline, code, amount);

        } else if(action.equals("message")) {
            if(!parameters.contains("id") || !parameters.contains("text")) {
                reply = "type: message, ok: false";
            } else {
                String id = parse("id", parameters);
                String text = parse("text", parameters);
                reply = message(username, id, text);
            }
        } else if(action.equals("online_users")) {
            reply = onlineUsers(username);
        } else {
            reply = "[SERVER] THIS IS NOT A VALID REQUEST";
        }

        return reply;
    }

    private String logIn(String username, String password) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.login(username, password);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String register(String username, String password, String uuid) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.register(username, password, uuid);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String createAuction(String username, String code, String title, String description, String deadline, String amount, String uuid) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.createAuction(username, code, title, description, deadline, amount, uuid);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String searchAuction(String code) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.searchAuction(code);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String detailAuction(String username, String id) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.detailAuction(username, id);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String myAuctions(String username) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.myAuctions(username);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String bid(String username, String id, String amount) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.bid(username, id, amount);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String editAuction(String username, String id, String title, String description, String deadline, String code, String amount) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.editAuction(username, id, title, description, deadline, code, amount);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String message(String username, String id, String text) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.message(username, id, text);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private String onlineUsers(String username) {
        String reply = new String();

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected) {
            try {
                retries++;
                reply = Server.iBei.onlineUsers(username);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected && retries == 4) return "[SERVER] CANNOT HANDLE YOUR REQUEST AT THIS TIME";
            else if(!reconnected) {
                try {
                    Thread.sleep(10000);
                    System.out.println("[SERVER] CONNECTION FAILED, ATTEMPTING ANOTHER TIME");
                } catch(Exception sleep) {
                    return "[SERVER] SOMETHING WENT WRONG WITH THE THREAD SLEEP";
                }
            }
        }

        return reply;
    }

    private void logOutUser(String username) {
        String reply = new String();

        boolean reconnected = false;
        while(!reconnected) {
            try {
                Server.iBei.logOutUser(username);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected) {
                try {
                    Thread.sleep(10000);
                } catch(Exception sleep) {
                    return;
                }
            }
        }

        return;
    }

    private String parse(String parameter, String request) {
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

    private String parseString(String reply) {

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < reply.length(); i++) {
            if(!(reply.charAt(i) == '\0')) {
                sb.append(reply.charAt(i));
            } else break;
        }

        return sb.toString();
    }

    private String cleanUpStrings(String string) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < string.length(); i++) {
            if(!(string.charAt(i) == ' ')) {
                sb.append(string.charAt(i));
            }
        }

        return sb.toString();
    }

    private void cleanUpUUIDs(String table) {
        String reply = new String();

        boolean reconnected = false;
        while(!reconnected) {
            try {
                Server.iBei.cleanUpUUIDs(table);
                reconnected = true;
            } catch(Exception e) {
                try{
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiregistryport).lookup("iBei");
                } catch(Exception e2) {
                    reconnected = false;
                }
            }

            if(!reconnected) {
                try {
                    Thread.sleep(10000);
                } catch(Exception sleep) {
                    return;
                }
            }
        }

        return;
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
