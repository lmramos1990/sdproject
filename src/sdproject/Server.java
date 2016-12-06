package sdproject;

import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

class Server extends UnicastRemoteObject implements NotificationCenter {
    private static ServerSocket serverSocket;
    private static int port = 7000;
    static String rmiHost;
    static int registryPort;

    static int numberOfClients;
    static ArrayList <Socket> clientSockets = new ArrayList<>();
    static ArrayList<ClientObject> listOfClients = new ArrayList<>();
    static AuctionInterface iBei;

    public static Server server;
    static ArrayList<RequestObject> requests = new ArrayList<>();

    private Server() throws RemoteException {
        super();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String args[]) {
        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPServer");
            return;
        }

        readProperties();
        System.setProperty("java.rmi.server.hostname", rmiHost);
        System.setProperty("java.net.preferIPv4Stack" , "true");

        System.out.println("[SERVER] TRYING TO ESTABLISH A CONNECTION TO THE RMI SERVER");

        try {
            server = new Server();
        } catch(RemoteException re) {
            re.printStackTrace();
        }

        boolean connected = false;
        int connecting = 0;

        while(!connected && connecting < 6) {
            try {
                connecting++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, registryPort).lookup("iBei");
                iBei.subscribe(server);
                connected = true;
            } catch(Exception e) {
                System.out.println("[SERVER] CANNOT LOCATE THE RMISERVER AT THIS MOMENT");
            }

            if(!connected) {
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if(!connected) {
            System.out.println("[SERVER] CANNOT CONNECT TO THE RMI AT THIS TIME");
            System.out.println("[SERVER] POWERING DOWN");
            System.exit(0);
        }

        try {
            selectPort();

            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            InetAddress enumerationAddresses = null;

            if(enumeration.hasMoreElements()) {

                NetworkInterface n = (NetworkInterface) enumeration.nextElement();
                Enumeration ee = n.getInetAddresses();

                enumerationAddresses = (InetAddress) ee.nextElement();
            }

            if(enumerationAddresses == null) throw new AssertionError();
            String address = enumerationAddresses.getHostAddress();
            new ServerLoad(address, port);

            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("[SERVER] A CLIENT HAS CONNECTED WITH ME");
                numberOfClients++;
                new TCPConnection(clientSocket);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = new FileInputStream(propFileName);

            prop.load(inputStream);

            rmiHost = prop.getProperty("rmiHost");
            registryPort = Integer.parseInt(prop.getProperty("registryPort"));

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch(Exception e) {e.printStackTrace();}
        }
    }

