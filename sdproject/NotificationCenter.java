package sdproject;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationCenter extends Remote {
    public void receiveNotification(String notification) throws RemoteException;
}
