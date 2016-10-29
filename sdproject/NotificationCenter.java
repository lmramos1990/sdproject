package sdproject;

import java.rmi.Remote;
import java.util.*;
import java.rmi.RemoteException;

public interface NotificationCenter extends Remote {
    public void receiveNotification(String notification, ArrayList<String> envolvedUsers) throws RemoteException;
}
