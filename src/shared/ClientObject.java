package shared;

import java.net.Socket;

public class ClientObject {
    private Socket clientSocket;
    private String username;

    public ClientObject(Socket clientSocket, String username) {
        this.clientSocket = clientSocket;
        this.username = username;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public String getUsername() {
        return username;
    }
}
