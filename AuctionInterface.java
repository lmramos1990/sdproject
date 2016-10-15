import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionInterface extends Remote {
	public int createAuction(String buyer, String isbnCode, String title, String description, String details, float maxPrice, String deadlineStamp) throws RemoteException;
}