    private static void selectPort() {
        if(!isPortAvailable(port)) {
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

    public boolean isUserOnline(String username) throws RemoteException {
        for(int i = 0; i < Server.listOfClients.size(); i++) {
            if(username.equals(Server.listOfClients.get(i).getUsername())) {
                return true;
            }
        }

        return false;
    }

    public ArrayList getOnlineUsers() throws RemoteException {
        ArrayList<String> onlineUsersList = new ArrayList<>();

        for(ClientObject listOfClient : listOfClients) {
            onlineUsersList.add(listOfClient.getUsername());
        }

        return onlineUsersList;
    }

    public void sendNotificationToUser(String username, String message) throws RemoteException {
        PrintWriter toTheClient;

        for(int i = 0; i < Server.listOfClients.size(); i++) {
            if(username.equals(Server.listOfClients.get(i).getUsername())) {
                try {
                    toTheClient = new PrintWriter(Server.listOfClients.get(i).getClientSocket().getOutputStream(), true);
                    toTheClient.println(message);
                } catch(Exception e) {e.printStackTrace();}
            }
        }
    }

    public void updateRequest(String uuid) throws RemoteException {
        for(RequestObject request : requests) {
            if(uuid.equals(request.getUUID())) {
                request.setModified(1);
                return;
            }
        }
    }

    public int requestStatus(String uuid) throws RemoteException {
        for(RequestObject request : requests) {
            if(uuid.equals(request.getUUID())) {
                return request.getModified();
            }
        }
        return -1;
    }

//    private void printRequestObjects() {
//        for (RequestObject request : requests) {
//            System.out.println("REQUEST UUID: " + request.getUUID());
//            System.out.println("REQUEST FLAG: " + request.getModified());
//        }
//    }

}

class TCPConnection extends Thread {
    private BufferedReader inFromClient = null;
    private PrintWriter outToClient;
    private Socket clientSocket;
    private ClientObject client;

    private String username = "";
    private int numberOfRetries = 40;

    TCPConnection(Socket pclientSocket) {
        this.username = "";
        this.clientSocket = pclientSocket;

        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch(IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        this.start();
    }

    public void run() {
        String data;
        String reply;

        while(true) {
            try {
                data = inFromClient.readLine();

                if(data.equals("") || !data.startsWith("type")) {
                    outToClient.println("[SERVER] THIS IS NOT A VALID REQUEST");
                } else {
                    HashMap<String, String> request = new HashMap<>();
                    Arrays.stream(data.split(",")).map(s -> s.split(":")).forEach(i -> request.put(i[0].trim(), i[1].trim()));

                    reply = courseOfAction(request);

                    outToClient.println(reply);

                    if(reply.equals("type: login, ok: true")) getNotifications(username);

                }
            } catch(ArrayIndexOutOfBoundsException | IOException ioe) {
                outToClient.println("[SERVER] THIS IS NOT A VALID REQUEST");
            } catch(NullPointerException npe) {
                System.out.println("[SERVER] A CLIENT HAS DISCONNECTED");
                Server.numberOfClients--;

                synchronized (this) {
                    if(!username.equals("")) {
                        Server.listOfClients.remove(Server.listOfClients.indexOf(client));

                        try {
                            clientSocket.close();
                        } catch(IOException ioe) {
                            System.out.println("ERROR WHEN TRYING TO CLOSE THE CLIENT SOCKET: " + ioe.getMessage());
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private String courseOfAction(HashMap<String, String> request) {

        if(username.equals("") && request.get("type").equals("login") && request.containsKey("username") && request.containsKey("password") && request.size() == 3) {
            System.out.println("[SERVER] LOGIN");

            String isUser = isUser(request.get("username"));

            if(isUser.equals("NO")) return "type: login, ok: false, message: this user is not registered";
            else if(isUser.equals("SERVER DOWN")) return "type: login, ok: false, message: we are experiencing some problems on our servers, try again later";

            String esalt;
            String dsalt;
            String hpassword;

            try {
                esalt = getSalt(request.get("username"));
                if(esalt.equals("")) return "type: login, ok: false";
                dsalt = Encryptor.decrypt(esalt);
                hpassword = generateStrongPasswordHash(request.get("password"), dsalt);
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println();
                return "type: login, ok: false";
            }

            String reply = logIn(request.get("username"), hpassword);

            if(reply.equals("type: login, ok: true")) {
                username = request.get("username");
                client = new ClientObject(clientSocket, username);
                Server.listOfClients.add(client);
            }

            return reply;
        } else if(username.equals("") && request.get("type").equals("register") && request.containsKey("username") && request.containsKey("password") && request.size() == 3) {
            System.out.println("[SERVER] REGISTER");

            String isUser = isUser(request.get("username"));

            if(isUser.equals("YES")) return "type: register, ok: false, message: this user already exists";
            else if(isUser.equals("SERVER DOWN")) return "type: register, ok: false, message: we are experiencing some problems on our servers, try again later";

            String salt;
            String hpassword;
            String esalt;

            try {
                salt = getSalt();
                hpassword = generateStrongPasswordHash(request.get("password"), salt);
                esalt = Encryptor.encrypt(salt);
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("[SERVER] SOMETHING WENT WRONG WHEN GENERATING THE PWD HASH");
                return "type: register, ok: false";
            }

            String uuid = UUID.randomUUID().toString();
            RequestObject requestObject = new RequestObject(uuid, 0);

            if(!Server.requests.contains(requestObject)) Server.requests.add(requestObject);

            return register(uuid, request.get("username"), hpassword, esalt);
        } else if(!username.equals("") && request.get("type").equals("create_auction") && request.containsKey("code") && request.containsKey("title") && request.containsKey("description") && request.containsKey("deadline") && request.containsKey("amount") && request.size() == 6) {
            System.out.println("[SERVER] CREATE AUCTION");

            float fAmount;

            try {
                fAmount = Float.parseFloat(request.get("amount"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE AMOUNT DOES NOT HAVE A VALID VALUE");
                return "type: create_auction, ok: false";
            }

            if(request.get("code").length() != 13) {
                System.out.println("[SERVER] THIS IS NOT A VALID CODE");
                return "type: create_auction, ok: false";
            }

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
                LocalDateTime dateTime = LocalDateTime.parse(request.get("deadline"), formatter);
                Timestamp timestamp = Timestamp.valueOf(dateTime);
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                if(timestamp.before(currentTime)) return "type: create_auction, ok: false";
            } catch(Exception e) {
                System.out.println("[SERVER] THIS IS NOT A VALID DEADLINE");
                return "type: create_auction, ok: false";
            }

            String uuid = UUID.randomUUID().toString();
            RequestObject requestObject = new RequestObject(uuid, 0);

            if(!Server.requests.contains(requestObject)) Server.requests.add(requestObject);

            return createAuction(uuid, username, request.get("code"), request.get("title"), request.get("description"), request.get("deadline"), fAmount);
        } else if(!username.equals("") && request.get("type").equals("search_auction") && request.containsKey("code") && request.size() == 2) {
            System.out.println("[SERVER] SEARCH AUCTION");

            if(request.get("code").length() != 13) {
                System.out.println("[SERVER] THIS IS NOT A VALID CODE");
                return "type: search_auction, ok: false";
            }

            return searchAuction(request.get("code"));

        } else if(!username.equals("") && request.get("type").equals("detail_auction") && request.containsKey("id") && request.size() == 2) {
            System.out.println("[SERVER] DETAIL_AUCTION");

            int id;

            try {
                id = Integer.parseInt(request.get("id"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE ID IS NOT VALID");
                return "type: edit_auction, ok: false";
            }

            return detailAuction(id);

        } else if(!username.equals("") && request.get("type").equals("my_auctions") && request.size() == 1) {
            System.out.println("[SERVER] MY_AUCTIONS");

            return myAuctions(username);
        } else if(!username.equals("") && request.get("type").equals("bid") && request.containsKey("id") && request.containsKey("amount") && request.size() == 3) {
            System.out.println("[SERVER] BID");

            float fAmount;
            int id;

            try {
                id = Integer.parseInt(request.get("id"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE ID IS NOT VALID");
                return "type: bid, ok: false";
            }

            try {
                fAmount = Float.parseFloat(request.get("amount"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE AMOUNT IS NOT VALID");
                return "type: bid, ok: false";
            }

            String uuid = UUID.randomUUID().toString();
            RequestObject requestObject = new RequestObject(uuid, 0);

            if(!Server.requests.contains(requestObject)) Server.requests.add(requestObject);

            return bid(uuid, username, id, fAmount);

        } else if(!username.equals("") && request.get("type").equals("message") && request.containsKey("id") && request.containsKey("text") && request.size() == 3) {
            System.out.println("[SERVER] MESSAGE");

            int id;

            try {
                id = Integer.parseInt(request.get("id"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE ID IS NOT VALID");
                return "type: message, ok: false";
            }

            String uuid = UUID.randomUUID().toString();
            RequestObject requestObject = new RequestObject(uuid, 0);

            if(!Server.requests.contains(requestObject)) Server.requests.add(requestObject);

            return message(uuid, username, id, request.get("text"));

        } else if(!username.equals("") && request.get("type").equals("online_users") && request.size() == 1) {
            System.out.println("[SERVER] ONLINE USERS");

            return onlineUsers(username);
        } else if(!username.equals("") && request.get("type").equals("edit_auction") && request.containsKey("id") && (request.size() > 2 && request.size() < 8)) {
            System.out.println("[SERVER] EDIT AUCTION");

            String title, description, deadline, code, amount;

            if(!request.containsKey("title")) title = "";
            else title = request.get("title");

            if(!request.containsKey("description")) description = "";
            else description = request.get("description");

            if(!request.containsKey("deadline")) deadline = "";
            else deadline = request.get("deadline");

            if(!request.containsKey("code")) code = "";
            else code = request.get("code");

            if(!request.containsKey("amount")) amount = "";
            else amount = request.get("amount");

            int id;
            float fAmount = -1.0f;
            boolean isNumber;

            try {
                id = Integer.parseInt(request.get("id"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE ID IS NOT VALID");
                return "type: edit_auction, ok: false";
            }

            if(amount.equals("")) {
                fAmount = -1.0f;
                isNumber = true;
            } else {
                try {
                    fAmount = Float.parseFloat(amount);
                    isNumber = true;
                } catch(Exception e) {
                    isNumber = false;
                }
            }

            if(!deadline.equals("")) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
                    LocalDateTime dateTime = LocalDateTime.parse(request.get("deadline"), formatter);
                    Timestamp timestamp = Timestamp.valueOf(dateTime);
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                    if(timestamp.before(currentTime)) return "type: edit_auction, ok: false";
                } catch(Exception e) {
                    System.out.println("[SERVER] THIS IS NOT A VALID DEADLINE");
                    return "type: edit_auction, ok: false";
                }
            }

            if(isNumber) {
                String uuid = UUID.randomUUID().toString();
                RequestObject requestObject = new RequestObject(uuid, 0);

                if(!Server.requests.contains(requestObject)) Server.requests.add(requestObject);
                return editAuction(uuid, username, id, title, description, deadline, code, fAmount);
            } else {
                System.out.println("[SERVER] THE AMOUNT IS NOT VALID");
                return "type: edit_auction, ok: false";
            }
        } else {
            return "type: " + request.get("type") + ", ok: false, message: this is not a valid request";
        }
    }

    private String logIn(String username, String password) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.login(username, password);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][LOGIN] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) reply = "type: login, ok: false, message: we are experiencing some problems on our servers, try again later";
        }

        return reply;

    }

    private String isUser(String username) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.isUser(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String getSalt(String username) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.getSalt(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String register(String uuid, String username, String hpassword, String esalt) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.register(uuid, username, hpassword, esalt);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][REGISTER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN")) reply = "type: register, ok: false, message: we are experiencing some problems on our servers, try again later";
        else cleanUpUUID(uuid);

        return reply;
    }

    private String createAuction(String uuid, String username, String code, String title, String description, String deadline, float amount) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.createAuction(uuid, username, code, title, description, deadline, amount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][CREATE AUCTION] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN")) reply = "type: create_auction, ok: false, message: we are experiencing some problems on our servers, try again later";
        else cleanUpUUID(uuid);

        return reply;
    }

    private String searchAuction(String code) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.searchAuction(code);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][SEARCH AUCTION] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) reply = "type: search_auction, items_count: 0, message: we are experiencing some problems on our servers, try again later";
        }

        return reply;
    }

    private String detailAuction(int id) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.detailAuction(id);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][DETAIL AUCTION] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) reply = "type: detail_auction, ok: false, message: we are experiencing some problems on our servers, try again later";
        }

        return reply;
    }

    private String myAuctions(String username) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.myAuctions(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][MY AUCTIONS] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) reply = "type: my_auction, items_count = 0, message: we are experiencing some problems on our servers, try again later";
        }

        return reply;
    }

    private String bid(String uuid, String username, int id, float amount) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.bid(uuid, username, id, amount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][BID] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN")) reply = "type: bid, ok: false, message: we are experiencing some problems on our servers, try again later";
        else cleanUpUUID(uuid);

        return reply;
    }

    private String editAuction(String uuid, String username, int id, String title, String description, String deadline, String code, float amount) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.editAuction(uuid, username, id, title, description, deadline, code, amount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][EDIT AUCTION] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN")) reply = "type: edit_auction, ok: false, message: we are experiencing some problems on our servers, try again later";
        else cleanUpUUID(uuid);

        return reply;
    }

    private String message(String uuid, String username, int id, String text) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.message(uuid, username, id, text);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][MESSAGE] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(reply.equals("SERVER DOWN")) reply = "type: message, ok: false, message: we are experiencing some problems on our servers, try again later";
        else cleanUpUUID(uuid);

