package sdproject;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionInterface extends Remote {

	public String login(String username, String password) throws RemoteException;
	public String register(String username, String password) throws RemoteException;
	public String createAuction(String username, String code, String title, String description, String deadline, String amount) throws RemoteException;
	public String searchAuction(String code) throws RemoteException;
	public String detailAuction(String username, String id) throws RemoteException;
	public String myAuctions(String username) throws RemoteException;
	public String bid(String username, String id, String amount) throws RemoteException;
	public String editAuction(String username, String id, String title, String description, String deadline) throws RemoteException;
	public String message(String username, String id, String text) throws RemoteException;
	public String onlineUsers(String username) throws RemoteException;
}
