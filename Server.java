import java.util.*;
import java.net.*;
import java.io.*;

class Server {

    private static ServerSocket serverSocket;
    private static int port = 7000;

    public static void main(String args[]) {
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
                new Connection(clientSocket, number);

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

class Connection extends Thread {
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket clientSocket;
    int threadNumber;

    public Connection (Socket pclientSocket, int number) {
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
                String data = dataInputStream.readUTF();
                System.out.println("THREAD[" + threadNumber + "] RECIEVED: " + data);

                String action = parse("type: ", data);

                reply = courseOfAction(action, data);

                dataOutputStream.writeUTF(reply);
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
            String username = parse("username: ", parameters);
            String password = parse("password: ", parameters);

            if(action.equals("login")) {
                reply = "type: login, ok: true";
            } else {
                reply = "type: register, ok: true";
            }

        } else if(action.equals("create_auction")) {

            String code = parse("code: ", parameters);
            String title = parse("title: ", parameters);
            String description = parse("description: ", parameters);
            String deadline = parse("deadeline: ", parameters);
            String amount = parse("amount: ", parameters);

        } else if(action.equals("search_auction")) {
            String code = parse("code: ", parameters);

        } else if(action.equals("detail_auction")) {
            String id = parse("id: ", parameters);
        } else if(action.equals("my_auctions")) {
            //ONLY ACTION type: my_auctions
        } else if(action.equals("bid")) {
            String id = parse("id: ", parameters);
            String amount = parse("amount: ", parameters);

        } else if(action.equals("edit_auction")) {
            String id = parse("id: ", parameters);
            String deadline = parse("deadline: ", parameters);

        } else if(action.equals("message")) {
            String id = parse("id: ", parameters);
            String text = parse("text: ", parameters);

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

        return string;
    }

    private static String attemptLoginRegister(String action, String username, String password) {
        if(action.equals("login")) {
            System.out.println("LOGIN -> SEND THIS BULLSHIT TO THE RMI SERVER");
        } else {
            System.out.println("REGISTER -> SEND THIS BULLSHIT TO THE RMI SERVER");
        }

        return action;
    }
}

/*class ServerLoad extends Thread {
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    int threadNumber;

    public ServerLoad () {
        try {
    		DatagramSocket udpSocket = new DatagramSocket();
    		String texto = "";
    		InputStreamReader input = new InputStreamReader(System.in);
    		BufferedReader reader = new BufferedReader(input);

            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {
            System.out.println("DATAGRAM: " + e.getMessage());
        }
    }

    public void run() {
        try {
			while(true){
				System.out.print("Mensagem a enviar = ");
				// READ STRING FROM KEYBOARD
    	     	  try{
                    texto = "some text";
    				byte [] m = texto.getBytes();

    				DatagramPacket request = new DatagramPacket(m, m.length);
    				udpSocket.send(request);

    				byte[] buffer = new byte[1000];

    				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
    				udpSocket.receive(reply);

    				System.out.println("Recebeu: " + new String(reply.getData(), 0, reply.getLength()));
    			}
		} catch (IOException e){System.out.println("IO: " + e.getMessage());
		} finally {if(udpSocket != null) udpSocket.close();}
    }
}*/


// AULA
    // ATOMIC INTEGER
    // COPYONWIRTEARAYLIST
    // CONCURRENTHASHMAP

    // ENTRADA NO SERVIDOR PODE SER type:login!!!!
    // USAR UM FICHEIRO DE CONFIGURAÇAO PARA DECIDIR ONDE VAO ESTAR ALOJADOS OS SERVIDORES
    // USAR MULTICAST SOCKETS! PARA SABER A CARGA DOS SERVIDORES
