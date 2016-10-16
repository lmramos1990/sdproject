import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

class RMIServer extends UnicastRemoteObject implements AuctionInterface{
    private static int port = 8000;
    //CONSTRUCTOR
    protected RMIServer() throws RemoteException {
        super();
    }

    //METHODS
    public int createAuction(String buyer, String isbnCode, String title, String description, String details, float maxPrice, String deadlineStamp) throws RemoteException{
        return 0;
    }

    private static int selectPort() {
        int myPort = port;
        if(isPortAvailable(port) == false) {
            port += 1;
            selectPort();
        }
        return (port - myPort + 1);
    }

    private static Boolean isPortAvailable(int port) {
        try {
            AuctionInterface iBei = new RMIServer();
            LocateRegistry.createRegistry(port).rebind("iBei", iBei);
        } catch(Exception e) {
            return false;
        }

        return true;
    }

    //MAIN
    public static void main(String[] args) throws RemoteException{
        int rmiNumber = selectPort();
        System.out.println("iBei ready at port: " + port);
        System.out.println("I'm RMIServer Number: " + rmiNumber);
        RMIRequestListener requestlistener = new RMIRequestListener();
        if (rmiNumber > 1) {
            KeepAlive keepalive = new KeepAlive();
        }
    }
}

// Class that handles the requests of the clients
class RMIRequestListener extends Thread {
    public RMIRequestListener() {
        try{
            this.start();
        } catch( Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void run() {

    }
}

// Class that is supposed to be keeping the servers running at all times
class KeepAlive extends Thread{
    public KeepAlive() {
        try{
            this.start();
        } catch( Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        System.out.println("I'm responsible to keep alive the RMI Servers");
        while(true){
        }
    }
}

// Class that handles the connection to the database
class DBConnection extends Thread {
    public DBConnection() {
        try{
            this.start();
        } catch( Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void run() {

    }
}
