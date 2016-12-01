package sdproject;

import java.rmi.Remote;
import java.util.*;
import java.rmi.RemoteException;

public interface NotificationCenter extends Remote {
    boolean isUserOnline(String username) throws RemoteException;
    ArrayList getOnlineUsers() throws RemoteException;
    void sendNotificationToUser(String username, String message) throws RemoteException;
}
