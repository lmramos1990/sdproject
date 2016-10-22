import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

class RMIServer extends UnicastRemoteObject implements AuctionInterface {
    public static int rmiregistryport = 0;

    private static final long serialVersionUID = 1L;

    protected RMIServer() throws RemoteException {
        super();
    }

    protected RMIServer(boolean online) throws RemoteException {
        RMIServer rmiServer = new RMIServer();

        String toBind = "rmi://localhost:" + Integer.toString(rmiregistryport) + "/iBei";
        System.out.println(toBind);

        if(online == true) {
            try {
                Naming.rebind(toBind, rmiServer);
                primaryRMIServer();
            } catch(Exception e) {
                System.out.println("ERROR REBINDING: " + e);
                return;
            }
        } else {
            try {
                Naming.bind(toBind, rmiServer);
                primaryRMIServer();

            } catch(AlreadyBoundException abe) {
                System.out.println("THERE IS A RMISERVER ALREADY ONLINE");
                ConnectionToPrimaryServer secondaryServer = new ConnectionToPrimaryServer();
            } catch(MalformedURLException murle) {
                System.out.println("ERROR: " + murle.getMessage());
                return;
            }
        }
    }

    public static void main(String[] args) {

        // System.getProperties().put("java.security.policy", "policy.all");
        // System.setSecurityManager(new RMISecurityManager());

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java RMIServer");
            return;
        }

        RMIServer.rmiregistryport = getPort();

        try {
            RMIServer rmiServer = new RMIServer(false);
        } catch(Exception e) {
            System.out.println("RMI SERVER CREATION FUCKED UP");
            return;
        }
    }

    private static void primaryRMIServer() {
        System.out.println("IM THE PRIMARY RMISERVER");
        ConnectionToSecondaryServer connectionToSecondaryServer = new ConnectionToSecondaryServer();
    }



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

class ConnectionToSecondaryServer extends Thread {

    DatagramSocket udpSocket;

    public ConnectionToSecondaryServer() {
        this.start();
    }

    public void run() {
        try {
            udpSocket = new DatagramSocket(9876);
        } catch(Exception e) {
            Thread.currentThread().interrupt();
            return;
        }

        byte[] receiveData = new byte[10];
        byte[] sendData = new byte[10];

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                udpSocket.receive(receivePacket);
            } catch(Exception e) {
                Thread.currentThread().interrupt();
            }

            String receiveString = new String(receiveData);
            System.out.println(receiveString);

            String sentence = "YES";

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();

            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                udpSocket.send(sendPacket);
            } catch(Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class ConnectionToPrimaryServer extends Thread {

    DatagramSocket udpSocket;
    private static int count = 0;

    public ConnectionToPrimaryServer() {
        this.start();
    }

    public void run() {
        System.out.println("IM THE SECONDARY RMISERVER");

        while(true) {
            try {
                udpSocket = new DatagramSocket();
                udpSocket.setSoTimeout(5000);
            } catch(Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            InetAddress IPAddress = null;

            try {
                IPAddress = InetAddress.getByName("localhost");
            } catch(Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            byte[] sendData = new byte[10];
            byte[] receiveData = new byte[10];
            String sentence = "ARE YOU ALIVE";

            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);


            try {
                udpSocket.send(sendPacket);
            } catch(Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                udpSocket.receive(receivePacket);
            } catch(Exception e) {
                Thread.currentThread().interrupt();
                return;
            }

            String receivedSentence = new String(receivePacket.getData());
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < receivedSentence.length(); i++) {
                if(!(receivedSentence.charAt(i) == '\0')) {
                    sb.append(receivedSentence.charAt(i));
                } else break;
            }

            String newString = sb.toString();

            if(!(newString.equals("YES"))) {
                System.out.println("PRIMARY SERVER FAILED TO RESPOND");
                count++;
            } else System.out.println(newString);

            if(count == 3) {
                count = 0;

                try {
                    RMIServer myServer = new RMIServer(true);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e);
                    Thread.currentThread().interrupt();
                    return;
                }

                Thread.currentThread().interrupt();
                return;
            }

            try {
                Thread.sleep(5000);
            } catch(Exception e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
