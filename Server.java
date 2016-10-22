import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;

class Server {
    private static ServerSocket serverSocket;
    private static int port = 7000;
    public static int numberOfClients = 0;

    public static void main(String args[]) {
        System.setProperty("java.net.preferIPv4Stack" , "true");

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPServer");
            return;
        }

        try {
            selectPort();
            InetAddress localip = null;

            try {
                localip = InetAddress.getLocalHost();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                return;
            }

            String adress = localip.getHostAddress();
            new ServerLoad(adress, port);

            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] A CLIENT HAS CONNECTED WITH ME");
                Server.numberOfClients++;
                new TCPConnection(clientSocket);
            }

        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
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
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket clientSocket;

    // SOME OF THESE VARIABLES MAY CHANGE TO LOCAL OVER TIME (BEWARE)
    private static String username = new String();
    private static String password = new String();
    private static String code = new String();
    private static String title = new String();
    private static String description = new String();
    private static String deadline = new String();
    private static String amount = new String();
    private static String id = new String();
    private static String text = new String();
    // SOME OF THESE VARIABLES MAY CHANGE TO LOCAL OVER TIME (BEWARE)

    public TCPConnection(Socket pclientSocket) {

        try {
            AuctionInterface iBei = (AuctionInterface) Naming.lookup("rmi://localhost/iBei");
        } catch(NotBoundException nbe) {
            System.out.println("ERROR LOOKUP: " + nbe);
            return;
        } catch(RemoteException re) {
            System.out.println("ERROR LOOKUP: " + re);
            return;
        } catch(MalformedURLException murle) {
            System.out.println("ERROR LOOKUP: " + murle);
            return;
        }


        try {
            clientSocket = pclientSocket;

            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public void run() {
        String reply = new String();

        try {
            while(true) {
                byte[] buffer = new byte[1024];
                dataInputStream.read(buffer);
                String data = new String(buffer);

                System.out.println("[CLIENT] receiveD: " + data);

                String action = parse("type", data);

                reply = courseOfAction(action, data);

                byte[] message = reply.getBytes();
                dataOutputStream.write(message);
            }
        } catch(Exception e) {
            System.out.println("[SERVER] THE CLIENT DISCONNECTED");
            Server.numberOfClients--;

            try {
                this.clientSocket.close();
            } catch(IOException ioe) {
                System.out.println("ERROR WHEN TRYING TO CLOSE THE CLIENT SOCKET: " + ioe.getMessage());
            }

            Thread.currentThread().interrupt();
            return;
        }
    }

    private static String courseOfAction(String action, String parameters) {
        String reply = new String();

        if(action.equals("login") || action.equals("register")) {

            username = parse("username", parameters);
            password = parse("password", parameters);

            attemptLoginRegister(action, username, password);

            // if(action.equals("login")) {
            //     reply = "type: login, ok: true";
            // } else {
            //     reply = "type: register, ok: true";
            // }

        } else if(action.equals("create_auction")) {

            code = parse("code", parameters);
            title = parse("title", parameters);
            description = parse("description", parameters);
            deadline = parse("deadeline", parameters);
            amount = parse("amount", parameters);

        } else if(action.equals("search_auction")) {
            code = parse("code", parameters);

        } else if(action.equals("detail_auction")) {
            id = parse("id", parameters);

        } else if(action.equals("my_auctions")) {
            //ONLY ACTION type: my_auctions

        } else if(action.equals("bid")) {
            id = parse("id", parameters);
            amount = parse("amount", parameters);

        } else if(action.equals("edit_auction")) {
            id = parse("id", parameters);
            deadline = parse("deadline", parameters);

        } else if(action.equals("message")) {
            id = parse("id", parameters);
            text = parse("text", parameters);

        } else if(action.equals("online_users")) {
            // ONLY ACTION type: online_users

        } else {
            return "ERROR: THIS IS'NT A VALID REQUEST";
        }

        return reply;
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

    private static String attemptLoginRegister(String action, String username, String password) {
        if(action.equals("login")) {
            System.out.println("LOGIN -> SEND THIS TO THE RMI SERVER");

        } else {
            System.out.println("REGISTER -> SEND THIS TO THE RMI SERVER");
        }

        return action;
    }
}

class ServerLoad extends Thread {
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    private static MulticastSocket mcSocket;
    private static int mcport = 8000;
    private static int port;
    private static String ipAdress;

    public ServerLoad(String adress, int tcpport) {
        port = tcpport;
        ipAdress = adress;
        this.start();
    }

    public void run() {
        selectPort();

        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            mcSocket.joinGroup(group);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
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
                String sentence = "THE SERVER HOSTED IN " + ipAdress + " ON PORT " + port + " HAS " + Server.numberOfClients + " CLIENTS CONNECTED TO IT";

                byte [] sendData = sentence.getBytes();
                DatagramPacket serverInfoPacket = null;

                try {
                    serverInfoPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("228.5.6.7"), mcport);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e);
                    Thread.currentThread().interrupt();
                    return;
                }

                try {
                    mcSocket.send(serverInfoPacket);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, 0, 60000);
    }

    private static void receiveOthersInfo() {
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                byte [] receiveData = new byte[1024];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    mcSocket.receive(receivePacket);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e);
                    Thread.currentThread().interrupt();
                    return;
                }

                String receiveString = new String(receivePacket.getData());

                System.out.println(receiveString);

            }
        }, 0, 60000);
    }

    private static void selectPort() {
        if(isPortAvailable(mcport) == false) {
            mcport += 1;
            selectPort();
        }
    }

    private static Boolean isPortAvailable(int port) {
        try {
            mcSocket = new MulticastSocket(port);
        } catch(Exception e) {
            return false;
        }

        return true;
    }
}
// AULA
    // CENAS PARA TRABALHAR CONCURRENTEMENTE (WhAT?)
    // ATOMIC INTEGER
    // COPYONWIRTEARAYLIST
    // CONCURRENTHASHMAP

    // USAR UM FICHEIRO DE CONFIGURAÇAO PARA DECIDIR ONDE VAO ESTAR ALOJADOS OS SERVIDORES???? <- maybe not
    // USAR MULTICAST SOCKETS PARA SABER A CARGA DOS SERVIDORES right!

    // CODIGO ISBN/ESN TEM 13 DIGITOS!!!


    // SERVER FAZ LOOKUP E PODE DAR BODE <-- CUIDADO!!

