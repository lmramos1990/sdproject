import java.util.*;
import java.net.*;
import java.io.*;

public class ClientObject {
    Socket clientSocket;
    String username;

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
