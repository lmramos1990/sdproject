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

        System.out.print("INSERT THE HOST: ");
        host = reader.nextLine();
        System.out.print("INSERT PORT: ");

        Socket socket;

        try {
            socket = new Socket(host, port);
        } catch(Exception e) {
            System.out.println("fuck my life");
        }
    }
}
