import java.util.*;
import java.net.*;
import java.io.*;

public class Connection extends Thread {
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket clientSocket;
    int threadNumber;

    // SOME OF THESE VARIABLES MAY CHANGE TO LOCAL OVER TIME (BEWARE)
    private static String username = new String();
    private static String password = new String();
    private static String code = new String();
    private static String title = new String();
    private static String description = new String();
    private static String deadline = new String();
    private static String amount = new String();
    private static String id = new String();
    private static String text = new String();

    // SOME OF THESE VARIABLES MAY CHANGE TO LOCAL OVER TIME (BEWARE)

    public Connection (Socket pclientSocket, int number) {
        threadNumber = number;
        try {
            clientSocket = pclientSocket;

            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        String reply = new String();

        try {
            while(true) {
                String data = dataInputStream.readUTF();
                System.out.println("THREAD[" + threadNumber + "] RECIEVED: " + data);

                String action = parse("type:", data);

                reply = courseOfAction(action, data);

                dataOutputStream.writeUTF(reply);
            }
        } catch(EOFException eofe) {
            System.out.println("[SERVER] THE CLIENT DISCONNECTED");

            // IF A REQUEST COMES AND THE USER DOESNT GET IT HE SOULD BE NOTIFIED ABOUT IT WHEN HE COMES BACK!!!

            try {
                this.clientSocket.close();
            } catch(IOException ioe) {
                System.out.println("ERROR WHEN TRYING TO CLOSE THE CLIENT SOCKET: " + ioe.getMessage());
            }

            Thread.currentThread().interrupt();
            return;
        } catch(IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static String courseOfAction(String action, String parameters) {
        String reply = new String();

        if(action.equals("login") || action.equals("register")) {

            username = parse("username:", parameters);
            password = parse("password:", parameters);

            if(action.equals("login")) {
                reply = "type: login, ok: true";
            } else {
                reply = "type: register, ok: true";
            }

        } else if(action.equals("create_auction")) {

            code = parse("code:", parameters);
            title = parse("title:", parameters);
            description = parse("description:", parameters);
            deadline = parse("deadeline:", parameters);
            amount = parse("amount:", parameters);

        } else if(action.equals("search_auction")) {
            code = parse("code:", parameters);

        } else if(action.equals("detail_auction")) {
            id = parse("id:", parameters);
        } else if(action.equals("my_auctions")) {
            //ONLY ACTION type: my_auctions
        } else if(action.equals("bid")) {
            id = parse("id:", parameters);
            amount = parse("amount:", parameters);

        } else if(action.equals("edit_auction")) {
            id = parse("id:", parameters);
            deadline = parse("deadline:", parameters);

        } else if(action.equals("message")) {
            id = parse("id:", parameters);
            text = parse("text:", parameters);

        } else if(action.equals("online_users")) {
            // ONLY ACTION type: online_users

        } else {
            return "ERROR: THIS IS'NT A VALID REQUEST";
        }

        return reply;
    }

    private static String parse(String parameter, String request) {
        int j = 0, k = 0;
        int plen = parameter.length();

        for(int i = 0; i < request.length(); i++) {
            if(j != plen && (request.charAt(i) == parameter.charAt(j))) {
                j++;
            }

            if(j == plen) {
                j = i;
                break;
            }
        }

        for(int i = 0; i < request.length(); i++) {
            if(request.charAt(i) == ',' && j < i) {
                k = i;
                break;
            }
        }

        String string = new String();

        if(k == 0) {
            k = request.length();
            string = request.substring(j + 1, k);
        } else {
            string = request.substring(j + 1, k);
        }

        StringBuilder sb = new StringBuilder(string);

        while(string.charAt(0) == ' ') {
            sb.deleteCharAt(0);
            string = sb.toString();
        }

        return string;
    }

    private static String attemptLoginRegister(String action, String username, String password) {
        if(action.equals("login")) {
            System.out.println("LOGIN -> SEND THIS TO THE RMI SERVER");
        } else {
            System.out.println("REGISTER -> SEND THIS TO THE RMI SERVER");
        }

        return action;
    }
}
