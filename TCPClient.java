import java.io.*;
import java.net.*;
import java.util.*;

class TCPClient {

    public static void main(String[] args) {
        System.out.println("\t------ HELLO IAM AN AWESOME CLIENT INTERFACE ------");

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPClient");
            return;
        }
        int choice = 0;
        while (choice == 0) {
            choice = mainMenu();
        }

        return;

        // OutGoingRequests outgoingRequests = new OutGoingRequests("outgoing_requests");
    }

    private static int mainMenu() {
        int choice = 0;

        while(choice != 3) {
            System.out.println("[1] - Login\n[2] - Register\n[3] - Exit");
            Scanner reader = new Scanner(System.in);
            try {
                System.out.print("CHOOSE AN OPTION: ");
                choice = reader.nextInt();
                if(choice <= 0 || choice >= 4) {
                    System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                    choice = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                choice = 0;
            }

            if(choice == 1 || choice == 2) {
                choice = loginRegister(choice);
            }
        }

        return choice;
    }

    private static int loginRegister(int choice) {

        Scanner reader = new Scanner(System.in);
        String username = new String();
        String password = new String();
        Console console = System.console();

        if(console == null) {
            System.out.println("ERROR: Console does not exist");
            return 0;
        }

        System.out.print("USERNAME: ");
        username = reader.nextLine();
        char[] pwd = console.readPassword("PASSWORD: ");

        for(int i = 0; i < pwd.length; i++) {
            password = password.concat(Character.toString(pwd[i]));
        }

        if(choice == 1) {
            String a = "type: login, ";
            String b = "username: " + username + ", ";
            String c = "password: " + password;

            String request = a + b + c;

            System.out.println(request);
        } else {
            String a = "type: register, ";
            String b = "username: " + username + ", ";
            String c = "password: " + password;

            String request = a + b + c;

            System.out.println(request);
        }

        return choice;
    }


}

class OutGoingRequests implements Runnable {
    Thread thread;
    String name;

    OutGoingRequests(String name) {
        thread = new Thread(this, name);
        thread.start();
    }

    public void run() {
        System.out.println("HELLO THIS IS THE THREAD THAT THE CLIENT WILL USE TO MAKE REQUESTS TO THE SERVER");
        InetAddress serverAddress = getHost();
        int port = getPort();

        Socket clientSocket;

        try {
            clientSocket = new Socket(serverAddress, port);
            streamingStrings(clientSocket);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static InetAddress getHost() {
        String host = new String();
        Scanner reader = new Scanner(System.in);
        InetAddress serverAddress;

        System.out.print("INSERT THE HOST: ");
        host = reader.nextLine();
        try {
            serverAddress = InetAddress.getByName(host);
            return serverAddress;
        } catch(Exception e) {
          System.out.println("ERROR: " + e.getMessage());
        }
        return null;
    }

    private static int getPort() {
        int port = 0;

        while(port <= 1024) {
            System.out.print("INSERT PORT: ");
            try {
                Scanner reader = new Scanner(System.in);
                port = reader.nextInt();

                if(port <= 1024) {
                    System.out.println("THIS IS NOT A VALID VALUE FOR THE PORT");
                    port = 0;
                }
            } catch(Exception e) {
                System.out.println("THIS IS NOT A VALID VALUE FOR THE PORT");
                port = 0;
            }
        }

        return port;
    }

    private static int streamingStrings(Socket socket){
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String request = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(input);

            while (true) {
                System.out.print("INSERT SOME STUPID STRING: ");
                try {
                    request = reader.readLine();
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }

                dataOutputStream.writeUTF(request);

                String data = dataInputStream.readUTF();

                System.out.println("RECIEVED: " + data);
            }

        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return 0;
    }
}
