package sdproject;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface AuctionInterface extends Remote {

    void subscribe(NotificationCenter nc) throws RemoteException;
    String login(String username, String hpassword) throws RemoteException;
    String isUser(String username) throws RemoteException;
    String getSalt(String username) throws RemoteException;
    String register(String uuid, String username, String hpassword, String esalt) throws RemoteException;
    String createAuction(String username, String code, String title, String description, String deadline, float amount) throws RemoteException;
    String searchAuction(String code) throws RemoteException;
    String detailAuction(int id) throws RemoteException;
    String myAuctions(String username) throws RemoteException;
    String bid(String username, int id, float amount) throws RemoteException;
    String editAuction(String username, int id, String title, String description, String deadline, String code, float amount) throws RemoteException;
    String message(String username, int id, String text) throws RemoteException;
    String onlineUsers(String username) throws RemoteException;
    void startUpNotifications(String username) throws RemoteException;
}
