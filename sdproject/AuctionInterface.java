package sdproject;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionInterface extends Remote {

	public void subscribe(NotificationCenter nc) throws RemoteException;
	public String login(String username, String hpassword) throws RemoteException;
	public boolean isUser(String username) throws RemoteException;
	public String getSalt(String username) throws RemoteException;
	public String register(String username, String hpassword, String esalt) throws RemoteException;
	public String createAuction(String username, String code, String title, String description, String deadline, float amount) throws RemoteException;
	public String searchAuction(String code) throws RemoteException;
	public String detailAuction(int id) throws RemoteException;
	public String myAuctions(String username) throws RemoteException;
	public String bid(String username, int id, float amount) throws RemoteException;
	public String editAuction(String username, int id, String title, String description, String deadline, String code, float amount) throws RemoteException;
	public String message(String username, int id, String text) throws RemoteException;
	public String onlineUsers(String username) throws RemoteException;
	public void startUpNotifications(String username) throws RemoteException;
}
