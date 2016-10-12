import java.util.*;
import java.net.*;
import java.io.*;

class TCPServer {

    private static ServerSocket socket;
    private static int port = 7000;

    public static void main(String[] args) {

        if(args.length > 0) {
            System.out.println("ERROR: USAGE is java TCPServer");
            return;
        }

        selectPort();

        Socket incomingRequests;

        while(true) {
            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);

            try {
                incomingRequests = socket.accept();
            } catch(Exception e) {
                System.out.println("AN ERROR HAS OCURRED: " + e);
                return;
            }

            Reader input = new Reader();
            BufferedReader inputStream;
            // // DataOutputStream dos;
            //
            inputStream = new BufferedReader(input);
            // // dos = new DataOutputStream(incomingRequests.getOutputStream());
            //
            System.out.println("[SERVER] THE CLIENT CAN TALK WITH ME NOW");
            //
            // String request = inputStream.readLine();
            // System.out.println(request);
            //
            // socket.close();
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

class IncomingRequestsThread implements Runnable {
    Thread thread;
    String name;
    ServerSocket serverSocket;

    IncomingRequestsThread(String name, ServerSocket socket) {
        thread = new Thread(this, name);
        serverSocket = socket;

        thread.start();
    }

    public void run() {

    }
}
