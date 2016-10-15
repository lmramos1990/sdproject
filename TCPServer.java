import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;


class TCPServer {

    private static ServerSocket serverSocket;
    private static int port = 7000;

    public static void main(String args[]) {
        int number = 0;

        if(args.length > 0) {
            System.out.println("ERROR: USAGE is java TCPServer");
            return;
        }

        try {
            selectPort();

            // ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("\t\t ------ HELLO IAM AN AWESOME SERVER ------\n[SERVER] HOSTED ON PORT " + port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] THE CLIENT CAN TALK WITH ME NOW");
                number++;
                new Connection(clientSocket, number);

                //USING THE RMI SERVER - catch NotBoundException
                //System.out.println("[SERVER] I'M IN TOUCH WITH RMI SERVER");
                //AuctionInterface iBei = (AuctionInterface) Naming.lookup("iBei");

            }
        } catch(IOException e) {
            System.out.println("LISTEN: " + e.getMessage());
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
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    int threadNumber;

    public Connection (Socket pclientSocket, int number) {
        threadNumber = number;
        try {
            clientSocket = pclientSocket;

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {
            System.out.println("CONNECTION: " + e.getMessage());
        }
    }

    public void run() {
        String reply;
        try {
            while(true) {
                //an echo server
                String data = in.readUTF();
                System.out.println("THREAD[" + threadNumber + "] RECIEVED: " + data);
                reply = data.toUpperCase();
                out.writeUTF(reply);
            }
        } catch(EOFException e) {
            // WHEN A CLIENT DISCONNECTS!!!
            System.out.println("EOF: " + e);
        } catch(IOException e) {
            System.out.println("IO: " + e);
        }
    }
}
