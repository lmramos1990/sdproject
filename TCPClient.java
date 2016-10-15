import java.io.*;
import java.net.*;
import java.util.*;

class TCPClient {
    static Socket clientSocket;

    static DataInputStream dataInputStream;
    static DataOutputStream dataOutputStream;
    static InputStreamReader input;
    static BufferedReader reader;

    public static void main(String[] args) {
        System.out.println("\t------ HELLO IAM AN AWESOME CLIENT INTERFACE ------");

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java TCPClient");
            return;
        }

        InetAddress serverAddress = getHost();
        int port = getPort();

        try {
            clientSocket = new Socket(serverAddress, port);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }

        try {
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }


        input = new InputStreamReader(System.in);
        reader = new BufferedReader(input);

        int choice = 0;
        while (choice == 0) {
            choice = loginRegisterMenu();
        }

        return;
    }

    private static int loginRegisterMenu() {
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

    private static int mainMenu() {
        int choice = 0;

        while(choice != 9) {
            System.out.println("[1] - Create a new auction\n[2] - Search auction by article\n[3] - Auction details\n[4] - See my auctions\n[5] - Bid in an auction\n[6] - Edit an auction\n[7] - Comment on an auction\n[8] - List online users\n[9] - Logout");
            Scanner reader = new Scanner(System.in);
            try {
                System.out.print("CHOOSE AN OPTION: ");
                choice = reader.nextInt();
                if(choice <= 0 || choice >= 10) {
                    System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                    choice = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                choice = 0;
            }
        }

        switch(choice) {
            case 1: System.out.println("Create a new auction");
                break;
            case 2: System.out.println("Search auction by article");
                break;
            case 3: System.out.println("Auction details");
                break;
            case 4: System.out.println("See my auctions");
                break;
            case 5: System.out.println("Bid in an auction");
                break;
            case 6: System.out.println("Edit an auction");
                break;
            case 7: System.out.println("Comment on an auction");
                break;
            case 8: System.out.println("List online users");
                break;
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

            String reply = new String();

            reply = sendRequest(clientSocket, request);

            if(replay.equals("type: login, ok: true")) {
                System.out.println("segue para um menu");
            } else {
                System.out.println("segue para um menu");
            }

            System.out.println("[SERVER] " + reply);
        } else {
            String a = "type: register, ";
            String b = "username: " + username + ", ";
            String c = "password: " + password;

            String request = a + b + c;
            String reply = new String();

            reply = sendRequest(clientSocket, request);

            if(replay.equals("type: register, ok: true")) {
                System.out.println("segue para um menu");
            } else {
                System.out.println("segue para um menu");
            }

            System.out.println("[SERVER] " + reply);
        }

        return choice;
    }

    private static String sendRequest(Socket socket, String request) {
        String data = new String();
        System.out.println("THIS IS A REQUEST: " + request);

        try {
            dataOutputStream.writeUTF(request);

            data = dataInputStream.readUTF();
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return data;
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
            serverAddress = getHost();
        }
        return serverAddress;
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
}

// SEARCH FOR SOMETHING WITHIN A STRING !!!!!! <- MAYBE USEFULL
// public class RegionMatchesDemo {
//     public static void main(String[] args) {
//         String searchMe = "Green Eggs and Ham";
//         String findMe = "Eggs";
//         int searchMeLength = searchMe.length();
//         int findMeLength = findMe.length();
//         boolean foundIt = false;
//         for (int i = 0;
//              i <= (searchMeLength - findMeLength);
//              i++) {
//            if (searchMe.regionMatches(i, findMe, 0, findMeLength)) {
//               foundIt = true;
//               System.out.println(searchMe.substring(i, i + findMeLength));
//               break;
//            }
//         }
//         if (!foundIt)
//             System.out.println("No match found.");
//     }
// }
