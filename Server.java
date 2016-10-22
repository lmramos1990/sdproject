import java.util.*;
import java.net.*;
import java.io.*;

class Server {

    private static ServerSocket serverSocket;
    private static int port = 7000;

    public static void main(String args[]) {

        System.setProperty("java.net.preferIPv4Stack" , "true");

        int number = 0;
        int count = 0;

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPServer");
            return;
        }

        try {
            selectPort();
            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);

            //Sends/Receives Packets about the server load
            //new ServerLoad();

            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] A CLIENT HAS CONNECTED WITH ME");
                number++;
                new TCPConnection(clientSocket, number);
                new ServerLoad(port);

                //Joins Multicast Socket

                // InetAddress group = InetAddress.getByName("224.0.0.2");
                // MulticastSocket s = new MulticastSocket(7500);
                // s.joinGroup(group);
                //
                // DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, 7001);
                // s.send(hi);
                // // get their responses!
                // byte[] buf = new byte[1000];
                // DatagramPacket recv = new DatagramPacket(buf, buf.length);
                // s.receive(recv);

                //USING THE RMI SERVER - catch NotBoundException
                //System.out.println("[SERVER] I'M IN TOUCH WITH RMI SERVER");
                //AuctionInterface iBei = (AuctionInterface) Naming.lookup("iBei");


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
    int threadNumber;

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

    public TCPConnection(Socket pclientSocket, int number) {
        threadNumber = number;
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

                System.out.println("THREAD[" + threadNumber + "] RECIEVED: " + data);

                String action = parse("type", data);

                reply = courseOfAction(action, data);

                byte[] message = reply.getBytes();
                dataOutputStream.write(message);
            }
        } catch(EOFException eofe) {
            System.out.println("[SERVER] THE CLIENT DISCONNECTED");

            // IF A REQUEST COMES AND THE USER DOESNT GET IT HE SOULD BE NOTIFIED ABOUT IT WHEN HE COMES BACK!!!

            try {
                this.clientSocket.close();
            } catch(IOException ioe) {
                System.out.println("ERROR WHEN TRYING TO CLOSE THE CLIENT SOCKET: " + ioe.getMessage());
            }

            Thread.currentThread().interrupt();
            return;
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static String courseOfAction(String action, String parameters) {
        String reply = new String();

        if(action.equals("login") || action.equals("register")) {

            username = parse("username", parameters);
            password = parse("password", parameters);

            if(action.equals("login")) {
                reply = "type: login, ok: true";
            } else {
                reply = "type: register, ok: true";
            }

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

    public ServerLoad(int tcpport) {
        port = tcpport;
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

        InetAddress adress = null;

        try {
            adress = InetAddress.getLocalHost();
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }

        String hello = adress.getHostAddress();

        System.out.println("HELLO IAM A SERVER ON THE PORT " + port + " AND IAM AWESOME");
        System.out.println("MY ADDRESS IS: " + hello);
        // System.out.println("I HAVE " + numClients + " CONNECTED TO ME");

        // figure out how many clients I have connected and send them in a UDP packet!!!
        // how to update number of clients as soon as they connect ?


        sendMyInformation();

    }

    private static void sendMyInformation() {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("hello see you in 60s");
            }
        }, 60000, 60000);
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
