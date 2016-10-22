import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

class RMIServer extends UnicastRemoteObject implements AuctionInterface {
    private volatile boolean mainRMI = false;

    //CONSTRUCTOR
    protected RMIServer() throws RemoteException {
        super();
    }

    //METHODS
    public String login(String username, String password) throws RemoteException {
        return new String();
    }
    public String register(String username, String password) throws RemoteException {
        return new String();
    }
    public String createAuction(String username, String code, String title, String description, String deadline, String amount) throws RemoteException {
        return new String();
    }
    public String searchAuction(String code) throws RemoteException {
        return new String();
    }

    public synchronized void switchToMainRMI(boolean ismainRMI, RMIServer myRMI) throws IOException {
        //MAIN RMI CODE
        try {
            Naming.rebind("rmi://localhost:10000/iBei", myRMI);
            System.out.println("Fiz bind");
        } catch(Exception e){
            System.out.println("BIND: " + e.getMessage());
        }

        mainRMI = ismainRMI;
    }

    private synchronized void validateIfmainRMI() throws NotMainRMIException{
        if (!mainRMI) {
            throw new NotMainRMIException();
        }
    }
    public synchronized void ping() throws RemoteException {

    }

    public int createAuction(String buyer, String isbnCode, String title, String description, String details, float maxPrice, String deadlineStamp) throws RemoteException{
        return 0;
    }

    public void init (){
        //TODO: we need to find the ip on the configurations file first
        PingService udpPing = new PingService(this, "127.0.0.1");
        udpPing.start();

        try {
            while (true) {
                if (mainRMI) {
                    System.out.println("I'm the main RMI Server");
                    //MAIN RMI CODE



                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("INTERRUPTION:" + e.getMessage());
                }

            }
        } catch(Exception e){
            System.out.println("ERROR: " + e.getMessage());
        }

    }

    //MAIN
    public static void main(String[] args) throws RemoteException{
        RMIServer myRMI = new RMIServer();
        myRMI.init();


    }
}

// Class that handles the requests of the clients
class RMIRequestListener extends Thread {

    public void run() {

    }
}

//----------------------------------------------------------------------------------------------------------------
// Class that is supposed to be keeping the servers running at all times
class PingService extends Thread{
    RMIServer rmiServer;
    String ip;
    DatagramSocket pingSocket = null;
    DatagramPacket receivePacket, sendPacket;
    String message = "Y";
    byte[] dataIn = new byte[1];
    byte[] dataOut;
    //TODO: we need to find the ip on the configurations file first
    int port = 9000;
    int timeout = 1000;

    public PingService(RMIServer rmiServer, String ip) {
        this.rmiServer = rmiServer;
        this.ip = ip;
    }

    public void run() {
        try {
            int count = 0;
            int backup = 0;

            dataOut = message.getBytes(); //
            InetAddress rmiHost = InetAddress.getByName(ip); //
            pingSocket = new DatagramSocket(); //
            pingSocket.setSoTimeout(timeout); //

            while (true) {
                try {
                    sendPacket = new DatagramPacket(dataOut, dataOut.length, rmiHost, port);
                    pingSocket.send(sendPacket);

                    receivePacket = new DatagramPacket(dataIn, dataIn.length);
                    pingSocket.receive(receivePacket);
                    count = 0;
                    backup = 1;
                } catch (SocketException e) {
                    System.out.println("Socket: " + e.getMessage());
                } catch (UnknownHostException e) {
                    System.out.println("Host: " + e.getMessage());
                } catch (IOException e) {
                    count++;
                    if ((backup == 0) || (count == 3)) {
                        //Switching
                        rmiServer.switchToMainRMI(true, rmiServer);
                        break;
                    }
                }

                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    System.out.println("INTERRUPTION:" + e.getMessage());
                }
            }
            pingSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("Socket Exception");
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host Exception");
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }

        //main RMI waits indefinitly for a packet
        try {
            pingSocket.setSoTimeout(0);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }

        //mainRMI
        while (true) {
            try {
                receivePacket = new DatagramPacket(dataIn, dataIn.length);
                pingSocket.receive(receivePacket);
                String myMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                dataOut = myMessage.getBytes();
                sendPacket = new DatagramPacket(dataOut, dataOut.length, receivePacket.getAddress(), receivePacket.getPort());
                pingSocket.send(sendPacket);
            } catch (Exception e) {
                System.out.println("Socket: " + e.getMessage());

            }
        }

    }

}

//------------------------------------------------------------------------------------------------------------------------------
// Class that handles the connection to the database
class DBConnection extends Thread {
    public DBConnection() {

    }

    public void run() {

    }
}

//----------------------------------------------------------------------------------------------------------------------------------
class NotMainRMIException  extends java.lang.Exception {}
