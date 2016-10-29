package sdproject;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionInterface extends Remote {

	public void subscribe(NotificationCenter nc) throws RemoteException;

	public String login(String username, String password) throws RemoteException;
	public String register(String username, String password, String uuid) throws RemoteException;
	public String createAuction(String username, String code, String title, String description, String deadline, String amount, String uuid) throws RemoteException;
	public String searchAuction(String code) throws RemoteException;
	public String detailAuction(String username, String id) throws RemoteException;
	public String myAuctions(String username) throws RemoteException;
	public String bid(String username, String id, String amount) throws RemoteException;
	public String editAuction(String username, String id, String title, String description, String deadline, String code, String amount) throws RemoteException;
	public String message(String username, String id, String text) throws RemoteException;
	public String onlineUsers(String username) throws RemoteException;
	public void logOutUser(String username) throws RemoteException;
	public void cleanUpUUIDs(String table) throws RemoteException;
}
