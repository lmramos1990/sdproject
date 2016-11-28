package sdproject;

import java.rmi.Remote;
import java.util.*;
import java.rmi.RemoteException;

public interface NotificationCenter extends Remote {
    public boolean isUserOnline(String username) throws RemoteException;
    public ArrayList getOnlineUsers() throws RemoteException;
    public void sendNotificationToUser(String username, String message) throws RemoteException;
}
