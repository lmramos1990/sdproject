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

        String host = getHost();
        int port = getPort();

        Socket socket;

        try {
            socket = new Socket(host, port);
        } catch(Exception e) {
            System.out.println("NEITHER THE HOST NOR THE PORT HAVE VALID VALUES");
        }
    }

    private static String getHost() {
        String host = new String();
        Scanner reader = new Scanner(System.in);

        System.out.print("INSERT THE HOST: ");
        host = reader.nextLine();

        return host;
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

}
