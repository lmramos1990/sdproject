import java.util.*;
import java.net.*;
import java.io.*;

class TCPServer {

    private static int port = 7000;

    public static void main(String[] args) {

        if(args.length >= 1) {
            System.out.println("ERROR: USAGE is java TCPServer");
            return;
        }
    }
}

class IncomingRequests implements Runnable {

    public void run() {

    }
    
}
