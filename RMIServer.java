import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

class RMIServer extends UnicastRemoteObject implements AuctionInterface{
    //CONSTRUCTOR
    protected RMIServer() throws RemoteException {
        super();
    }

    //METHODS
    public int createAuction(String buyer, String isbnCode, String title, String description, String details, float maxPrice, String deadlineStamp) throws RemoteException{
        return 0;
    }

    //MAIN
    public static void main(String[] args) throws RemoteException{
        AuctionInterface iBei = new RMIServer();
        LocateRegistry.createRegistry(1099).rebind("iBei", iBei);
        System.out.println("iBei ready...");

        RMIRequestListener requestlistener = new RMIRequestListener();
        KeepAlive keepalive = new KeepAlive();


        requestlistener.run();
        keepalive.run();
    }
}

// Class that handles the requests of the clients
class RMIRequestListener implements Runnable {
    public RMIRequestListener() {
        System.out.println("This is the RMIRequestListener constructor");
    }

    public void run() {

    }
}

// Class that is supposed to be keeping the servers running at all times
class KeepAlive implements Runnable {
    public KeepAlive() {
        System.out.println("This is the KeepAlive constructor");
    }

    public void run() {

    }
}

// Class that handles the connection to the database
class DBConnection implements Runnable {
    public DBConnection() {
        System.out.println("This is the DBConnection constructor");
    }

    public void run() {

    }
}
