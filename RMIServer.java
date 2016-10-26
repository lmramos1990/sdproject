import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

class RMIServer extends UnicastRemoteObject implements AuctionInterface {
    public static int rmiregistryport = 0;
    private static final long serialVersionUID = 1L;
    private static Properties properties = new Properties();
    private static String rmiRegistryIP = new String();
    public static String rmiServerIP = new String();

    protected RMIServer() throws RemoteException {
        super();
    }

    protected RMIServer(boolean online) throws RemoteException {
        RMIServer rmiServer = new RMIServer();

        readProperties();

        String toBind = "rmi://" + rmiRegistryIP + ":" + Integer.toString(rmiregistryport) + "/iBei";

        if(online == true) {
            try {
                Naming.rebind(toBind, rmiServer);
                primaryRMIServer();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                return;
            }
        } else {
            try {
                Naming.bind(toBind, rmiServer);
                primaryRMIServer();

            } catch(AlreadyBoundException abe) {
                SecondaryServer secondaryServer = new SecondaryServer();
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
            System.out.println("ERROR: RMIREGISTRY IS NOT INITIALIZED");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void readProperties() {
        InputStream inputStream = null;

        try {
			Properties prop = new Properties();
			String propFileName = "config.properties";

			inputStream = new FileInputStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("ERROR: PROPERTY '" + propFileName + "' NOT FOUND IN THE CLASSPATH");
			}

			rmiRegistryIP = prop.getProperty("rmiRegistryIP");
            rmiServerIP = prop.getProperty("rmiServerIP");

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
            try {
                inputStream.close();
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
		}
    }

    private static void primaryRMIServer() {
        PrimaryServer primaryServer = new PrimaryServer();
    }



    public synchronized String login(String username, String password) throws RemoteException {
        System.out.println("[RMISERVER] LOGIN REQUEST");

        // CONNECT TO THE DATABASE

        return "type: login, ok: true";
    }
    
    public synchronized String register(String username, String password) throws RemoteException {
        return "register";
    }
    
    public synchronized String createAuction(String username, String code, String title, String description, String deadline, String amount) throws RemoteException {
        return "create_auction";
    }
    
    public synchronized String searchAuction(String username, String code) throws RemoteException {
        return "search_auction";
    }
    public synchronized String detailAuction(String username, String id) throws RemoteException {
        return "detailAuction";
    }
    public synchronized String myAuctions(String username) throws RemoteException {
        return "myAuctions";
    }
    public synchronized String bid(String username, String id, String amount) throws RemoteException {
        return "bid";
    }
    public synchronized String editAuction(String username, String id, String title, String description, String deadline) throws RemoteException {
        return "editAuction";
    }
    
    public synchronized String message(String username, String id, String text) throws RemoteException {
        return "message";
    }
    
    public synchronized String onlineUsers(String username) throws RemoteException {
        return "online_users";
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

class PrimaryServer extends Thread {

    DatagramSocket udpSocket;

    public PrimaryServer() {
        this.start();
    }

    public void run() {
        System.out.println("[RMISERVER] IM THE PRIMARY SERVER");

        try {
            udpSocket = new DatagramSocket(9876);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        byte[] receiveData = new byte[1];
        byte[] sendData = new byte[1];

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                udpSocket.receive(receivePacket);
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }

            String sentence = "Y";

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();

            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                udpSocket.send(sendPacket);
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

class SecondaryServer extends Thread {

    DatagramSocket udpSocket;
    private static int count = 0;

    public SecondaryServer() {
        this.start();
    }

    public void run() {
        try {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(1000);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        InetAddress ipAddress = null;

        try {
            // SUBJECT TO CHANGE!!!
            ipAddress = InetAddress.getByName(RMIServer.rmiServerIP);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }

        byte[] sendData = new byte[1];
        byte[] receiveData = new byte[1];
        String sentence = "A";

        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 9876);

        try {
            udpSocket.send(sendPacket);
        } catch(Exception e) {
            Thread.currentThread().interrupt();
            return;
        }

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            udpSocket.receive(receivePacket);
        } catch(SocketTimeoutException ste) {

            try {
                RMIServer myServer = new RMIServer(true);
            } catch(Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }

            Thread.currentThread().interrupt();
            return;
        } catch(IOException ioe) {
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("[RMISERVER] IM THE SECONDARY SERVER");

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    udpSocket.setSoTimeout(5000);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }

                InetAddress ipAddress = null;

                try {
                    // SUBJECT TO CHANGE!!!
                    ipAddress = InetAddress.getByName(RMIServer.rmiServerIP);
                } catch(Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    timer.cancel();
                    return;
                }

                byte[] sendData = new byte[1];
                byte[] receiveData = new byte[1];
                String sentence = "A";

                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 9876);

                try {
                    udpSocket.send(sendPacket);
                } catch(Exception e) {
                    timer.cancel();
                    return;
                }

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    udpSocket.receive(receivePacket);
                } catch(SocketTimeoutException ste) {
                    count++;
                } catch(IOException ioe) {
                    timer.cancel();
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

                if(!(newString.equals("Y"))) {
                    System.out.println("PRIMARY SERVER FAILED TO RESPOND");
                } else System.out.println("PRIMARY SERVER IS ALIVE");

                if(count == 3) {
                    try {
                        RMIServer myServer = new RMIServer(true);
                    } catch(Exception e) {
                        System.out.println("ERROR: " + e);
                        timer.cancel();
                        return;
                    }

                    timer.cancel();
                    return;
                }
            }
        }, 0, 5000);

        Thread.currentThread().interrupt();
        return;
    }
}
