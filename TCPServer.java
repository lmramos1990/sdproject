import java.util.*;
import java.net.*;
import java.io.*;

class TCPServer {

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
                /*InetAddress group = InetAddress.getByName("224.0.0.2");
                MulticastSocket s = new MulticastSocket(7500);
                s.joinGroup(group);

                DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, 7001);
                s.send(hi);
                // get their responses!
                byte[] buf = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                s.receive(recv);*/


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

                // CHANGE THIS BULLSHIT!!!
                // MUST CHANGE THIS 
                String [] aux1 = data.split("type: ");
                String [] aux2 = aux1[1].split(",", 2);
                String [] aux3 = aux2[1].split(" ", 2);

                String action = aux2[0];
                String parameters = aux3[1];

                reply = courseOfAction(action, parameters);

                dataOutputStream.writeUTF(reply);
            }
        } catch(EOFException eofe) {
            System.out.println("[SERVER] THE CLIENT DISCONNECTED");

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
            // CHANGE THIS BULLSHIT!!!
            // String username = new String();
            // String password = new String();
            //
            // String [] aux1 = parameters.split("username: ", 2);
            // String [] aux2 = aux1[1].split("password: ", 2);
            //
            // String aux3 = new String();
            //
            // for(int i = 0; i < aux2.length; i++) {
            //     aux3 = aux3.concat(aux2[i]);
            // }
            //
            // String [] aux4 = aux3.split(",", 2);
            // String [] aux5 = aux4[1].split(" ");
            //
            // username = aux4[0];
            // password = aux5[1];
            //
            // reply = attemptLoginRegister(action, username, password);

            // jsut for tests
            if(action.equals("login")) {
                reply = "type: login, ok: true";
            } else {
                reply = "type: register, ok: true";
            }
        } else if(action.equals("create_auction")) {
            System.out.println("create an auction");
        } else if(action.equals("search_auction")) {
            System.out.println("search_auction");
        } else if(action.equals("detail_auction")) {
            System.out.println("detail_auction");
        } else if(action.equals("my_auctions")) {
            System.out.println("my_auctions");
        } else if(action.equals("bid")) {
            System.out.println("bid");
        } else if(action.equals("edit_auction")) {
            System.out.println("edit_auction");
        } else if(action.equals("message")) {
            System.out.println("message");
        } else if(action.equals("online_users")) {
            System.out.println("online_users");
        } else {
            return "ERROR: THIS IS'NT A VALID REQUEST";
        }

        return reply;
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
                    texto = "merdinha";
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
