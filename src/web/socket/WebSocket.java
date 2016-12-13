package web.socket;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;

@ServerEndpoint(value = "/ws")
public class WebSocket {
    private static final AtomicInteger sequence = new AtomicInteger(1);
    private final String username;
    private Session session;

    public WebSocket() {
        username = "User" + sequence.getAndIncrement();
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        String message = "*" + username + "* connected.";
        sendMessage(message);
    }

    @OnClose
    public void end() {

        System.out.println("closing the websoket");

        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("deu coco ao fechar o websocket");
        }
    }

    @OnMessage
    public void receiveMessage(String message) {
        // one should never trust the client, and sensitive HTML
        // characters should be replaced with &lt; &gt; &quot; &amp;

        System.out.println("RECEIVE A MESSAGE");

        String upperCaseMessage = message.toUpperCase();
        sendMessage("[" + username + "] " + upperCaseMessage);
    }

    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
        System.out.println("its some king of bs when we see this");
    }

    private void sendMessage(String text) {
        // uses *this* object's session to call sendText()

        System.out.println("SEND A MESSAGE");

        try {
            this.session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            // clean up once the WebSocket connection is closed
            try {
                this.session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
