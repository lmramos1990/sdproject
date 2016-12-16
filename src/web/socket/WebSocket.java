package web.socket;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

@ServerEndpoint(value = "/ws", configurator = WebSocket.class)
public class WebSocket extends ServerEndpointConfig.Configurator {
    private WebSocketHelper wsHelper;
    private Session wsSession;
    private HttpSession httpSession;

    public WebSocket() {
        super();
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        config.getUserProperties().put(HttpSession.class.getName(),httpSession);
    }

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        this.wsSession = session;
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        try {
            this.wsHelper = new WebSocketHelper(this, httpSession);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void echo(String message) throws IOException {
        sendMessage(message);
    }

    @OnClose
    public void end() {
        wsHelper.removeSubscription();

        try {
            wsSession.close();
        } catch (IOException e) {
            System.out.println("[WEBSOCKET] AN ERROR OCURRED WHEN CLOSING THE WEBSOCKET");
        }
    }

    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
        System.out.println("[WEBSOCKET] SOME UNHANDLED ERROR OCURRED");
    }

    void sendMessage(String text) {
        try {
            this.wsSession.getBasicRemote().sendText(text);
        } catch (IOException e) {
            try {
                this.wsSession.close();
            } catch (IOException ignored) {}
        }
    }
}
