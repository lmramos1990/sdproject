package web.socket;

import shared.AuctionInterface;
import shared.NotificationCenter;
import shared.RequestObject;

import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;

public class WebSocketHelper extends UnicastRemoteObject implements NotificationCenter {
    private AuctionInterface iBei;
    private HttpSession browserSession;
    private WebSocket webSocket;
    private String rmiHost;
    private String machineHost;
    private int rmiPort;


    WebSocketHelper(WebSocket webSocket, HttpSession session) throws RemoteException {
        this.browserSession = session;
        this.webSocket = webSocket;

        readProperties();
        System.setProperty("java.rmi.server.hostname", machineHost);
        System.setProperty("java.net.preferIPv4Stack" , "true");

        int retries = 0;
        boolean connected = false;

        while (!connected && retries < 10) {
            try {
                retries++;
                iBei = (AuctionInterface) LocateRegistry.getRegistry(rmiHost, rmiPort).lookup("iBei");
                iBei.subscribe(this);
                connected = true;
            } catch (Exception e) {
                connected = false;
            }
        }
    }

    private void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            System.out.println();

            inputStream = new FileInputStream(propFileName);

            prop.load(inputStream);

            rmiHost = prop.getProperty("rmiHost");
            machineHost = prop.getProperty("machineHost");
            rmiPort = Integer.parseInt(prop.getProperty("rmiPort"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void removeSubscription() {
        try {
            iBei.removeSubscription(this);
        } catch (RemoteException ignored) {}
    }

    public synchronized boolean isUserOnline(String username) throws RemoteException {
        return username.equals(browserSession.getAttribute("username").toString());
    }

    public synchronized ArrayList getOnlineUsers() throws RemoteException {
        ArrayList<String> onlineUser = new ArrayList<>();

        onlineUser.add(browserSession.getAttribute("username").toString());

        return onlineUser;
    }

    public synchronized void sendNotificationToUser(String username, String message) throws RemoteException {
        if(username.equals(browserSession.getAttribute("username").toString())) {
            webSocket.sendMessage(message);
        }
    }

    public synchronized void updateRequest(String uuid) throws RemoteException {
        ArrayList<RequestObject> requestsmap = (ArrayList<RequestObject>) browserSession.getAttribute("requestsmap");

        if(requestsmap == null) return;

        for(RequestObject request : requestsmap) {
            if(uuid.equals(request.getUUID())) {
                request.setModified(1);
                browserSession.setAttribute("requestsmap", requestsmap);
            }
        }
    }

    public synchronized int requestStatus(String uuid) throws RemoteException {
        ArrayList<RequestObject> requestsmap = (ArrayList<RequestObject>) browserSession.getAttribute("requestsmap");

        if(requestsmap == null) return -1;

        for(RequestObject request : requestsmap) {
            if(uuid.equals(request.getUUID())) {
                return request.getModified();
            }
        }

        return -1;
    }
}