        return reply;
    }

    private String onlineUsers(String username) {
        String reply = "";
        int retries = 0;
        boolean reconnected = false;

        while(!reconnected && retries < numberOfRetries) {
            try {
                retries++;
                Server.iBei.subscribe(Server.server);
                reply = Server.iBei.onlineUsers(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER][ONLINE USERS] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
            if(reply.equals("SERVER DOWN")) reply = "type: online_users, users_count = 0, message: we are experiencing some problems on our servers, try again later";
        }

        return reply;
    }

    private void getNotifications(String username) {
        boolean reconnected = false;

        while(!reconnected) {
            try {
                Server.iBei.subscribe(Server.server);
                Server.iBei.startUpNotifications(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiHost, Server.registryPort).lookup("iBei");
                } catch(Exception ignored) {}
            }

            if(!reconnected) {
                try {
                    Thread.sleep(10000);
                } catch(Exception sleep) {
                    return;
                }
            }
        }
    }

    private void cleanUpUUID(String uuid) {
        synchronized(this) {
            for(int i = 0; i < Server.requests.size(); i++) {
                if(uuid.equals(Server.requests.get(i).getUUID())) {
                    Server.requests.remove(i);
                    return;
                }
            }
        }
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

    private String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Arrays.toString(salt);
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

    private String reconnect(int retries) {
        if(retries == numberOfRetries) {
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
}

class ServerLoad extends Thread {
    private MulticastSocket mcSocket;
    private int mcport = 10000;
    private int port;
    private String ipAddress;

    ServerLoad(String address, int tcpport) {
        this.ipAddress = address;
        this.port = tcpport;
        this.start();
    }

    public void run() {
        try {
            mcSocket = new MulticastSocket(mcport);
        } catch(Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return;
        }

        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            mcSocket.joinGroup(group);
        } catch(Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return;
        }

        sendMyInformation();
        receiveOthersInfo();
    }

    private void sendMyInformation() {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String sentence = "ip: " + ipAddress + ", port: " + Integer.toString(port) + ", numberofclients: " + Server.numberOfClients + ", count: 0";

                byte [] sendData = sentence.getBytes();
                DatagramPacket serverInfoPacket;

                try {
                    serverInfoPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("228.5.6.7"), mcport);
                } catch(Exception e) {
                    e.printStackTrace();
                    timer.cancel();
                    return;
                }

                try {
                    mcSocket.send(serverInfoPacket);
                } catch(Exception e) {
                    e.printStackTrace();
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }

    private void receiveOthersInfo() {
        ArrayList<String> serversList = new ArrayList<>();
        boolean threadRunning = false;

        while(true) {
            byte [] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                mcSocket.receive(receivePacket);
            } catch(Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }

            String serverInfo = new String(receivePacket.getData());
            serverInfo = parseString(serverInfo);

            if(serversList.size() == 0) {
                serversList.add(serverInfo);
            } else {
                boolean found = false;
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
            }

            if(!threadRunning) {
                threadRunning = true;
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        ArrayList <Socket> clients = Server.clientSockets;
                        PrintWriter outToServer;
                        String message;

                        StringBuilder sb = new StringBuilder();
                        sb.append("type: notification_load, server_list: ").append(serversList.size()).append(", ");

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

        String string;

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
}

class Encryptor {
    private static String key = "Bar12345Bar12345";
    private static String initVector = "RandomInitVector";

    static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            System.out.println("[SERVER] SOME ERROR WAS ENCOUNTERED DURING ENCRYPTION");
            ex.printStackTrace();
        }

        return null;
    }

    static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            System.out.println("[SERVER] SOME ERROR WAS ENCOUNTERED DURING DECRYPTION");
            ex.printStackTrace();
        }

        return null;
    }
}
