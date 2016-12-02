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
    static String rmiRegistryIP;

    static int numberOfClients = 0;
    static ArrayList <Socket> clientSockets = new ArrayList<>();
    static ArrayList<ClientObject> listOfClients = new ArrayList<>();
    static AuctionInterface iBei;
    static int rmiRegistryPort = -1;
    public static Server server;

    private Server() throws RemoteException {
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

        try {
            server = new Server();
        } catch(RemoteException re) {
            re.printStackTrace();
        }


        while(!connected && connecting < 6) {
            try {
                connecting++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiRegistryIP, rmiRegistryPort).lookup("iBei");
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
            e.printStackTrace();
        }
    }

    private static void readProperties() {
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
            rmiRegistryPort = Integer.parseInt(prop.getProperty("rmiRegistryPort"));

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
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

        for(int i = 0; i < Server.listOfClients.size(); i++) {
            onlineUsersList.add(Server.listOfClients.get(i).getUsername());
        }

        return onlineUsersList;
    }

    public void sendNotificationToUser(String username, String message) {
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
}

class TCPConnection extends Thread {
    private BufferedReader inFromClient = null;
    private PrintWriter outToClient;
    private Socket clientSocket;
    private ClientObject client;

    private String username = "";

    TCPConnection(Socket pclientSocket) {
        this.username = "";
        this.clientSocket = pclientSocket;

        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
            this.start();
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
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
            } catch(ArrayIndexOutOfBoundsException aioobe) {
                outToClient.println("[SERVER] THIS IS NOT A VALID REQUEST");
            } catch(IOException ioe) {
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

            String user = isUser(request.get("username"));

            if(user.equals("NO")) return "type: login, ok: false";
            else if(user.equals("SERVER DOWN")) return "[SERVER] CANNOT HANDLE THE REQUEST AT THIS MOMENT";

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
            String uuid = UUID.randomUUID().toString();

            System.out.println("THIS IS THE UUID: " + uuid);

            String user = isUser(request.get("username"));

            if(user.equals("YES")) return "type: register, ok: false";
            else if(user.equals("SERVER DOWN")) return "[SERVER] CANNOT HANDLE THE REQUEST AT THIS MOMENT";

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

            return register(request.get("username"), hpassword, esalt);
        } else if(!username.equals("") && request.get("type").equals("create_auction") && request.containsKey("code") && request.containsKey("title") && request.containsKey("description") && request.containsKey("deadline") && request.containsKey("amount") && request.size() == 6) {
            System.out.println("[SERVER] CREATE AUCTION");
            String uuid = UUID.randomUUID().toString();

            System.out.println("THIS IS THE UUID: " + uuid);

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

            return createAuction(username, request.get("code"), request.get("title"), request.get("description"), request.get("deadline"), fAmount);
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
            String uuid = UUID.randomUUID().toString();

            System.out.println("THIS IS THE UUID: " + uuid);

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

            return bid(username, id, fAmount);

        } else if(!username.equals("") && request.get("type").equals("message") && request.containsKey("id") && request.containsKey("text") && request.size() == 3) {
            System.out.println("[SERVER] MESSAGE");
            String uuid = UUID.randomUUID().toString();

            System.out.println("THIS IS THE UUID: " + uuid);

            int id;

            try {
                id = Integer.parseInt(request.get("id"));
            } catch(Exception e) {
                System.out.println("[SERVER] THE ID IS NOT VALID");
                return "type: message, ok: false";
            }

            return message(username, id, request.get("text"));

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

            if(isNumber) return editAuction(username, id, title, description, deadline, code, fAmount);
            else {
                System.out.println("[SERVER] THE AMOUNT IS NOT VALID");
                return "type: edit_auction, ok: false";
            }
        } else {
            return "[SERVER] THIS IS NOT A VALID REQUEST";
        }
    }

    private String logIn(String username, String password) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.login(username, password);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        if(!reply.equals("SERVER DOWN")) return reply;
        else return "[SERVER] CANNOT HANDLE THE REQUEST AT THIS MOMENT";

    }

    private String isUser(String username) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.isUser(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String getSalt(String username) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.getSalt(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String register(String username, String hpassword, String esalt) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.register(username, hpassword, esalt);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String createAuction(String username, String code, String title, String description, String deadline, float amount) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.createAuction(username, code, title, description, deadline, amount);
                Server.iBei.subscribe(Server.server);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String searchAuction(String code) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.searchAuction(code);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String detailAuction(int id) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.detailAuction(id);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String myAuctions(String username) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.myAuctions(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String bid(String username, int id, float amount) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.bid(username, id, amount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String editAuction(String username, int id, String title, String description, String deadline, String code, float amount) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.editAuction(username, id, title, description, deadline, code, amount);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String message(String username, int id, String text) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.message(username, id, text);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private String onlineUsers(String username) {
        String reply = "";

        int retries = 0;
        boolean reconnected = false;
        while(!reconnected && retries < 4) {
            try {
                retries++;
                reply = Server.iBei.onlineUsers(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {System.out.println("[SERVER] CANNOT LOCATE THE RMI SERVER AT THIS MOMENT");}
            }

            if(!reconnected) reply = reconnect(retries);
        }

        return reply;
    }

    private void getNotifications(String username) {
        boolean reconnected = false;
        while(!reconnected) {
            try {
                Server.iBei.startUpNotifications(username);
                reconnected = true;
            } catch(Exception e) {
                try {
                    Server.iBei = (AuctionInterface) LocateRegistry.getRegistry(Server.rmiRegistryIP, Server.rmiRegistryPort).lookup("iBei");
                } catch(Exception e2) {}
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

    private String reconnect(int retries) {
        if(retries == 4) {
            return "SERVER DOWN";
        } else {
            try {
                Thread.sleep(10000);
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
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }

                try {
                    mcSocket.send(serverInfoPacket);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
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
