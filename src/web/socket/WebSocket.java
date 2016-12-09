/*package web.socket;

import console.AuctionInterface;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

@ServerEndpoint(value = "/ws")
public class WebSocket {
    private AuctionInterface iBei;
    private Session session;

    private SocketObject ola;

    public WebSocket() {
        System.out.println("JUST CREATED AN INSTANCE OF A WEBSOCKET");

        try {
            iBei = (AuctionInterface) LocateRegistry.getRegistry("localhost", 10000).lookup("iBei");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        ola = new SocketObject(session);

        try {
            iBei.subscribe(ola);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void end() {

    }

    @OnMessage
    public void receiveMessage(String message) {

    }

    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
    }

    // read properties file
}
*/