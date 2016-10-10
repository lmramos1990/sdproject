import java.util.*;
import java.net.*;
import java.io.*;

class TCPServer {
    public static void main(String[] args) {

        if(args.length >= 1) {
            System.out.println("ERROR: USAGE is java TCPServer");
            return;
        }

        IncomingRequestsThread incomingRequests = new IncomingRequestsThread("incoming_requests_thread");
    }
}

class IncomingRequestsThread implements Runnable {
    Thread thread;
    String name;
    private static ServerSocket socket;
    private static int port = 7000;

    IncomingRequestsThread(String name) {
        thread = new Thread(this, name);
        thread.start();
    }

    public void run() {

        selectPort();

        String messageFromClient = new String();
        String messageToClient = new String();

        while(true) {
            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);

            try {
                socket.accept();
            } catch(Exception e) {
                System.out.println("AN ERROR HAS OCURRED: " + e);
                return;
            }

            System.out.println("[SERVER] THE CLIENT CAN TALK WITH ME NOW");
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
            socket = new ServerSocket(port);
        } catch(Exception e) {
            return false;
        }

        return true;
    }
}
