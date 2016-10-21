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

class ServerLoad extends Thread {
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    DatagramSocket socket;

    public ServerLoad() {

    }

    public void run() {

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
    // CENAS PARA TRABALHAR CONCURRENTEMENTE (WhAT?)
    // ATOMIC INTEGER
    // COPYONWIRTEARAYLIST
    // CONCURRENTHASHMAP

    // USAR UM FICHEIRO DE CONFIGURAÇAO PARA DECIDIR ONDE VAO ESTAR ALOJADOS OS SERVIDORES
    // USAR MULTICAST SOCKETS PARA SABER A CARGA DOS SERVIDORES

    // CODIGO ISBN/ESN TEM 13 DIGITOS!!!


    // RMI NAO USA PORTAS !?!?!?


    // SERVER FAZ LOOKUP E PODE DAR BODE <-- CUIDADO!!
    // RMI FAZ BIND PARA DECIDIR QUAL E O PRIMARIO E O SECUNDARIO!!!
