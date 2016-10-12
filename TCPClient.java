import java.io.*;
import java.net.*;
import java.util.*;

class TCPClient {
    public static void main(String[] args) {
        System.out.println("HELLO WORLD!");

        OutGoingRequests outgoingRequests = new OutGoingRequests("outgoing_requests");

        System.out.println("GOODBYE WORLD!");
    }
}

class OutGoingRequests implements Runnable {
    Thread thread;
    String name;

    OutGoingRequests(String name) {
        thread = new Thread(this, name);
        thread.start();
    }

    public void run() {
        System.out.println("HELLO THIS IS THE THREAD THAT THE CLIENT WILL USE TO MAKE REQUESTS TO THE SERVER");

        InetAddress serverAddress = getHost();
        int port = getPort();
        Socket socket;

        try {
            socket = new Socket(serverAddress, port);
            streamingStrings(socket);
        } catch(Exception e) {
            System.out.println("Sock:" + e.getMessage());
        }
    }

    private static InetAddress getHost() {
        String host = new String();
        Scanner reader = new Scanner(System.in);
        InetAddress serverAddress;

        System.out.print("INSERT THE HOST: ");
        host = reader.nextLine();
        try {
            serverAddress = InetAddress.getByName(host);
            return serverAddress;
        } catch(Exception e) {
          System.out.println("Sock:" + e.getMessage());
        }
        return null;
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

    private static int streamingStrings(Socket socket){
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String texto = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(input);
            System.out.println("Introduza texto:");

            // 3o passo
            while (true) {
            // READ STRING FROM KEYBOARD
            try {
                texto = reader.readLine();
            } catch (Exception e) {
            }

            // WRITE INTO THE SOCKET
            out.writeUTF(texto);

            // READ FROM SOCKET
            String data = in.readUTF();

            // DISPLAY WHAT WAS READ
            System.out.println("Received: " + data);
            }

        } catch(Exception e) {
          System.out.println("Sock:" + e.getMessage());
        }
        return 0;

    }
}
