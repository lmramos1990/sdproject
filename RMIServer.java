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
        KeepAlive keepalive = new KeepAlive(rmiNumber);
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

//----------------------------------------------------------------------------------------------------------------
// Class that is supposed to be keeping the servers running at all times
class KeepAlive extends Thread{
    private static int port = 9000;
    private int rmiNumber;
    private String isAlive = "Y";

    public KeepAlive(int _rmiNumber) {
        this.rmiNumber = _rmiNumber;
        try{
            this.start();
        } catch( Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        //MAIN RMI SERVER
        if (rmiNumber == 1) {
            try{
                selectPort();
                DatagramSocket rmiSocket = new DatagramSocket(port);
                System.out.println("RMI to RMI Socket Listening at Port: " + port);
                while(true){
                    byte[] buffer = new byte[1];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    rmiSocket.receive(request);
                    DatagramPacket reply = new DatagramPacket(request.getData(),request.getLength(), request.getAddress(), request.getPort());
                    rmiSocket.send(reply);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                      System.out.println("Thread: " + e.getMessage());
                    }
                }
            }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
    		}catch (IOException e) {System.out.println("IO: " + e.getMessage());
    		}

        //SECONDARY RMI SERVER
        } else {
            try{
                DatagramSocket rmiSocket = new DatagramSocket();
                InputStreamReader input = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(input);

                while(true){
                    byte [] m = isAlive.getBytes();
                    //TODO: it should be generic
                    InetAddress aHost = InetAddress.getByName("127.0.0.1");

                    DatagramPacket request = new DatagramPacket(m,m.length,aHost,port);
                    rmiSocket.send(request);
                    byte[] buffer = new byte[1];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    rmiSocket.receive(reply);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                      System.out.println("Thread: " + e.getMessage());
                    }
                }
            }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
            }catch (IOException e){System.out.println("IO: " + e.getMessage());
            }
        }

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
}

//------------------------------------------------------------------------------------------------------------------------------
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
