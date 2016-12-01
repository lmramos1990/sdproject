package sdproject;

import java.net.*;

class ClientObject {
    private Socket clientSocket;
    private String username;

    ClientObject(Socket clientSocket, String username) {
        this.clientSocket = clientSocket;
        this.username = username;
    }

    Socket getClientSocket() {
        return clientSocket;
    }

    String getUsername() {
        return username;
    }
}
