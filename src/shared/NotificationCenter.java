package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NotificationCenter extends Remote {
    boolean isUserOnline(String username) throws RemoteException;
    ArrayList getOnlineUsers() throws RemoteException;
    void sendNotificationToUser(String username, String message) throws RemoteException;
    void updateRequest(String uuid) throws RemoteException;
    int requestStatus(String uuid) throws RemoteException;
}
