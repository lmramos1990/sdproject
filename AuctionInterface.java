import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionInterface extends Remote {
	// createAuction();
	// searchAuction();
	// auctionDetails();
	// myAuctions();
	// makeBid();
	// editAuction();
	// commentInAuction();
	// listOnlineUsers();


	public String login(String username, String password) throws RemoteException;
	public String register(String username, String password) throws RemoteException;
	public String createAuction(String username, String code, String title, String description, String deadline, String amount) throws RemoteException;
	public String searchAuction(String code) throws RemoteException;
	// public String
}
