package sdproject;

import java.rmi.Remote;
import java.util.*;
import java.rmi.RemoteException;

public interface NotificationCenter extends Remote {
    public boolean checkUsersOnline(String username) throws RemoteException;
    public void receiveNotification(String notification, ArrayList<String> involvedUsers) throws RemoteException;
}