// package pt.uc.dei.sd.ibei.helpers;
//
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.List;
// import java.util.NoSuchElementException;
// import java.util.stream.Collectors;
// import java.util.stream.IntStream;
//
// public class ProtocolParser {
//     public static HashMap<String, String> parse(String line) {
// 		HashMap<String, String> g = new HashMap<>();
// 		Arrays.stream(line.split(",")).map(s -> s.split(":")).forEach( i -> g.put(i[0].trim(), i[1].trim()) );
// 		return g;
// 	}
//
//     public static List<HashMap<String, String>> getList(HashMap<String, String> map, String field) {
//     	if (!map.containsKey(field + "_count")) {
//     		throw new NoSuchElementException();
//     	}
//     	int count = Integer.parseInt(map.get(field + "_count"));
//     	return IntStream.range(0, count).mapToObj((int i) -> {
//     		HashMap<String, String> im = new HashMap<>();
//     		String prefix = field + "_" + i;
// 			map.keySet().stream().filter((t) -> t.startsWith(prefix)).forEach((k) -> {
// 				im.put(k.substring(prefix.length()+1), map.get(k));
// 			});
// 			return im;
//     	}).collect(Collectors.toList());
//     }
//
//     public static void main(String[] args) {
//     	String a = "type : search_auction , items_count : 2, items_0_id : 101, items_0_code : 9780451524935, items_0_title : 1984, items_1_id : 103, items_1_code : 9780451524935, items_1_title : 1984 usado";
//     	HashMap<String, String> m = ProtocolParser.parse(a);
//
//     	assert(m.get("type").equals("search_auction"));
//     	assert(ProtocolParser.getList(m, "items").size() > 0);
//     	assert(ProtocolParser.getList(m, "items").get(0).get("id").equals("101"));
//     	assert(ProtocolParser.getList(m, "items").get(1).get("code").equals("9780451524935"));
//
//     	for (HashMap<String, String> element : ProtocolParser.getList(m, "items")) {
//     		assert(Integer.parseInt(element.get("id")) > 0);
//     	}
//
//     }
// }
